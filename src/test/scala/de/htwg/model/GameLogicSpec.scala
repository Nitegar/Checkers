package de.htwg.model

import de.htwg.model.Board.Board
import de.htwg.controller.GameLogic.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameLogicSpec extends AnyWordSpec with Matchers {

  // --- Board Creation Helpers ---
  private def createTestBoard(): Board = Board().empty()
    .addPiece(5, 4, Regular(isRed = true)) // R5, C4 (e6)
    .addPiece(2, 3, Regular(isRed = false)) // R2, C3 (d3)
    .build()

  "GameLogic" when {

    // --- Utility Function Tests ---

    "using isValidPosition" should {
      "return true for valid coordinates" in {
        isValidPosition(0, 0) should be(true)
        isValidPosition(7, 7) should be(true)
        isValidPosition(3, 5) should be(true)
      }
      "return false for invalid coordinates" in {
        isValidPosition(-1, 0) should be(false)
        isValidPosition(8, 0) should be(false)
        isValidPosition(0, -1) should be(false)
        isValidPosition(0, 8) should be(false)
      }
    }

    // --- Core Move Execution (makeMove) ---

    "using makeMove" should {
      val board = createTestBoard()

      "execute a simple non-jump move correctly (Red)" in {
        val (newBoard, kills) = makeMove(board, 5, 4, 4, 5) // e6 to f5
        kills should be(0)
        newBoard(5)(4) should be(Empty)
        newBoard(4)(5) should be(Regular(true))
        newBoard(2)(3) should be(Regular(false)) // Ensure other pieces untouched
      }

      "execute a simple non-jump move correctly (Black)" in {
        val (newBoard, kills) = makeMove(board, 2, 3, 3, 4) // d3 to e4
        kills should be(0)
        newBoard(2)(3) should be(Empty)
        newBoard(3)(4) should be(Regular(false))
      }

      "execute a jump move and remove the jumped piece (Red)" in {
        // Setup: Red at R5, C1, Black at R4, C2
        val jumpBoard = Board().empty()
          .addPiece(5, 1, Regular(true))
          .addPiece(4, 2, Regular(false))
          .build()

        val (newBoard, kills) = makeMove(jumpBoard, 5, 1, 3, 3) // b6 to d4
        kills should be(1)
        newBoard(5)(1) should be(Empty) // Source empty
        newBoard(4)(2) should be(Empty) // Jumped piece empty
        newBoard(3)(3) should be(Regular(true)) // Destination has piece
      }

      // --- Kinging Logic ---

      "promote a Regular Red piece to King when reaching row 0" in {
        // Red piece at R1, C1
        val boardBefore = Board().empty().addPiece(1, 1, Regular(true)).build()
        val (newBoard, kills) = makeMove(boardBefore, 1, 1, 0, 2)
        kills should be(0)
        newBoard(0)(2) should be(King(true))
        newBoard(1)(1) should be(Empty)
      }

      "promote a Regular Black piece to King when reaching row 7" in {
        // Black piece at R6, C6
        val boardBefore = Board().empty().addPiece(6, 6, Regular(false)).build()
        val (newBoard, kills) = makeMove(boardBefore, 6, 6, 7, 5)
        kills should be(0)
        newBoard(7)(5) should be(King(false))
        newBoard(6)(6) should be(Empty)
      }

      "not change a King piece" in {
        // Red King at R1, C1
        val boardBefore = Board().empty().addPiece(1, 1, King(true)).build()
        val (newBoard, kills) = makeMove(boardBefore, 1, 1, 0, 2)
        kills should be(0)
        newBoard(0)(2) should be(King(true)) // Still a King
      }
    }

    // --- Jump Detection (getJumps) ---

    "using getJumps" should {
      "find a valid jump for a Regular Red piece" in {
        // Red at R5, C1, Black at R4, C2
        val board = Board().empty()
          .addPiece(5, 1, Regular(true))
          .addPiece(4, 2, Regular(false))
          .build()

        val jumps = getJumps(board, 5, 1, isRed = true, regular = true)
        jumps should contain((3, 3, true)) // To R3, C3
        jumps.size should be(1)
      }

      "find a valid jump for a Regular Red piece to kill a black King" in {
        // Red at R5, C1, Black at R4, C2
        val board = Board().empty()
          .addPiece(5, 1, Regular(true))
          .addPiece(4, 2, King(false))
          .build()

        val jumps = getJumps(board, 5, 1, isRed = true, regular = true)
        jumps should contain((3, 3, true)) // To R3, C3
        jumps.size should be(1)
      }

      "find a valid jump for a Regular Black piece" in {
        // Black at R2, C3, Red at R3, C4
        val board = Board().empty()
          .addPiece(2, 3, Regular(false))
          .addPiece(3, 4, Regular(true))
          .build()

        val jumps = getJumps(board, 2, 3, isRed = false, regular = true)
        jumps should contain((4, 5, true)) // To R4, C5
        jumps.size should be(1)
      }

      "find a valid jump for a Regular Black piece to kill a red King" in {
        // Black at R2, C3, Red at R3, C4
        val board = Board().empty()
          .addPiece(2, 3, Regular(false))
          .addPiece(3, 4, King(true))
          .build()

        val jumps = getJumps(board, 2, 3, isRed = false, regular = true)
        jumps should contain((4, 5, true)) // To R4, C5
        jumps.size should be(1)
      }

      "find jumps in all four directions for a King piece" in {
        // King at R3, C3. Enemies in all four adjacent spots.
        val board = Board().empty()
          .addPiece(3, 3, King(true))
          .addPiece(2, 2, Regular(false))
          .addPiece(2, 4, Regular(false))
          .addPiece(4, 2, Regular(false))
          .addPiece(4, 4, Regular(false))
          .build()

        val jumps = getJumps(board, 3, 3, isRed = true, regular = false)
        jumps should contain allOf((1,1,true), (1,5,true), (5,1,true), (5,5,true))
        jumps.size should be(4)
      }

      "return Nil when no jump is available" in {
        val board = Board().empty().addPiece(5, 5, Regular(true)).build()
        val jumps = getJumps(board, 5, 5, isRed = true, regular = true)
        jumps should be(Nil)
      }

      "return Nil when jump destination is occupied" in {
        val board = Board().empty()
          .addPiece(5, 1, Regular(true))
          .addPiece(4, 2, Regular(false))
          .addPiece(3, 3, Regular(false)) // Destination blocked
          .build()

        val jumps = getJumps(board, 5, 1, isRed = true, regular = true)
        jumps should be(Nil)
      }
    }

    // --- Jump Chain Logic (findJumpChain) ---

    "using findJumpChain" should {

      "return the board unchanged and 0 kills if no jump is available" in {
        val board = createTestBoard()
        val (finalBoard, kills) = findJumpChain(board, 5, 4, 0)
        kills should be(0)
        finalBoard should be(board)
      }

      "execute a single jump and return the final state and kill count" in {
        // Setup: Red piece at R5, C1, Black at R4, C2. No subsequent jump.
        val board = Board().empty()
          .addPiece(5, 1, Regular(true))
          .addPiece(4, 2, Regular(false))
          .build()

        val (finalBoard, kills) = findJumpChain(board, 5, 1, 0)

        kills should be(1)
        finalBoard(5)(1) should be(Empty)
        finalBoard(4)(2) should be(Empty)
        finalBoard(3)(3) should be(Regular(true))
      }

      "execute a full double jump chain and accumulate kills" in {
        // Setup: Red at R5, C1. Black at R4, C2 and R2, C4
        val board = Board().empty()
          .addPiece(4, 1, Regular(true)) // Start
          .addPiece(3, 2, Regular(false)) // Jump 1 target
          .addPiece(1, 4, Regular(false)) // Jump 2 target (after jump 1 lands at R3, C3)
          .build()
        val (finalBoard, kills) = findJumpChain(board, 4, 1, 0)

        kills should be(2)
        // Red ends at R1, C5
        finalBoard(0)(5) should be(King(true)) // Should be promoted
        finalBoard(4)(1) should be(Empty)
        finalBoard(3)(2) should be(Empty)
        finalBoard(1)(4) should be(Empty)
      }

      "stop the chain and execute only the first jump if multiple options are available" in {
        // Setup: Red at R5, C3. Enemies at R4, C2 and R4, C4 (two potential jumps)
        val board = Board().empty()
          .addPiece(5, 3, Regular(true)) // Start (d6)
          .addPiece(4, 2, Regular(false)) // Jump 1 target (c5)
          .addPiece(4, 4, Regular(false)) // Jump 2 target (e5)
          .build()

        // getJumps returns available jumps. The implementation arbitrarily picks the first one found: (3, 1, true)
        val (finalBoard, kills) = findJumpChain(board, 5, 3, 0)

        kills should be(1)
        // Check if the first jump (to R3, C1) was executed
        finalBoard(3)(1) should be(Regular(true))
        finalBoard(4)(2) should be(Empty) // Jumped target 1 removed
        // Check if the chain stopped (the second target remains)
        finalBoard(4)(4) should be(Regular(false)) // Jumped target 2 remains
      }
    }

    // --- Jump Availability Check (hasJumpsAvailable) ---

    "using hasJumpsAvailable" should {
      "return true when a jump is available for the Red player" in {
        // Red at R5, C1, Black at R4, C2
        val board = Board().empty()
          .addPiece(5, 1, Regular(true))
          .addPiece(4, 2, Regular(false))
          .build()

        hasJumpsAvailable(board, isRedTurn = true) should be(true)
      }

      "return true when a jump is available for the Black player" in {
        // Black at R2, C3, Red at R3, C4
        val board = Board().empty()
          .addPiece(2, 3, Regular(false))
          .addPiece(3, 4, Regular(true))
          .build()

        hasJumpsAvailable(board, isRedTurn = false) should be(true)
      }

      "return false when no jump is available" in {
        val board = createTestBoard()
        hasJumpsAvailable(board, isRedTurn = true) should be(false)
        hasJumpsAvailable(board, isRedTurn = false) should be(false)
      }

      "return false when no jump on a board with a king is available" in {
        val board = Board().empty()
          .addPiece(5, 4, King(isRed = true)) 
          .addPiece(2, 3, Regular(isRed = false)) 
          .build()
        hasJumpsAvailable(board, isRedTurn = true) should be(false)
        hasJumpsAvailable(board, isRedTurn = false) should be(false)
      }

      "return false if opponent has a jump but it's not the current player's turn" in {
        // Black has a jump, but it's Red's turn
        val board = Board().empty()
          .addPiece(1, 2, Regular(false))
          .addPiece(2, 3, Regular(false))
          .addPiece(3, 4, Regular(true))
          .build()

        hasJumpsAvailable(board, isRedTurn = true) should be(false)
      }
    }

    // --- Move Retrieval Dispatch (getValidMoves) ---

    // Note: Since KingMoveStrategy and RegularMoveStrategy are not provided,
    // we focus only on testing if jumps are correctly included, as the logic for
    // simple moves is external to GameLogic but jump detection is internal via getJumps.

    "using getValidMoves" should {
      "return a move list containing a jump when one is available" in {
        // Red at R5, C1, Black at R4, C2
        val board = Board().empty()
          .addPiece(5, 1, Regular(true))
          .addPiece(4, 2, Regular(false))
          .build()

        // We expect the result to contain the jump (3, 3, true)
        val moves = getValidMoves(board, 5, 1)
        moves.exists(m => m._3) should be(true) // Ensures jump is included
      }

      "return a move list with no jumps when none are available" in {
        val board = createTestBoard() // No jumps available

        // We assume RegularMoveStrategy returns at least one non-jump move here,
        // but the key check is that no 'true' jumps are present.
        val moves = getValidMoves(board, 5, 4) // Red piece at e6
        moves.exists(m => m._3) should be(false)
      }

      "return a jump list of length 4 for red king" in {
        val board = Board().empty()
          .addPiece(3, 3, King(true))
          .addPiece(2, 2, Regular(false))
          .addPiece(2, 4, Regular(false))
          .addPiece(4, 2, Regular(false))
          .addPiece(4, 4, Regular(false))
          .build()

        val jumps = getValidMoves(board, 3, 3) // Red piece at e6
        jumps.exists(m => m._3) should be(true)
        jumps.size should be(4)
      }

      "return a jump list of length 4 for Black King" in {
        val board = Board().empty()
          .addPiece(3, 3, King(false)) // Black King
          .addPiece(2, 2, Regular(true)) // NW Red
          .addPiece(2, 4, Regular(true)) // NE Red
          .addPiece(4, 2, Regular(true)) // SW Red
          .addPiece(4, 4, Regular(true)) // SE Red
          .build()

        val moves = getValidMoves(board, 3, 3)
        moves.exists(m => m._3) should be(true)
        moves.size should be(4)
      }

      "return Nil for an empty position" in {
        val board = createTestBoard()
        getValidMoves(board, 3, 3) should be(Nil)
      }
    }
  }
}