package de.htwg.controller

import de.htwg.model.Board.Board
import de.htwg.model.{GameLogic, King, Regular}
import de.htwg.patterns.Command

trait MoveResult
case class MoveSuccess(newBoard: Board, isJump: Boolean) extends MoveResult
case object MoveFailure extends MoveResult

trait Executable {
  def execute(): MoveResult
}

case class MoveCommand(board: Board, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int, isRedTurn: Boolean) extends Command with Executable {
  override def execute(): MoveResult = {
    board(fromRow)(fromCol) match {
      case Regular(isRed) if isRed != isRedTurn => return MoveFailure
      case King(isRed) if isRed != isRedTurn => return MoveFailure
      case _ =>
    }

    val moves = GameLogic.getValidMoves(board, fromRow, fromCol)
    val mustJump = GameLogic.hasJumpsAvailable(board, isRedTurn)
    val move = moves.find { case (r, c, _) => r == toRow && c == toCol }

    move match {
      case Some((_, _, isJump)) if !mustJump || isJump =>
        MoveSuccess(GameLogic.makeMove(board, fromRow, fromCol, toRow, toCol), isJump)
      case _ => MoveFailure
    }
  }
}

case class QuitCommand() extends Command

case class InvalidCommand() extends Command