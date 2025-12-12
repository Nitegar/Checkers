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

    // Start all input handlers in parallel
    handlers.foreach { handler =>
      handler.requestInput().foreach { input =>
        promise.trySuccess(input) // only the first success counts
      }
    }

    promise.future
  }
}
