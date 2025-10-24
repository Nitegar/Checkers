package de.htwg.controller

import org.scalatest.funsuite.AnyFunSuite
import de.htwg.model.*
import de.htwg.model.Board.Board

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

class GameControllerTest extends AnyFunSuite {
  
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

  test("startGame prints welcome message and board") {
    val output = captureOutput("\nq\n") {
      GameController.startGame()
    }
    assert(output.contains("WELCOME TO CHECKERS"))
    assert(output.contains("Press Enter to start"))
    assert(output.contains("RED (○)"))
  }

  test("gameLoop ends immediately if red has 0 pieces") {
    val board = createEmptyBoard()
    val output = captureOutput() {
      GameController.gameLoop(board, isRedTurn = true)
    }
    assert(output.contains("BLACK")) // Black wins
  }

  test("gameLoop ends immediately if black has 0 pieces") {
    val board = Board.create()
    // Remove black pieces
    for (r <- 0 until 3; c <- 0 until 8 if (r + c) % 2 == 1) board(r)(c) = Empty

    val output = captureOutput() {
      GameController.gameLoop(board, isRedTurn = true)
    }
    assert(output.contains("RED")) // Red wins
  }

  test("gameLoop quits on 'quit' or 'q'") {
    val board = Board.create()
    val outputQuit = captureOutput("quit\n") {
      GameController.gameLoop(board, isRedTurn = true)
    }
    val outputQ = captureOutput("q\n") {
      GameController.gameLoop(board, isRedTurn = true)
    }
    assert(outputQuit.contains("Thanks for playing!"))
    assert(outputQ.contains("Thanks for playing!"))
  }

  test("gameLoop rejects invalid move input") {
    val board = Board.create()
    val output = captureOutput("invalid input\nq\n") {
      GameController.gameLoop(board, isRedTurn = true)
    }
    assert(output.contains("Invalid input"))
  }

  test("gameLoop rejects out-of-bounds positions") {
    val board = Board.create()
    val output = captureOutput("0 0 10 10\nq\n") {
      GameController.gameLoop(board, isRedTurn = true)
    }
    assert(output.contains("Invalid position"))
  }
}
