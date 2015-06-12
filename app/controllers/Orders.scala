package controllers

import play.api.mvc._
import play.api.libs.json.Json
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import com.semisafe.ticketoverlords.Order
import controllers.responses._

object Orders extends Controller {
  def list = Action.async { request =>
    val orders = Order.list
    orders.map { o =>
      Ok(Json.toJson(SuccessResponse(o)))
    }
  }

  def getByID(orderID: Long) = Action.async { request =>
    val orderFuture = Order.getByID(orderID)

    orderFuture.map { order =>
      order.fold {
        NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No order found")))
      } { o =>
        Ok(Json.toJson(SuccessResponse(o)))
      }
    }
  }

  def create = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[Order]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: ${error}"
      val response = ErrorResponse(ErrorResponse.INVALID_JSON, errorMessage)
      Future.successful(BadRequest(Json.toJson(response)))
    }, { order =>
      // save order and get a copy back
      val createdOrder = Order.create(order)

      createdOrder.map { co =>
        Created(Json.toJson(SuccessResponse(co)))
      }
    })
  }
}
