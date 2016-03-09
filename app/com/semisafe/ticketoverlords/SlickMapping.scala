package com.semisafe.ticketoverlords

import java.sql.Timestamp

import org.joda.time.DateTime
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

abstract class SlickMapping extends HasDatabaseConfigProvider[JdbcProfile] {
  import dbConfig.driver.api._

  implicit val jodaDateTimeMapping = {
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.getMillis),
      ts => new DateTime(ts))
  }
}
