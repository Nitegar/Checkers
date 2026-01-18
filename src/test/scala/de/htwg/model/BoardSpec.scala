package de.htwg.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model.Board.Board

class BoardSpec extends AnyWordSpec with Matchers {

  "A Board (Array[Array[Piece]])" when {

    // --- Initialization and Dimensions ---

    "created using Board().empty().build()" should {
      val emptyBoard: Board = Board().empty().build()

      "have 8 rows and 8 columns" in {
        // Check array dimensions directly
        emptyBoard.length should be(8) // Check rows
        emptyBoard.head.length should be(8) // Check columns
      }

      "have every cell set to Empty" in {
        // Use flatten on the array directly
        emptyBoard.flatten.forall(_ == Empty) should be(true)
      }
    }

    "created using Board().withStandardSetup().build()" should {
      val standardBoard: Board = Board().withStandardSetup().build()

      "place 12 Red pieces" in {
        val redPieces = standardBoard.flatten.count {
          case Regular(true) | King(true) => true
          case _ => false
        }
        redPieces should be(12)
      }

      "place 12 Black pieces" in {
        val blackPieces = standardBoard.flatten.count {
          case Regular(false) | King(false) => true
          case _ => false
        }
        blackPieces should be(12)
      }

      "have empty rows 3 and 4 (indices 3 and 4)" in {
        // Rows 3 and 4 are empty in standard setup
        standardBoard(3).forall(_ == Empty) should be(true)
        standardBoard(4).forall(_ == Empty) should be(true)
      }
    }

    // --- Piece Access and Builder's addPiece ---

    "accessed via apply(row)(col)" should {
      val board = Board().withStandardSetup().build()

      "return the correct piece (Red at a6 / R5, C0)" in {
        board(5)(0) should be(Regular(isRed = true))
      }

      "return the correct piece (Black at b1 / R0, C1)" in {
        board(0)(1) should be(Regular(isRed = false))
      }

      "return Empty for middle cells (e5 / R4, C4)" in {
        board(4)(4) should be(Empty)
      }
    }

    "configured using addPiece" should {
      // Use the builder to configure and then build the final Board
      val initialBuilder = Board().empty()
      val newBoard = initialBuilder
        .addPiece(5, 13, Regular(isRed = false))
        .addPiece(4, 4, King(isRed = true)).build()

      // We test the builder's state by building a board after the changes
      "reflect the added piece in the built board" in {
        newBoard(4)(4) should be(King(isRed = true))
      }
    }
  }
}