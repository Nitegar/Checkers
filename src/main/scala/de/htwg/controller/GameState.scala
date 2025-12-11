package de.htwg.controller

import de.htwg.model.Board.Board
import de.htwg.model.*
import de.htwg.model.GameLogic.*
import de.htwg.model.command.{CommandHistory, MoveCommand}

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.util.{Failure, Success}

// --- State Interface ---
sealed trait GameState {
  def process(controller: GameController.type, board: Board, isRedTurn: Boolean, inputHandler: InputHandler): (GameState, Board, Boolean)
}

// --- Concrete States ---

case object AwaitingInputState extends GameState {
  override def process(controller: GameController.type, board: Board, isRedTurn: Boolean, inputHandler: InputHandler): (GameState, Board, Boolean) = {

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
  override def process(controller: GameController.type, board: Board, isRedTurn: Boolean, inputHandler: InputHandler): (GameState, Board, Boolean) = {

    // Use the input handler to get input (works for both TUI and GUI)
    val inputFuture = inputHandler.requestInput()
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
//            Thread.sleep(800)
            (AwaitingInputState, board, isRedTurn)
        }

      case "redo" | "r" =>
        CommandHistory.redo(board) match {
          case Some(nextBoard) =>
            controller.notifyObservers(MoveRedone)
            (AwaitingInputState, nextBoard, !isRedTurn)
          case None =>
            controller.notifyObservers(MoveFailed("Nothing to redo."))
//            Thread.sleep(800)
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
  override def process(controller: GameController.type, currentBoard: Board, isRedTurn: Boolean, inputHandler: InputHandler): (GameState, Board, Boolean) = {

    // Coordinate Flipping
    val (srcR, srcC, destR, destC) = (input.srcRow, input.srcCol, input.destRow, input.destCol)
//      if (isRedTurn) (input.srcRow, input.srcCol, input.destRow, input.destCol)
//      else (7 - input.srcRow, 7 - input.srcCol, 7 - input.destRow, 7 - input.destCol)

    // Piece Ownership/Empty Check
    currentBoard(srcR)(srcC) match {
      case Regular(isRed) if isRed != isRedTurn =>
        controller.notifyObservers(MoveFailed("Not your piece."))
//        Thread.sleep(800)
        return (AwaitingInputState, currentBoard, isRedTurn)

      case King(isRed) if isRed != isRedTurn =>
        controller.notifyObservers(MoveFailed("Not your piece."))
//        Thread.sleep(800)
        return (AwaitingInputState, currentBoard, isRedTurn)

      case Empty =>
        controller.notifyObservers(MoveFailed("No piece at that position."))
//        Thread.sleep(800)
        return (AwaitingInputState, currentBoard, isRedTurn)
      case _ =>
    }

    val command = MoveCommand(currentBoard, Input(srcR, srcC, destR, destC), isRedTurn)
    val (newBoard, success) = command.execute()

    if (success) {
      if (command.wasJump) {
        controller.notifyObservers(KillEffect(command.killCount))
//        Thread.sleep(2000)
      }
      CommandHistory.push(command)
      (AwaitingInputState, newBoard, !isRedTurn)
    } else {
      val requiredJumpMissed = hasJumpsAvailable(currentBoard, isRedTurn)
      val reason = if (requiredJumpMissed) "Must make jump." else "Invalid move."

      controller.notifyObservers(MoveFailed(reason))
//      Thread.sleep(800)

      (AwaitingInputState, currentBoard, isRedTurn)
    }
  }
}

case object GameOverState extends GameState {
  override def process(controller: GameController.type, board: Board, isRedTurn: Boolean, inputHandler: InputHandler): (GameState, Board, Boolean) = {
    (GameOverState, board, isRedTurn)
  }
}