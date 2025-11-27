package de.htwg.controller

import de.htwg.model.GameLogic
import de.htwg.patterns.GameState

case class PlayerTurnState(isRedTurn: Boolean) extends GameState {
  override def onEnter(controller: GameController): Unit = {
    controller.view.showTurnAnnouncement(isRedTurn)
    Thread.sleep(500)
    controller.notifyObservers(controller.board, isRedTurn)
    val (red, black) = GameLogic.countPieces(controller.board)
    if (red == 0) {
      controller.currentState = GameOverState(winner = Some(false))
      controller.currentState.onEnter(controller)
    } else if (black == 0) {
      controller.currentState = GameOverState(winner = Some(true))
      controller.currentState.onEnter(controller)
    } else {
      controller.view.askForMovePrompt(isRedTurn, red, black)
    }
  }

  override def handleInput(input: String, controller: GameController): Unit = {
    val command = controller.parseInput(input, isRedTurn)
    command match {
      case cmd: MoveCommand =>
        cmd.execute() match {
          case MoveSuccess(newBoard, isJump) =>
            if (isJump) {
              controller.view.showKillEffect(1)
              Thread.sleep(2000)
            }
            controller.board = newBoard
            controller.currentState = PlayerTurnState(!isRedTurn)
            controller.currentState.onEnter(controller)
          case MoveFailure =>
            controller.view.showInvalidMove()
            Thread.sleep(800)
            onEnter(controller)
        }
      case _: QuitCommand =>
        controller.currentState = GameOverState(None)
        controller.currentState.onEnter(controller)
      case _: InvalidCommand =>
        controller.view.showInvalidInput()
        Thread.sleep(800)
        onEnter(controller)
    }
  }
}
