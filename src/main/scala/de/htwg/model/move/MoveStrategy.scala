package de.htwg.model.move

import de.htwg.model.Board.Board

trait MoveStrategy {
  def validMoves(board: Board, row: Int, col: Int): List[(Int, Int, Boolean)]
}
