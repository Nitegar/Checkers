package de.htwg.model.command

import de.htwg.model.Board.*
import de.htwg.model.*

case class MoveCommand(
                        initialBoard: Board,
                        fromRow: Int,
                        fromCol: Int,
                        toRow: Int,
                        toCol: Int,
                        isRedTurn: Boolean
                      ) extends Command {
  private var boardAfter: Board = initialBoard
  private var executionSuccess: Boolean = false
  var wasJump: Boolean = false
  var killCount: Int = 0

  /**
   * Executes the move command.
   * @return A tuple of (New Board state, Success/Failure flag).
   */
  def execute(): (Board, Boolean) = {
    val piece = initialBoard(fromRow)(fromCol)

    // Check if the move is valid and required jumps are handled (using GameLogic.getValidMoves)
    val availableMoves = GameLogic.getValidMoves(initialBoard, fromRow, fromCol)
    val validDestinations = availableMoves.map(m => (m._1, m._2))

    if (!validDestinations.contains((toRow, toCol))) {
      // Move is invalid (not in valid list)
      return (initialBoard, false)
    }

    // Check if a jump was available but a non-jump move was made
    val hasJumpsAvailable = GameLogic.hasJumpsAvailable(initialBoard, isRedTurn)
    val attemptedMoveIsJump = math.abs(toRow - fromRow) == 2

    if (hasJumpsAvailable && !attemptedMoveIsJump) {
      // Failed: Must jump
      return (initialBoard, false)
    }

    // 1. Execute the single move
    val (boardAfterFirstMove, kills) = GameLogic.makeMove(initialBoard, fromRow, fromCol, toRow, toCol)

    wasJump = attemptedMoveIsJump
    killCount = kills

    val finalBoard: Board = if (wasJump) {
      // 2. If it was a jump, check for a chain reaction
      val (boardAfterChain, totalKills) = GameLogic.findJumpChain(boardAfterFirstMove, toRow, toCol, kills)
      killCount = totalKills // Update the total kill count
      boardAfterChain
    } else {
      // Simple non-jump move
      boardAfterFirstMove
    }

    // 🎯 FIX 2: Store the result of successful execution 🎯
    this.boardAfter = finalBoard
    this.executionSuccess = true

    // Return the board after the full chain
    (finalBoard, true)
  }

  /**
   * Reverses the move, returning the state before this command was executed.
   *
   * @param currentBoard The board state *after* this command (used for verification).
   * @return The board state before this command.
   */
  override def undo(currentBoard: Board): Board = {
    // The undo operation is simple: return the board state stored BEFORE execution.
    if (executionSuccess) initialBoard else currentBoard
  }

  /**
   * Re-applies the move, returning the state after this command was executed.
   *
   * @param currentBoard The board state *before* this command (used for verification).
   * @return The board state after this command.
   */
  override def redo(currentBoard: Board): Board = {
    // The redo operation is simple: return the board state stored AFTER execution.
    if (executionSuccess) boardAfter else currentBoard
  }
}