package de.htwg.controller

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object TuiInputHandler extends InputHandler {
    override def requestInput(): Future[String] = Future {
        StdIn.readLine().trim.toLowerCase
    }
}