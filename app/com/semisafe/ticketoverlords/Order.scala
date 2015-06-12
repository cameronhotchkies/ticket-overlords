package com.semisafe.ticketoverlords

import org.joda.time.DateTime
import play.api.libs.json.{ Json, Format }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import play.api.Play.current
import play.api.db.DBApi
import SlickMapping.jodaDateTimeMapping
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

case class Order(id: Option[Long],
                 ticketBlockID: Long,
                 customerName: String,
                 customerEmail: String,
                 ticketQuantity: Int,
                 timestamp: Option[DateTime])

object Order {
  implicit val format: Format[Order] = Json.format[Order]

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](current)
  import dbConfig._
  import dbConfig.driver.api._

  class OrdersTable(tag: Tag) extends Table[Order](tag, "ORDERS") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def ticketBlockID = column[Long]("TICKET_BLOCK_ID")
    def customerName = column[String]("CUSTOMER_NAME")
    def customerEmail = column[String]("CUSTOMER_EMAIL")
    def ticketQuantity = column[Int]("TICKET_QUANTITY")
    def timestamp = column[DateTime]("TIMESTAMP")

    def ticketBlock = foreignKey("O_TICKETBLOCK", ticketBlockID, TicketBlock.table)(_.id)

    def * = (id.?, ticketBlockID, customerName, customerEmail, ticketQuantity, timestamp.?) <>
      ((Order.apply _).tupled, Order.unapply)
  }

  val table = TableQuery[OrdersTable]

  def list: Future[Seq[Order]] = {
    db.run(table.result)
  }

  def getByID(orderID: Long): Future[Option[Order]] = {
    db.run {
      table.filter { f =>
        f.id === orderID
      }.result.headOption
    }
  }

  def create(newOrder: Order): Future[Order] = {
    val nowStamp = new DateTime()
    val withTimestamp = newOrder.copy(timestamp = Option(nowStamp))

    val insertion = (table returning table.map(_.id)) += withTimestamp

    db.run(insertion).map { resultID =>
      withTimestamp.copy(id = Option(resultID))
    }
  }
}