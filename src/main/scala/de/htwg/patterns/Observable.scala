package de.htwg.patterns

import de.htwg.model.Board.Board

trait Observable {
  private var observers: List[Observer] = Nil

  def addObserver(observer: Observer): Unit = {
    observers = observer :: observers
  }

  def removeObserver(observer: Observer): Unit = {
    observers = observers.filterNot(_ == observer)
  }

  def notifyObservers(board: Board, isRedTurn: Boolean): Unit = {
    observers.foreach(_.update(board, isRedTurn))
  }
}
