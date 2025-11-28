package de.htwg.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model.*
import de.htwg.model.Board.Board
import de.htwg.view.tui.{AsciiEffect, ConsoleView}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

class GameControllerSpec extends AnyWordSpec with Matchers {

  /** Helper to create an empty 8x8 checkers board. */
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

  /** * Helper to capture printed output and simulate user input.
   * It redirects System.out and System.in during the execution of the block.
   */
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

    // --- Start Game and Initial Blocking Input Coverage ---

    "starting a new game" should {

      "print welcome message and initial board after 'Press Enter' is bypassed" in {
        // Input: \n (to press enter), q\n (to quit after the board is displayed)
        val output = captureOutput("\nq\n") {
          // This covers initializeGame(), the first readLine(), and the first call to gameLoop.
          GameController.startGame()
        }
        output should include("WELCOME TO CHECKERS")
        output should include("RED (○)") // Ensures turn starts
      }
    }

    // --- Game Over Condition Coverage ---

    "running the game loop" should {

      "end immediately if black has no pieces remaining (Red wins)" in {
        val board = createEmptyBoard()
        board(7)(0) = Regular(isRed = true) // Only one Red piece remains

        val output = captureOutput() {
          // Game loop checks count immediately
          GameController.add(ConsoleView)
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include(AsciiEffect.RedWins.art)
      }

      "end immediately if red has no pieces remaining (Black wins)" in {
        val board = createEmptyBoard()
        board(0)(7) = Regular(isRed = false) // Only one Black piece remains

        val output = captureOutput() {
          // Game loop checks count immediately
          GameController.add(ConsoleView)
          GameController.gameLoop(board, isRedTurn = false)
        }
        output should include(AsciiEffect.BlackWins.art)
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
    }

    // --- Input Parsing and Validation Coverage ---

    "validating player input" should {

      "reject invalid move input format (parseInput returns None)" in {
        val board = Board.create()
        // 'invalid input' returns None
        val output = captureOutput("invalid input\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input. Use format: colRow colRow")
      }

      "reject input with wrong number of arguments (not enough)" in {
        val board = Board.create()
        // 'b3' returns None (only one position provided)
        val output = captureOutput("b3\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input. Use format: colRow colRow")
      }

      "reject input with wrong number of arguments (too many)" in {
        val board = Board.create()
        // 'b3' returns None (only one position provided)
        val output = captureOutput("b3 c4 d5\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input. Use format: colRow colRow")
      }

      "reject out-of-bounds column (parseInput returns None)" in {
        val board = Board.create()
        // 'z3 a4' has invalid column 'z'
        val output = captureOutput("z3 a4\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input. Use format: colRow colRow")
      }

      "reject out-of-bounds row (parseInput returns None)" in {
        val board = Board.create()
        // 'a0 b1' has invalid row '0'
        val output = captureOutput("a0 b1\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input. Use format: colRow colRow")
      }

      "reject when trying to move opponent's regular piece (Red turn)" in {
        val board = Board.create()
        // Red (true) tries to move Black piece at b1 (R0, C1)
        val output = captureOutput("b1 a2\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("That piece does not belong to you!")
      }

      "reject when trying to move opponent's king piece (Black turn, coordinate flipped)" in {
        val board = createEmptyBoard()
        board(0)(0) = King(isRed = true) // Red king at a8 (flipped to a1 for black)
        board(3)(3) = Regular(isRed = false) // Black piece for turn validity

        // Black's turn (isRedTurn = false), tries to move Red King: Input "a1 g7" (a1 is R0 C0 from black's perspective, which is R7 C0 unflipped)
        // Controller flips input a1 to a8 (R0, C0) -> this is the Red King
        val output = captureOutput("h8 g7\nq\n") {
          GameController.add(ConsoleView)
          GameController.gameLoop(board, isRedTurn = false)
        }
        output should include("That piece does not belong to you!")
      }

      "reject when trying to move opponent's king piece (Red turn)" in {
        val board = createEmptyBoard()
        board(0)(0) = King(isRed = false) // Red king at a8 (flipped to a1 for black)
        board(3)(3) = Regular(isRed = true) // Black piece for turn validity

        val output = captureOutput("a1 b2\nq\n") {
          GameController.add(ConsoleView)
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("That piece does not belong to you!")
      }

      "reject when selecting empty position" in {
        val board = createEmptyBoard()
        board(1)(1) = Regular(isRed = false) // Red piece at f6
        board(5)(5) = Regular(isRed = true) // Red piece at f6

        val output = captureOutput("c5 d4\nq\n") {
          GameController.add(ConsoleView)
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("No piece at that position")
      }

      "reject invalid move when valid moves exist (Command returns failure, requiredJumpMissed=false)" in {
        val board = Board.create()
        // a6 to d4 is an illegal non-jump move
        val output = captureOutput("a6 d4\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid move.")
      }

      "reject non-jump move when jump is available (Command returns failure, requiredJumpMissed=true)" in {
        val board = createEmptyBoard()
        // Setup jump: Red piece at b6 can jump Black at c5 to d4
        board(5)(1) = Regular(isRed = true) // b6 (R5, C1)
        board(4)(2) = Regular(isRed = false) // c5 (R4, C2)

        // Try to make a legal but non-jump move with b6 to a5 (R5, C1 -> R4, C0)
        val output = captureOutput("b6 a5\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("You must make a jump when available!")
      }
    }

    // --- Successful Move Coverage ---

    "making valid moves" should {

      "successfully execute a simple move for red and transition turn" in {
        val board = Board.create()
        // Move red piece from a6 to b5
        val output = captureOutput("a6 b5\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        // Should switch to black's turn
        output should include(AsciiEffect.BlackTurn.art)
      }

      "successfully execute a jump move, display KillEffect, and wins" in {
        val board = createEmptyBoard()
        board(5)(1) = Regular(isRed = true) // b6
        board(4)(2) = Regular(isRed = false) // c5

        // Jump from b6 to d4 (covers command.wasJump = true branch)
        val output = captureOutput("b6 d4\nq\n") {
          GameController.add(ConsoleView)
          GameController.gameLoop(board, isRedTurn = true)
        }
        // Check for KillEffect announcement
        output should include(AsciiEffect.SingleKill.art)
        // Should switch to black's turn after successful jump
        output should include(AsciiEffect.RedWins.art)
      }

      "handle coordinate flipping for black's turn (successful move)" in {
        val board = Board.create()
        val output = captureOutput("c6 d5\nq\n") {
          GameController.add(ConsoleView)
          GameController.gameLoop(board, isRedTurn = false) // Start with Black's turn
        }
        output should include(AsciiEffect.RedTurn.art)
      }
    }
  }
}