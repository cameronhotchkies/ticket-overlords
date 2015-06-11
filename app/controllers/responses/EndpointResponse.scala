package controllers.responses

import play.api.libs.json.{ Json, Format }

case class ErrorResult(status: Int, message: String)

object ErrorResult {
  implicit val format: Format[ErrorResult] = Json.format[ErrorResult]
}

case class EndpointResponse(
  result: String,
  response: Option[Any],
  error: Option[ErrorResult])

object EndpointResponse {
  implicit val format: Format[EndpointResponse] = Json.format[EndpointResponse]
}
