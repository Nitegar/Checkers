package de.htwg.controller

import de.htwg.model.Board
import de.htwg.model.Board.Board
import de.htwg.patterns.{Command, GameState, Observable}
import de.htwg.view.ConsoleView

import scala.io.StdIn

class GameController(var board: Board, val view: ConsoleView) extends Observable {
  var currentState: GameState = PlayerTurnState(isRedTurn = true)

  def startGame(): Unit = {
    view.showStartup()
    StdIn.readLine()
    currentState.onEnter(this)
    gameLoop()
  }

  private def gameLoop(): Unit = {
    while (true) {
      val input = StdIn.readLine()
      currentState.handleInput(input, this)
    }
  }

  def parseInput(input: String, isRedTurn: Boolean): Command = {
    input.trim.toLowerCase match {
      case "quit" | "q" => return QuitCommand()
      case _ =>
    }

    val parts = input.split("\\s+")
    if (parts.length != 2) return InvalidCommand()

    val positions = parts.flatMap { part =>
      if (part.length < 2) None
      else {
        for {
          col <- columnToIndex(part(0))
          rowNum <- part.substring(1).toIntOption
          row <- rowToIndex(rowNum)
        } yield (row, col)
      }
    }

    if (positions.length == 2) {
      val ((fr, fc), (tr, tc)) = (positions(0), positions(1))
      val (fromRow, fromCol, toRow, toCol) = 
        if (isRedTurn) (fr, fc, tr, tc)
        else (7 - fr, 7 - fc, 7 - tr, 7 - tc)
      MoveCommand(board, fromRow, fromCol, toRow, toCol, isRedTurn)
    } else InvalidCommand()
  }

  private def columnToIndex(col: Char): Option[Int] =
    Option(col - 'a').filter(i => i >= 0 && i < 8)

  private def rowToIndex(row: Int): Option[Int] =
    Option(row - 1).filter(i => i >= 0 && i < 8)
}
