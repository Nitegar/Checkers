package de.htwg

import de.htwg.controller.GameController
import de.htwg.model.Board
import de.htwg.view.ConsoleView

object CheckersApp {
  def main(args: Array[String]): Unit = {
    val board = Board.create()
    val view = new ConsoleView()
    val controller = new GameController(board, view)
    controller.addObserver(view)
    controller.startGame()
  }
}