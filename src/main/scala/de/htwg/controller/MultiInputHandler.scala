package de.htwg.controller

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Input handler that listens to multiple handlers at once.
 * The first one that produces input "wins" the turn.
 */
class MultiInputHandler(handlers: InputHandler*) extends InputHandler {
  override def requestInput(): Future[String] = {
    val promise = Promise[String]()
    val turnAtRequest = GameController.getCurrentTurn

    handlers.foreach { handler =>
      handler.requestInput().foreach { input =>
        // Only fulfill if we are still on the same turn
        // AND the input isn't just a leftover empty string
        if (GameController.getCurrentTurn == turnAtRequest && input.nonEmpty) {
          promise.trySuccess(input)
        }
      }
    }
    promise.future
  }
}