package de.htwg.view.tui

import de.htwg.model.*
import de.htwg.view.tui.{AsciiEffect, ConsoleView}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayOutputStream, PrintStream} // Import the AsciiEffect enum

class ConsoleViewSpec extends AnyWordSpec with Matchers {

  // --- Helper to capture printed output (for testing update method side effects) ---

  /** Helper to capture printed output by redirecting System.out. */
  private def captureOutput(block: => Unit): String = {
    val outStream = new ByteArrayOutputStream()
    // Temporarily redirect System.out to our stream
    Console.withOut(new PrintStream(outStream)) {
      block
    }
    outStream.toString.trim
  }

  // Create a minimal board state for scoring tests
  private val testBoard = {
    val b = Board.create()
    // Ensure scores are known for RequestInput test
    b(0)(1) = Empty
    b
  }


  "ConsoleView" when {

    // --- 1. PURE RENDERING TESTS (Testing methods that return strings) ---

    "showing turn announcements" should {
      "show RedTurn ASCII art for red's turn" in {
        val output = ConsoleView.turnAnnouncementString(isRedTurn = true)
        output should include(AsciiEffect.RedTurn.art)
      }

      "show BlackTurn ASCII art for black's turn" in {
        val output = ConsoleView.turnAnnouncementString(isRedTurn = false)
        output should include(AsciiEffect.BlackTurn.art)
      }
    }

    "displaying kill effects" should {
      "show SingleKill ASCII art for 1 kill" in {
        val output = ConsoleView.killEffectString(1)
        output should include(AsciiEffect.SingleKill.art)
      }
      "show DoubleKill ASCII art for 2 kills" in {
        val output = ConsoleView.killEffectString(2)
        output should include(AsciiEffect.DoubleKill.art)
      }
      "show TripleKill ASCII art for 3 kills" in {
        val output = ConsoleView.killEffectString(3)
        output should include(AsciiEffect.TripleKill.art)
      }
      "show UltraKill ASCII art for more than 3 kills" in {
        val output = ConsoleView.killEffectString(4)
        output should include(AsciiEffect.UltraKill.art)
      }
    }

    "printing the board" should {
      "display board with correct structure and all row/column numbers/letters" in {
        val board = Board.create()
        val output = ConsoleView.boardString(board, isRedTurn = true)

        for (i <- 0 until 8) {
          val number = i + 1;
          val letter = ('a' + i).toChar.toString
          output should include(s"$number |")
          output should include(s" $letter ")
        }
      }

      "show pieces with correct symbols and flip board for black's turn" in {
        val board = Board.create()
        val redOutput = ConsoleView.boardString(board, isRedTurn = true)
        val blackOutput = ConsoleView.boardString(board, isRedTurn = false)

        redOutput should (include("○") and include("●"))
        redOutput should not equal blackOutput
      }
    }

    "showing the winner" should {
      "display RedWins ASCII art when red wins" in {
        val output = ConsoleView.winnerString(isRed = true)
        output should include(AsciiEffect.RedWins.art)
      }
      "display BlackWins ASCII art when black wins" in {
        val output = ConsoleView.winnerString(isRed = false)
        output should include(AsciiEffect.BlackWins.art)
      }
    }
    
    "receiving GameEvents via update" should {

      "handle StartGame event by printing the welcome message" in {
        val output = captureOutput { ConsoleView.update(StartGame()) }
        output should include("WELCOME TO CHECKERS")
        output should include("Rules:")
      }

      "handle QuitGame event by printing goodbye message" in {
        val output = captureOutput { ConsoleView.update(QuitGame) }
        output should include("Thanks for playing!")
      }

      "handle TurnAnnounced event by printing the RedTurn ASCII art" in {
        // Since the controller only sends TurnAnnounced(true) initially, we test that path
        val output = captureOutput { ConsoleView.update(TurnAnnounced(true)) }
        output should include(AsciiEffect.RedTurn.art)
      }

      "handle GameEnded event by displaying the BlackWins ASCII art" in {
        val output = captureOutput { ConsoleView.update(GameEnded(false)) }
        output should include(AsciiEffect.BlackWins.art)
      }

      "handle KillEffect event by displaying the SingleKill ASCII art" in {
        val output = captureOutput { ConsoleView.update(KillEffect(1)) }
        output should include(AsciiEffect.SingleKill.art)
      }

      "handle RequestInput during initial setup (scores 0,0)" in {
        ConsoleView.update(BoardUpdated(Board.create(), true))

        // Test the main game prompt (since BoardUpdated is usually called first in the loop)
        val output = captureOutput { ConsoleView.update(RequestInput(isRedTurn = true)) }
        output should include("RED (○)'s turn")
        output should include("Enter move")
      }

      "map InvalidInput('Invalid format.') to a user-friendly error" in {
        val output = captureOutput { ConsoleView.update(InvalidInput()) }
        output should include("❌ Invalid input. Use format: colRow colRow")
      }

      "map MoveFailed('Not your piece.') to a user-friendly error" in {
        val output = captureOutput { ConsoleView.update(MoveFailed("Not your piece.")) }
        output should include("❌ That piece does not belong to you!")
      }

      "map MoveFailed('No piece at position.') to a user-friendly error" in {
        val output = captureOutput {
          ConsoleView.update(MoveFailed("No piece at position."))
        }
        output should include("❌ No piece at that position.")
      }

      "map MoveFailed('Must make jump.') to a user-friendly error" in {
        val output = captureOutput { ConsoleView.update(MoveFailed("Must make jump.")) }
        output should include("❌ You must make a jump when available!")
      }

      "map MoveFailed('Invalid move.') to a user-friendly error" in {
        val output = captureOutput { ConsoleView.update(MoveFailed("Invalid move.")) }
        output should include("❌ Invalid move.")
      }

      "map unhandled errors gracefully" in {
        val output = captureOutput { ConsoleView.update(MoveFailed("Unknown error.")) }
        output should include("❌ Move failed: Unknown error.")
      }
    }
  }
}