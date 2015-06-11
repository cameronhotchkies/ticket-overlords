package controllers.responses

import play.api.libs.json.{ Json, Format, JsValue, JsNull }

case class ErrorResult(status: Int, message: String)

object ErrorResult {
  implicit val format: Format[ErrorResult] = Json.format[ErrorResult]
}

case class EndpointResponse(
  result: String,
  response: JsValue,
  error: Option[ErrorResult])

object EndpointResponse {
  implicit val format: Format[EndpointResponse] = Json.format[EndpointResponse]
}

object ErrorResponse {
  def apply(status: Int, message: String) = {
    EndpointResponse("ko", JsNull, Option(ErrorResult(status, message)))
  }
}

object SuccessResponse {
  def apply(successResponse: JsValue) = {
    EndpointResponse("ok", successResponse, None)
  }
}
