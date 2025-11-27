package de.htwg.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model._
import java.io.ByteArrayOutputStream

class ConsoleViewSpec extends AnyWordSpec with Matchers {

  def withCapturedOutput(test: (ByteArrayOutputStream, ConsoleView) => Unit): Unit = {
    val out = new ByteArrayOutputStream()
    val view = new ConsoleView()
    Console.withOut(out) {
      test(out, view)
    }
  }

  "ConsoleView" when {

    "showing startup" should {
      "display a welcome message and rules" in withCapturedOutput { (out, view) =>
        view.showStartup()
        val output = out.toString
        output should include("WELCOME TO CHECKERS")
        output should include("Rules:")
      }
    }

    "showing turn announcements" should {
      "show red's turn" in withCapturedOutput { (out, view) =>
        view.showTurnAnnouncement(isRedTurn = true)
        val output = out.toString
        output should include("RED")
      }

      "show black's turn" in withCapturedOutput { (out, view) =>
        view.showTurnAnnouncement(isRedTurn = false)
        val output = out.toString
        output should include("BLACK")
      }
    }

    "displaying kill effects" should {
      "show single kill message" in withCapturedOutput { (out, view) =>
        view.showKillEffect(1)
        out.toString should include("SINGLE KILL")
      }

      "show double kill message" in withCapturedOutput { (out, view) =>
        view.showKillEffect(2)
        out.toString should include("DOUBLE KILL")
      }

      "show triple kill message" in withCapturedOutput { (out, view) =>
        view.showKillEffect(3)
        out.toString should include("TRIPLE KILL")
      }

      "show ultra kill message" in withCapturedOutput { (out, view) =>
        view.showKillEffect(4)
        out.toString should include("ULTRA KILL")
      }
    }

    "showing the winner" should {
      "display red as winner" in withCapturedOutput { (out, view) =>
        view.showWinner(isRed = true)
        out.toString should include("RED WINS")
      }

      "display black as winner" in withCapturedOutput { (out, view) =>
        view.showWinner(isRed = false)
        out.toString should include("BLACK WINS")
      }
    }

    "displaying the board" should {
      "show the board with pieces" in withCapturedOutput { (out, view) =>
        val board = Board.create()
        view.showBoard(board, isRedTurn = true)
        val output = out.toString
        output should include("a")
        output should include("h")
        output should include("1")
        output should include("8")
        output should include("○")
        output should include("●")
      }
    }

    "asking for move" should {
      "prompt for red's move" in withCapturedOutput { (out, view) =>
        view.askForMovePrompt(isRedTurn = true, 12, 12)
        val output = out.toString
        output should include("RED (○)'s turn")
        output should include("Enter move")
      }

      "prompt for black's move" in withCapturedOutput { (out, view) =>
        view.askForMovePrompt(isRedTurn = false, 12, 12)
        val output = out.toString
        output should include("BLACK (●)'s turn")
        output should include("Enter move")
      }
    }

    "showing messages" should {
      "display invalid input message" in withCapturedOutput { (out, view) =>
        view.showInvalidInput()
        out.toString should include("Invalid input")
      }

      "display invalid move message" in withCapturedOutput { (out, view) =>
        view.showInvalidMove()
        out.toString should include("Invalid move")
      }

      "display not your piece message" in withCapturedOutput { (out, view) =>
        view.showNotYourPiece()
        out.toString should include("That piece does not belong to you")
      }

      "display must jump message" in withCapturedOutput { (out, view) =>
        view.showMustJump()
        out.toString should include("You must make a jump")
      }

      "display thanks message" in withCapturedOutput { (out, view) =>
        view.showThanks()
        out.toString should include("Thanks for playing")
      }
    }
  }
}