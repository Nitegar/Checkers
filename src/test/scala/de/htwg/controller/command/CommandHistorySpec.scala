package de.htwg.controller.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model.*
import de.htwg.model.Board.*

class CommandHistorySpec extends AnyWordSpec with Matchers {

  "CommandHistory" should {

    "undo and redo a command" in {
      CommandHistory.clear()

      val board = Board()
        .empty()
        .addPiece(5, 0, Regular(true))
        .build()

      val input = Input(5, 0, 4, 1)
      val command = MoveCommand(board, input, isRedTurn = true)

      val (afterMove, success) = command.execute()
      success shouldBe true

      CommandHistory.push(command)

      val undone = CommandHistory.undo(afterMove)
      undone.isDefined shouldBe true
      undone.get shouldBe board

      val redone = CommandHistory.redo(board)
      redone.isDefined shouldBe true
      redone.get shouldBe afterMove
    }

    "clear redo stack when a new command is pushed" in {
      CommandHistory.clear()

      val board = Board()
        .empty()
        .addPiece(5, 0, Regular(true))
        .build()

      val cmd1 = MoveCommand(board, Input(5, 0, 4, 1), true)
      val (b1, _) = cmd1.execute()
      CommandHistory.push(cmd1)

      CommandHistory.undo(b1)

      val cmd2 = MoveCommand(board, Input(5, 0, 4, 1), true)
      cmd2.execute()
      CommandHistory.push(cmd2)

      CommandHistory.redo(board) shouldBe None
    }

    "return None when undo stack is empty" in {
      CommandHistory.clear()

      val board = Board().empty().build()

      CommandHistory.undo(board) shouldBe None
    }

    "return None when redo stack is empty" in {
      CommandHistory.clear()

      val board = Board().empty().build()

      CommandHistory.redo(board) shouldBe None
    }
  }
}
