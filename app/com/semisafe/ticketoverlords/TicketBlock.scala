package com.semisafe.ticketoverlords

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import play.api.db.DBApi

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import akka.actor.{ActorSelection, ActorSystem}
import play.libs.Akka
import slick.backend.DatabaseConfig

case class TicketBlock(
  id: Option[Long],
  eventID: Long,
  name: String,
  productCode: String,
  price: BigDecimal,
  initialSize: Int,
  saleStart: DateTime,
  saleEnd: DateTime,
  availability: Option[Int] = None)

object TicketBlock {
  implicit val format: Format[TicketBlock] = Json.format[TicketBlock]
}

@javax.inject.Singleton
class TicketBlockDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                               protected val eventDao: EventDao,
                               system: ActorSystem) extends SlickMapping {
  import dbConfig.driver.api._

  class TicketBlocksTable(tag: Tag)
      extends Table[TicketBlock](tag, "TICKET_BLOCKS") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def eventID = column[Long]("EVENT_ID")
    def name = column[String]("NAME")
    def productCode = column[String]("PRODUCT_CODE")
    def price = column[BigDecimal]("PRICE")
    def initialSize = column[Int]("INITIAL_SIZE")
    def saleStart = column[DateTime]("SALE_START")
    def saleEnd = column[DateTime]("SALE_END")

    def event = foreignKey("TB_EVENT", eventID, eventDao.table)(_.id)

    def * = (id.?, eventID, name, productCode, price, initialSize,
      saleStart, saleEnd) <>
      (
        (TicketBlock.apply(_: Option[Long], _: Long, _: String, _: String,
          _: BigDecimal, _: Int, _: DateTime, _: DateTime,
          None)).tupled, { tb: TicketBlock =>
            TicketBlock.unapply(tb).map {
              case (a, b, c, d, e, f, g, h, _) => (a, b, c, d, e, f, g, h)
            }
          })
  }

  val table = TableQuery[TicketBlocksTable]

  def list: Future[Seq[TicketBlock]] = {
    val blockList = table.result
    db.run(blockList)
  }

  def listForEvent(eventID: Long): Future[Seq[TicketBlock]] = {
    val blockList = table.filter { tb =>
      tb.eventID === eventID
    }.result
    db.run(blockList)
  }

  def getByID(blockID: Long): Future[Option[TicketBlock]] = {
    val blockByID = table.filter { f =>
      f.id === blockID
    }.result.headOption
    db.run(blockByID)
  }

  def create(newTicketBlock: TicketBlock): Future[TicketBlock] = {
    val insertion = (table returning table.map(_.id)) += newTicketBlock
    db.run(insertion).map { resultID =>
      val createdBlock = newTicketBlock.copy(id = Option(resultID))

      val issuer = TicketIssuer.getSelection(system)
      issuer ! TicketBlockCreated(createdBlock)

      createdBlock
    }
  }

  def availability(ticketBlockID: Long): Future[Int] = {
    db.run {
      val query = sql"""
     select INITIAL_SIZE - COALESCE(SUM(TICKET_QUANTITY), 0)
     from TICKET_BLOCKS tb
     left join ORDERS o on o.TICKET_BLOCK_ID=tb.ID
     where tb.ID=${ticketBlockID}
     group by INITIAL_SIZE;
     """.as[Int]

      query.headOption
    }.map { _.getOrElse(0) }
  }
}
