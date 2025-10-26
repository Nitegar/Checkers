package de.htwg.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model._
import de.htwg.model.Board._

import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ConsoleViewSpec extends AnyWordSpec with Matchers {

  private def captureOutput(block: => Unit): String = {
    val stream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(stream)) {
      block
    }
    stream.toString
  }

  "ConsoleView" when {

    "clearing the screen" should {

      "print ANSI clear codes" in {
        val output = captureOutput(ConsoleView.clearScreen())
        output should include("\u001b[2J")
        output should include("\u001b[H")
      }

      "contain exactly the expected ANSI sequence" in {
        val output = captureOutput(ConsoleView.clearScreen())
        output shouldBe "\u001b[2J\u001b[H"
      }
    }

    "showing turn announcements" should {

      "clear screen and show red content for red's turn" in {
        val output = captureOutput(ConsoleView.showTurnAnnouncement(isRedTurn = true))
        output should include("\u001b[2J")
        output should (include("RED") or include("○"))
      }

      "clear screen and show black content for black's turn" in {
        val output = captureOutput(ConsoleView.showTurnAnnouncement(isRedTurn = false))
        output should include("\u001b[2J")
        output should (include("BLACK") or include("●"))
      }
    }

    "displaying kill effects" should {

      "show single kill message for 1 kill" in {
        val output = captureOutput(ConsoleView.showKillEffect(1))
        output should include("K I L L")
      }

      "show double kill message for 2 kills" in {
        val output = captureOutput(ConsoleView.showKillEffect(2))
        output should include("D O U B L E  K I L L")
      }
    }

    "printing the board" should {

      "display board with correct structure and all row/column numbers" in {
        val board = Board.create()
        val output = captureOutput(ConsoleView.printBoard(board, isRedTurn = true))

        for (i <- 0 until 8) {
          output should include(s" $i ")
        }
        val boardLines = output.split("\n").filter(_.contains("|"))
        boardLines.length should be >= 8
      }

      "show pieces with correct symbols and flip board for black's turn" in {
        val board = Board.create()
        val redOutput = captureOutput(ConsoleView.printBoard(board, isRedTurn = true))
        val blackOutput = captureOutput(ConsoleView.printBoard(board, isRedTurn = false))

        redOutput should (include("○") or include("●"))
        redOutput should not equal blackOutput
      }
    }

    "showing the winner" should {

      "clear screen and display red as winner" in {
        val output = captureOutput(ConsoleView.showWinner(isRed = true))
        output should include("\u001b[2J")
        output.toUpperCase should include("RED")
      }

      "clear screen and display black as winner" in {
        val output = captureOutput(ConsoleView.showWinner(isRed = false))
        output should include("\u001b[2J")
        output.toUpperCase should include("BLACK")
      }
    }
  }
}