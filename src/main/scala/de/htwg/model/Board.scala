package de.htwg.model

object Board {
  type Board = Array[Array[Piece]]

  def create(): Board = {
    val board = Array.ofDim[Piece](8, 8)
    for {
      row <- 0 until 8
      col <- 0 until 8
    } {
      board(row)(col) = (row, col) match {
        case (r, c) if r < 3 && (r + c) % 2 == 1 => Regular(false)
        case (r, c) if r > 4 && (r + c) % 2 == 1 => Regular(true)
        case _ => Empty
      }
    }
    board
  }

  def isValidPosition(row: Int, col: Int): Boolean =
    row >= 0 && row < 8 && col >= 0 && col < 8
}
