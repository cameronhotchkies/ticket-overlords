package com.semisafe.ticketoverlords

import org.joda.time.DateTime
import play.api.libs.json.{ Json, Format }

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import play.api.Play.current
import play.api.db.DBApi
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import akka.util.Timeout
import akka.pattern.ask

import SlickMapping.jodaDateTimeMapping

case class Event(
    id: Option[Long],
    name: String,
    start: DateTime,
    end: DateTime,
    address: String,
    city: String,
    state: String,
    country: String) {

  def ticketBlocksWithAvailability(implicit timeout: Timeout): Future[Seq[TicketBlock]] = {
    this.id.fold {
      Future.successful(Nil: Seq[TicketBlock])
    } { eid =>

      val basicBlocks = TicketBlock.listForEvent(eid)
      val issuer = TicketIssuer.getSelection
      val blocksWithAvailability: Future[Seq[TicketBlock]] =
        basicBlocks.flatMap { blocks =>
          val updatedBlocks: Seq[Future[TicketBlock]] = for {
            block <- blocks
            blockID <- block.id
            availabilityRaw = issuer ? AvailabilityCheck(blockID)

            availability = availabilityRaw.mapTo[Int]

            updatedBlock = availability.map { a =>
              block.copy(availability = Option(a))
            }
          } yield updatedBlock

          // Transform Seq[Future[...]] to Future[Seq[...]]
          Future.sequence(updatedBlocks)
        }

      blocksWithAvailability
    }
  }
}

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

    def * = (id.?, name, start, end, address, city, state, country) <>
      ((Event.apply _).tupled, Event.unapply)
  }

  val table = TableQuery[EventsTable]

  def list: Future[Seq[Event]] = {
    val eventList = table.result
    db.run(eventList)
  }

  def getByID(eventID: Long): Future[Option[Event]] = {
    val eventByID = table.filter { f =>
      f.id === eventID
    }.result.headOption

    db.run(eventByID)
  }

  def create(newEvent: Event): Future[Event] = {
    val insertion = (table returning table.map(_.id)) += newEvent

    val insertedIDFuture = db.run(insertion)

    val createdCopy: Future[Event] = insertedIDFuture.map { resultID =>
      newEvent.copy(id = Option(resultID))
    }

    createdCopy
  }
}
