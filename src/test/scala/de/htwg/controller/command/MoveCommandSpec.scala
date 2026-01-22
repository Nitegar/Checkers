package de.htwg.controller.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.model.*
import de.htwg.model.Board.*

class MoveCommandSpec extends AnyWordSpec with Matchers {

  "MoveCommand" should {

    "execute a valid non-jump move" in {
      val board = Board()
        .empty()
        .addPiece(5, 0, Regular(true))
        .build()

      val input = Input(5, 0, 4, 1)
      val command = MoveCommand(board, input, isRedTurn = true)

      val (newBoard, success) = command.execute()

      success shouldBe true
      newBoard(4)(1) shouldBe Regular(true)
      newBoard(5)(0) shouldBe Empty
      command.wasJump shouldBe false
      command.killCount shouldBe 0
    }

    "reject an invalid move" in {
      val board = Board()
        .empty()
        .addPiece(5, 0, Regular(true))
        .build()

      val input = Input(5, 0, 5, 1)
      val command = MoveCommand(board, input, isRedTurn = true)

      val (newBoard, success) = command.execute()

      success shouldBe false
      newBoard shouldBe board
    }

    "reject non-jump move when a jump is available" in {
      val board = Board()
        .empty()
        .addPiece(5, 0, Regular(true))
        .addPiece(4, 1, Regular(false))
        .build()

      val input = Input(5, 0, 4, 1) // normal move, jump exists
      val command = MoveCommand(board, input, isRedTurn = true)

      val (_, success) = command.execute()

      success shouldBe false
    }

    "execute a jump and count a kill" in {
      val board = Board()
        .empty()
        .addPiece(5, 0, Regular(true))
        .addPiece(4, 1, Regular(false))
        .build()

      val input = Input(5, 0, 3, 2)
      val command = MoveCommand(board, input, isRedTurn = true)

      val (newBoard, success) = command.execute()

      success shouldBe true
      newBoard(3)(2) shouldBe Regular(true)
      command.wasJump shouldBe true
      command.killCount shouldBe 1
    }

    "undo and redo a successful move" in {
      val board = Board()
        .empty()
        .addPiece(5, 0, Regular(true))
        .build()

      val input = Input(5, 0, 4, 1)
      val command = MoveCommand(board, input, isRedTurn = true)

      val (afterMove, _) = command.execute()

      command.undo(afterMove) shouldBe board
      command.redo(board) shouldBe afterMove
    }
  }
}
