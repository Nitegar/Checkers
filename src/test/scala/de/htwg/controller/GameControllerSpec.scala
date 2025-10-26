package de.htwg.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model.*
import de.htwg.model.Board.Board

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
    }

    "validating player input" should {

      "reject invalid move input format" in {
        val board = Board.create()
        val output = captureOutput("invalid input\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid input")
      }

      "reject out-of-bounds positions" in {
        val board = Board.create()
        val output = captureOutput("0 0 10 10\nq\n") {
          GameController.gameLoop(board, isRedTurn = true)
        }
        output should include("Invalid position")
      }
    }
  }
}