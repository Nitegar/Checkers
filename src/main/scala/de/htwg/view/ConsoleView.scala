package de.htwg.view

import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.view.AsciiEffect

object ConsoleView {

  /** Turn announcement (pure string) */
  def turnAnnouncementString(isRedTurn: Boolean): String = {
    val effect = if (isRedTurn) AsciiEffect.RedTurn else AsciiEffect.BlackTurn
    effect.color + effect.art + AnsiColor.Reset
  }

  /** Kill effect as pure string */
  def killEffectString(kills: Int): String = {
    val effect = kills match {
      case 1 => AsciiEffect.SingleKill
      case 2 => AsciiEffect.DoubleKill
      case 3 => AsciiEffect.TripleKill
      case _ => AsciiEffect.UltraKill
    }
    effect.color + effect.art + AnsiColor.Reset
  }

  /** Winner screen as pure string */
  def winnerString(isRed: Boolean): String = {
    val effect = if (isRed) AsciiEffect.RedWins else AsciiEffect.BlackWins
    effect.color + effect.art + AnsiColor.Reset
  }

  /** Full board as string */
  def boardString(board: Board, isRedTurn: Boolean): String = {
    val reset = "\u001b[0m"
    val red = "\u001b[91m"
    val black = "\u001b[90m"

    val displayBoard =
      if (isRedTurn) board else board.reverse.map(_.reverse)

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
          case Regular(true)  => s"${red}○${reset} "
          case Regular(false) => s"${black}●${reset} "
          case King(true)  => s"${red}◎${reset} "
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
