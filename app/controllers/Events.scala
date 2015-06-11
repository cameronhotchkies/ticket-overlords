package controllers

import play.api.mvc._
import play.api.libs.json.Json

import com.semisafe.ticketoverlords.Event

object Events extends Controller {
  def list = Action { request =>
    val events: Seq[Event] = ???
    Ok(Json.toJson(events))
  }

  def getByID(eventID: Long) = Action { request =>
    val event: Option[Event] = ???
    event.fold {
      NotFound(Json.toJson("No event found"))
    } { e =>
      Ok(Json.toJson(e))
    }
  }

  def create = Action { request =>
    // parse from json post body
    val incomingEvent: Event = ???

    // save event and get a copy back
    val createdEvent: Event = ???

    Created(Json.toJson(createdEvent))
  }
}
