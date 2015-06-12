package com.semisafe.ticketoverlords

import org.joda.time.DateTime
import play.api.libs.json.{ Json, Format }

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import play.api.Play.current
import play.api.db.DBApi

import SlickMapping.jodaDateTimeMapping

case class Event(
  id: Option[Long],
  name: String,
  start: DateTime,
  end: DateTime,
  address: String,
  city: String,
  state: String,
  country: String)

object Event {
  implicit val format: Format[Event] = Json.format[Event]

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](current)
  import dbConfig._
  import dbConfig.driver.api._

  class EventsTable(tag: Tag) extends Table[Event](tag, "EVENTS") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def start = column[DateTime]("START")
    def end = column[DateTime]("END")
    def address = column[String]("ADDRESS")
    def city = column[String]("CITY")
    def state = column[String]("STATE")
    def country = column[String]("COUNTRY")

    def * = ???
  }

  val table = TableQuery[EventsTable]

  def list: Seq[Event] = { ??? }

  def getByID(eventID: Long): Option[Event] = { ??? }

  def create(event: Event): Event = { ??? }
}
