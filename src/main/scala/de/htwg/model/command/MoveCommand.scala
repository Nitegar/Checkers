package de.htwg.model.command

import de.htwg.model.Board.Board
import de.htwg.model.GameLogic

case class MoveCommand(currentBoard: Board, fromR: Int, fromC: Int, toR: Int, toC: Int, isRedTurn: Boolean) extends GameCommand {
    override def execute(): (Board, Boolean) = {
      val moves = GameLogic.getValidMoves(currentBoard, fromR, fromC)
      val hasJumps = GameLogic.hasJumpsAvailable(currentBoard, isRedTurn)
      val move = moves.find { case (r, c, _) => r == toR && c == toC }

      move match {
        case Some((r, c, isJump)) if !hasJumps || isJump =>
          val newBoard = GameLogic.makeMove(currentBoard, fromR, fromC, toR, toC)
          (newBoard, true)

        case _ =>
          (currentBoard, false)
      }
    }

    def wasJump: Boolean = math.abs(toR - fromR) == 2
}
