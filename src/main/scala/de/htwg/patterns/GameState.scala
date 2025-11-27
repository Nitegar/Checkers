package de.htwg.patterns

import de.htwg.controller.GameController

trait GameState {
  def handleInput(input: String, controller: GameController): Unit
  def onEnter(controller: GameController): Unit
}
