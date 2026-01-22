package de.htwg.controller

import de.htwg.controller.inputhandler.InputHandler
import de.htwg.model.Board.Board
import de.htwg.model.{Board, GameSession, Regular}

object ChangeBoardState extends GameState {
  override def process(
                        session: GameSession,
                        inputHandler: InputHandler
                      ): (GameState, Board, Boolean, List[GameEvent]) = {
    val nextBoard = Board().empty().addPiece(1, 1, Regular(isRed = true)).build()
    (GameOverState, nextBoard, session.isRedTurn, List(GameEnded(session.board, winnerIsRed = true)))
  }
}

object DoNothingState extends GameState {
  override def process(
                        session: GameSession,
                        inputHandler: InputHandler
                      ): (GameState, Board, Boolean, List[GameEvent]) = {
    (GameOverState, session.board, session.isRedTurn, List(GameEnded(session.board, winnerIsRed = true)))
  }
}