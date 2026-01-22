package de.htwg.controller

import de.htwg.controller.command.{CommandHistory, MoveCommand}
import de.htwg.controller.inputhandler.InputHandler
import de.htwg.model.*
import de.htwg.model.Board.Board
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future

class GameStateSpec extends AnyWordSpec with Matchers {

  class TestInputStub(val input: String) extends InputHandler {
    override def requestInput(): Future[String] = Future.successful(input)
    override def attachSession(session: GameSession): Unit = {}
  }

  def createSession(board: Board, isRed: Boolean): GameSession = {
    val s = GameSession()
    s.board = board
    s.isRedTurn = isRed
    s
  }

  "AwaitingInputState" should {
    "transition to GameOverState when Red has no pieces" in {
      val board = Board().empty().addPiece(0, 0, Regular(false)).build() // Only Black left
      val (nextState, _, _, events) = AwaitingInputState.process(createSession(board, true), new TestInputStub(""))

      nextState shouldBe GameOverState
      events.head shouldBe a[GameEnded]
      events.head.asInstanceOf[GameEnded].winnerIsRed shouldBe false
    }

    "transition to InputHandlingState and announce turn if pieces remain" in {
      val board = Board().withStandardSetup().build()
      val (nextState, _, _, events) = AwaitingInputState.process(createSession(board, true), new TestInputStub(""))

      nextState shouldBe InputHandlingState
      events should contain allOf (TurnAnnounced(true), RequestInput(true))
    }
  }

  "InputHandlingState" should {
    val initialBoard = Board().withStandardSetup().build()

    "transition to GameOverState on 'q'" in {
      val (nextState, _, _, events) = InputHandlingState.process(createSession(initialBoard, true), new TestInputStub("q"))
      nextState shouldBe GameOverState
      events should contain(QuitGame())
    }

    "handle undo correctly when history exists then move undone" in {
      CommandHistory.clear()

      val oldBoard = Board().empty().build()
      val newBoard = Board().empty().addPiece(1, 1, Regular(true)).build()
      val commandToUndo = MoveCommand(oldBoard, Input(5, 0, 4, 1), true)

      CommandHistory.push(commandToUndo)

      val (nextState, resultBoard, nextTurn, events) = InputHandlingState.process(createSession(newBoard, false), new TestInputStub("u"))

      events.head shouldBe a[MoveUndone]
    }

    "handle undo correctly when history does not exist then move failed" in {
      CommandHistory.clear()
      val newBoard = Board().empty().addPiece(1, 1, Regular(true)).build()

      val (nextState, resultBoard, nextTurn, events) = InputHandlingState.process(createSession(newBoard, false), new TestInputStub("u"))

      events.head shouldBe a[MoveFailed]
    }

    "handle redo correctly when history exists then move redone" in {
      CommandHistory.clear()

      val oldBoard = Board().empty().build()
      val newBoard = Board().empty().addPiece(1, 1, Regular(true)).build()
      val commandToUndo = MoveCommand(oldBoard, Input(5, 0, 4, 1), true)

      CommandHistory.push(commandToUndo)

      InputHandlingState.process(createSession(newBoard, false), new TestInputStub("u"))
      val (nextState, resultBoard, nextTurn, events) = InputHandlingState.process(createSession(newBoard, false), new TestInputStub("r"))

      events.head shouldBe a[MoveRedone]
    }

    "handle redo correctly when history does not exist then move failed" in {
      CommandHistory.clear()
      val newBoard = Board().empty().addPiece(1, 1, Regular(true)).build()

      val (nextState, resultBoard, nextTurn, events) = InputHandlingState.process(createSession(newBoard, false), new TestInputStub("r"))

      events.head shouldBe a[MoveFailed]
    }

    "transition to MoveExecutionState on valid coordinates" in {
      val (nextState, _, _, _) = InputHandlingState.process(createSession(initialBoard, true), new TestInputStub("a3 b4"))
      nextState shouldBe a[MoveExecutionState]
      nextState.asInstanceOf[MoveExecutionState].input shouldBe Input(2, 0, 3, 1)
    }

    "return to AwaitingInputState on invalid string format" in {
      val (nextState, _, _, events) = InputHandlingState.process(createSession(initialBoard, true), new TestInputStub("invalid"))
      nextState shouldBe AwaitingInputState
      events.head shouldBe a[InvalidInput]
    }
  }

  "MoveExecutionState" should {
    val boardWithPiece = Board().empty()
      .addPiece(5, 0, Regular(true))
      .addPiece(7, 7, King(true)).build()

    "fail if moving the wrong color piece" in {
      val state = MoveExecutionState(Input(5, 0, 4, 1))
      val (nextState, _, _, events) = state.process(createSession(boardWithPiece, false), new TestInputStub("")) // Black's turn

      nextState shouldBe AwaitingInputState
      events should contain(MoveFailed("Not your piece."))
    }

    "fail if moving the wrong color king piece" in {
      val state = MoveExecutionState(Input(7, 7, 6, 6))
      val (nextState, _, _, events) = state.process(createSession(boardWithPiece, false), new TestInputStub("")) // Black's turn

      nextState shouldBe AwaitingInputState
      events should contain(MoveFailed("Not your piece."))
    }

    "fail if source cell is empty" in {
      val state = MoveExecutionState(Input(4, 4, 3, 3))
      val (nextState, _, _, events) = state.process(createSession(boardWithPiece, true), new TestInputStub(""))

      nextState shouldBe AwaitingInputState
      events should contain(MoveFailed("No piece at that position."))
    }

    "move failed for invalid move" in {
      val state = MoveExecutionState(Input(5, 0, 2, 2))
      val (nextState, _, _, events) = state.process(createSession(boardWithPiece, true), new TestInputStub(""))

      nextState shouldBe AwaitingInputState
      events should contain(MoveFailed("Invalid move."))
    }

    "move failed because player must make a jump" in {
      val mustMakeJumpBoard = Board().empty()
        .addPiece(4, 1, Regular(true))
        .addPiece(3, 2, Regular(false))
        .build()

      val state = MoveExecutionState(Input(4, 1, 3, 0))
      val (nextState, _, _, events) = state.process(createSession(mustMakeJumpBoard, true), new TestInputStub(""))

      nextState shouldBe AwaitingInputState
      events should contain(MoveFailed("Must make jump."))
    }


    "successfully execute move and flip turn" in {
      val state = MoveExecutionState(Input(5, 0, 4, 1))
      val (nextState, newBoard, nextTurn, _) = state.process(createSession(boardWithPiece, true), new TestInputStub(""))

      nextState shouldBe AwaitingInputState
      nextTurn shouldBe false
      newBoard(5)(0) shouldBe Empty
      newBoard(4)(1) shouldBe Regular(true)
    }

    "handle jump moves and return KillEffect" in {
      val jumpBoard = Board().empty()
        .addPiece(5, 0, Regular(true))
        .addPiece(4, 1, Regular(false)).build()

      val state = MoveExecutionState(Input(5, 0, 3, 2))
      val (_, _, _, events) = state.process(createSession(jumpBoard, true), new TestInputStub(""))

      events should contain(KillEffect(1))
    }
  }

  "GameOverState" should {
    "remain in GameOverState infinitely" in {
      val board = Board().empty().build()
      val (nextState, _, _, _) = GameOverState.process(createSession(board, true), new TestInputStub(""))
      nextState shouldBe GameOverState
    }
  }
}