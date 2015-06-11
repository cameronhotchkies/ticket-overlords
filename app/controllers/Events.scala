package controllers

import play.api.mvc._
import play.api.libs.json.Json

import com.semisafe.ticketoverlords.Event
import controllers.responses._

object Events extends Controller {
  def list = Action { request =>
    val events: Seq[Event] = ???
    Ok(Json.toJson(SuccessResponse(events)))
  }

  def getByID(eventID: Long) = Action { request =>
    val event: Option[Event] = ???
    event.fold {
      NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No event found")))
    } { e =>
      Ok(Json.toJson(SuccessResponse(e)))
    }
  }

  def create = Action { request =>
    // parse from json post body
    val incomingEvent: Event = ???

    // save event and get a copy back
    val createdEvent: Event = ???

    Created(Json.toJson(SuccessResponse(createdEvent)))
  }
}
