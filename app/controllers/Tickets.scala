package controllers

import play.api.mvc._

object Tickets extends Controller {
  def ticketsAvailable = Action { request =>
    val availableTickets = 1000
    Ok(availableTickets.toString)
  }
}