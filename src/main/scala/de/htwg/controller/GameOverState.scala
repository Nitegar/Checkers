package de.htwg.controller

import de.htwg.patterns.GameState

case class GameOverState(winner: Option[Boolean]) extends GameState {
  override def onEnter(controller: GameController): Unit = {
    winner match {
      case Some(isRed) => controller.view.showWinner(isRed)
      case None => controller.view.showThanks()
    }
  }

  override def handleInput(input: String, controller: GameController): Unit = {
    // Game is over, no input is handled
  }
}
