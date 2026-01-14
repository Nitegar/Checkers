package de.htwg.controller.command

import de.htwg.model.Board.Board
import scala.collection.mutable.Stack

/**
 * Manages the history of successfully executed commands for Undo/Redo.
 */
object CommandHistory {
  private val undoStack: Stack[Command] = Stack()
  private val redoStack: Stack[Command] = Stack()

  /** Adds a successfully executed command to the undo stack. */
  def push(command: Command): Unit = {
    undoStack.push(command)
    // Any new command clears the redo stack
    redoStack.clear()
  }

  /** Undoes the last command and moves it to the redo stack. */
  def undo(currentBoard: Board): Option[Board] = {
    if (undoStack.nonEmpty) {
      val command = undoStack.pop()
      redoStack.push(command)
      // The command itself knows the state before it ran
      Some(command.undo(currentBoard))
    } else {
      None
    }
  }

  /** Redoes the last undone command and moves it back to the undo stack. */
  def redo(currentBoard: Board): Option[Board] = {
    if (redoStack.nonEmpty) {
      val command = redoStack.pop()
      undoStack.push(command)
      // The command itself knows the state after it ran
      Some(command.redo(currentBoard))
    } else {
      None
    }
  }

  /** Clears all history. */
  def clear(): Unit = {
    undoStack.clear()
    redoStack.clear()
  }
}