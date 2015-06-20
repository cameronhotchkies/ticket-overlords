package com.semisafe.ticketoverlords

import akka.actor.Actor
import akka.actor.Status.{ Failure => ActorFailure }
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import akka.actor.{ ActorRef, Props }

case class InsufficientTicketsAvailable(
  ticketBlockID: Long,
  ticketsAvailable: Int) extends Throwable

class TicketIssuer extends Actor {

  var workers = Map[Long, ActorRef]()

  def createWorker(ticketBlockID: Long) {
    if (!workers.contains(ticketBlockID)) {
      val worker = context.actorOf(
        Props(classOf[TicketIssuerWorker], ticketBlockID),
        name = ticketBlockID.toString)
      workers = workers + (ticketBlockID -> worker)
    }
  }

  override def preStart = {
    val ticketBlocksResult = TicketBlock.list

    for {
      ticketBlocks <- ticketBlocksResult
      block <- ticketBlocks
      id <- block.id
    } createWorker(id)
  }

  def placeOrder(order: Order) {
    // This is important!!
    val origin = sender

    // Get available quantity as a future
    val availabilityResult = TicketBlock.availability(order.ticketBlockID)

    availabilityResult.map { availability =>
      // Compare to order amount
      if (availability >= order.ticketQuantity) {
        // place order
        val createdOrderResult: Future[Order] = Order.create(order)

        createdOrderResult.map { createdOrder =>
          // send completed order back to originator
          origin ! createdOrder
        }
      } else {
        // if not possible send a failure result
        val failureResponse = InsufficientTicketsAvailable(
          order.ticketBlockID,
          availability)

        origin ! ActorFailure(failureResponse)
      }
    }
  }

  def receive = {
    case order: Order => placeOrder(order)
  }
}