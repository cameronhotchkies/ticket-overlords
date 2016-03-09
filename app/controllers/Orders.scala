package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.libs.json.Json

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import com.semisafe.ticketoverlords._
import controllers.responses._
import play.api.libs.concurrent.Akka
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import play.api.{Configuration, Logger}
import play.filters.csrf._

class Orders @Inject()(configuration: Configuration,
                       orderDao: OrderDao,
                       system:ActorSystem,
                       csrfCheck: CSRFCheck) extends Controller {

  def list = Action.async { request =>
    val orders = orderDao.list
    orders.map { o =>
      Ok(Json.toJson(SuccessResponse(o)))
    }
  }

  def getByID(orderID: Long) = Action.async { request =>
    val orderFuture = orderDao.getByID(orderID)

    orderFuture.map { order =>
      order.fold {
        NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No order found")))
      } { o =>
        Ok(Json.toJson(SuccessResponse(o)))
      }
    }
  }

  def create = csrfCheck {
    Action.async(parse.json) { request =>
      val incomingBody = request.body.validate[Order]

      incomingBody.fold(error => {
        val errorMessage = s"Invalid JSON: ${error}"
        val response = ErrorResponse(ErrorResponse.INVALID_JSON, errorMessage)
        Future.successful(BadRequest(Json.toJson(response)))
      }, { order =>
        val timeoutKey = "ticketoverlords.timeouts.issuer"
        val configuredTimeout = configuration.getInt(timeoutKey)
        val resolvedTimeout = configuredTimeout.getOrElse(5)
        implicit val timeout = Timeout(resolvedTimeout.seconds)

        val issuer = TicketIssuer.getSelection(system)
        val orderFuture = (issuer ? order).mapTo[Order]

        // Convert successful future to Json
        orderFuture.map { createdOrder =>
          Ok(Json.toJson(SuccessResponse(createdOrder)))
        }.recover({
          case ita: InsufficientTicketsAvailable => {
            val responseMessage =
              "There are not enough tickets remaining to complete this order." +
                s" Quantity Remaining: ${ita.ticketsAvailable}"

            val response = ErrorResponse(
              ErrorResponse.NOT_ENOUGH_TICKETS,
              responseMessage)

            BadRequest(Json.toJson(response))
          }
          case tba: TicketBlockUnavailable => {
            val responseMessage =
              s"Ticket Block ${order.ticketBlockID} is not available."
            val response = ErrorResponse(
              ErrorResponse.TICKET_BLOCK_UNAVAILABLE,
              responseMessage)

            BadRequest(Json.toJson(response))
          }
          case unexpected => {
            Logger.error(
              s"Unexpected error while placing an order: ${unexpected.toString}")
            val response = ErrorResponse(
              INTERNAL_SERVER_ERROR,
              "An unexpected error occurred")

            InternalServerError(Json.toJson(response))
          }
        })
      })
    }
  }
}
