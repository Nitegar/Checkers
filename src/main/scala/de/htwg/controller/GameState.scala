package de.htwg.controller

import de.htwg.model.Board.Board
import de.htwg.model.*
import de.htwg.model.GameLogic.*
import de.htwg.model.command.{CommandHistory, MoveCommand}

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.util.{Failure, Success}

sealed trait GameState {
  def process(controller: GameController.type, board: Board, isRedTurn: Boolean): (GameState, Board, Boolean)
}


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

case object InputHandlingState extends GameState {
  override def process(controller: GameController.type, board: Board, isRedTurn: Boolean): (GameState, Board, Boolean) = {

    // Use the input handler to get input (works for both TUI and GUI)
    val inputFuture = controller.getInputHandler.requestInput()
    val input = Await.result(inputFuture, Duration.Inf).trim.toLowerCase

    input match {
      case "quit" | "q" =>
        controller.notifyObservers(QuitGame)
        (GameOverState, board, isRedTurn)

      case "undo" | "u" =>
        CommandHistory.undo(board) match {
          case Some(previousBoard) =>
            controller.notifyObservers(MoveUndone)
            (AwaitingInputState, previousBoard, !isRedTurn)
          case None =>
            controller.notifyObservers(MoveFailed("Nothing to undo."))
            (AwaitingInputState, board, isRedTurn)
        }

      case "redo" | "r" =>
        CommandHistory.redo(board) match {
          case Some(nextBoard) =>
            controller.notifyObservers(MoveRedone)
            (AwaitingInputState, nextBoard, !isRedTurn)
          case None =>
            controller.notifyObservers(MoveFailed("Nothing to redo."))
            (AwaitingInputState, board, isRedTurn)
        }

      case moveInput =>
        controller.parseInput(moveInput) match {
          case Success(parsedInput) =>
            (MoveExecutionState(parsedInput), board, isRedTurn)

          case Failure(e) =>
            controller.notifyObservers(InvalidInput(e.getMessage))
            (AwaitingInputState, board, isRedTurn)
        }
    }
  }
}

case class MoveExecutionState(input: Input) extends GameState {
  override def process(controller: GameController.type, currentBoard: Board, isRedTurn: Boolean): (GameState, Board, Boolean) = {

    val shouldFlip = !isRedTurn && controller.isTuiActive

    val (srcR, srcC, destR, destC) = (input.srcRow, input.srcCol, input.destRow, input.destCol)
//      if (shouldFlip) {
//        // Apply the flip ONLY for TUI Black moves
//        (7 - input.srcRow, 7 - input.srcCol, 7 - input.destRow, 7 - input.destCol)
//      } else {
//        // Use coordinates directly (used by Red TUI, and all GUI moves)
//        (input.srcRow, input.srcCol, input.destRow, input.destCol)
//      }

    // Piece Ownership/Empty Check
    currentBoard(srcR)(srcC) match {
      case Regular(isRed) if isRed != isRedTurn =>
        controller.notifyObservers(MoveFailed("Not your piece."))
        return (AwaitingInputState, currentBoard, isRedTurn)

      case King(isRed) if isRed != isRedTurn =>
        controller.notifyObservers(MoveFailed("Not your piece."))
        return (AwaitingInputState, currentBoard, isRedTurn)

      case Empty =>
        controller.notifyObservers(MoveFailed("No piece at that position."))
        return (AwaitingInputState, currentBoard, isRedTurn)
      case _ =>
    }

    val command = MoveCommand(currentBoard, Input(srcR, srcC, destR, destC), isRedTurn)
    val (newBoard, success) = command.execute()

    if (success) {
      if (command.wasJump) {
        controller.notifyObservers(KillEffect(command.killCount))
      }
      CommandHistory.push(command)
      (AwaitingInputState, newBoard, !isRedTurn)
    } else {
      val requiredJumpMissed = hasJumpsAvailable(currentBoard, isRedTurn)
      val reason = if (requiredJumpMissed) "Must make jump." else "Invalid move."

      controller.notifyObservers(MoveFailed(reason))

      (AwaitingInputState, currentBoard, isRedTurn)
    }
  }
}

case object GameOverState extends GameState {
  override def process(controller: GameController.type, board: Board, isRedTurn: Boolean): (GameState, Board, Boolean) = {
    (GameOverState, board, isRedTurn)
  }
}