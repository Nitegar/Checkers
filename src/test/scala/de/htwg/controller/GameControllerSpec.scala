package de.htwg.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model.*
import de.htwg.model.Board.Board
import de.htwg.view.AsciiEffect

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

class GameControllerSpec extends AnyWordSpec with Matchers {

  private def createEmptyBoard(): Board = {
    val board = Array.ofDim[Piece](8, 8)
    for {
      row <- 0 until 8
      col <- 0 until 8
    } {
      board(row)(col) = Empty
    }
    board
  }

  /** Helper to capture printed output */
  private def captureOutput(input: String = "")(block: => Unit): String = {
    val outStream = new ByteArrayOutputStream()
    val inStream = new ByteArrayInputStream(input.getBytes)
    Console.withOut(new PrintStream(outStream)) {
      Console.withIn(inStream) {
        block
      }
    }
    outStream.toString
  }

  "GameController" when {

    "starting a new game" should {

      "print welcome message and initial board" in {
        val output = captureOutput("\nq\n") {
          GameController.startGame()
        }
        output should include("WELCOME TO CHECKERS")
        output should include("Press Enter to start")
        output should include("RED (○)")
      }
    }

    "running the game loop" should {

      "end immediately if red has no pieces remaining" in {
        val board = createEmptyBoard()
        val output = captureOutput() {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("BLACK")
      }

      "end immediately if black has no pieces remaining" in {
        val board = Board.create()
        // Remove black pieces
        for (r <- 0 until 3; c <- 0 until 8 if (r + c) % 2 == 1) {
          board(r)(c) = Empty
        }

        val output = captureOutput() {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("RED")
      }

      "quit when player enters 'quit'" in {
        val board = Board.create()
        val output = captureOutput("quit\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Thanks for playing!")
      }

      "quit when player enters 'q'" in {
        val board = Board.create()
        val output = captureOutput("q\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Thanks for playing!")
      }

      "not show turn announcement when showTurn is false" in {
        val board = createEmptyBoard()
        val output = captureOutput() {
          GameController.gameLoop(board, isRedTurn = true, showTurn = false)
        }
        // Should still display board but game ends immediately (no pieces)
        output should not be empty
      }
    }

    "validating player input" should {

      "reject invalid move input format" in {
        val board = Board.create()
        val output = captureOutput("invalid input\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input")
      }

      "reject input with wrong number of arguments" in {
        val board = Board.create()
        val output = captureOutput("b3\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input")
      }

      "reject out-of-bounds column" in {
        val board = Board.create()
        val output = captureOutput("z3 a4\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input")
      }

      "reject out-of-bounds row" in {
        val board = Board.create()
        val output = captureOutput("a0 b1\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input")
      }

      "reject when trying to move opponent's regular piece" in {
        val board = Board.create()
        // Try to move black piece (at row 0, col 1 = b1) when it's red's turn
        val output = captureOutput("b1 a2\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("does not belong to you")
      }

      "reject when trying to move opponent's king piece from red turns" in {
        val board = createEmptyBoard()
        board(3)(3) = King(isRed = false) // Black king at d4
        board(5)(5) = Regular(isRed = true) // Red piece at f6

        val output = captureOutput("d4 e5\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("does not belong to you")
      }

      "reject when trying to move opponent's king piece from black turns" in {
        val board = createEmptyBoard()
        board(0)(0) = King(isRed = true) // Red king at h8
        board(3)(3) = Regular(isRed = false) // Black piece at f6

        val output = captureOutput("h8 g7\nq\n") {
          GameController.gameLoop(board, isRedTurn = false)
        }
        output should include("does not belong to you")
      }

      "reject when selecting empty position" in {
        val board = createEmptyBoard()
        board(5)(5) = Regular(isRed = false)
        board(5)(6) = Regular(isRed = true)

        val output = captureOutput("b2 c3\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("No piece at that position")
      }

      "reject invalid move when valid moves exist" in {
        val board = Board.create()
        val output = captureOutput("a6 d4\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid move")
      }

      "reject non-jump move when jump is available" in {
        val board = createEmptyBoard()
        // Setup: Red piece at b6 can jump
        board(5)(1) = Regular(isRed = true) // b6
        board(4)(2) = Regular(isRed = false) // c5
        // Position d4 (3,3) is empty, so jump is available

        // Try to make a non-jump move instead with piece at d6
        board(5)(3) = Regular(isRed = true) // d6

        val output = captureOutput("d6 e5\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("must make a jump")
      }
    }

    "making valid moves" should {

      "successfully execute a simple move for red" in {
        val board = Board.create()
        // Move red piece from b6 to c5 (row 5 col 1 to row 4 col 2)
        val output = captureOutput("a6 b5\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include(AsciiEffect.BlackTurn.art)
      }

      "successfully execute a jump move" in {
        val board = createEmptyBoard()
        board(5)(1) = Regular(isRed = true) // b6
        board(4)(2) = Regular(isRed = false) // c5

        // Jump from b6 to d4
        val output = captureOutput("b6 d4\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        // Should switch to black's turn after successful jump
        output should include(AsciiEffect.BlackTurn.art)
      }

      "handle coordinate flipping for black's turn" in {
        val board = Board.create()
        // First move by red: b6 to c5, then black moves: b3 to a4
        val output = captureOutput("b6 c5\nb3 a4\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        // Black should be able to make a move (coordinates get flipped)
        output should include("RED (○)'s turn")
      }

      "reject opponent piece for black player (coordinate flipped)" in {
        val board = createEmptyBoard()
        board(2)(1) = Regular(isRed = false) // b3 (black)
        board(5)(1) = Regular(isRed = true) // b6 (red)

        // Black's turn, trying to move red piece
        // Input "b3" from black's perspective becomes b6 after flip (red piece)
        val output = captureOutput("b3 a4\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("does not belong to you")
      }

      "reject empty position for black player (coordinate flipped)" in {
        val board = createEmptyBoard()
        board(5)(5) = Regular(isRed = true)
        board(2)(1) = Regular(isRed = false) // b3 (black)

        // Black's turn, trying to select empty position
        // Input "b7" for black will be flipped to b2 which is empty
        val output = captureOutput("b7 c6\nq\n") {
          GameController.gameLoop(board, isRedTurn = false)
        }
        output should include("No piece at that position")
      }
    }

    "parsing input" should {

      "accept lowercase column letters" in {
        val board = Board.create()
        val output = captureOutput("b6 c5\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include(AsciiEffect.RedTurn.art)
      }

      "handle mixed spacing in input" in {
        val board = Board.create()
        val output = captureOutput("b6  c5\nq\n") {
          GameController.gameLoop(board, isRedTurn = false)
        }
        output should include(AsciiEffect.BlackTurn.art)
      }

      "reject invalid column characters" in {
        val board = Board.create()
        val output = captureOutput("16 25\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input")
      }

      "reject incomplete position format" in {
        val board = Board.create()
        val output = captureOutput("b c5\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input")
      }
    }
  }
}