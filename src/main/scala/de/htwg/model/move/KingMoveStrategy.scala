package de.htwg.model.move

import de.htwg.model.Board.Board
import de.htwg.model.{Empty, GameLogic}
import de.htwg.model.GameLogic.isValidPosition

class KingMoveStrategy(isRed: Boolean) extends MoveStrategy {

  override def validMoves(board: Board, row: Int, col: Int): List[(Int, Int, Boolean)] = {
    val moves = List(
      (row - 1, col - 1, false), (row - 1, col + 1, false),
      (row + 1, col - 1, false), (row + 1, col + 1, false)
    ).filter { case (r, c, _) =>
      isValidPosition(r, c) && board(r)(c) == Empty
    }
    val jumps = GameLogic.getJumps(board, row, col, isRed, regular = false)
    jumps ++ moves
  }
}
