package com.semisafe.ticketoverlords

import javax.inject.Inject

import com.google.inject.AbstractModule
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment}
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.Future

class Module extends AbstractModule with AkkaGuiceSupport {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def configure = {
    logger.info("Configuring module")

    bindActor[TicketIssuer]("ticketIssuer")
    bind(classOf[ApplicationHooks]).asEagerSingleton()
  }
}

@javax.inject.Singleton
class ApplicationHooks @Inject() (lifecycle: ApplicationLifecycle) {
  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  lifecycle.addStopHook { () =>
    logger.info("Shutting down module")
    Future.successful(())
  }
}
