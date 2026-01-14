package de.htwg.controller.inputhandler

import de.htwg.model.GameSession

import scala.concurrent.{Future, Promise}

trait InputHandler {
  def attachSession(session: GameSession): Unit = ()
  def requestInput(): Future[String]
  def submitInput(input: String): Unit = ()
}