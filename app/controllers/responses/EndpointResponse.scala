package controllers.responses

case class ErrorResult(status: Int, message: String)

case class EndpointResponse(
  result: String,
  response: Option[Any],
  error: Option[ErrorResult])
