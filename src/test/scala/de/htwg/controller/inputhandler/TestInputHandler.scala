package de.htwg.controller.inputhandler

import de.htwg.model.GameSession

import scala.concurrent.{Future, Promise}

class TestInputHandler extends InputHandler {

  var sessionAttached = false

  override def attachSession(session: GameSession): Unit =
    sessionAttached = true

  override def requestInput(): Future[String] =
    Promise[String]().future // never completes, but is never used

  override def submitInput(input: String): Unit = ()
}
