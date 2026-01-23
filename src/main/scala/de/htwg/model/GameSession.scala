package de.htwg.model

import de.htwg.model.Board.Board

case class GameSession(
          var turnCount: Int = 0,
          var isRedTurn: Boolean = true,
          var board: Board = Board().withStandardSetup().build()
        ) {
  def incrementTurn(): Unit = turnCount += 1
}