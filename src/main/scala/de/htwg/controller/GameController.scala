package de.htwg.controller

import scala.io.StdIn.readLine
import de.htwg.model.*
import de.htwg.model.Board.*
import GameLogic.*
import de.htwg.view.ConsoleView.*

import scala.annotation.tailrec

object GameController {

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
    if (showTurn) showTurnAnnouncement(isRedTurn)
    printBoard(board, isRedTurn)

    val (red, black) = countPieces(board)
    if (red == 0) { showWinner(isRed = false); return }
    if (black == 0) { showWinner(isRed = true); return }

    println(s"\n${if (isRedTurn) "RED (○)" else "BLACK (●)"}'s turn (Red: $red, Black: $black)")
    print("Enter move (from_row from_col to_row to_col) or 'quit/'q': ")

    readLine().trim.toLowerCase match {
      case "quit" | "q" => println("Thanks for playing!")
      case input =>
        val coords = input.split("\\s+").flatMap(_.toIntOption)
        if (coords.length != 4) {
          println("❌ Invalid input. Use format: rowFrom colFrom rowTo colTo")
          Thread.sleep(800)
          gameLoop(board, isRedTurn, false)
        } else {
          val Array(fromRow, fromCol, toRow, toCol) = coords
          
          // --- FIX 1: Koordinaten umdrehen, falls Schwarz dran ist ---
          val (fromR, fromC, toR, toC) =
            if (isRedTurn) (fromRow, fromCol, toRow, toCol)
            else (7 - fromRow, 7 - fromCol, 7 - toRow, 7 - toCol)

          if (!isValidPosition(fromR, fromC) || !isValidPosition(toR, toC)) {
            println("❌ Invalid position. Use 0-7.")
            Thread.sleep(800)
            gameLoop(board, isRedTurn, false)
          } else {

            // --- FIX 2: Prüfen, ob der Spieler auf eigenes Piece klickt ---
            (board(fromR)(fromC)) match {
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

              case _ => // alles ok
            }

            val piece = board(fromR)(fromC)
            val moves = getValidMoves(board, fromR, fromC)
            val hasJumps = hasJumpsAvailable(board, isRedTurn)
            val move = moves.find { case (r, c, _) => r == toR && c == toC }
      
            move match {
              case Some((r, c, isJump)) if !hasJumps || isJump =>
                val newBoard = makeMove(board, fromR, fromC, toR, toC)
                if (isJump) showKillEffect(1)
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
}
