package controllers

import play.api.mvc._
import play.api.libs.json.Json
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import com.semisafe.ticketoverlords.{ Order, TicketBlock }
import controllers.responses._

import play.api.libs.concurrent.Akka
import play.api.Play.current
import com.semisafe.ticketoverlords.TicketIssuer
import akka.actor.Props

object Orders extends Controller {

  val issuer = Akka.system.actorOf(
    Props[TicketIssuer],
    name = "ticketIssuer")

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

      val availFuture = TicketBlock.availability(order.ticketBlockID)

      availFuture.flatMap { availability =>
        if (availability >= order.ticketQuantity) {
          // save order and get a copy back
          val createdOrder = Order.create(order)

          createdOrder.map { co =>
            Created(Json.toJson(SuccessResponse(co)))
          }
        } else {
          val responseMessage = "There are not enough tickets remaining to complete this order." +
            s" Quantity Remaining: ${availability}"

          val response = ErrorResponse(
            ErrorResponse.NOT_ENOUGH_TICKETS,
            responseMessage)

          Future.successful(BadRequest(Json.toJson(response)))
        }
      }
    })
  }
}
