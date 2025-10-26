package de.htwg.view

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import de.htwg.model._
import de.htwg.model.Board._

import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ConsoleViewTest extends AnyFunSuite with Matchers {

  private def captureOutput(block: => Unit): String = {
    val stream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(stream)) {
      block
    }
    stream.toString
  }

  test("clearScreen prints ANSI clear codes") {
    val output = captureOutput(ConsoleView.clearScreen())
    assert(output.contains("\u001b[2J"))
    assert(output.contains("\u001b[H"))
  }

  test("clearScreen contains exactly the expected ANSI sequence") {
    val output = captureOutput(ConsoleView.clearScreen())
    output should equal("\u001b[2J\u001b[H")
  }

  test("showTurnAnnouncement for red turn clears screen and shows red content") {
    val output = captureOutput(ConsoleView.showTurnAnnouncement(isRedTurn = true))
    assert(output.contains("\u001b[2J"))
    assert(output.contains("RED") || output.contains("○"))
  }

  test("showTurnAnnouncement for black turn clears screen and shows black content") {
    val output = captureOutput(ConsoleView.showTurnAnnouncement(isRedTurn = false))
    assert(output.contains("\u001b[2J"))
    assert(output.contains("BLACK") || output.contains("●"))
  }

  test("showKillEffect displays correct message based on kill count") {
    val singleKill = captureOutput(ConsoleView.showKillEffect(1))
    val doubleKill = captureOutput(ConsoleView.showKillEffect(2))

    assert(singleKill.contains("K I L L"))
    assert(doubleKill.contains("D O U B L E  K I L L"))
  }


  test("printBoard displays board with correct structure and dimensions") {
    val board = Board.create()
    val output = captureOutput(ConsoleView.printBoard(board, isRedTurn = true))

    for (i <- 0 until 8) {
      assert(output.contains(s" $i "))
    }
    val boardLines = output.split("\n").filter(_.contains("|"))
    assert(boardLines.length >= 8)
  }

  test("printBoard shows pieces with correct symbols and flips board for black turn") {
    val board = Board.create()
    val redOutput = captureOutput(ConsoleView.printBoard(board, isRedTurn = true))
    val blackOutput = captureOutput(ConsoleView.printBoard(board, isRedTurn = false))

    assert(redOutput.contains("○") || redOutput.contains("●"))
    assert(redOutput != blackOutput)
  }

  test("showWinner displays correct winner for red") {
    val output = captureOutput(ConsoleView.showWinner(isRed = true))
    assert(output.contains("\u001b[2J"))
    assert(output.toUpperCase.contains("RED"))
  }

  test("showWinner displays correct winner for black") {
    val output = captureOutput(ConsoleView.showWinner(isRed = false))
    assert(output.contains("\u001b[2J"))
    assert(output.toUpperCase.contains("BLACK"))
  }
}