package de.htwg.controller

import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.model.GameLogic.*
import de.htwg.model.command.MoveCommand
import de.htwg.util.Observable
import de.htwg.view.tui.ConsoleView

import scala.annotation.tailrec
import scala.io.StdIn.readLine

/**
 * GameController acts as the central state manager and event publisher (Subject).
 * It contains only game logic and publishes abstract GameEvents.
 * It is completely freed from all string literals and console I/O, except for readLine().
 */
object GameController extends Observable[GameEvent] {

  // Convert column letter (a-h) to index (0-7)
  def columnToIndex(col: Char): Option[Int] = {
    val index = col - 'a'
    if (index >= 0 && index < 8) Some(index) else None
  }

  // Convert row number (1-8) to index (0-7)
  def rowToIndex(row: Int): Option[Int] = {
    val index = row - 1
    if (index >= 0 && index < 8) Some(index) else None
  }

  // Expose parseInput for testing (if necessary)
  private[controller] def parseInput(input: String): Option[(Int, Int, Int, Int)] = {
    // ... (Original implementation)
    val parts = input.split("\\s+")
    if (parts.length != 2) return None

    val positions = parts.flatMap { part =>
      val col = part.charAt(0)
      val rowStr = part.substring(1)
      for {
        colIdx <- columnToIndex(col)
        rowNum <- rowStr.toIntOption
        rowIdx <- rowToIndex(rowNum)
      } yield (rowIdx, colIdx)
    }

    if (positions.length == 2) {
      val (fromR, fromC) = positions(0)
      val (toR, toC) = positions(1)
      Some((fromR, fromC, toR, toC))
    } else None
  }

  // --- State Management ---
  private var currentState: GameState = AwaitingInputState

  // (columnToIndex, rowToIndex, and parseInput remain UNCHANGED)

  def startGame(): Unit = {
    this.add(ConsoleView)

    notifyObservers(StartGame())

    notifyObservers(RequestInput(isRedTurn = true))
    readLine() // Blocking input for "Press Enter"

    // --- State Machine Execution Loop ---
    var currentBoard: Board = Board.create()
    var isRedTurn: Boolean = true

    // Start the state machine iteration
    currentState = AwaitingInputState

    while (currentState != GameOverState) {
      // The current state processes the request (input, execution, etc.)
      // and returns the NEXT state, the new board, and whose turn it is.
      val (nextState, newBoard, nextTurn) = currentState.process(this, currentBoard, isRedTurn)

      // Update the context variables for the next iteration
      currentState = nextState
      currentBoard = newBoard
      isRedTurn = nextTurn
    }
    // Loop ends when currentState is GameOverState
  }

}