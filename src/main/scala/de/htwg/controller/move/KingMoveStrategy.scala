package de.htwg.controller.move

import de.htwg.controller.GameLogic
import de.htwg.controller.GameLogic.isValidPosition
import de.htwg.model.Board.Board
import de.htwg.model.Empty

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
