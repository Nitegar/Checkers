package de.htwg.controller.inputhandler.impl

import de.htwg.controller.inputhandler.InputHandler
import de.htwg.model.GameSession

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

/**
 * Input handler that listens to multiple handlers at once.
 * The first one that produces input "wins" the turn.
 */
class MultiInputHandler(handlers: InputHandler*) extends InputHandler {
  private var gameSession: GameSession = _

  override def attachSession(session: GameSession): Unit = {
    gameSession = session
    handlers.foreach(_.attachSession(session))
  }

  override def submitInput(input: String): Unit = {
    handlers.foreach(_.submitInput(input))
  }

  override def requestInput(): Future[String] = {
    val promise = Promise[String]()
    val turnAtRequest = gameSession.turnCount

    handlers.foreach { handler =>
      handler.requestInput().foreach { input =>
        // Only fulfill if we are still on the same turn
        // AND the input isn't just a leftover empty string
        if (gameSession.turnCount == turnAtRequest && input.nonEmpty) {
          promise.trySuccess(input)
        }
      }
    }
    promise.future
  }

}