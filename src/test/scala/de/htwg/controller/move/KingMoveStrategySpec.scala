package de.htwg.controller.move

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model.*
import de.htwg.model.Board.*

class KingMoveStrategySpec extends AnyWordSpec with Matchers {

  "KingMoveStrategy" should {

    "return diagonal moves in all directions" in {
      val board = Board()
        .empty()
        .addPiece(4, 4, King(true))
        .build()

      val strategy = new KingMoveStrategy(isRed = true)
      val moves = strategy.validMoves(board, 4, 4)

      moves should contain allOf (
        (3, 3, false), (3, 5, false),
        (5, 3, false), (5, 5, false)
      )
    }

    "include jumps when available" in {
      val board = Board()
        .empty()
        .addPiece(4, 4, King(true))
        .addPiece(3, 3, Regular(false))
        .build()

      val strategy = new KingMoveStrategy(isRed = true)
      val moves = strategy.validMoves(board, 4, 4)

      moves should contain((2, 2, true))
    }
  }
}
