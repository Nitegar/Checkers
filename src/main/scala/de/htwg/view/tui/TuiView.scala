package de.htwg.view.tui

import de.htwg.controller.*
import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.util.Observer
import de.htwg.controller.inputhandler.InputHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TuiView(inputHandler: InputHandler) extends Observer[GameEvent] {

  private var currentBoard: Option[Board] = None

  private def clearScreen(): Unit = print("\u001b[2J\u001b[H")
  private def getScores: (Int, Int) = currentBoard.map(GameLogic.countPieces).getOrElse((0, 0))

  override def update(event: GameEvent): Unit = {
    Future {
      event match {
        case MoveUndone => println("⬅️ Move successfully undone.")
        case MoveRedone => println("➡️ Move successfully redone.")
        case StartGame() =>
          clearScreen()
          println("""
          ==================================================
                        WELCOME TO CHECKERS
          ==================================================
          Rules:
          - Regular pieces move diagonally forward
          - Kings move diagonally in any direction
          - You must jump when available
          - Reach the opposite end to become a King
        """)
        case BoardUpdated(board, isRedTurn) =>
          currentBoard = Some(board)
          clearScreen()
          println(boardString(board, isRedTurn))
        case GameEnded(winnerIsRed) =>
          println(winnerString(winnerIsRed))
          println("\nGame Over! Enter 'revanche' to play again or 'quit' to exit: ") //          clearScreen()
        case RequestInput(isRedTurn) =>
          val (redCount, blackCount) = getScores
          if (redCount == 0 && blackCount == 0) {
            print("Press Enter to start...")
          } else {
            val player = if (isRedTurn) "RED (○)" else "BLACK (●)"
            print(s"\n${player}'s turn (Red: ${redCount}, Black: ${blackCount})\nEnter move (e.g., 'b3 c4') or 'quit'/'q': ")
          }
        case InvalidInput(message) => println(message)
        case MoveFailed(abstractReason) =>
          abstractReason match {
            case "Not your piece." => println("❌ That piece does not belong to you!")
            case "No piece at position." => println("❌ No piece at that position.")
            case "Must make jump." => println("❌ You must make a jump when available!")
            case "Invalid move." => println("❌ Invalid move.")
            case _ => println(s"❌ Move failed: $abstractReason")
          }
        case QuitGame => println("\nThanks for playing!")
          sys.exit(0)
        case TurnAnnounced(isRedTurn) =>
          clearScreen()
          println(turnAnnouncementString(isRedTurn))
        case KillEffect(kills) =>
          clearScreen()
          println(killEffectString(kills))
      }
    }
  }

  def run(): Unit = {
    // Use Iterator to be more idiomatic and less likely to block tight
    scala.io.Source.stdin.getLines().foreach { input =>
      val cmd = input.trim.toLowerCase
      if (cmd == "revanche") {
        de.htwg.controller.command.CommandHistory.clear()
        inputHandler.submitInput("revanche")
      } else if (cmd.nonEmpty) {
        inputHandler.submitInput(cmd)
      }
    }
  }
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
  val reset = "\u001b[0m"; val red = "\u001b[91m";
  val black = "\u001b[90m"
  val columns = ('a' to 'h').map(c => s" $c ").mkString
  val sb = new StringBuilder
  sb.append("\n   " + columns + "\n")
  sb.append("  " + "+--" * 8 + "+" + "\n")
  for (row <- 0 until 8) {
    val rowNumber = row + 1
    sb.append(s"$rowNumber |")
    for (col <- 0 until 8) {
      val piece = board(row)(col) match {
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
  sb.append(s"Pieces: ${red}○${reset}/${red}◎${reset} = Red, ${black}●${reset}/${black}◉${reset} = Black\n")
  sb.toString()
}