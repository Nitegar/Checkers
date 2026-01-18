package de.htwg.controller

import de.htwg.model.Board.Board
import de.htwg.util.Observable

trait IController extends Observable[GameEvent] {
  def startGame(): Unit
  def getBoard: Board
  def isRedTurn: Boolean
}


