package com.semisafe.ticketoverlords

import org.joda.time.DateTime

case class Order(id: Option[Long],
                 ticketBlockID: Long,
                 customerName: String,
                 customerEmail: String,
                 ticketQuantity: Int,
                 timestamp: Option[DateTime])
