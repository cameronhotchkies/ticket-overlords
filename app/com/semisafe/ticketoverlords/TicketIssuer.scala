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
    val workerRef = workers.get(order.ticketBlockID)

    workerRef.fold {
      // We need a new type of error here if the ActorRef does
      // not exist, or has not yet been initialized
      ???
    } { worker =>
      worker forward order
    }
  }

  def receive = {
    case order: Order => placeOrder(order)
  }
}