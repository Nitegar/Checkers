package de.htwg.controller

import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.util.Observable
import scala.concurrent.{ExecutionContext, Future} // NEW: Future and ExecutionContext
import scala.util.{Failure, Success, Try}

// --- REQUIRED FOR ASYNCHRONOUS EXECUTION ---
object GameController extends Observable[GameEvent] {
  implicit val executionContext: ExecutionContext = ExecutionContext.global

  // State variables are now object members to be updated by runGameLoop
  private var currentState: GameState = AwaitingInputState
  private var currentBoard: Board = Board().withStandardSetup().build()
  private var isRedTurn: Boolean = true
  private var inputHandler: InputHandler = TuiInputHandler

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



  def setInputHandler(handler: InputHandler): Unit = {
    inputHandler = handler
  }
  // --- ASYNCHRONOUS START GAME (REMOVED ALL BLOCKING CODE) ---
  def startGame(): Unit = {
    notifyObservers(StartGame())

    // 1. Initialize State and Board
    currentBoard = Board().withStandardSetup().build()
    isRedTurn = true
    currentState = AwaitingInputState

    // 2. Start the non-blocking game loop immediately.
    runGameLoop(currentState, currentBoard, isRedTurn)
  }

  // --- ASYNCHRONOUS GAME LOOP (Recursive Future Chain) ---
  private def runGameLoop(state: GameState, board: Board, redTurn: Boolean): Unit = {
    // Update the state variables
    currentState = state
    currentBoard = board
    isRedTurn = redTurn

    // Phase 1: Preparation/Announcement (Uses the new processPreparation)
    val (stateAfterPrep, boardAfterPrep, turnAfterPrep) = state match {
      case AwaitingInputState =>
        AwaitingInputState.processPreparation(this, board, redTurn)
      case _ => (state, board, redTurn)
    }

    stateAfterPrep match {
      case GameOverState =>
        return

      case InputHandlingState =>
        notifyObservers(RequestInput(turnAfterPrep))

        // This is the only place we request input. The resulting Future determines when to continue.
        inputHandler.requestInput().onComplete {
          case Success(input) =>
            // Phase 2: Process the input (using the new processInput)
            val (nextState, nextBoard, nextTurn) =
              InputHandlingState.processInput(this, boardAfterPrep, turnAfterPrep, input)

            // Phase 3: Execute the move (using the new processExecution)
            val (finalState, finalBoard, finalTurn) = nextState match {
              case MoveExecutionState(parsedInput) =>
                MoveExecutionState(parsedInput).processExecution(this, nextBoard, nextTurn)
              case _ => (nextState, nextBoard, nextTurn)
            }

            // Phase 4: Recurse for the next turn
            if (finalState != GameOverState) {
              runGameLoop(finalState, finalBoard, finalTurn)
            }

          case Failure(e) =>
            notifyObservers(InvalidInput(s"Input handler error: ${e.getMessage}"))
            runGameLoop(AwaitingInputState, boardAfterPrep, turnAfterPrep)
        }

      case _ =>
        runGameLoop(AwaitingInputState, boardAfterPrep, turnAfterPrep)
    }
  }
}