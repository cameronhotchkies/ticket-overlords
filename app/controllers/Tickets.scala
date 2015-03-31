package controllers

import play.api.mvc._
import play.api.libs.json.Json

object Tickets extends Controller {
  def ticketsAvailable = Action { request =>
    val availableTickets = 1000

    val result = Json.obj(
      "result" -> "ok", 
      "ticketQuantity" -> availableTickets)
    Ok(result)
  }
}