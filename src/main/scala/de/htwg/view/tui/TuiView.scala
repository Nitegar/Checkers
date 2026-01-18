package de.htwg.view.tui

import de.htwg.controller.*
import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.util.Observer

class TuiView extends Observer[GameEvent] {

  // Store the last known board state to access piece counts when RequestInput arrives
  private var currentBoard: Option[Board] = None

  private def clearScreen(): Unit = print("\u001b[2J\u001b[H")

  // Helper to count pieces from the stored board
  private def getScores: (Int, Int) = currentBoard.map(GameLogic.countPieces).getOrElse(0, 0)

  override def update(event: GameEvent): Unit = event match {

    case MoveUndone() =>
      println("⬅️ Move successfully undone.")

    case MoveRedone() =>
      println("➡️ Move successfully redone.")

    // --- New Start Game Event ---
    case StartGame() =>
      clearScreen()
      // Display the welcome and rules message
      println(
        """
          ==================================================
                        WELCOME TO CHECKERS
          ==================================================
          Rules:
          - Regular pieces move diagonally forward
          - Kings move diagonally in any direction
          - You must jump when available
          - Reach the opposite end to become a King

        """
      )

    // --- State Change Events ---
    case BoardUpdated(board, isRedTurn) =>
      currentBoard = Some(board) // Update internal state
      clearScreen()
      println(boardString(board, isRedTurn))

    case GameEnded(winnerIsRed) =>
      clearScreen()
      println(winnerString(winnerIsRed))

    // --- Input and Prompt Logic ---
    case RequestInput(isRedTurn) =>
      val (redCount, blackCount) = getScores

      val player = if (isRedTurn) "RED (○)" else "BLACK (●)"
      print(s"\n${player}'s turn (Red: ${redCount}, Black: ${blackCount})\nEnter move (e.g., 'b3 c4') or 'quit'/'q': ")

    case InvalidInput(message) =>
      println(message)

    case MoveFailed(abstractReason) =>
      abstractReason match {
        case "Not your piece." => println("❌ That piece does not belong to you!")
        case "No piece at position." => println("❌ No piece at that position.")
        case "Must make jump." => println("❌ You must make a jump when available!")
        case "Invalid move." => println("❌ Invalid move.")
        case _ => println(s"❌ Move failed: $abstractReason")
      }

    case QuitGame() =>
      println("Thanks for playing!")

    case TurnAnnounced(isRedTurn) =>
      clearScreen()
      println(turnAnnouncementString(isRedTurn))

    case KillEffect(kills) =>
      clearScreen()
      println(killEffectString(kills))
  }


  def turnAnnouncementString(isRedTurn: Boolean): String = {
    val effect = if (isRedTurn) AsciiEffect.RedTurn else AsciiEffect.BlackTurn
    effect.color + effect.art + AnsiColor.Reset
  }

  def killEffectString(kills: Int): String = {
    val effect = kills match {
      case 1 => AsciiEffect.SingleKill
      case 2 => AsciiEffect.DoubleKill
      case 3 => AsciiEffect.TripleKill
      case _ => AsciiEffect.UltraKill
    }
    effect.color + effect.art + AnsiColor.Reset
  }

  def winnerString(isRed: Boolean): String = {
    val effect = if (isRed) AsciiEffect.RedWins else AsciiEffect.BlackWins
    effect.color + effect.art + AnsiColor.Reset
  }

  def boardString(board: Board, isRedTurn: Boolean): String = {
    val reset = "\u001b[0m"
    val red = "\u001b[91m"
    val black = "\u001b[90m"

    // Don't flip the board for black or red
    val displayBoard = board

    val columns = ('a' to 'h').map(c => s" $c ").mkString
    val sb = new StringBuilder

    sb.append("\n   " + columns + "\n")
    sb.append("  " + "+--" * 8 + "+" + "\n")

    for (row <- 0 until 8) {
      val rowNumber = row + 1
      sb.append(s"$rowNumber |")

      for (col <- 0 until 8) {
        val piece = displayBoard(row)(col) match {
          case Empty => "  "
          case Regular(true) => s"${red}○${reset} "
          case Regular(false) => s"${black}●${reset} "
          case King(true) => s"${red}◎${reset} "
          case King(false) => s"${black}◉${reset} "
        }
        sb.append(piece + "|")
      }

      sb.append(s" $rowNumber\n")
      sb.append("  " + "+--" * 8 + "+" + "\n")
    }

    sb.append("   " + columns + "\n\n")
    sb.append(
      s"Pieces: ${red}○${reset}/${red}◎${reset} = Red, " +
        s"${black}●${reset}/${black}◉${reset} = Black (Ring = King)\n"
    )

    sb.toString()
  }
}