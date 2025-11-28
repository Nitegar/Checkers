package de.htwg.model.command

import de.htwg.model.Board.Board

trait GameCommand {
  def execute(): (Board, Boolean)
}
