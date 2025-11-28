package de.htwg.model

import de.htwg.model.Board.*
import de.htwg.model.move.{KingMoveStrategy, RegularMoveStrategy}
import de.htwg.model.{Empty, King, Regular}

object GameLogic {

  def getValidMoves(board: Board, row: Int, col: Int): List[(Int, Int, Boolean)] = {
    board(row)(col) match {
      case Empty => Nil
      case Regular(isRed) =>
        val strategy = new RegularMoveStrategy(isRed)
        strategy.validMoves(board, row, col)
      case King(isRed) =>
        val strategy = new KingMoveStrategy(isRed)
        strategy.validMoves(board, row, col)
    }
  }

  def getJumps(board: Board, row: Int, col: Int, isRed: Boolean, regular: Boolean): List[(Int, Int, Boolean)] = {
    val directions = if (regular) {
      val dir = if (isRed) -1 else 1
      List((dir, -1), (dir, 1))
    } else {
      List((-1, -1), (-1, 1), (1, -1), (1, 1))
    }

    directions.flatMap { case (dr, dc) =>
      val midRow = row + dr
      val midCol = col + dc
      val endRow = row + 2 * dr
      val endCol = col + 2 * dc

      if (isValidPosition(endRow, endCol) && board(endRow)(endCol) == Empty) {
        board(midRow)(midCol) match {
          case Regular(enemyRed) if enemyRed != isRed => Some((endRow, endCol, true))
          case King(enemyRed) if enemyRed != isRed => Some((endRow, endCol, true))
          case _ => None
        }
      } else None
    }
  }

  def makeMove(board: Board, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Board = {
    val newBoard = board.map(_.clone())
    val piece = newBoard(fromRow)(fromCol)
    newBoard(toRow)(toCol) = piece match {
      case Regular(true) if toRow == 0 => King(true)
      case Regular(false) if toRow == 7 => King(false)
      case p => p
    }
    newBoard(fromRow)(fromCol) = Empty

    if (math.abs(toRow - fromRow) == 2) {
      val midRow = (fromRow + toRow) / 2
      val midCol = (fromCol + toCol) / 2
      newBoard(midRow)(midCol) = Empty
    }
    newBoard
  }

  def hasJumpsAvailable(board: Board, isRedTurn: Boolean): Boolean = {
    (for {
      row <- 0 until 8
      col <- 0 until 8
      if board(row)(col) match {
        case Regular(red) => red == isRedTurn
        case King(red) => red == isRedTurn
        case _ => false
      }
      moves = getValidMoves(board, row, col)
      if moves.exists(_._3)
    } yield true).nonEmpty
  }

  def countPieces(board: Board): (Int, Int) = {
    var red, black = 0
    for (row <- board; piece <- row) {
      piece match {
        case Regular(true) | King(true) => red += 1
        case Regular(false) | King(false) => black += 1
        case _ =>
      }
    }
    (red, black)
  }

  def isValidPosition(row: Int, col: Int): Boolean =
    row >= 0 && row < 8 && col >= 0 && col < 8
}
