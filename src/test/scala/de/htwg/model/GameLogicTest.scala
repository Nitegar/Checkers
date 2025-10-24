package de.htwg.model

import de.htwg.model.{Board, Empty, King, Regular}
import org.scalatest.funsuite.AnyFunSuite

class GameLogicTest extends AnyFunSuite {

  test("countPieces counts red and black correctly") {
    val board = Board.create()
    val (red, black) = GameLogic.countPieces(board)
    assert(red > 0)
    assert(black > 0)
    assert(red + black <= 24)
  }

  test("hasJumpsAvailable returns false if no jumps on initial board") {
    val board = Board.create()
    assert(!GameLogic.hasJumpsAvailable(board, isRedTurn = true))
    assert(!GameLogic.hasJumpsAvailable(board, isRedTurn = false))
  }

  test("getValidMoves returns empty for Empty square") {
    val board = Board.create()
    assert(GameLogic.getValidMoves(board, 3, 3).isEmpty) // middle row is empty
  }

  test("getValidMoves returns valid moves for a Regular piece") {
    val board = Board.create()
    val moves = GameLogic.getValidMoves(board, 5, 0)
    assert(moves.nonEmpty)
    assert(moves.forall { case (r, c, _) => Board.isValidPosition(r, c) })
  }

  test("getValidMoves returns valid moves for a King piece") {
    val board = Board.create()
    val kingBoard = board.map(_.clone())
    kingBoard(3)(3) = King(true)
    val moves = GameLogic.getValidMoves(kingBoard, 3, 3)
    assert(moves.nonEmpty)
    assert(moves.forall { case (r, c, _) => Board.isValidPosition(r, c) })
  }

  test("getJumps detects a possible jump") {
    val board = Board.create().map(_.clone())
    board(2)(2) = Regular(true)
    board(1)(1) = Regular(false)
    board(0)(0) = Empty
    val jumps = GameLogic.getJumps(board, 2, 2, isRed = true, regular = true)
    assert(jumps.contains((0, 0, true)))
  }

  test("makeMove moves piece and removes jumped piece") {
    val board = Board.create().map(_.clone())
    val firstTurnBoard = GameLogic.makeMove(board, 5, 0, 4, 1)
    val secondTurnBoard = GameLogic.makeMove(firstTurnBoard, 2, 3, 3, 2)
    val thirdTurnBoard = GameLogic.makeMove(secondTurnBoard, 4,1, 2, 3)

    assert(thirdTurnBoard(3)(2) == Empty)
    assert(thirdTurnBoard(2)(3) == Regular(true))
  }

  test("makeMove promotes Regular to King when reaching last row") {
    val board = Board.create().map(_.clone())
    board(1)(2) = Regular(true)
    board(0)(3) = Empty
    val newBoard = GameLogic.makeMove(board, 1, 2, 0, 3)
    assert(newBoard(0)(3).isInstanceOf[King])
  }
}
