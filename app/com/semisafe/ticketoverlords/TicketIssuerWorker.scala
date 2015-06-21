package com.semisafe.ticketoverlords

import akka.actor.Actor
import akka.actor.Status.{ Failure => ActorFailure }
import play.api.libs.concurrent.Execution.Implicits._

class OrderRoutingException(message: String) extends Exception(message)

class TicketIssuerWorker(ticketBlockID: Long) extends Actor {

  override def preStart = {
    val availabilityFuture = TicketBlock.availability(ticketBlockID)

    availabilityFuture.onSuccess {
      case result => availability = result
    }
  }

  def validateRouting(requestedID: Long) = {
    if (ticketBlockID != requestedID) {

      val msg = s"IssuerWorker #${ticketBlockID} recieved " +
        s"an order for Ticket Block ${requestedID}"

      sender ! ActorFailure(new OrderRoutingException(msg))
      false
    } else {
      true
    }
  }

  case class AddTickets(quantity: Int)

  def initializing: Actor.Receive = {
    case AddTickets(availability) => {
      context.become(normalOperation(availability))
    }
    case order: Order => {
      if (validateRouting(order.ticketBlockID)) {
        val failureResponse = TicketBlockUnavailable(
          order.ticketBlockID)

        sender ! ActorFailure(failureResponse)
      }
    }
  }

  def normalOperation(availability: Int): Actor.Receive = {
    case AddTickets(newQuantity) => {
      context.become(normalOperation(availability + newQuantity))
    }
    case order: Order => placeOrder(order, availability)
  }

  def soldOut: Actor.Receive = {
    case AddTickets(availability) => {
      context.become(normalOperation(availability))
    }
    case order: Order => {
      if (validateRouting(order.ticketBlockID)) {
        val failureResponse = InsufficientTicketsAvailable(
          order.ticketBlockID, 0)

        sender ! ActorFailure(failureResponse)
      }
    }
  }

  // This replaces the previous definition of receive
  def receive = initializing

  def placeOrder(order: Order) {
    val origin = sender

    if (ticketBlockID != order.ticketBlockID) {

      val msg = s"IssuerWorker #${ticketBlockID} recieved " +
        s"an order for Ticket Block ${order.ticketBlockID}"

      origin ! ActorFailure(new OrderRoutingException(msg))

    } else {
      if (availability >= order.ticketQuantity) {
        availability -= order.ticketQuantity
        val createdOrder = Order.create(order)
        createdOrder.map(origin ! _)
      } else {
        val failureResponse = InsufficientTicketsAvailable(
          order.ticketBlockID,
          availability)

        origin ! ActorFailure(failureResponse)
      }
    }
  }
}
