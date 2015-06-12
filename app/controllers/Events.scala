package controllers

import play.api.mvc._
import play.api.libs.json.Json
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import com.semisafe.ticketoverlords.Event
import controllers.responses._

object Events extends Controller {
  def list = Action.async { request =>
    val eventFuture: Future[Seq[Event]] = Event.list

    val response = eventFuture.map { events =>
      Ok(Json.toJson(SuccessResponse(events)))
    }

    response
  }

  def getByID(eventID: Long) = Action { request =>
    val event: Option[Event] = ???
    event.fold {
      NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No event found")))
    } { e =>
      Ok(Json.toJson(SuccessResponse(e)))
    }
  }

  def create = Action(parse.json) { request =>
    val incomingBody = request.body.validate[Event]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: ${error}"
      val response = ErrorResponse(ErrorResponse.INVALID_JSON, errorMessage)
      BadRequest(Json.toJson(response))
    }, { event =>
      // save event and get a copy back
      val createdEvent: Event = ???

      Created(Json.toJson(SuccessResponse(createdEvent)))
    })
  }

}
