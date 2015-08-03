package controllers.responses

import play.api.mvc._
import play.api.mvc.Results.Forbidden
import play.api.http.Status
import play.api.libs.json.Json
import scala.concurrent.Future
import play.filters.csrf.CSRF.ErrorHandler

class CSRFFilterError extends ErrorHandler {
  def handle(req: RequestHeader, msg: String): Future[Result] = {
    val response = ErrorResponse(Status.FORBIDDEN, msg)
    val result = Forbidden(Json.toJson(response))

    Future.successful(result)
  }
}