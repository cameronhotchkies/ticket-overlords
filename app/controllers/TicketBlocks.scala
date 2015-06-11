package controllers

import play.api.mvc._
import play.api.libs.json.Json

import com.semisafe.ticketoverlords.TicketBlock
import controllers.responses._

object TicketBlocks extends Controller {
    def list = Action { request =>
    val ticketBlocks: Seq[TicketBlock] = ???
    Ok(Json.toJson(SuccessResponse(ticketBlocks)))
  }

  def getByID(ticketBlockID: Long) = Action { request =>
    val ticketBlock: Option[TicketBlock] = ???

    ticketBlock.fold {
      NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No ticket block found")))
    } { tb =>
      Ok(Json.toJson(SuccessResponse(tb)))
    }
  }

  def create = Action(parse.json) { request =>
    val incomingBody = request.body.validate[TicketBlock]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: ${error}"
      val response = ErrorResponse(ErrorResponse.INVALID_JSON, errorMessage)
      BadRequest(Json.toJson(response))
    }, { ticketBlock =>
      // save ticket block and get a copy back
      val createdBlock: TicketBlock = ???

      Created(Json.toJson(SuccessResponse(createdBlock)))
    })
  }
}
