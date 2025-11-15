package de.htwg.view

import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.view.AsciiEffect._

object ConsoleView {

  def clearScreen(): Unit = print("\u001b[2J\u001b[H")

  def showTurnAnnouncement(isRedTurn: Boolean): Unit = {
    clearScreen()
    if (isRedTurn) RedTurn.show()
    else BlackTurn.show()
    Thread.sleep(500)
    clearScreen()
  }

  def showKillEffect(kills: Int): Unit = {
    clearScreen()
    kills match {
      case 1 => SingleKill.show()
      case 2 => DoubleKill.show()
      case 3 => TripleKill.show()
      case _ => UltraKill.show()
    }
    Thread.sleep(2000)
    clearScreen()
  }

  def printBoard(board: Board, isRedTurn: Boolean): Unit = {
    val reset = "\u001b[0m"
    val red = "\u001b[91m"
    val black = "\u001b[90m"

    val displayBoard = if (isRedTurn) board else board.reverse.map(_.reverse)
    println("\n  " + (0 until 8).map(i => s" $i ").mkString)
    println("  " + "+--" * 8 + "+")

    for (row <- 0 until 8) {
      print(s"$row |")
      for (col <- 0 until 8) {
        val piece = displayBoard(row)(col) match {
          case Empty => "  "
          case Regular(true) => s"${red}○${reset} " // 🔴 Red regular piece
          case Regular(false) => s"${black}●${reset} " // ⚫ Black regular piece
          case King(true) => s"${red}◎${reset} " // 🔴 Red king
          case King(false) => s"${black}◉${reset} " // ⚫ Black king
        }
        print(piece + "|")
      }
      println(s" $row")
      println("  " + "+--" * 8 + "+")
    }
    println("  " + (0 until 8).map(i => s" $i ").mkString) //NEW unten auch nummeriert

    println("\nPieces: " +
      s"${red}○${reset}/${red}◎${reset} = Red, " +
      s"${black}●${reset}/${black}◉${reset} = Black (Ring = King)"
    )
  }


  def showWinner(isRed: Boolean): Unit = {
    clearScreen()
    println(if (isRed) "○ RED WINS!" else "● BLACK WINS!")
  }
}
