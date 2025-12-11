package de.htwg.view.gui

import de.htwg.controller.GuiInputHandler
import de.htwg.model.*
import de.htwg.model.Board.*
import de.htwg.util.Observer

import java.awt.{BasicStroke, Color, Font, Graphics2D, RenderingHints}
import javax.swing.JOptionPane
import scala.swing.*
import scala.swing.MenuBar.NoMenuBar.{listenTo, reactions}
import scala.swing.event.*

object GuiView extends Observer[GameEvent] {

  // State management
  private var currentBoard: Option[Board] = None
  private var isRedTurn: Boolean = true
  private var selectedPiece: Option[(Int, Int)] = None // (Internal Row, Internal Col)
  private var validMoves: List[(Int, Int, Boolean)] = Nil // (Internal Row, Internal Col, isJump)
  private var statusMessage: String = "Welcome to Checkers"
  private var awaitingInput: Boolean = false

  // UI Components
  private val boardPanel = new BoardPanel
  private val statusLabel = new Label(statusMessage) {
    font = new Font("Arial", Font.BOLD, 16)
    horizontalAlignment = Alignment.Center
    preferredSize = new Dimension(600, 30)
  }

  private val scoreLabel = new Label("Red: 12 | Black: 12") {
    font = new Font("Arial", Font.PLAIN, 14)
    horizontalAlignment = Alignment.Center
  }

  private val rulesButton = new Button("Rules (R)") { enabled = true }
  private val undoButton = new Button("Undo (U)") { enabled = false }
  private val redoButton = new Button("Redo (R)") { enabled = false }
  private val quitButton = new Button("Quit (Q)") { enabled = true }
  private val startButtonPlaceholder = new Button("Start Game") { enabled = false; visible = false }

  private val frame = new MainFrame {
    title = "Checkers Game"

    listenTo(this)
    reactions += {
      case UIElementResized(_) =>
        boardPanel.revalidate()
        boardPanel.repaint()
    }
    contents = new BorderPanel {
      layout(new BoxPanel(Orientation.Vertical) {
        contents += statusLabel
        contents += Swing.VStrut(5)
      }) = BorderPanel.Position.North

      layout(boardPanel) = BorderPanel.Position.Center

      layout(new BoxPanel(Orientation.Vertical) {
        contents += new BoxPanel(Orientation.Horizontal) {
          contents += scoreLabel
          contents += Swing.HGlue
        }
        contents += Swing.VStrut(5)
        contents += new BoxPanel(Orientation.Horizontal) {
          contents += Swing.HGlue
          contents += rulesButton // <--- BUTTON PLACEMENT
          contents += Swing.HStrut(10)
          contents += undoButton
          contents += Swing.HStrut(10)
          contents += redoButton
          contents += Swing.HStrut(10)
          contents += quitButton
          contents += Swing.HGlue
        }
        contents += Swing.VStrut(5)
        contents += startButtonPlaceholder
      }) = BorderPanel.Position.South
    }
    size = new Dimension(600, 700)
    centerOnScreen()
  }

  // Event listeners
  listenTo(boardPanel.mouse.clicks)
  listenTo(undoButton)
  listenTo(rulesButton) // <--- LISTEN TO NEW BUTTON
  listenTo(redoButton)
  listenTo(quitButton)

  reactions += {
    case MouseClicked(source, point, _, _, _) =>
      if (awaitingInput) {
        handleBoardClick(point.x, point.y)
      }
    case ButtonClicked(`rulesButton`) => // <--- HANDLE RULES BUTTON CLICK
      showRulesDialog()
    case ButtonClicked(`undoButton`) =>
      if (awaitingInput) {
        GuiInputHandler.submitInput("undo")
        awaitingInput = false
      }
    case ButtonClicked(`redoButton`) =>
      if (awaitingInput) {
        GuiInputHandler.submitInput("redo")
        awaitingInput = false
      }
    case ButtonClicked(`quitButton`) =>
      if (awaitingInput) {
        GuiInputHandler.submitInput("quit")
        awaitingInput = false
      } else {
        frame.dispose()
        System.exit(0)
      }
  }

  def show(): Unit = {
    frame.visible = true
  }

  // --- NEW METHOD FOR RULES DIALOG ---
  private def showRulesDialog(): Unit = {
    val rules =
      """
        |Checkers Rules:
        |
        |1. **Movement**: Regular pieces move diagonally forward one square.
        |2. **Kings**: When a piece reaches the opponent's back row, it becomes a King (♔). Kings can move diagonally forward or backward.
        |3. **Jumping/Capturing**: You must capture an opponent's piece if possible. Captures are made by jumping diagonally over an opponent's piece to the empty square immediately beyond it.
        |4. **Multiple Jumps**: If a jump lands you in a position where another jump is immediately available (a "multi-jump"), you must take the subsequent jump in the same turn.
        |5. **Forced Move**: If any jump is available on the board, you MUST take a jump. You cannot make a non-jump move.
        |6. **Winning**: The game is won by the player who captures all of the opponent's pieces.
        """.stripMargin.trim

    // Use Swing's simple dialog utility
    JOptionPane.showMessageDialog(frame.peer, rules, "Checkers Rules", JOptionPane.INFORMATION_MESSAGE)
  }

  override def update(event: GameEvent): Unit = event match {
    case StartGame() =>
      statusMessage = "Press 'Start Game' to begin"
      statusLabel.text = statusMessage
      statusLabel.background = new Color(240, 240, 240)
      statusLabel.opaque = true
      show()

    case BoardUpdated(board, redTurn) =>
      Swing.onEDT {
        currentBoard = Some(board)
        isRedTurn = redTurn
        selectedPiece = None
        validMoves = Nil
        updateScoreLabel(board)
        boardPanel.repaint()
        undoButton.enabled = true
        redoButton.enabled = true
      }

    case TurnAnnounced(redTurn) =>
      Swing.onEDT {
        isRedTurn = redTurn
        val player = if (redTurn) "Red (●)" else "Black (○)"
        statusMessage = s"$player's turn - Click a piece to move"
        statusLabel.text = statusMessage
        statusLabel.background = if (redTurn) new Color(255, 200, 200) else new Color(200, 200, 200)
        statusLabel.opaque = true
      }

    case RequestInput(redTurn) =>
      Swing.onEDT {
        awaitingInput = true
        val player = if (redTurn) "Red" else "Black"
        if (currentBoard.isEmpty) {
          statusMessage = "Welcome! Press Start Game to begin"

        } else {
          statusMessage = s"$player's turn - Make your move"
        }
        statusLabel.text = statusMessage
      }

    case GameEnded(winnerIsRed) =>
      Swing.onEDT {
        val winner = if (winnerIsRed) "Red" else "Black"
        statusMessage = s"🎉 Game Over! $winner wins! 🎉"
        statusLabel.text = statusMessage
        statusLabel.background = if (winnerIsRed) new Color(255, 180, 180) else new Color(180, 180, 180)
        statusLabel.opaque = true
        selectedPiece = None
        validMoves = Nil
        boardPanel.repaint()
        awaitingInput = false
        undoButton.enabled = false
        redoButton.enabled = false
      }

    case MoveFailed(reason) =>
      Swing.onEDT {
        statusMessage = s"❌ Invalid: $reason"
        statusLabel.text = statusMessage
        statusLabel.background = new Color(255, 220, 220)
        statusLabel.opaque = true
        awaitingInput = true
      }

    case InvalidInput(message) =>
      Swing.onEDT {
        statusMessage = s"❌ Error: $message"
        statusLabel.text = statusMessage
        statusLabel.background = new Color(255, 220, 220)
        statusLabel.opaque = true
        awaitingInput = true
      }

    case KillEffect(kills) =>
      Swing.onEDT {
        statusMessage = s"💥 ${kills} piece${if (kills > 1) "s" else ""} captured!"
        statusLabel.text = statusMessage
        statusLabel.background = new Color(255, 255, 180)
        statusLabel.opaque = true
      }

    case MoveUndone =>
      Swing.onEDT {
        statusMessage = "⬅️ Move undone"
        statusLabel.text = statusMessage
        statusLabel.background = new Color(220, 220, 255)
        statusLabel.opaque = true
      }

    case MoveRedone =>
      Swing.onEDT {
        statusMessage = "➡️ Move redone"
        statusLabel.text = statusMessage
        statusLabel.background = new Color(220, 255, 220)
        statusLabel.opaque = true
      }

    case QuitGame =>
      Swing.onEDT {
        frame.dispose()
        System.exit(0)
      }

    case _ => // Ignore other events
  }

  private def updateScoreLabel(board: Board): Unit = {
    val (red, black) = GameLogic.countPieces(board)
    scoreLabel.text = s"Red: $red | Black: $black"
  }

  // --- FIX 1: Remove all rotation logic from click handling ---
  // --- In GuiView.scala (Modified handleBoardClick) ---

  // This method must be synchronous and fast, running on the EDT
  private def handleBoardClick(x: Int, y: Int): Unit = {
    currentBoard.foreach { board =>

      // 1. Get the actual, current size of the *square* board area.
      // This MUST match the calculation used in paintComponent.
      val boardSize = math.min(boardPanel.size.width, boardPanel.size.height)

      // 2. Calculate the correct, current cell size.
      val cellSize = boardSize / 8

      // 3. Since the board is centered, we might need to adjust coordinates
      //    if the click is in the empty margins.
      //    If the click is outside the boardSize x boardSize area, ignore it.
      if (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {

        // 4. Calculate coordinates using the new cellSize.
        val internalCol = x / cellSize
        val internalRow = y / cellSize

        if (internalRow >= 0 && internalRow < 8 && internalCol >= 0 && internalCol < 8) {

          selectedPiece match {
            case Some((selRow, selCol)) =>
              if (validMoves.exists { case (r, c, _) => r == internalRow && c == internalCol }) {
                executeMove(selRow, selCol, internalRow, internalCol)
              } else {
                trySelectPiece(board, internalRow, internalCol)
              }

            case None =>
              trySelectPiece(board, internalRow, internalCol)
          }
        }
      }
    }
  }
  
  private def trySelectPiece(board: Board, row: Int, col: Int): Unit = {
    Swing.onEDT {
      board(row)(col) match {
        case Regular(red) if red == isRedTurn =>
          selectPiece(board, row, col)
        case King(red) if red == isRedTurn =>
          selectPiece(board, row, col)
        case _ =>
          selectedPiece = None
          validMoves = Nil
          statusMessage = "Select one of your own pieces"
          statusLabel.text = statusMessage
          boardPanel.repaint()
      }
    }
  }

  private def selectPiece(board: Board, row: Int, col: Int): Unit = {
    Swing.onEDT {
      val moves = GameLogic.getValidMoves(board, row, col)
      val hasJumps = GameLogic.hasJumpsAvailable(board, isRedTurn)

      validMoves = if (hasJumps) moves.filter(_._3) else moves

      if (validMoves.isEmpty) {
        statusMessage = if (hasJumps) "❌ Must jump! Select a piece that can jump." else "❌ No valid moves for this piece"
        statusLabel.text = statusMessage
        statusLabel.background = new Color(255, 220, 220)
        statusLabel.opaque = true
        selectedPiece = None
      } else {
        selectedPiece = Some((row, col))
        val colChar = ('a' + col).toChar
        val rowNum = row + 1
        statusMessage = s"✓ Selected ($colChar$rowNum). Click destination (Green=move, Red=jump)"
        statusLabel.text = statusMessage
        statusLabel.background = new Color(220, 255, 220)
        statusLabel.opaque = true
      }

      boardPanel.repaint()
    }
  }

  private def executeMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Unit = {
    val fromColChar = ('a' + fromCol).toChar
    val fromRowNum = fromRow + 1
    val toColChar = ('a' + toCol).toChar
    val toRowNum = toRow + 1

    val input = s"$fromColChar$fromRowNum $toColChar$toRowNum"

    GuiInputHandler.submitInput(input)

    Swing.onEDT {
      awaitingInput = false
      selectedPiece = None
      validMoves = Nil
      boardPanel.repaint()
    }
  }

  // Custom panel for drawing the checkers board
  private class BoardPanel extends Panel {
    preferredSize = new Dimension(600, 600)

    override def preferredSize: Dimension = {
      // If the frame has not been packed/shown yet, return a default minimum.
      if (frame.contents.head.size.width == 0) new Dimension(600, 600)
      else {
        // Get the current available width and height for the central panel
        val containerWidth = frame.contents.head.size.width
        val containerHeight = frame.contents.head.size.height - statusLabel.preferredSize.height -
          scoreLabel.preferredSize.height - 40 // ~40 accounts for padding/gaps/buttons

        // The board size is the minimum of the available width and height
        val size = math.min(containerWidth, containerHeight)
        new Dimension(size, size)
      }
    }

    override def paintComponent(g: Graphics2D): Unit = {
      val boardSize = math.min(size.width, size.height)
      val cellSize = boardSize / 8

      super.paintComponent(g)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      currentBoard.foreach { board =>
        val hasJumpsAvailable = GameLogic.hasJumpsAvailable(board, isRedTurn)

        // Draw board squares, coordinates, and highlights
        for (displayRow <- 0 until 8; displayCol <- 0 until 8) {
          val x = displayCol * cellSize
          val y = displayRow * cellSize

          // --- FIX 2 & 3: Internal coordinates are now ALWAYS the same as Display Coordinates ---
          val internalRow = displayRow
          val internalCol = displayCol

          // Checkerboard pattern
          g.setColor(if ((displayRow + displayCol) % 2 == 0) new Color(240, 217, 181) else new Color(181, 136, 99))
          g.fillRect(x, y, cellSize, cellSize)

          // Draw coordinate labels (Fixed: Always display 1-8 from bottom to top, a-h from left to right)
          // Row numbers are displayed according to the internal coordinate (0=top row/Black home, 7=bottom row/Red home)
          val rowLabel = 8 - displayRow
          if (displayCol == 0) {
            g.setColor(Color.BLACK)
            g.setFont(new Font("Arial", Font.PLAIN, 10))
            g.drawString(s"$rowLabel", x + 2, y + 12)
          }
          if (displayRow == 7) {
            g.setColor(Color.BLACK)
            g.setFont(new Font("Arial", Font.PLAIN, 10))
            val colLabel = ('a' + displayCol).toChar
            g.drawString(colLabel.toString, x + cellSize - 12, y + cellSize - 2)
          }

          // --- Playable Piece Highlighting (using internalRow/Col which are now fixed) ---
          board(internalRow)(internalCol) match {
            case Regular(isRed) if isRed == isRedTurn =>
              handleHighlight(g, board, internalRow, internalCol, hasJumpsAvailable, x, y, cellSize)
            case King(isRed) if isRed == isRedTurn =>
              handleHighlight(g, board, internalRow, internalCol, hasJumpsAvailable, x, y, cellSize)
            case _ => // Empty or opponent's piece: no highlighting
          }

          // Highlight selected piece
          selectedPiece.foreach { case (selRow, selCol) =>
            if (selRow == internalRow && selCol == internalCol) {
              g.setColor(new Color(255, 255, 0, 120))
              g.fillRect(x, y, cellSize, cellSize)
              g.setStroke(new BasicStroke(3))
              g.setColor(new Color(255, 215, 0))
              g.drawRect(x + 2, y + 2, cellSize - 4, cellSize - 4)
            }
          }

          // Highlight valid moves
          validMoves.foreach { case (moveRow, moveCol, isJump) =>
            if (moveRow == internalRow && moveCol == internalCol) {
              g.setColor(if (isJump) new Color(255, 0, 0, 100) else new Color(0, 255, 0, 100))
              g.fillRect(x, y, cellSize, cellSize)

              // Draw indicator circle
              g.setColor(if (isJump) new Color(200, 0, 0) else new Color(0, 180, 0))
              g.setStroke(new BasicStroke(3))
              val circleSize = cellSize / 3
              g.drawOval(x + cellSize/2 - circleSize/2, y + cellSize/2 - circleSize/2, circleSize, circleSize)
            }
          }

          // Draw pieces (last, to be on top of all highlights)
          board(internalRow)(internalCol) match {
            case Regular(isRed) =>
              drawPiece(g, x, y, cellSize, isRed, isKing = false)
            case King(isRed) =>
              drawPiece(g, x, y, cellSize, isRed, isKing = true)
            case Empty =>
          }
        }
      }
    }

    private def handleHighlight(g: Graphics2D, board: Board, row: Int, col: Int, hasJumpsAvailable: Boolean, x: Int, y: Int, cellSize: Int): Unit = {
      val moves = GameLogic.getValidMoves(board, row, col)
      val isPlayable = if (hasJumpsAvailable) moves.exists(_._3) else moves.nonEmpty

      if (isPlayable) {
        g.setColor(new Color(0, 150, 255, 100)) // Blue overlay
        g.fillOval(x + 5, y + 5, cellSize - 10, cellSize - 10)

        g.setStroke(new BasicStroke(2))
        g.setColor(new Color(0, 100, 255)) // Darker Blue Ring
        g.drawOval(x + 5, y + 5, cellSize - 10, cellSize - 10)
      }
    }

    private def drawPiece(g: Graphics2D, x: Int, y: Int, size: Int, isRed: Boolean, isKing: Boolean): Unit = {
      val pieceSize = (size * 0.65).toInt
      val offset = (size - pieceSize) / 2
      val centerX = x + offset
      val centerY = y + offset

      // Draw shadow
      g.setColor(new Color(0, 0, 0, 60))
      g.fillOval(centerX + 3, centerY + 3, pieceSize, pieceSize)

      // Draw piece main color
      g.setColor(if (isRed) new Color(200, 30, 30) else new Color(50, 50, 50))
      g.fillOval(centerX, centerY, pieceSize, pieceSize)

      // Draw highlight
      g.setColor(new Color(255, 255, 255, 80))
      g.fillOval(centerX + pieceSize/4, centerY + pieceSize/6, pieceSize/3, pieceSize/4)

      // Draw border
      g.setStroke(new BasicStroke(2))
      g.setColor(if (isRed) new Color(150, 0, 0) else Color.BLACK)
      g.drawOval(centerX, centerY, pieceSize, pieceSize)

      // Draw king crown
      if (isKing) {
        g.setColor(new Color(255, 215, 0))
        g.setFont(new Font("Serif", Font.BOLD, (size * 0.5).toInt))
        val metrics = g.getFontMetrics
        val crownText = "♔"
        val crownX = centerX + (pieceSize - metrics.stringWidth(crownText)) / 2
        val crownY = centerY + ((pieceSize - metrics.getHeight) / 2) + metrics.getAscent
        g.drawString(crownText, crownX, crownY)
      }
    }
  }
}