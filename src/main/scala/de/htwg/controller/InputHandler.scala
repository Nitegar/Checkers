package de.htwg.controller

import scala.concurrent.{Future, Promise}

trait InputHandler {
  def requestInput(): Future[String]
}