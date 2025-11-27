package de.htwg.view

import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.patterns.Observer
import de.htwg.view.AsciiEffect

class ConsoleView extends Observer {

  override def update(board: Board, isRedTurn: Boolean): Unit = {
    showBoard(board, isRedTurn)
  }

  def clearScreen(): Unit = print("\u001b[2J\u001b[H")

  def showStartup(): Unit = {
    clearScreen()
    println("=" * 50)
    println("          WELCOME TO CHECKERS")
    println("=" * 50)
    println("\nRules:")
    println("- Regular pieces move diagonally forward")
    println("- Kings move diagonally in any direction")
    println("- You must jump when available")
    println("- Reach the opposite end to become a King")
    println("\nPress Enter to start...")
  }

  def showTurnAnnouncement(isRedTurn: Boolean): Unit = {
    clearScreen()
    val art = if (isRedTurn) AsciiEffect.RedTurn else AsciiEffect.BlackTurn
    println(art.color + art.art + AnsiColor.Reset)
  }

  def showKillEffect(kills: Int): Unit = {
    clearScreen()
    val art = kills match {
      case 1 => AsciiEffect.SingleKill
      case 2 => AsciiEffect.DoubleKill
      case 3 => AsciiEffect.TripleKill
      case _ => AsciiEffect.UltraKill
    }
    println(art.color + art.art + AnsiColor.Reset)
  }

  def showWinner(isRed: Boolean): Unit = {
    clearScreen()
    val art = if (isRed) AsciiEffect.RedWins else AsciiEffect.BlackWins
    println(art.color + art.art + AnsiColor.Reset)
  }

  def showBoard(board: Board, isRedTurn: Boolean): Unit = {
    clearScreen()
    println(buildBoardString(board, isRedTurn))
  }

  def askForMovePrompt(isRedTurn: Boolean, redCount: Int, blackCount: Int): Unit = {
    println(s"\n${if (isRedTurn) "RED (○)" else "BLACK (●)"}'s turn " +
      s"(Red: $redCount, Black: $blackCount)")
    print("Enter move (e.g., 'b3 c4') or 'quit'/'q': ")
  }

  def showInvalidInput(): Unit = println("❌ Invalid input. Use: colRow colRow (e.g., b3 c4)")
  def showInvalidMove(): Unit = println("❌ Invalid move.")
  def showNotYourPiece(): Unit = println("❌ That piece does not belong to you!")
  def showMustJump(): Unit = println("❌ You must make a jump when available!")
  def showThanks(): Unit = println("Thanks for playing!")


  // --- INTERNAL PURE STRING BUILDER (NOT CALLED BY CONTROLLER) ---

  private def buildBoardString(board: Board, isRedTurn: Boolean): String = {
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
      sb.append(s"${row + 1} |")

      for (col <- 0 until 8) {
        val piece = displayBoard(row)(col) match {
          case Empty => "  "
          case Regular(true)  => s"${red}○${reset} "
          case Regular(false) => s"${black}●${reset} "
          case King(true)     => s"${red}◎${reset} "
          case King(false)    => s"${black}◉${reset} "
        }
        sb.append(piece + "|")
      }

      sb.append(s" ${row + 1}\n")
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