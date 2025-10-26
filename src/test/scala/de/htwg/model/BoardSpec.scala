package de.htwg.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class BoardSpec extends AnyWordSpec with Matchers {

  "Board" when {

    "creating a new board" should {

      "generate a standard 8x8 board" in {
        val board = Board.create()
        board should have length 8
        board.forall(_.length == 8) shouldBe true
      }

      "place black pieces on top 3 rows on dark squares" in {
        val board = Board.create()

        for (r <- 0 until 3; c <- 0 until 8 if (r + c) % 2 == 1) {
          board(r)(c) shouldBe Regular(false)
        }
      }

      "leave rows 3 and 4 empty" in {
        val board = Board.create()

        for (r <- 3 to 4; c <- 0 until 8) {
          board(r)(c) shouldBe Empty
        }
      }

      "place red pieces on bottom 3 rows on dark squares" in {
        val board = Board.create()

        for (r <- 5 until 8; c <- 0 until 8 if (r + c) % 2 == 1) {
          board(r)(c) shouldBe Regular(true)
        }
      }
    }

    "validating positions" should {

      "correctly identify valid coordinates" in {
        Board.isValidPosition(0, 0) shouldBe true
        Board.isValidPosition(7, 7) shouldBe true
        Board.isValidPosition(3, 5) shouldBe true
      }

      "correctly identify invalid coordinates" in {
        Board.isValidPosition(-1, 0) shouldBe false
        Board.isValidPosition(0, 8) shouldBe false
        Board.isValidPosition(8, 8) shouldBe false
        Board.isValidPosition(-1, -1) shouldBe false
      }
    }
  }
}