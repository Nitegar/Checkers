package de.htwg.controller.inputhandler

import de.htwg.controller.inputhandler.InputHandler

import scala.concurrent.{Future, Promise}

class GuiInputHandler extends InputHandler {
  private var inputPromise: Option[Promise[String]] = None

  override def requestInput(): Future[String] = {
    val promise = Promise[String]()
    inputPromise = Some(promise)
    promise.future
  }

  /**
   * Called by GUI when user makes a move
   */
  override def submitInput(input: String): Unit = {
    inputPromise.foreach(_.success(input))
    inputPromise = None
  }
}
