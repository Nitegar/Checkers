package de.htwg.controller

import de.htwg.controller.GameLogic.*
import de.htwg.controller.command.{CommandHistory, MoveCommand}
import de.htwg.controller.inputhandler.InputHandler
import de.htwg.model.*
import de.htwg.model.Board.Board

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.util.{Failure, Success}

trait GameState {
  def process(session: GameSession, inputHandler: InputHandler): (GameState, Board, Boolean, List[GameEvent])
}

case object AwaitingInputState extends GameState {
  override def process(session: GameSession, inputHandler: InputHandler): (GameState, Board, Boolean, List[GameEvent]) = {
    val board = session.board
    val isRedTurn = session.isRedTurn

    val (red, black) = countPieces(board)
    if (red == 0 || black == 0) {
      return (GameOverState, board, isRedTurn, List(GameEnded(winnerIsRed = black == 0)))
    }

    val events = List(
      TurnAnnounced(isRedTurn),
      BoardUpdated(board, isRedTurn),
      RequestInput(isRedTurn)
    )

    // Transition to the Input Handling State
    (InputHandlingState, board, isRedTurn, events)
  }
}

case object InputHandlingState extends GameState {
  override def process(session: GameSession, inputHandler: InputHandler): (GameState, Board, Boolean, List[GameEvent]) = {
    val board = session.board
    val isRedTurn = session.isRedTurn

    // Use the input handler to get input (works for both TUI and GUI)
    val inputFuture = inputHandler.requestInput()
    val input = Await.result(inputFuture, Duration.Inf).trim.toLowerCase

    input match {
      case "quit" | "q" =>
        (GameOverState, board, isRedTurn, List(QuitGame()))

      case "undo" | "u" =>
        CommandHistory.undo(board) match {
          case Some(previousBoard) =>
            (AwaitingInputState, previousBoard, !isRedTurn, List(MoveUndone()))
          case None =>
            (AwaitingInputState, board, isRedTurn, List(MoveFailed("Nothing to undo.")))
        }

      case "redo" | "r" =>
        CommandHistory.redo(board) match {
          case Some(nextBoard) =>
            (AwaitingInputState, nextBoard, !isRedTurn, List(MoveRedone()))
          case None =>
            (AwaitingInputState, board, isRedTurn, List(MoveFailed("Nothing to redo.")))
        }

      case moveInput =>
        GameLogic.parseInput(moveInput) match {
          case Success(parsedInput) =>
            (MoveExecutionState(parsedInput), board, isRedTurn, List())
          case Failure(e) =>
            (AwaitingInputState, board, isRedTurn, List(InvalidInput(e.getMessage)))
        }
    }
  }
}

case class MoveExecutionState(input: Input) extends GameState {
  override def process(session: GameSession, inputHandler: InputHandler): (GameState, Board, Boolean, List[GameEvent]) = {
    val board = session.board
    val isRedTurn = session.isRedTurn
    val currentBoard = session.board
    val (srcR, srcC, destR, destC) = (input.srcRow, input.srcCol, input.destRow, input.destCol)

    currentBoard(srcR)(srcC) match {
      case Regular(isRed) if isRed != isRedTurn =>
        return (AwaitingInputState, currentBoard, isRedTurn, List(MoveFailed("Not your piece.")))

      case King(isRed) if isRed != isRedTurn =>
        return (AwaitingInputState, currentBoard, isRedTurn, List(MoveFailed("Not your piece.")))

      case Empty =>
        return (AwaitingInputState, currentBoard, isRedTurn, List(MoveFailed("No piece at that position.")))
      case _ =>
    }

    val command = MoveCommand(currentBoard, Input(srcR, srcC, destR, destC), isRedTurn)
    val (newBoard, success) = command.execute()

    if (success) {
      var events: List[GameEvent] = List()
      if (command.wasJump) {
        events = List(KillEffect(command.killCount))
      }
      CommandHistory.push(command)
      (AwaitingInputState, newBoard, !isRedTurn, events)
    } else {
      val requiredJumpMissed = hasJumpsAvailable(currentBoard, isRedTurn)
      val reason = if (requiredJumpMissed) "Must make jump." else "Invalid move."

      (AwaitingInputState, currentBoard, isRedTurn, List(MoveFailed(reason)))
    }
  }
}

case object GameOverState extends GameState {
  override def process(session: GameSession, inputHandler: InputHandler): (GameState, Board, Boolean, List[GameEvent]) = {
    (GameOverState, session.board, session.isRedTurn, List())
  }
}