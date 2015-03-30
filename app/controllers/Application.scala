package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    val ticketsAvailable = 1000
    Ok(views.html.index(ticketsAvailable))
  }

}