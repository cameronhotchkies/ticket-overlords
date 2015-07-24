package controllers

import play.api._
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def jsRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.Events.list,
        routes.javascript.Events.ticketBlocksForEvent,
        routes.javascript.Orders.create
      )
    )
  }

}