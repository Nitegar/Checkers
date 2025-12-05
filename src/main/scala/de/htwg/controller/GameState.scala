package de.htwg.controller

import de.htwg.model.Board.Board
import de.htwg.model.*
import de.htwg.model.GameLogic.*
import de.htwg.model.command.{CommandHistory, MoveCommand}

import scala.io.StdIn.readLine
import scala.util.{Failure, Success}

// --- State Interface ---
sealed trait GameState {
  def process(controller: GameController.type, board: Board, isRedTurn: Boolean): (GameState, Board, Boolean)
}

// --- Concrete States ---

case object AwaitingInputState extends GameState {
  override def process(controller: GameController.type, board: Board, isRedTurn: Boolean): (GameState, Board, Boolean) = {

    // Check for game end conditions
    val (red, black) = countPieces(board)
    if (red == 0 || black == 0) {
      controller.notifyObservers(GameEnded(winnerIsRed = black == 0))
      return (GameOverState, board, isRedTurn)
    }

    controller.notifyObservers(TurnAnnounced(isRedTurn))
    controller.notifyObservers(BoardUpdated(board, isRedTurn))
    controller.notifyObservers(RequestInput(isRedTurn))

    // Transition to the Input Handling State
    (InputHandlingState, board, isRedTurn)
  }
}

// In de.htwg.controller.InputHandlingState
case object InputHandlingState extends GameState {
  override def process(controller: GameController.type, board: Board, isRedTurn: Boolean): (GameState, Board, Boolean) = {

    val input = readLine().trim.toLowerCase

    input match {
      case "quit" | "q" =>
        controller.notifyObservers(QuitGame)
        (GameOverState, board, isRedTurn)

      case "undo" | "u" =>
        // 1. Attempt to undo the last move
        CommandHistory.undo(board) match {
          case Some(previousBoard) =>
            controller.notifyObservers(MoveUndone) // Notify view (new event needed)
            // Go back to the previous board and flip the turn
            (AwaitingInputState, previousBoard, !isRedTurn)
          case None =>
            controller.notifyObservers(MoveFailed("Nothing to undo."))
            Thread.sleep(800)
            (AwaitingInputState, board, isRedTurn) // Stay with same board/turn
        }

      case "redo" | "r" =>
        // 1. Attempt to redo the last undone move
        CommandHistory.redo(board) match {
          case Some(nextBoard) =>
            controller.notifyObservers(MoveRedone) // Notify view (new event needed)
            // Advance to the next board and flip the turn
            (AwaitingInputState, nextBoard, !isRedTurn)
          case None =>
            controller.notifyObservers(MoveFailed("Nothing to redo."))
            Thread.sleep(800)
            (AwaitingInputState, board, isRedTurn) // Stay with same board/turn
        }

      case moveInput =>
        controller.parseInput(moveInput) match {

          case Success(input) =>
            (MoveExecutionState(input), board, isRedTurn)

          case Failure(e) =>
            controller.notifyObservers(InvalidInput(e.getMessage))
            (AwaitingInputState, board, isRedTurn)
        }
    }
  }
}
case class MoveExecutionState(input: Input) extends GameState {
  override def process(controller: GameController.type, currentBoard: Board, isRedTurn: Boolean): (GameState, Board, Boolean) = {

    // 1. Coordinate Flipping
    val (srcR, srcC, destR, destC) =
      if (isRedTurn) (input.srcRow, input.srcCol, input.destRow, input.destCol)
      else (7 - input.srcRow, 7 - input.srcCol, 7 - input.destRow, 7 - input.destCol)

    // 2. Piece Ownership/Empty Check (Simplified from original due to return constraints)
    currentBoard(srcR)(srcC) match {
      case Regular(isRed) if isRed != isRedTurn =>
        controller.notifyObservers(MoveFailed("Not your piece."))
        Thread.sleep(800)
        return (AwaitingInputState, currentBoard, isRedTurn)

      // Case 2: The piece is King AND its color is NOT the current player's color
      case King(isRed) if isRed != isRedTurn =>
        controller.notifyObservers(MoveFailed("Not your piece."))
        Thread.sleep(800)
        return (AwaitingInputState, currentBoard, isRedTurn)

      case Empty =>
        controller.notifyObservers(MoveFailed("No piece at that position."))
        Thread.sleep(800)
        return (AwaitingInputState, currentBoard, isRedTurn)
      case _ =>
    }

    val command = MoveCommand(currentBoard, Input(srcR, srcC, destR, destC), isRedTurn)
    val (newBoard, success) = command.execute()

    if (success) {
      if (command.wasJump) {
        controller.notifyObservers(KillEffect(command.killCount))
        Thread.sleep(2000)
      }
      CommandHistory.push(command)
      (AwaitingInputState, newBoard, !isRedTurn)
    } else {
      val requiredJumpMissed = hasJumpsAvailable(currentBoard, isRedTurn)
      val reason = if (requiredJumpMissed) "Must make jump." else "Invalid move."

      controller.notifyObservers(MoveFailed(reason))
      Thread.sleep(800)

      (AwaitingInputState, currentBoard, isRedTurn)
    }
  }
}

case object GameOverState extends GameState {
  override def process(controller: GameController.type, board: Board, isRedTurn: Boolean): (GameState, Board, Boolean) = {
    // Terminal state. Return itself.
    (GameOverState, board, isRedTurn)
  }
}