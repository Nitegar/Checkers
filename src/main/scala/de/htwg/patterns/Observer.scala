package de.htwg.patterns

import de.htwg.model.Board.Board

trait Observer {
  def update(board: Board, isRedTurn: Boolean): Unit
}
