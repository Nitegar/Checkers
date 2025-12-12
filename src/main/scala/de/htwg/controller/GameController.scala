package de.htwg.controller

import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.util.Observable
import de.htwg.view.tui.TuiView

import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}
import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * GameController acts as the central state manager and event publisher (Subject).
 * It contains only game logic and publishes abstract GameEvents.
 *
 * MODIFIED: Now supports non-blocking input via an internal queue for GUI support.
 */
object GameController extends Observable[GameEvent] {

  // --- New: Non-blocking input handling for GUI/TUI ---
  private val inputQueue = new LinkedBlockingQueue[String]()
  private var isGuiMode: Boolean = false // Flag to switch between blocking TUI and non-blocking GUI wait

  /** Injects input from the GUI or other sources. */
  def setInput(input: String): Unit = inputQueue.put(input)

  /** Retrieves the next input. Blocks until input is provided. */
  def retrieveInput(): String = {
    // This blocks, waiting for the TUI readLine or the GUI setInput to inject an element.
    inputQueue.take()
  }

  // Convert column letter (a-h) to index (0-7)
  /**
   * Converts a column character ('a'-'h') to a 0-based index (0-7).
   * Returns Failure if the character is out of range.
   *
   * @param col The column character (e.g., 'a', 'h').
   * @return Success(index) or Failure(IllegalArgumentException).
   */
  def columnToIndex(col: Char): Try[Int] = {
    // Ensure case-insensitivity by converting to lower case
    val lowerCol = col.toLower
    val index = lowerCol - 'a'

    if (index >= 0 && index < 8) {
      Success(index)
    } else {
      Failure(new IllegalArgumentException(s"Invalid column character: '$col'. Expected 'a'-'h'."))
    }
  }

  /**
   * Converts a row number (1-8) to a 0-based index (0-7).
   * Returns Failure if the number is out of range.
   *
   * @param row The row number (e.g., 1, 8).
   * @return Success(index) or Failure(IllegalArgumentException).
   */
  def rowToIndex(row: Int): Try[Int] = {
    val index = row - 1

    if (index >= 0 && index < 8) {
      Success(index)
    } else {
      Failure(new IllegalArgumentException(s"Invalid row number: $row. Expected 1-8."))
    }
  }

  /**
   * Helper function to safely parse a row string into an Int.
   */
  private def parseRowString(rowStr: String, pos: String): Try[Int] = {
    Try(rowStr.toInt).recoverWith {
      case _: NumberFormatException =>
        Failure(new IllegalArgumentException(s"Invalid $pos row input: '$rowStr'. Expected a digit (1-8)."))
    }
  }

  /**
   * Parses a chess move string (e.g., "A1 B2") into an Input case class.
   * Uses a for-comprehension over Try to sequence coordinate validation and parsing.
   *
   * @param input The raw input string.
   * @return Success(Input) with 0-based indices or Failure(IllegalArgumentException).
   */
  def parseInput(input: String): Try[Input] = {
    // 1. Basic format validation
    val parts = input.trim.toLowerCase.split("\\s+")
    if (parts.length != 2) {
      return Failure(new IllegalArgumentException("Invalid format. Expected format: 'A1 B2' (source destination)."))
    }

    // 2. Extract parts
    val srcStr = parts(0)
    val destStr = parts(1)

    if (srcStr.length < 2 || destStr.length < 2) {
      return Failure(new IllegalArgumentException("Coordinates must contain a column and a row (e.g., A1)."))
    }

    // 3. Sequential validation using a Try for-comprehension
    for {
      // Source Parsing
      srcColChar <- Try(srcStr.charAt(0))
      srcColIdx <- columnToIndex(srcColChar)
      srcRowStr = srcStr.substring(1)
      srcRowInt <- parseRowString(srcRowStr, "source")
      srcRowIdx <- rowToIndex(srcRowInt)

      // Destination Parsing
      destColChar <- Try(destStr.charAt(0))
      destColIdx <- columnToIndex(destColChar)
      destRowStr = destStr.substring(1)
      destRowInt <- parseRowString(destRowStr, "destination")
      destRowIdx <- rowToIndex(destRowInt)

    } yield Input(srcRowIdx, srcColIdx, destRowIdx, destColIdx)
  }

  // --- State Management ---
  private var currentState: GameState = AwaitingInputState
  // RESTORED: InputHandler state and methods
  private var inputHandler: InputHandler = TuiInputHandler

  def isTuiActive: Boolean = inputHandler == TuiInputHandler

  def setInputHandler(handler: InputHandler): Unit = {
    inputHandler = handler
  }

  def getInputHandler: InputHandler = inputHandler
  // END RESTORED

  /**
   * Starts the game loop.
   * @param useGui If true, starts the game loop in a non-blocking background thread for GUI.
   */
  def startGame(useGui: Boolean = false): Unit = {
    this.isGuiMode = useGui
    this.add(TuiView) // TUI is always active for logging/alternative output

    // If using GUI, run the state machine in a separate thread
    if (useGui) {
      Future { runGameLoop() }
    } else {
      // For TUI, just run the loop in the current thread (it will block)
      runGameLoop()
    }
  }

  private def runGameLoop(): Unit = {
    notifyObservers(StartGame())

    // Initial TUI prompt
    if (!isGuiMode) {
      notifyObservers(RequestInput(isRedTurn = true))
      // TUI: Blocks and directly reads from console, then injects into the queue
      val input = readLine()
      inputQueue.put(input)
    }

    // --- State Machine Execution Loop ---
    var currentBoard: Board = Board().withStandardSetup().build()
    var isRedTurn: Boolean = true

    // Start the state machine iteration
    currentState = AwaitingInputState

    while (currentState != GameOverState) {
      // The current state processes the request (input, execution, etc.)
      // and returns the NEXT state, the new board, and whose turn it is.
      val (nextState, newBoard, nextTurn) = currentState.process(this, currentBoard, isRedTurn)

      // Update the context variables for the next iteration
      currentState = nextState
      currentBoard = newBoard
      isRedTurn = nextTurn
    }
  }

}