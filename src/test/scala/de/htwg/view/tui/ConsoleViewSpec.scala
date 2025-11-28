package de.htwg.view.tui

import de.htwg.model.*
import de.htwg.view.tui.ConsoleView
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class ConsoleViewSpec extends AnyWordSpec with Matchers {

  "ConsoleView" when {

    "showing turn announcements" should {

      "show red content for red's turn" in {
        val output = ConsoleView.turnAnnouncementString(isRedTurn = true)
        output should (include("RED") or include("○"))
      }

      "show black content for black's turn" in {
        val output = ConsoleView.turnAnnouncementString(isRedTurn = false)
        output should (include("BLACK") or include("●"))
      }
    }

    "displaying kill effects" should {

      "show single kill message for 1 kill" in {
        val output = ConsoleView.killEffectString(1)
        output should include("K I L L")
      }

      "show double kill message for 2 kills" in {
        val output = ConsoleView.killEffectString(2)
        output should include("D O U B L E  K I L L")
      }
      "show double kill message for 3 kills" in {
        val output = ConsoleView.killEffectString(3)
        output should include("T R I P L E  K I L L")
      }
      "show double kill message for more than 3 kills" in {
        val output = ConsoleView.killEffectString(4)
        output should include("U L T R A  K I L L")
      }
    }

    "printing the board" should {

      "display board with correct structure and all row/column numbers/letters" in {
        val board = Board.create()
        val output = ConsoleView.boardString(board, isRedTurn = true)

        for (i <- 0 until 8) {
          val number = i + 1;
          val letter = ('a' + i).toChar.toString
          output should include(s"$number ")
          output should include(s" $number")
          output should include(s" $letter ")
        }
        val boardLines = output.split("\n").filter(_.contains("|"))
        boardLines.length should be >= 8
      }

      "show pieces with correct symbols and flip board for black's turn" in {
        val board = Board.create()
        val redOutput = ConsoleView.boardString(board, isRedTurn = true)
        val blackOutput = ConsoleView.boardString(board, isRedTurn = false)

        redOutput should (include("○") or include("●"))
        redOutput should not equal blackOutput
      }
    }

    "showing the winner" should {

      "clear screen and display red as winner" in {
        val output = ConsoleView.winnerString(isRed = true)
        output.toUpperCase should include("RED")
      }

      "clear screen and display black as winner" in {
        val output = ConsoleView.winnerString(isRed = false)
        output.toUpperCase should include("BLACK")
      }
    }
  }
}