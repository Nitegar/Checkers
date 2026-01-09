package de.htwg.controller

import scala.concurrent.{Future, Promise}

object GuiInputHandler extends InputHandler {
  private var inputPromise: Option[Promise[String]] = None

  override def requestInput(): Future[String] = {
    val promise = Promise[String]()
    inputPromise = Some(promise)
    promise.future
  }

  /**
   * Called by GUI when user makes a move
   */
  def submitInput(input: String): Unit = {
    inputPromise.foreach(_.success(input))
    inputPromise = None
  }
}
