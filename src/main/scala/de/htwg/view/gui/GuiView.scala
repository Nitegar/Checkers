package de.htwg.view.gui

import de.htwg.controller.*
import de.htwg.controller.inputhandler.InputHandler
import de.htwg.model.*
import de.htwg.util.Observer
import scalafx.application.{JFXApp3, Platform}
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.{BorderPane, HBox, StackPane, VBox}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}
import scalafx.scene.control.{Alert, Button, Label}
import scalafx.scene.control.Alert.AlertType
import scalafx.Includes.*
import scalafx.animation.*
import scalafx.util.Duration

import scala.compiletime.uninitialized
import de.htwg.model.Board.{Board, BoardBuilder}

class GuiView(inputHandler: InputHandler) extends Observer[GameEvent] {


  def run(controller: IController): Unit = {
    CheckersGuiApp.inputHandler = inputHandler
    CheckersGuiApp.controller = controller
    CheckersGuiApp.guiInstance = this
    CheckersGuiApp.main(Array())
  }

  override def update(event: GameEvent): Unit = {
    Platform.runLater {
      CheckersGuiApp.updateView(event)
    }
  }

  private object CheckersGuiApp extends JFXApp3 {
    private val NUMBER_OF_PIECES: Int = 12
    private var redTurn: Boolean = true

    var inputHandler: InputHandler = uninitialized
    var controller: IController = uninitialized
    var guiInstance: GuiView = uninitialized
    private var canvas: Canvas = uninitialized
    private var redPointsLabel: Label = uninitialized
    private var blackPointsLabel: Label = uninitialized
    private var turnLabel: Label = uninitialized
    private var currentBoard: Option[Board] = Some(BoardBuilder(8).withStandardSetup().build())
    private var selectedSquare: Option[(Int, Int)] = None

    // Animation States
    private var animatingPiece: Option[(Piece, Double, Double, Double, Double, Double)] = None // Piece, fromCol, fromRow, toCol, toRow, progress
    private var animationTimeline: Timeline = uninitialized

    override def start(): Unit = {
      canvas = new Canvas()

      redPointsLabel = new Label("0") {
        style = "-fx-font-size: 44px; -fx-text-fill: white; -fx-font-weight: bold;"
      }
      blackPointsLabel = new Label("0") {
        style = "-fx-font-size: 44px; -fx-text-fill: white; -fx-font-weight: bold;"
      }
      turnLabel = new Label("TURN: RED") {
        style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #990000; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 2);"
      }

      val redScoreCard = new VBox {
        alignment = Pos.Center
        minWidth = 140
        minHeight = 140
        style = "-fx-background-color: linear-gradient(to bottom right, #cc0000, #800000); -fx-background-radius: 20; -fx-border-color: #660000; -fx-border-width: 4; -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 5, 5);"
        children = Seq(new Label("RED") {
          style = "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;"
        }, redPointsLabel)
      }

      val blackScoreCard = new VBox {
        alignment = Pos.Center
        minWidth = 140
        minHeight = 140
        style = "-fx-background-color: linear-gradient(to bottom right, #444444, #111111); -fx-background-radius: 20; -fx-border-color: black; -fx-border-width: 4; -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 5, 5);"
        children = Seq(new Label("BLACK") {
          style = "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;"
        }, blackPointsLabel)
      }

      val sidePanel = new VBox {
        alignment = Pos.Center
        padding = Insets(30)
        spacing = 35
        prefWidth = 240
        style = "-fx-background-color: rgba(220, 220, 220, 0.7); -fx-border-color: #aaaaaa; -fx-border-width: 0 0 0 2;"
        children = Seq(new Label("SCOREBOARD") {
          style = "-fx-font-size: 20px; -fx-font-weight: bold;"
        }, redScoreCard, blackScoreCard, turnLabel)
      }

      val bottomControls = new HBox {
        alignment = Pos.Center
        spacing = 25
        padding = Insets(15)
        style = "-fx-background-color: #f4f4f4;"
        children = Seq(
          new Button("↩ Undo") {
            style = "-fx-font-size: 14px;"
            onAction = _ => inputHandler.submitInput("undo")
          },
          new Button("↪ Redo") {
            style = "-fx-font-size: 14px;"
            onAction = _ => inputHandler.submitInput("redo")
          },
          new Button("ℹ Rules") {
            style = "-fx-font-size: 14px;"
            onAction = _ => showRulesDialog()
          },
          new Button("❌ Quit") {
            style = "-fx-font-size: 14px;"
            onAction = _ => sys.exit(0)
          }
        )
      }

      val centerPane = new StackPane {
        style = "-fx-background-color: #2e7d32;"
        children = Seq(canvas)
      }

      canvas.width <== centerPane.width
      canvas.height <== centerPane.height
      canvas.width.onChange(draw())
      canvas.height.onChange(draw())

      stage = new JFXApp3.PrimaryStage {
        title = "Checkers"
        scene = new Scene(950, 750) {
          root = new BorderPane {
            center = centerPane
            right = sidePanel
            bottom = bottomControls
          }
        }
      }

      controller.add(guiInstance)
      setupInput()
      updateScore()
      draw()
    }

    def updateView(event: GameEvent): Unit = event match {
      case BoardUpdated(board, isRed) =>
        redTurn = isRed
        detectMove(board)
        currentBoard = Some(board)
        selectedSquare = None

        val stackPane = canvas.parent.value.asInstanceOf[javafx.scene.layout.StackPane]
        val overlays = stackPane.getChildren.filter(_.isInstanceOf[javafx.scene.layout.VBox])
        if (overlays.nonEmpty) {
          stackPane.getChildren.removeAll(overlays)
        }
        if (redPointsLabel != null) updateScore()
        draw()

      case GameEnded(finalBoard, winnerIsRed) =>
        detectMove(finalBoard)
        currentBoard = Some(finalBoard)
        updateScore()
        draw()

        val pause = new scalafx.animation.PauseTransition(scalafx.util.Duration(500))
        pause.onFinished = _ => {
          showWinnerOverlay(if (winnerIsRed) "RED" else "BLACK")
        }
        pause.play()
      case _ =>
    }

    private def detectMove(newBoard: Board): Unit = {
      for (oldB <- currentBoard) {
        var from: Option[(Int, Int)] = None
        var to: Option[(Int, Int)] = None
        var movedPiece: Option[Piece] = None

        for (r <- 0 until 8; c <- 0 until 8) {
          if (oldB(r)(c) != Empty && newBoard(r)(c) == Empty) from = Some((r, c))
          if (oldB(r)(c) == Empty && newBoard(r)(c) != Empty) {
            to = Some((r, c))
            movedPiece = Some(newBoard(r)(c))
          }
        }

        for ((fR, fC) <- from; (tR, tC) <- to; p <- movedPiece) {
          animatingPiece = Some((p, fC.toDouble, fR.toDouble, tC.toDouble, tR.toDouble, 0.0))
          if (animationTimeline != null) animationTimeline.stop()

          animationTimeline = new Timeline {
            keyFrames = Seq(
              KeyFrame(Duration(16), onFinished = _ => {
                animatingPiece = animatingPiece.map(ap => ap.copy(_6 = Math.min(1.0, ap._6 + 0.05)))
                draw()
                if (animatingPiece.exists(_._6 >= 1.0)) {
                  animatingPiece = None
                  draw()
                  stop()
                }
              })
            )
            cycleCount = Timeline.Indefinite
          }
          animationTimeline.play()
        }
      }
    }

    private def updateScore(): Unit = {
      val (red, black) = GameLogic.countPieces(currentBoard.get)
      redPointsLabel.text = s"${NUMBER_OF_PIECES - black}"
      blackPointsLabel.text = s"${NUMBER_OF_PIECES - red}"
      turnLabel.text = if (redTurn) "TURN: RED" else "TURN: BLACK"
      turnLabel.style = s"-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: ${if (redTurn) "#990000" else "#323232"};"
    }

    // Helper to keep your 3D piece logic exactly as it was
    private def drawPiece(gc: scalafx.scene.canvas.GraphicsContext, p: Piece, x: Double, y: Double, tileSize: Double, redTurn: Boolean, board: Option[Board], row: Int, col: Int): Unit = {
      val isRed = p match {
        case Regular(r) => r
        case King(r) => r
        case _ => false
      }

      // Glow logic (only if row/col provided - skipped for sliding ghost)
      if (row != -1 && isRed == redTurn) {
        board.foreach { b =>
          val moves = GameLogic.getValidMoves(b, row, col)
          if (if (GameLogic.hasJumpsAvailable(b, redTurn)) moves.exists(_._3) else moves.nonEmpty) {
            gc.setStroke(Color.web("#FFFFFF", 0.9))
            gc.setLineWidth(4)
            gc.strokeRoundRect(x + 4, y + 4, tileSize - 8, tileSize - 8, 12, 12)
          }
        }
      }

      // Piece Shadow (3D Depth)
      gc.fill = Color.web("#000000", 0.4)
      gc.fillOval(x + tileSize * 0.18, y + tileSize * 0.18, tileSize * 0.68, tileSize * 0.68)

      // Glossy Body
      val baseColor = if (isRed) Color.web("#cc0000") else Color.web("#222222")
      val grad = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NoCycle,
        List(Stop(0.0, baseColor.brighter), Stop(0.6, baseColor), Stop(1.0, baseColor.darker)))

      gc.fill = grad
      gc.fillOval(x + tileSize * 0.15, y + tileSize * 0.15, tileSize * 0.7, tileSize * 0.7)

      // Piece Ridge (Ring)
      gc.setStroke(baseColor.darker)
      gc.setLineWidth(1.5)
      gc.strokeOval(x + tileSize * 0.25, y + tileSize * 0.25, tileSize * 0.5, tileSize * 0.5)

      if (p.isInstanceOf[King]) {
        gc.fill = Color.Gold
        gc.font = scalafx.scene.text.Font.font("Serif", scalafx.scene.text.FontWeight.Bold, tileSize * 0.5)
        gc.textAlign = scalafx.scene.text.TextAlignment.Center
        gc.textBaseline = scalafx.geometry.VPos.Center
        gc.fillText("♔", x + tileSize / 2, y + tileSize / 2 - (tileSize * 0.05))
        gc.textAlign = scalafx.scene.text.TextAlignment.Left
        gc.textBaseline = scalafx.geometry.VPos.Baseline
      }
    }

    private def draw(): Unit = {
      if (canvas == null || canvas.width.value <= 0) return
      val gc = canvas.graphicsContext2D
      val size = Math.min(canvas.width.value, canvas.height.value) * 0.9
      val tileSize = size / 8
      val offsetX = (canvas.width.value - size) / 2
      val offsetY = (canvas.height.value - size) / 2

      gc.clearRect(0, 0, canvas.width.value, canvas.height.value)

      // 1. 3D Board Frame
      gc.fill = Color.web("#5d3a1a")
      gc.fillRoundRect(offsetX - 10, offsetY - 10, size + 20, size + 20, 10, 10)

      // 2. Main Board Squares
      for (row <- 0 until 8; col <- 0 until 8) {
        val x = offsetX + col * tileSize
        val y = offsetY + row * tileSize
        gc.fill = if ((row + col) % 2 == 0) Color.web("#ebcd99") else Color.web("#966333")
        gc.fillRect(x, y, tileSize, tileSize)
        gc.stroke = Color.web("#000000", 0.1)
        gc.strokeRect(x, y, tileSize, tileSize)
      }

      // 3. Move Predictions
      selectedSquare.foreach { case (sR, sC) =>
        currentBoard.foreach { b =>
          val moves = GameLogic.getValidMoves(b, sR, sC)
          val hasJ = GameLogic.hasJumpsAvailable(b, redTurn)
          (if (hasJ) moves.filter(_._3) else moves).foreach { case (mR, mC, isJump) =>
            gc.fill = if (isJump) Color.web("#8B0000", 0.5) else Color.web("#00FF00", 0.3)
            gc.fillRect(offsetX + mC * tileSize, offsetY + mR * tileSize, tileSize, tileSize)
          }
        }
      }

      // 4. Static Pieces
      for (row <- 0 until 8; col <- 0 until 8) {
        currentBoard.foreach { b =>
          val p = b(row)(col)
          val isArriving = animatingPiece.exists(ap => ap._4.toInt == col && ap._5.toInt == row)
          if (p != Empty && !isArriving) {
            drawPiece(gc, p, offsetX + col * tileSize, offsetY + row * tileSize, tileSize, redTurn, currentBoard, row, col)
          }
        }
      }

      // 5. Sliding Piece
      animatingPiece.foreach { case (p, fC, fR, tC, tR, progress) =>
        val curC = fC + (tC - fC) * progress
        val curR = fR + (tR - fR) * progress
        drawPiece(gc, p, offsetX + curC * tileSize, offsetY + curR * tileSize, tileSize, redTurn, None, -1, -1)
      }
    }

    private def setupInput(): Unit = {
      canvas.onMouseClicked = e => {
        val size = Math.min(canvas.width.value, canvas.height.value) * 0.9
        val offsetX = (canvas.width.value - size) / 2
        val offsetY = (canvas.height.value - size) / 2
        val col = ((e.x - offsetX) / (size / 8)).toInt
        val row = ((e.y - offsetY) / (size / 8)).toInt

        if (col >= 0 && col < 8 && row >= 0 && row < 8) {
          selectedSquare match {
            case None =>
              currentBoard.foreach { b =>
                val piece = b(row)(col)
                val isValidSelection = piece match {
                  case Regular(isRed) => isRed == redTurn
                  case King(isRed) => isRed == redTurn
                  case _ => false
                }
                if (isValidSelection) {
                  selectedSquare = Some((row, col))
                  draw()
                }
              }
            case Some((sR, sC)) =>
              if (sR == row && sC == col) {
                selectedSquare = None
                draw()
              } else {
                val moveCommand = s"${('a' + sC).toChar}${sR + 1} ${('a' + col).toChar}${row + 1}"
                try {
                  inputHandler.submitInput(moveCommand)
                } catch {
                  case ex: Exception => ex.printStackTrace()
                }
                selectedSquare = None
                draw()
              }
          }
        }
      }
    }

    private def showWinnerOverlay(winner: String): Unit = {
      lazy val overlay: VBox = new VBox {
        alignment = Pos.Center
        spacing = 10
        padding = Insets(50)
        style =
          """
          -fx-background-color: radial-gradient(center 50% 50%, radius 80%, rgba(40, 30, 20, 0.95), rgba(10, 10, 10, 0.98));
          -fx-background-radius: 20;
          -fx-border-color: linear-gradient(to bottom, #FFD700, #B8860B, #8B4513);
          -fx-border-width: 5;
          -fx-border-radius: 15;
          -fx-effect: dropshadow(gaussian, gold, 30, 0, 0, 0);
        """
        maxWidth = 550
        maxHeight = 350
        children = Seq(
          new Label("✧ VICTORY ✧") {
            style = "-fx-text-fill: linear-gradient(to bottom, #FFFFFF, #FFD700); -fx-font-size: 32px; -fx-font-weight: bold; -fx-font-family: 'Georgia';"
          },
          new Label(s"$winner") {
            val textColor: String = if (winner.toUpperCase == "RED") "#FF3333" else "#CCCCCC"
            style = s"-fx-text-fill: $textColor; -fx-font-size: 68px; -fx-font-weight: bold; -fx-font-family: 'Verdana'; -fx-effect: dropshadow(one-pass-box, black, 5, 5, 0, 0);"
          },
          new Label("HAS CONQUERED THE BOARD") {
            style = "-fx-text-fill: #DAA520; -fx-font-size: 16px; -fx-font-letter-spacing: 2px; -fx-font-weight: bold;"
          },
          new HBox {
            alignment = Pos.Center
            padding = Insets(30, 0, 0, 0)
            children = Seq(
              new Button("PLAY AGAIN") {
                style =
                  """
                      -fx-background-color: linear-gradient(#2e7d32, #1b5e20);
                      -fx-text-fill: white;
                      -fx-font-size: 18px;
                      -fx-font-weight: bold;
                      -fx-padding: 12 30;
                      -fx-background-radius: 5;
                      -fx-border-color: #81c784;
                      -fx-border-width: 2;
                      -fx-cursor: hand;
                    """
                onAction = _ => {
                  de.htwg.controller.command.CommandHistory.clear()
                  inputHandler.submitInput("revanche")
                }
              },
              new Button("QUIT") {
                style =
                  """
                      -fx-background-color: linear-gradient(#4b3621, #2c1e12);
                      -fx-text-fill: #DAA520;
                      -fx-font-size: 18px;
                      -fx-font-weight: bold;
                      -fx-padding: 12 30;
                      -fx-background-radius: 5;
                      -fx-border-color: #DAA520;
                      -fx-border-width: 2;
                      -fx-cursor: hand;
                    """
                onAction = _ => sys.exit(0)
              }
            )
          }
        )
      }
      val stackPane = canvas.parent.value.asInstanceOf[javafx.scene.layout.StackPane]
      stackPane.getChildren.add(overlay)
      overlay.opacity = 0.0
      overlay.scaleX = 0.5
      overlay.scaleY = 0.5
      val fadeIn = new scalafx.animation.FadeTransition(scalafx.util.Duration(600), overlay) {
        toValue = 1.0
      }
      val scaleUp = new scalafx.animation.ScaleTransition(scalafx.util.Duration(600), overlay) {
        toX = 1.0
        toY = 1.0
      }
      new scalafx.animation.ParallelTransition(Seq(fadeIn, scaleUp)).play()
    }

    private def showRulesDialog(): Unit = {
      val alert = new Alert(AlertType.Information) {
        initOwner(stage)
        title = "*** Checkers Rules ***"
        headerText = "Game Rules:"
      }
      val content = new Label {
        text = "1. Movement: Diagonally forward.\n2. Kings: Move diagonally forward/backward.\n3. Jumps: Forced if available.\n4. Winning: Capture all opponent pieces."
        style = "-fx-font-size: 14px; -fx-padding: 10;"
      }
      alert.getDialogPane.setContent(content)
      alert.getDialogPane.setPrefWidth(500)
      alert.showAndWait()
    }
  }
}