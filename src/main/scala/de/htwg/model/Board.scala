package de.htwg.model

import scala.collection.immutable.Vector

object Board {
  // Use immutable Vector[Vector[Piece]] for the Board type
  type Board = Vector[Vector[Piece]]

  // The 'create()' method is replaced by the Builder's 'apply()'
  def apply(): BoardBuilder = new BoardBuilder(8)

  // -------------------------------------------------------------
  // BUILDER CLASS
  // -------------------------------------------------------------

  class BoardBuilder(size: Int) {
    // Internal mutable state for the board under construction,
    // initialized with an empty (Vector.fill) board
    private var internalBoard: Board = Vector.fill(size, size)(Empty)

    // --- Configuration Methods (Fluent Interface) ---

    /** Initializes the board with the standard Checkers starting setup. */
    def withStandardSetup(): BoardBuilder = {
      for {
        row <- 0 until size
        col <- 0 until size
      } {
        val newPiece: Piece = (row, col) match {
          // Black pieces (top rows 0-2)
          case (r, c) if r < 3 && (r + c) % 2 == 1 => Regular(false)
          // Red pieces (bottom rows 5-7)
          case (r, c) if r > 4 && (r + c) % 2 == 1 => Regular(true)
          case _ => Empty
        }

        // Use Vector.updated to create a new, modified vector for the row,
        // and then update the internalBoard with the new row vector.
        val updatedRow = internalBoard(row).updated(col, newPiece)
        internalBoard = internalBoard.updated(row, updatedRow)
      }
      this // Return 'this' for method chaining
    }

    /** Adds a custom piece at a specific location. */
    def addPiece(row: Int, col: Int, piece: Piece): BoardBuilder = {
      if (row >= 0 && row < size && col >= 0 && col < size) {
        // Use Vector.updated for immutable update of the specific cell
        val updatedRow = internalBoard(row).updated(col, piece)
        internalBoard = internalBoard.updated(row, updatedRow)
      }
      this
    }

    /** Sets the board to be completely empty by initializing all cells with the Empty piece. */
    def empty(): BoardBuilder = {
      // Use Vector.fill to concisely create and initialize the 2D immutable vector
      internalBoard = Vector.fill(size, size)(Empty)
      this
    }

    // --- Final Construction Method ---

    /** Creates and returns the immutable final board object. */
    def build(): Board = internalBoard
  }
}