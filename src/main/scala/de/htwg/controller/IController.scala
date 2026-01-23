package de.htwg.controller

import de.htwg.util.Observable

trait IController extends Observable[GameEvent] {
  def startGame(): Unit
}


