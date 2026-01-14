package de.htwg.controller

import de.htwg.controller.inputhandler.{InputHandler, TuiInputHandler}
import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.util.Observable

import scala.util.{Failure, Success, Try}

/**
 * GameController acts as the central state manager and event publisher (Subject).
 * Modified to support both TUI and GUI through InputHandler abstraction.
 */
class GameController(inputHandler: InputHandler) extends IController {

  private val session = GameSession()

  inputHandler.attachSession(session)
  
  private var currentState: GameState = AwaitingInputState

  override def startGame(): Unit = {
    notifyObservers(StartGame())
    notifyObservers(RequestInput(isRedTurn = true))

    val currentBoard: Board = Board().withStandardSetup().build()
    val isRedTurn: Boolean = true

    currentState = AwaitingInputState

    while (currentState != GameOverState) {
      val (nextState, nextBoard, nextTurn, events) = currentState.process(session, inputHandler)

      events.foreach(notifyObservers)

      if (nextBoard != session.board) session.incrementTurn()
      session.board = nextBoard
      session.isRedTurn = nextTurn
      currentState = nextState
    }
  }
}