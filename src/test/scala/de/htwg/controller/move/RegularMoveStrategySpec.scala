package de.htwg.controller.move

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model.*
import de.htwg.model.Board.*

class RegularMoveStrategySpec extends AnyWordSpec with Matchers {

  "RegularMoveStrategy" should {

    "return forward diagonal moves for red piece" in {
      val board = Board()
        .empty()
        .addPiece(5, 2, Regular(true))
        .build()

      val strategy = new RegularMoveStrategy(isRed = true)
      val moves = strategy.validMoves(board, 5, 2)

      moves should contain((4, 1, false))
      moves should contain((4, 3, false))
    }

    "return forward diagonal moves for black piece" in {
      val board = Board()
        .empty()
        .addPiece(2, 3, Regular(false))
        .build()

      val strategy = new RegularMoveStrategy(isRed = false)
      val moves = strategy.validMoves(board, 2, 3)

      moves should contain((3, 2, false))
      moves should contain((3, 4, false))
    }

    "include jumps when available" in {
      val board = Board()
        .empty()
        .addPiece(5, 0, Regular(true))
        .addPiece(4, 1, Regular(false))
        .build()

      val strategy = new RegularMoveStrategy(isRed = true)
      val moves = strategy.validMoves(board, 5, 0)

      moves.exists(_._3) shouldBe true
      moves should contain((3, 2, true))
    }
  }
}
