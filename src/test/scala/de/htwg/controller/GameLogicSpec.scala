package de.htwg.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model.*
import de.htwg.model.Board.*

class GameLogicSpec extends AnyWordSpec with Matchers {

  "parseInput" should {
    "parse valid input correctly (lower and upper case)" in {
      GameLogic.parseInput("a3 b4").get shouldBe Input(2, 0, 3, 1)
      GameLogic.parseInput("  H8 G7  ").get shouldBe Input(7, 7, 6, 6)
    }

    "fail on invalid format (too many or too few parts)" in {
      GameLogic.parseInput("A3 B4 C5").isFailure shouldBe true
      GameLogic.parseInput("A3").isFailure shouldBe true
    }

    "fail on coordinate strings that are too short" in {
      GameLogic.parseInput("A B4").isFailure shouldBe true
      GameLogic.parseInput("A3 B").isFailure shouldBe true
    }

    "fail on invalid column characters" in {
      GameLogic.parseInput("z3 b4").isFailure shouldBe true
      GameLogic.parseInput("a3 !4").isFailure shouldBe true
    }

    "fail on non-numeric row inputs" in {
      // Covers parseRowString NumberFormatException
      GameLogic.parseInput("aR b4").isFailure shouldBe true
      GameLogic.parseInput("a3 bX").isFailure shouldBe true
    }

    "fail on out-of-bounds row numbers" in {
      GameLogic.parseInput("a9 b4").isFailure shouldBe true
      GameLogic.parseInput("a3 b0").isFailure shouldBe true
    }
  }

  "makeMove" should {
    "promote a Red piece to King at row 0" in {
      val board = Board().empty().addPiece(1, 1, Regular(true)).build()
      val (newBoard, _) = GameLogic.makeMove(board, 1, 1, 0, 0)
      newBoard(0)(0) shouldBe King(true)
    }

    "promote a Black piece to King at row 7" in {
      val board = Board().empty().addPiece(6, 1, Regular(false)).build()
      val (newBoard, _) = GameLogic.makeMove(board, 6, 1, 7, 2)
      newBoard(7)(2) shouldBe King(false)
    }

    "not promote if destination is not the end row" in {
      val board = Board().empty().addPiece(5, 5, Regular(true)).build()
      val (newBoard, _) = GameLogic.makeMove(board, 5, 5, 4, 4)
      newBoard(4)(4) shouldBe Regular(true)
    }
  }

  "findJumpChain" should {
    "execute a double jump chain automatically" in {
      // Red piece at 5,0 captures Black at 4,1 (to 3,2), then captures Black at 2,3 (to 1,4)
      val board = Board().empty()
        .addPiece(5, 0, Regular(true))
        .addPiece(4, 1, Regular(false))
        .addPiece(2, 3, Regular(false))
        .build()

      val (finalBoard, totalKills) = GameLogic.findJumpChain(board, 5, 0, 0)

      totalKills shouldBe 2
      finalBoard(5)(0) shouldBe Empty
      finalBoard(4)(1) shouldBe Empty
      finalBoard(2)(3) shouldBe Empty
      finalBoard(1)(4) shouldBe Regular(true)
    }

    "handle a King in a jump chain" in {
      val board = Board().empty()
        .addPiece(3, 3, King(true))
        .addPiece(4, 4, Regular(false))
        .build()

      val (finalBoard, kills) = GameLogic.findJumpChain(board, 3, 3, 0)
      kills shouldBe 1
      finalBoard(5)(5) shouldBe King(true)
    }
  }

  "getValidMoves" should {
    "return Nil for an empty cell" in {
      val board = Board().empty().build()
      GameLogic.getValidMoves(board, 4, 4) shouldBe Nil
    }

    "return moves for a King piece" in {
      val board = Board().empty().addPiece(4, 4, King(true)).build()
      val moves = GameLogic.getValidMoves(board, 4, 4)
      moves should not be empty
    }
  }

  "getJumps" should {
    "detect jumps in all directions for a King" in {
      val board = Board().empty()
        .addPiece(4, 4, King(true))
        .addPiece(3, 3, Regular(false)) // Up-Left
        .addPiece(5, 5, Regular(false)) // Down-Right
        .build()

      val jumps = GameLogic.getJumps(board, 4, 4, isRed = true, regular = false)
      jumps.map(j => (j._1, j._2)) should contain allOf ((2, 2), (6, 6))
    }

    "not jump over own pieces" in {
      val board = Board().empty()
        .addPiece(5, 0, Regular(true))
        .addPiece(4, 1, Regular(true)) // Same color
        .build()

      GameLogic.getJumps(board, 5, 0, isRed = true, regular = true) shouldBe Nil
    }

    "not jump if destination is occupied" in {
      val board = Board().empty()
        .addPiece(5, 0, Regular(true))
        .addPiece(4, 1, Regular(false))
        .addPiece(3, 2, Regular(false)) // Destination blocked
        .build()

      GameLogic.getJumps(board, 5, 0, isRed = true, regular = true) shouldBe Nil
    }
  }

  "hasJumpsAvailable" should {
    "check all pieces on the board for a specific player" in {
      val board = Board().empty()
        .addPiece(7, 7, Regular(true))
        .addPiece(6, 6, Regular(false))
        .build()

      GameLogic.hasJumpsAvailable(board, isRedTurn = true) shouldBe true
      GameLogic.hasJumpsAvailable(board, isRedTurn = false) shouldBe false
    }
  }

  "Edge Cases" should {
    "correctly identify valid and invalid positions" in {
      val valid = for { r <- 0 to 7; c <- 0 to 7 } yield GameLogic.isValidPosition(r, c)
      valid.forall(_ == true) shouldBe true

      GameLogic.isValidPosition(0, 8) shouldBe false
      GameLogic.isValidPosition(8, 0) shouldBe false
      GameLogic.isValidPosition(-1, 4) shouldBe false
    }
  }
}