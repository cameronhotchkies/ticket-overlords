package com.semisafe.ticketoverlords

import akka.actor.Actor

class TicketIssuer extends Actor {

  def placeOrder(order: Order) {
    // Get available quantity

    // Compare to order amount

    // place order if possible

    // send completed order back to originator

    // if not possible send a failure result
  }

  def receive = {
    case order: Order => placeOrder(order)
  }
}