package controllers

import play.api.mvc._
import play.api.libs.json.Json

class Tickets extends Controller {

  case class AvailabilityResponse(result: String, ticketQuantity: Option[Long])
  object AvailabilityResponse {
    implicit val responseFormat = Json.format[AvailabilityResponse]
  }
  def ticketsAvailable = Action { request =>
    val availableTickets = 1000
    val response = AvailabilityResponse("ok", Option(availableTickets))
    Ok(Json.toJson(response))
  }
}
