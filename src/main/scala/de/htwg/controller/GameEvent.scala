package de.htwg.controller

import de.htwg.model.Board.Board

sealed trait GameEvent

// State Change Events
case class StartGame() extends GameEvent
case class BoardUpdated(board: Board, isRedTurn: Boolean) extends GameEvent
case class GameEnded(board: Board, winnerIsRed: Boolean) extends GameEvent

// Message/Feedback Events
case class QuitGame() extends GameEvent
// RequestInput now only needs turn info, as scores are available in BoardUpdated
case class RequestInput(isRedTurn: Boolean) extends GameEvent
case class InvalidInput(message: String) extends GameEvent
case class MoveFailed(reason: String) extends GameEvent

// UI/Effect Events
case class TurnAnnounced(isRedTurn: Boolean) extends GameEvent
case class KillEffect(kills: Int) extends GameEvent

case class MoveUndone() extends GameEvent
case class MoveRedone() extends GameEvent

