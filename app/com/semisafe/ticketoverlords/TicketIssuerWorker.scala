package com.semisafe.ticketoverlords

import akka.actor.Actor
import akka.actor.Status.{ Failure => ActorFailure }
import play.api.libs.concurrent.Execution.Implicits._

class OrderRoutingException(message: String) extends Exception(message)

class TicketIssuerWorker(ticketBlockID: Long) extends Actor {

  def placeOrder(order: Order) {
    val origin = sender

    if (ticketBlockID != order.ticketBlockID) {

      val msg = s"IssuerWorker #${ticketBlockID} recieved " +
        s"an order for Ticket Block ${order.ticketBlockID}"

      origin ! ActorFailure(new OrderRoutingException(msg))

    } else {
      val availResult = TicketBlock.availability(ticketBlockID)

      availResult.map { availability =>
        if (availability >= order.ticketQuantity) {
          val createdOrder = Order.create(order)

          createdOrder.map { origin ! _ }
        } else {

          val failureResponse = InsufficientTicketsAvailable(
            order.ticketBlockID,
            availability)

          origin ! ActorFailure(failureResponse)
        }
      }
    }
  }

  def receive = {
    case order: Order => placeOrder(order)
  }
}
