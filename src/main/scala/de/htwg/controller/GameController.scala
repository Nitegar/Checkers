package de.htwg.controller

import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.util.Observable

import scala.util.{Failure, Success, Try}

/**
 * GameController acts as the central state manager and event publisher (Subject).
 * Modified to support both TUI and GUI through InputHandler abstraction.
 */
object GameController extends Observable[GameEvent] {

  // Convert column letter (a-h) to index (0-7)
  def columnToIndex(col: Char): Try[Int] = {
    val lowerCol = col.toLower
    val index = lowerCol - 'a'

    if (index >= 0 && index < 8) {
      Success(index)
    } else {
      Failure(new IllegalArgumentException(s"Invalid column character: '$col'. Expected 'a'-'h'."))
    }
  }

  def rowToIndex(row: Int): Try[Int] = {
    val index = row - 1

    if (index >= 0 && index < 8) {
      Success(index)
    } else {
      Failure(new IllegalArgumentException(s"Invalid row number: $row. Expected 1-8."))
    }
  }

  private def parseRowString(rowStr: String, pos: String): Try[Int] = {
    Try(rowStr.toInt).recoverWith {
      case _: NumberFormatException =>
        Failure(new IllegalArgumentException(s"Invalid $pos row input: '$rowStr'. Expected a digit (1-8)."))
    }
  }

  def parseInput(input: String): Try[Input] = {
    val parts = input.trim.toLowerCase.split("\\s+")
    if (parts.length != 2) {
      return Failure(new IllegalArgumentException("Invalid format. Expected format: 'A1 B2' (source destination)."))
    }

    val srcStr = parts(0)
    val destStr = parts(1)

    if (srcStr.length < 2 || destStr.length < 2) {
      return Failure(new IllegalArgumentException("Coordinates must contain a column and a row (e.g., A1)."))
    }

    for {
      srcColChar <- Try(srcStr.charAt(0))
      srcColIdx <- columnToIndex(srcColChar)
      srcRowStr = srcStr.substring(1)
      srcRowInt <- parseRowString(srcRowStr, "source")
      srcRowIdx <- rowToIndex(srcRowInt)

      destColChar <- Try(destStr.charAt(0))
      destColIdx <- columnToIndex(destColChar)
      destRowStr = destStr.substring(1)
      destRowInt <- parseRowString(destRowStr, "destination")
      destRowIdx <- rowToIndex(destRowInt)

    } yield Input(srcRowIdx, srcColIdx, destRowIdx, destColIdx)
  }

  // --- State Management ---
  private var currentState: GameState = AwaitingInputState
  private var inputHandler: InputHandler = TuiInputHandler

  def isTuiActive: Boolean = inputHandler == TuiInputHandler

  def setInputHandler(handler: InputHandler): Unit = {
    inputHandler = handler
  }

  def getInputHandler: InputHandler = inputHandler

  def startGame(): Unit = {
    notifyObservers(StartGame())

    notifyObservers(RequestInput(isRedTurn = true))

    // --- State Machine Execution Loop ---
    var currentBoard: Board = Board().withStandardSetup().build()
    var isRedTurn: Boolean = true

    // Start the state machine iteration
    currentState = AwaitingInputState

    while (currentState != GameOverState) {
      val (nextState, newBoard, nextTurn) = currentState.process(this, currentBoard, isRedTurn)

      currentState = nextState
      currentBoard = newBoard
      isRedTurn = nextTurn
    }
  }
}