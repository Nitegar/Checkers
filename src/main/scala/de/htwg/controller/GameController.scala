package de.htwg.controller

import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.model.GameLogic.*
import de.htwg.model.command.MoveCommand
import de.htwg.util.Observable
import de.htwg.view.tui.ConsoleView

import scala.annotation.tailrec
import scala.io.StdIn.readLine

/**
 * GameController acts as the central state manager and event publisher (Subject).
 * It contains only game logic and publishes abstract GameEvents.
 * It is completely freed from all string literals and console I/O, except for readLine().
 */
object GameController extends Observable[GameEvent] {

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

  def startGame(): Unit = {
    this.add(ConsoleView)

    notifyObservers(StartGame())

    notifyObservers(RequestInput(isRedTurn = true))
    readLine()

    val board = Board.create()
    gameLoop(board, isRedTurn = true)
  }

  @tailrec
  def gameLoop(board: Board, isRedTurn: Boolean): Unit = {
    val (red, black) = countPieces(board)
    if (red == 0 || black == 0) {
      notifyObservers(GameEnded(winnerIsRed = black == 0))
      return
    }

    notifyObservers(TurnAnnounced(isRedTurn))
    Thread.sleep(500)

    notifyObservers(BoardUpdated(board, isRedTurn))

    notifyObservers(RequestInput(isRedTurn))

    readLine().trim.toLowerCase match {
      case "quit" | "q" =>
        notifyObservers(QuitGame)
      case input =>
        parseInput(input) match {
          case None =>
            notifyObservers(InvalidInput("Invalid format."))
            Thread.sleep(800)
            gameLoop(board, isRedTurn)

          case Some((fromRow, fromCol, toRow, toCol)) =>
            val (fromR, fromC, toR, toC) =
              if (isRedTurn) (fromRow, fromCol, toRow, toCol)
              else (fromRow, fromCol-1, 7 - toRow, 7 - toCol)

            board(fromR)(fromC) match {
              case Regular(red) if red != isRedTurn =>
                notifyObservers(MoveFailed("Not your piece."))
                Thread.sleep(800)
                return gameLoop(board, isRedTurn)
              case King(red) if red != isRedTurn =>
                notifyObservers(MoveFailed("Not your piece."))
                Thread.sleep(800)
                return gameLoop(board, isRedTurn)
              case Empty =>
                notifyObservers(MoveFailed("No piece at that position."))
                Thread.sleep(800)
                return gameLoop(board, isRedTurn)
              case _ =>
            }

            val command = MoveCommand(board, fromR, fromC, toR, toC, isRedTurn)
            val (newBoard, success) = command.execute()

            if (success) {
              if (command.wasJump) {
                notifyObservers(KillEffect(1))
                Thread.sleep(2000)
              }
              gameLoop(newBoard, !isRedTurn)

            } else {
              val requiredJumpMissed = hasJumpsAvailable(board, isRedTurn)
              val reason = if (requiredJumpMissed) {
                "Must make jump."
              } else {
                "Invalid move."
              }
              notifyObservers(MoveFailed(reason))
              Thread.sleep(800)
              gameLoop(board, isRedTurn)
            }
        }
    }
  }
}