package de.htwg.controller

import scala.io.StdIn.readLine
import de.htwg.model._
import de.htwg.model.Board._
import GameLogic._
import de.htwg.view.ConsoleView

import scala.annotation.tailrec

object GameController {

  // Convert column letter (a-h) to index (0-7)
  private def columnToIndex(col: Char): Option[Int] = {
    val index = col - 'a'
    if (index >= 0 && index < 8) Some(index) else None
  }

  // Convert row number (1-8) to index (0-7)
  private def rowToIndex(row: Int): Option[Int] = {
    val index = row - 1
    if (index >= 0 && index < 8) Some(index) else None
  }

  // Parse input in format: "colRow colRow" (e.g., "b3 c4")
  private def parseInput(input: String): Option[(Int, Int, Int, Int)] = {
    val parts = input.split("\\s+")
    if (parts.length != 2) return None

    val positions = parts.flatMap { part =>
      if (part.length < 2) None
      else {
        val col = part.charAt(0)
        val rowStr = part.substring(1)
        for {
          colIdx <- columnToIndex(col)
          rowNum <- rowStr.toIntOption
          rowIdx <- rowToIndex(rowNum)
        } yield (rowIdx, colIdx)
      }
    }

    if (positions.length == 2) {
      val (fromR, fromC) = positions(0)
      val (toR, toC) = positions(1)
      Some((fromR, fromC, toR, toC))
    } else None
  }

  /** Clear the terminal screen (controller handles side-effects). */
  private def clearScreen(): Unit = print("\u001b[2J\u001b[H")

  def startGame(): Unit = {
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
    readLine()

    val board = Board.create()
    gameLoop(board, isRedTurn = true)
  }

  @tailrec
  def gameLoop(board: Board, isRedTurn: Boolean, showTurn: Boolean = true): Unit = {
    if (showTurn) {
      clearScreen()
      // Print turn announcement, wait, then clear again (controller responsible for timing/clearing)
      println(ConsoleView.turnAnnouncementString(isRedTurn))
      Thread.sleep(500)
      clearScreen()
    }

    // Print the board (controller prints the string returned by the pure view)
    println(ConsoleView.boardString(board, isRedTurn))

    val (red, black) = countPieces(board)
    if (red == 0) {
      clearScreen()
      println(ConsoleView.winnerString(isRed = false))
      return
    }
    if (black == 0) {
      clearScreen()
      println(ConsoleView.winnerString(isRed = true))
      return
    }

    println(s"\n${if (isRedTurn) "RED (○)" else "BLACK (●)"}'s turn (Red: $red, Black: $black)")
    print("Enter move (e.g., 'b3 c4') or 'quit'/'q': ")

    readLine().trim.toLowerCase match {
      case "quit" | "q" => println("Thanks for playing!")
      case input =>
        parseInput(input) match {
          case None =>
            println("❌ Invalid input. Use format: colRow colRow (e.g., b3 c4)")
            Thread.sleep(800)
            gameLoop(board, isRedTurn, false)

          case Some((fromRow, fromCol, toRow, toCol)) =>
            // Flip coordinates if it's black's turn (board was displayed mirrored earlier)
            val (fromR, fromC, toR, toC) =
              if (isRedTurn) (fromRow, fromCol, toRow, toCol)
              else (7 - fromRow, 7 - fromCol, 7 - toRow, 7 - toCol)

            // Check if the player is selecting their own piece
            board(fromR)(fromC) match {
              case Regular(red) if red != isRedTurn =>
                println("❌ That piece does not belong to you!")
                Thread.sleep(800)
                return gameLoop(board, isRedTurn, false)

              case King(red) if red != isRedTurn =>
                println("❌ That piece does not belong to you!")
                Thread.sleep(800)
                return gameLoop(board, isRedTurn, false)

              case Empty =>
                println("❌ No piece at that position.")
                Thread.sleep(800)
                return gameLoop(board, isRedTurn, false)

              case _ =>
            }

            val moves = getValidMoves(board, fromR, fromC)
            val hasJumps = hasJumpsAvailable(board, isRedTurn)
            val move = moves.find { case (r, c, _) => r == toR && c == toC }

            move match {
              case Some((r, c, isJump)) if !hasJumps || isJump =>
                val newBoard = makeMove(board, fromR, fromC, toR, toC)
                if (isJump) {
                  // controller prints the kill effect returned by view and manages timing/clearing
                  clearScreen()
                  println(ConsoleView.killEffectString(1))
                  Thread.sleep(2000)
                  clearScreen()
                }
                gameLoop(newBoard, !isRedTurn, true)

              case Some(_) =>
                println("❌ You must make a jump when available!")
                Thread.sleep(800)
                gameLoop(board, isRedTurn, false)

              case None =>
                println("❌ Invalid move.")
                Thread.sleep(800)
                gameLoop(board, isRedTurn, false)
            }
        }
    }
  }
}
