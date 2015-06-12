package controllers

import play.api.mvc._
import play.api.libs.json.Json

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import com.semisafe.ticketoverlords.TicketBlock
import controllers.responses._

object TicketBlocks extends Controller {
  def list = Action.async { request =>
    val ticketBlocks: Future[Seq[TicketBlock]] = TicketBlock.list

    ticketBlocks.map { tbs =>
      Ok(Json.toJson(SuccessResponse(tbs)))
    }
  }

  def getByID(ticketBlockID: Long) = Action.async { request =>
    val ticketBlockFuture: Future[Option[TicketBlock]] = TicketBlock.getByID(ticketBlockID)

    ticketBlockFuture.map { ticketBlock =>
      ticketBlock.fold {
        NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No ticket block found")))
      } { tb =>
        Ok(Json.toJson(SuccessResponse(tb)))
      }
    }
  }

  def create = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[TicketBlock]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: ${error}"
      val response = ErrorResponse(ErrorResponse.INVALID_JSON, errorMessage)
      Future.successful(BadRequest(Json.toJson(response)))
    }, { ticketBlock =>
      // save ticket block and get a copy back
      val createdBlock: Future[TicketBlock] = TicketBlock.create(ticketBlock)

      createdBlock.map { cb =>
        Created(Json.toJson(SuccessResponse(cb)))
      }
    })
  }
}
