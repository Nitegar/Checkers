package de.htwg.model.command

import de.htwg.model.Board.Board

trait Command {
  def execute(): (Board, Boolean)

  def undo(board: Board): Board

  def redo(board: Board): Board
}
