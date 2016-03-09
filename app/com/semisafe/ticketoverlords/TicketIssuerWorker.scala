package com.semisafe.ticketoverlords

import javax.inject.Inject

import akka.actor.Actor
import akka.actor.Status.{Failure => ActorFailure}
import play.api.libs.concurrent.Execution.Implicits._

class OrderRoutingException(message: String) extends Exception(message)

class TicketIssuerWorker @Inject()(ticketBlockID: Long, ticketBlockDao: TicketBlockDao, orderDao: OrderDao) extends Actor {

  override def preStart = {
    val availabilityFuture = ticketBlockDao.availability(ticketBlockID)

    availabilityFuture.onSuccess {
      case result => self ! AddTickets(result)
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
    case AvailabilityCheck(ticketBlockID) => {
      val failureResponse = TicketBlockUnavailable(ticketBlockID)
      sender ! ActorFailure(failureResponse)
    }
  }

  def normalOperation(availability: Int): Actor.Receive = {
    case AddTickets(newQuantity) => {
      context.become(normalOperation(availability + newQuantity))
    }
    case order: Order         => placeOrder(order, availability)
    case _: AvailabilityCheck => sender ! availability
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
    case _: AvailabilityCheck => sender ! 0
  }

  // This replaces the previous definition of receive
  def receive = initializing

  def placeOrder(order: Order, availability: Int) {
    val origin = sender

    if (validateRouting(order.ticketBlockID)) {
      if (availability >= order.ticketQuantity) {
        val newAvailability = availability - order.ticketQuantity
        context.become(normalOperation(newAvailability))

        val createdOrder = orderDao.create(order)

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
