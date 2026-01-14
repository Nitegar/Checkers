package de.htwg.controller

import de.htwg.controller.command.{CommandHistory, MoveCommand}
import de.htwg.controller.inputhandler.InputHandler
import de.htwg.model.*
import de.htwg.model.Board.Board
import de.htwg.view.tui.TuiView
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future
import scala.util.Success

class GameControllerSpec extends AnyWordSpec with Matchers {

  class TestInputStub(testInput: String) extends InputHandler {
    // Returns a Future that is already resolved with the test string
    override def requestInput(): Future[String] = {
      Future.successful(testInput)
    }
  }


  private def createSimpleBoard(): Board = Board().empty()
    .addPiece(5, 4, Regular(isRed = true)) // Red at e6 (R5, C4)
    .addPiece(2, 3, Regular(isRed = false)) // Black at d3 (R2, C3)
    .build()

  /**
   * Helper function to execute a single step of the state machine for testing
   * the InputHandlingState, which requires mocking readLine().
   */
  private def processStep(state: GameState, board: Board, isRedTurn: Boolean, input: String): (GameState, Board, Boolean) = {
    // Use ByteArrayInputStream to mock readLine()
    val originalHandler = GameController.getInputHandler

    // 2. Create the stub with the specific test input and inject it
    val testStub = new TestInputStub(input)
    GameController.setInputHandler(testStub)

    try {
      // 3. Run the state process. Await.result will block, but the stub's Future
      // is already completed, so it resolves instantly with the desired input.
      state.process(GameController, board, isRedTurn)
    } finally {
      // 4. CRITICAL: Restore the original handler (TuiInputHandler/GuiInputHandler)
      GameController.setInputHandler(originalHandler)
    }
  }

  "GameController.parseInput" when {

    "given valid checker notation" should {

      "parse a Red forward move (a6 b5)" in {
        // a6 (R5, C0) -> b5 (R4, C1)
        GameController.parseInput("a6 b5") should be(Success(Input(5, 0, 4, 1)))
      }

      "parse a Black forward move (h1 g2)" in {
        // h1 (R0, C7) -> g2 (R1, C6)
        GameController.parseInput("h1 g2") should be(Success(Input(0, 7, 1, 6)))
      }

      "handle mixed case input (A6 B5)" in {
        GameController.parseInput("A6 B5") should be(Success(Input(5, 0, 4, 1)))
      }

      "handle extra whitespace" in {
        GameController.parseInput("  a6  b5  ") should be(Success(Input(5, 0, 4, 1)))
      }
    }

    "given invalid notation" should {

      "return Failure for completely wrong format (missing destination)" in {
        GameController.parseInput("a6").failure.exception shouldBe an[IllegalArgumentException]
        GameController.parseInput("a6").failure.exception.getMessage should startWith("Invalid format")
      }

      "return Failure for completely wrong format (too many parts)" in {
        GameController.parseInput("a6 b5 c4").failure.exception shouldBe an[IllegalArgumentException]
        GameController.parseInput("a6 b5 c4").failure.exception.getMessage should startWith("Invalid format")
      }


      "return Failure for invalid source column (z6 b5)" in {
        GameController.parseInput("z6 b5").failure.exception shouldBe an [IllegalArgumentException]
        GameController.parseInput("z6 b5").failure.exception.getMessage should include("Invalid column character: 'z'. Expected 'a'-'h")
      }

      "return Failure for invalid source row (aR b5) - Non-numeric" in {
        GameController.parseInput("aR b5").failure.exception shouldBe an [IllegalArgumentException]
        GameController.parseInput("aR b5").failure.exception.getMessage should include("Invalid source row input: 'r'. Expected a digit (1-8).")
      }

      "return Failure for out-of-bounds source row (a9 b5)" in {
        GameController.parseInput("a9 b5").failure.exception shouldBe an[IllegalArgumentException]
        GameController.parseInput("a9 b5").failure.exception.getMessage should include("Invalid row number: 9. Expected 1-8.")
      }

      "return Failure for invalid destination column (a6 z5)" in {
        // Z is not a valid column character (a-h)
        GameController.parseInput("a6 z5").failure.exception shouldBe an [IllegalArgumentException]
        GameController.parseInput("a6 z5").failure.exception.getMessage should include("Invalid column character: 'z'. Expected 'a'-'h")
      }

      "return Failure for invalid destination row (a6 bR) - Non-numeric" in {
        // R is not a valid row number, triggers toIntOption failure
        GameController.parseInput("a6 bR").failure.exception shouldBe an [IllegalArgumentException]
        GameController.parseInput("a6 bR").failure.exception.getMessage should include("Invalid destination row input: 'r'. Expected a digit (1-8).")
      }

      "return Failure for out-of-bounds destination row (a6 b9)" in {
        GameController.parseInput("a6 b9").failure.exception shouldBe an[IllegalArgumentException]
        GameController.parseInput("a6 b9").failure.exception.getMessage should include("Invalid row number: 9. Expected 1-8.")
      }
    }
  }


  // --- GameState Unit Tests (100% Coverage on State Logic) ---

  "AwaitingInputState" should {

    "transition to GameOverState when Red has no pieces" in {
      // Red has 0 pieces
      val board = Board().empty().addPiece(0, 7, Regular(isRed = false)).build()
      val (nextState, _, _) = AwaitingInputState.process(GameController, board, isRedTurn = true)
      nextState should be(GameOverState)
    }

    "transition to GameOverState when Black has no pieces" in {
      // Black has 0 pieces
      val board = Board().empty().addPiece(7, 0, Regular(isRed = true)).build()
      val (nextState, _, _) = AwaitingInputState.process(GameController, board, isRedTurn = false)
      nextState should be(GameOverState)
    }

    "transition to InputHandlingState on normal execution" in {
      val board = Board().withStandardSetup().build()
      val (nextState, _, _) = AwaitingInputState.process(GameController, board, isRedTurn = true)
      nextState should be(InputHandlingState)
    }
  }

  "InputHandlingState" should {
    val initialBoard = createSimpleBoard() // Red at e6, Black at d3

    "transition to GameOverState on 'quit' or 'q' input" in {
      val (nextStateQ, _, _) = processStep(InputHandlingState, initialBoard, true, "q")
      nextStateQ should be(GameOverState)

      val (nextStateQuit, _, _) = processStep(InputHandlingState, initialBoard, true, "quit")
      nextStateQuit should be(GameOverState)
    }

    "transition to AwaitingInputState and undo on 'undo' input" in {
      // 1. Setup: Make a move and push to history
      val moveCommand = MoveCommand(initialBoard, Input(5, 4, 4, 5), true)
      val (movedBoard, success) = moveCommand.execute()
      success should be(true)
      CommandHistory.push(moveCommand)

      // 2. Undo
      val (nextState, newBoard, nextTurn) = processStep(InputHandlingState, movedBoard, false, "undo")

      nextState should be(AwaitingInputState)
      newBoard should be(initialBoard)
      nextTurn should be(true)
    }

    "stay in AwaitingInputState on failed 'undo' (empty history)" in {
      CommandHistory.clear()
      val (nextState, newBoard, nextTurn) = processStep(InputHandlingState, initialBoard, true, "undo\n")

      // Should stay in same turn/board
      nextState should be(AwaitingInputState)
      newBoard should be(initialBoard)
      nextTurn should be(true)
    }

    "transition to AwaitingInputState and redo on 'redo' input" in {
      // 1. Setup: Push and then immediately undo a move to prime the redo stack
      val moveCommand = command.MoveCommand(initialBoard, Input(5, 4, 4, 5), true)
      val (movedBoard, _) = moveCommand.execute()
      CommandHistory.push(moveCommand)
      CommandHistory.undo(movedBoard)

      // 2. Redo (Note: current board is 'initialBoard', current turn is 'false' after undo)
      val (nextState, newBoard, nextTurn) = processStep(InputHandlingState, initialBoard, false, "redo\n")

      // Should apply redo and flip turn
      nextState should be(AwaitingInputState)
      newBoard should be(movedBoard)
      nextTurn should be(true) // Turn flips back to Red
    }

    "stay in AwaitingInputState on failed 'redo' (empty redo stack)" in {
      CommandHistory.clear()
      val (nextState, newBoard, nextTurn) = processStep(InputHandlingState, initialBoard, true, "redo\n")

      // Should stay in same turn/board
      nextState should be(AwaitingInputState)
      newBoard should be(initialBoard)
      nextTurn should be(true)
    }

    "transition to MoveExecutionState on valid input" in {
      // Valid input for Red: e6 f5
      val (nextState, _, _) = processStep(InputHandlingState, initialBoard, true, "e6 f5\n")
      nextState should be(MoveExecutionState(Input(5, 4, 4, 5)))
    }

    "transition to AwaitingInputState on invalid move input" in {
      // Invalid input format
      val (nextState, newBoard, nextTurn) = processStep(InputHandlingState, initialBoard, true, "bad_format\n")
      nextState should be(AwaitingInputState)
      newBoard should be(initialBoard)
      nextTurn should be(true)
    }
  }

  "MoveExecutionState" should {
    val initialBoard = Board().withStandardSetup().build()

    // --- Success Paths ---

    "successfully execute a simple move and switch turn" in {
      // Red move: a6 to b5 (R5, C0 -> R4, C1)
      val executionState = MoveExecutionState(Input(5, 0, 4, 1))
      val (nextState, newBoard, nextTurn) = executionState.process(GameController, initialBoard, isRedTurn = true)

      nextState should be(AwaitingInputState)
      nextTurn should be(false) // Switched to Black's turn
      newBoard(5)(0) should be(Empty)
      newBoard(4)(1) should be(Regular(true))
    }

    "successfully execute a jump move, push to history, and switch turn" in {
      // Setup: Red piece at b6 (R5, C1), Black piece at c5 (R4, C2)
      val board = Board().empty().addPiece(5, 1, Regular(true)).addPiece(4, 2, Regular(false)).build()
      val executionState = MoveExecutionState(Input(5, 1, 3, 3)) // b6 to d4

      val (nextState, newBoard, nextTurn) = executionState.process(GameController, board, isRedTurn = true)

      nextState should be(AwaitingInputState)
      nextTurn should be(false) // Switched to Black's turn
      newBoard(5)(1) should be(Empty) // Old piece gone
      newBoard(4)(2) should be(Empty) // Jumped piece gone
      newBoard(3)(3) should be(Regular(true)) // New piece there
    }

    // --- Failure Paths (Returning to AwaitingInputState) ---

    "fail if attempting to move opponent's regular piece (Red turn)" in {
      // Red tries to move Black piece at b1 (R0, C1)
      val executionState = MoveExecutionState(Input(0, 1, 1, 2))
      val (nextState, newBoard, nextTurn) = executionState.process(GameController, initialBoard, isRedTurn = true)

      nextState should be(AwaitingInputState)
      nextTurn should be(true) // Turn remains Red
      newBoard should be(initialBoard)
    }

    "fail if attempting to move opponent's King piece (Black turn)" in {
      // Setup: Red King at a8 (R0, C0), Black's turn
      val board = Board().empty().addPiece(0, 0, King(true)).addPiece(3, 3, Regular(false)).build()
      // Black tries to move Red King (h8 in flipped coords -> R0, C0 unflipped)
      val executionState = MoveExecutionState(Input(7, 7, 6, 6))
      val (nextState, newBoard, nextTurn) = executionState.process(GameController, board, isRedTurn = false)

      nextState should be(AwaitingInputState)
      nextTurn should be(false) // Turn remains Black
      newBoard should be(board)
    }

    "fail if selecting an empty position" in {
      // Try to move from e5 (R4, C4), which is empty on standard board
      val executionState = MoveExecutionState(Input(4, 4, 3, 3))
      val (nextState, newBoard, nextTurn) = executionState.process(GameController, initialBoard, isRedTurn = true)

      nextState should be(AwaitingInputState)
      newBoard should be(initialBoard)
      nextTurn should be(true)
    }

    "fail if attempting a non-jump move when a jump is required" in {
      // Setup: Red at b6 (R5, C1), Black at c5 (R4, C2) - Jump available
      val board = Board().empty().addPiece(5, 1, Regular(true)).addPiece(4, 2, Regular(false)).build()
      // Invalid move: b6 to a5 (non-jump when jump is mandatory)
      val executionState = MoveExecutionState(Input(5, 1, 4, 0))

      val (nextState, newBoard, nextTurn) = executionState.process(GameController, board, isRedTurn = true)

      nextState should be(AwaitingInputState)
      newBoard should be(board)
      nextTurn should be(true)
    }

    "fail if attempting an invalid move (e.g., diagonal backwards for regular piece)" in {
      // Red at a6 (R5, C0). Try to move a6 to b7 (backwards)
      val executionState = MoveExecutionState(Input(5, 0, 6, 1))

      val (nextState, newBoard, nextTurn) = executionState.process(GameController, initialBoard, isRedTurn = true)

      nextState should be(AwaitingInputState)
      newBoard should be(initialBoard)
      nextTurn should be(true)
    }
  }

  "GameOverState" should {
    "remain in GameOverState" in {
      val board = Board().empty().build()
      val (nextState, newBoard, nextTurn) = GameOverState.process(GameController, board, isRedTurn = true)

      nextState should be(GameOverState)
      newBoard should be(board)
      nextTurn should be(true)
    }
  }
}