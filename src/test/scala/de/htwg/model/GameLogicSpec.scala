package de.htwg.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameLogicSpec extends AnyWordSpec with Matchers {

  "GameLogic" when {

    "counting pieces" should {

      "count red and black correctly on initial board" in {
        val board = Board.create()
        val (red, black) = GameLogic.countPieces(board)
        red should be > 0
        black should be > 0
        (red + black) should be <= 24
      }
    }

    "checking for available jumps" should {

      "return false if no jumps exist on initial board" in {
        val board = Board.create()
        GameLogic.hasJumpsAvailable(board, isRedTurn = true) shouldBe false
        GameLogic.hasJumpsAvailable(board, isRedTurn = false) shouldBe false
      }
    }

    "getting valid moves" should {

      "return empty list for an empty square" in {
        val board = Board.create()
        GameLogic.getValidMoves(board, 3, 3) shouldBe empty
      }

      "return valid moves for a Regular piece" in {
        val board = Board.create()
        val moves = GameLogic.getValidMoves(board, 5, 0)
        moves should not be empty
        moves.forall { case (r, c, _) => Board.isValidPosition(r, c) } shouldBe true
      }

      "return valid moves for a King piece" in {
        val board = Board.create()
        val kingBoard = board.map(_.clone())
        kingBoard(3)(3) = King(true)
        val moves = GameLogic.getValidMoves(kingBoard, 3, 3)
        moves should not be empty
        moves.forall { case (r, c, _) => Board.isValidPosition(r, c) } shouldBe true
      }
    }

    "detecting jumps" should {

      "detect a possible jump" in {
        val board = Board.create().map(_.clone())
        board(2)(2) = Regular(true)
        board(1)(1) = Regular(false)
        board(0)(0) = Empty
        val jumps = GameLogic.getJumps(board, 2, 2, isRed = true, regular = true)
        jumps should contain((0, 0, true))
      }
    }

    "making moves" should {

      "move piece and remove jumped piece" in {
        val board = Board.create().map(_.clone())
        val firstTurnBoard = GameLogic.makeMove(board, 5, 0, 4, 1)
        val secondTurnBoard = GameLogic.makeMove(firstTurnBoard, 2, 3, 3, 2)
        val thirdTurnBoard = GameLogic.makeMove(secondTurnBoard, 4, 1, 2, 3)

        thirdTurnBoard(3)(2) shouldBe Empty
        thirdTurnBoard(2)(3) shouldBe Regular(true)
      }

      "promote Regular to King when reaching last row" in {
        val board = Board.create().map(_.clone())
        board(1)(2) = Regular(true)
        board(0)(3) = Empty
        val newBoard = GameLogic.makeMove(board, 1, 2, 0, 3)
        newBoard(0)(3) shouldBe a[King]
      }
    }
  }
}