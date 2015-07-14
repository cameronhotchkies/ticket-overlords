package controllers

import play.api.mvc._
import play.api.libs.json.Json
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import com.semisafe.ticketoverlords.Event
import com.semisafe.ticketoverlords.TicketBlock
import controllers.responses._

object Events extends Controller {
  def list = Action.async { request =>
    val eventFuture: Future[Seq[Event]] = Event.list

    val response = eventFuture.map { events =>
      Ok(Json.toJson(SuccessResponse(events)))
    }

    response
  }

  def getByID(eventID: Long) = Action.async { request =>
    val eventFuture: Future[Option[Event]] = Event.getByID(eventID)

    eventFuture.map { event =>
      event.fold {
        NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No event found")))
      } { e =>
        Ok(Json.toJson(SuccessResponse(e)))
      }
    }
  }

  def create = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[Event]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: ${error}"
      val response = ErrorResponse(ErrorResponse.INVALID_JSON, errorMessage)
      Future.successful(BadRequest(Json.toJson(response)))
    }, { event =>
      // save event and get a copy back
      val createdEventFuture: Future[Event] = Event.create(event)

      createdEventFuture.map { createdEvent =>
        Created(Json.toJson(SuccessResponse(createdEvent)))
      }

    })
  }

  def ticketBlocksForEvent(eventID: Long) = Action.async { request =>
    val eventFuture = Event.getByID(eventID)

    eventFuture.flatMap { event =>
      event.fold {
        Future.successful(
          NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No event found"))))
      } { e =>
        val ticketBlocks: Future[Seq[TicketBlock]] = e.ticketBlocksWithAvailability
        ticketBlocks.map { tb =>
          Ok(Json.toJson(SuccessResponse(tb)))
        }
      }
    }
  }
}

