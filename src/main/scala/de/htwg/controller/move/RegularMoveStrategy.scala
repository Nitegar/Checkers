package de.htwg.controller.move

import de.htwg.controller.GameLogic
import de.htwg.model.Board.Board
import de.htwg.model.Empty
import de.htwg.controller.GameLogic.isValidPosition


class RegularMoveStrategy(isRed: Boolean) extends MoveStrategy {

  override def validMoves(board: Board, row: Int, col: Int): List[(Int, Int, Boolean)] = {
    val direction = if (isRed) -1 else 1
    val moves = List(
      (row + direction, col - 1, false),
      (row + direction, col + 1, false)
    ).filter { case (r, c, _) =>
      isValidPosition(r, c) && board(r)(c) == Empty
    }
    val jumps = GameLogic.getJumps(board, row, col, isRed, regular = true)
    jumps ++ moves
  }
}
