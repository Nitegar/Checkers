package de.htwg.controller

import de.htwg.model.Board
import de.htwg.view.ConsoleView
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

class GameControllerSpec extends AnyWordSpec with Matchers {

  "A GameController" should {
    var board = Board.create()
    val view = new ConsoleView()
    var controller = new GameController(board, view)
    controller.addObserver(view)

    "correctly parse a valid move" in {
      val command = controller.parseInput("a2 a3", isRedTurn = true)
      command shouldBe a[MoveCommand]
    }

    "return an InvalidCommand for invalid input" in {
      val command = controller.parseInput("invalid", isRedTurn = true)
      command shouldBe an[InvalidCommand]
    }

    "return a QuitCommand when 'q' is entered" in {
      val command = controller.parseInput("q", isRedTurn = true)
      command shouldBe a[QuitCommand]
    }

    "return a QuitCommand when 'quit' is entered" in {
      val command = controller.parseInput("quit", isRedTurn = true)
      command shouldBe a[QuitCommand]
    }

    "handle state transitions correctly" in {
      // Initial state should be PlayerTurnState(isRedTurn = true)
      controller.currentState shouldBe PlayerTurnState(isRedTurn = true)

      // Simulate a move
      controller.handleInput("a6 b5", controller)
      controller.currentState shouldBe PlayerTurnState(isRedTurn = false)

      // Simulate another move
      controller.handleInput("b3 a4", controller)
      controller.currentState shouldBe PlayerTurnState(isRedTurn = true)
    }

    "notify observers when the board changes" in {
      val out = new ByteArrayOutputStream()
      val printStream = new PrintStream(out)
      val originalOut = System.out
      System.setOut(printStream)

      try {
        controller.notifyObservers(board, isRedTurn = true)
        val output = out.toString
        output should include("a")
        output should include("h")
      } finally {
        System.setOut(originalOut)
      }
    }
  }
}
