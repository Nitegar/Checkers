package de.htwg.model

import org.scalatest.funsuite.AnyFunSuite

class BoardTest extends AnyFunSuite {

  test("create() generates a standard 8x8 board") {
    val board = Board.create()
    assert(board.length == 8)
    assert(board.forall(_.length == 8))
  }

  test("create() places black pieces on top 3 rows and red on bottom 3") {
    val board = Board.create()

    for (r <- 0 until 3; c <- 0 until 8 if (r + c) % 2 == 1) {
      assert(board(r)(c) == Regular(false))
    }

    for (r <- 3 to 4; c <- 0 until 8) {
      assert(board(r)(c) == Empty)
    }

    for (r <- 5 until 8; c <- 0 until 8 if (r + c) % 2 == 1) {
      assert(board(r)(c) == Regular(true))
    }
  }

  test("isValidPosition correctly identifies valid and invalid coordinates") {
    assert(Board.isValidPosition(0, 0))
    assert(Board.isValidPosition(7, 7))
    assert(!Board.isValidPosition(-1, 0))
    assert(!Board.isValidPosition(0, 8))
    assert(!Board.isValidPosition(8, 8))
  }
}
