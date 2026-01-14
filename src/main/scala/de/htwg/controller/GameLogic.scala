package de.htwg.controller

import de.htwg.controller.move.{KingMoveStrategy, RegularMoveStrategy}
import de.htwg.model.Board.*
import de.htwg.model.{Board, *}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object  GameLogic {

  // Convert column letter (a-h) to index (0-7)
  private def columnToIndex(col: Char): Try[Int] = {
    val lowerCol = col.toLower
    val index = lowerCol - 'a'

    if (index >= 0 && index < 8) {
      Success(index)
    } else {
      Failure(new IllegalArgumentException(s"Invalid column character: '$col'. Expected 'a'-'h'."))
    }
  }

  private def rowToIndex(row: Int): Try[Int] = {
    val index = row - 1

    if (index >= 0 && index < 8) {
      Success(index)
    } else {
      Failure(new IllegalArgumentException(s"Invalid row number: $row. Expected 1-8."))
    }
  }

  private def parseRowString(rowStr: String, pos: String): Try[Int] = {
    Try(rowStr.toInt).recoverWith {
      case _: NumberFormatException =>
        Failure(new IllegalArgumentException(s"Invalid $pos row input: '$rowStr'. Expected a digit (1-8)."))
    }
  }

  def parseInput(input: String): Try[Input] = {
    val parts = input.trim.toLowerCase.split("\\s+")
    if (parts.length != 2) {
      return Failure(new IllegalArgumentException("Invalid format. Expected format: 'A1 B2' (source destination)."))
    }

    val srcStr = parts(0)
    val destStr = parts(1)

    if (srcStr.length < 2 || destStr.length < 2) {
      return Failure(new IllegalArgumentException("Coordinates must contain a column and a row (e.g., A1)."))
    }

    for {
      srcColChar <- Try(srcStr.charAt(0))
      srcColIdx <- columnToIndex(srcColChar)
      srcRowStr = srcStr.substring(1)
      srcRowInt <- parseRowString(srcRowStr, "source")
      srcRowIdx <- rowToIndex(srcRowInt)

      destColChar <- Try(destStr.charAt(0))
      destColIdx <- columnToIndex(destColChar)
      destRowStr = destStr.substring(1)
      destRowInt <- parseRowString(destRowStr, "destination")
      destRowIdx <- rowToIndex(destRowInt)

    } yield Input(srcRowIdx, srcColIdx, destRowIdx, destColIdx)
  }


  def makeMove(board: Board, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): (Board, Int) = {
    val piece = board(fromRow)(fromCol)

    // 1. Determine the piece to place at the destination (checking for king promotion)
    val destinationPiece = piece match {
      case Regular(true) if toRow == 0 => King(true)
      case Regular(false) if toRow == 7 => King(false)
      case p => p
    }

    // Helper function for immutable cell update: returns a new Board with the cell (r, c) updated.
    val update = (b: Board, r: Int, c: Int, p: Piece) =>
      b.updated(r, b(r).updated(c, p))

    val boardAfterDestination = update(board, toRow, toCol, destinationPiece)

    val boardAfterSourceClear = update(boardAfterDestination, fromRow, fromCol, Empty)

    val isJump = math.abs(toRow - fromRow) == 2

    if (isJump) {
      val midRow = (fromRow + toRow) / 2
      val midCol = (fromCol + toCol) / 2

      val finalBoard = update(boardAfterSourceClear, midRow, midCol, Empty)
      val kills = 1
      (finalBoard, kills)
    } else {
      (boardAfterSourceClear, 0)
    }
  }
  // --- NEW: Recursive Jump Chain Function ---

  /**
   * Recursively checks for and executes subsequent jumps with the SAME piece.
   *
   * @param board      The current board state.
   * @param pieceRow   The current row of the jumping piece.
   * @param pieceCol   The current column of the jumping piece.
   * @param totalKills Accumulated kill count in the chain.
   * @return A tuple of (Final Board after chain, Total kills in the chain).
   */
  @tailrec
  def findJumpChain(board: Board, pieceRow: Int, pieceCol: Int, totalKills: Int): (Board, Int) = {

    val piece = board(pieceRow)(pieceCol)

    // Check for the piece type and color
    val (isRed, isRegular) = piece match {
      case Regular(r) => (r, true)
      case King(r) => (r, false)
    }

    // Get all available jumps for the piece at its new location
    val availableJumps = getJumps(board, pieceRow, pieceCol, isRed, isRegular)

    if (availableJumps.isEmpty) {
      // No more jumps available for this piece. Chain ends.
      (board, totalKills)
    } else if (availableJumps.size == 1) {
      // Exactly one jump available: execute it and continue the recursion.
      val (nextRow, nextCol, _) = availableJumps.head

      // Execute the single jump
      val (boardAfterJump, kills) = makeMove(board, pieceRow, pieceCol, nextRow, nextCol)

      // The piece may have become a King during this jump.
      // Need to re-evaluate its type in the next recursive call.
      findJumpChain(boardAfterJump, nextRow, nextCol, totalKills + kills)

    } else {
      // Multiple jumps available: The player MUST choose. We must STOP the automatic chain here.
      // In a TUI, we cannot automatically pick. We return the current state and let the Controller
      // handle prompting the user to select the next jump.

      // However, for pure *automatic* chain (as requested), we can arbitrarily pick one,
      // but the correct Checkers rule is usually forced selection.

      // *SIMPLIFIED RULE (For automatic solution): Pick the first available jump.*
      val (nextRow, nextCol, _) = availableJumps.head

      val (boardAfterJump, kills) = makeMove(board, pieceRow, pieceCol, nextRow, nextCol)

      findJumpChain(boardAfterJump, nextRow, nextCol, totalKills + kills)
    }
  }

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
