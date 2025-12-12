package de.htwg.controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn

object TuiInputHandler extends InputHandler {
    override def requestInput(): Future[String] = Future {
        StdIn.readLine().trim.toLowerCase
    }
}   