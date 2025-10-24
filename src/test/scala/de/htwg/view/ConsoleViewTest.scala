package de.htwg.view

import org.scalatest.funsuite.AnyFunSuite
import de.htwg.model._

import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ConsoleViewTest extends AnyFunSuite {

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

  test("showTurnAnnouncement prints correct text for red and black turns") {
    val redOutput = captureOutput(ConsoleView.showTurnAnnouncement(isRedTurn = true))
    val blackOutput = captureOutput(ConsoleView.showTurnAnnouncement(isRedTurn = false))
    assert(redOutput.contains("RED'S TURN") || redOutput.contains("○ RED"))
    assert(blackOutput.contains("BLACK") || blackOutput.contains("● BLACK"))
  }

  test("printBoard prints all 8 rows and 8 columns") {
    val board = Board.create()
    val output = captureOutput(ConsoleView.printBoard(board, isRedTurn = true))
    val lines = output.split("\n").filter(_.contains("|"))
    assert(lines.count(_.contains("|")) >= 8)
  }
}
