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
import java.util.concurrent.Executors
import scala.compiletime.uninitialized

class GuiView(inputHandler: InputHandler) extends Observer[GameEvent] {
  private var registered = false
  
  def run(controller: IController): Unit = {
    CheckersGuiApp.inputHandler = inputHandler
    CheckersGuiApp.controller = controller
    CheckersGuiApp.guiInstance = this
    CheckersGuiApp.main(Array())
  }

  override def update(event: GameEvent): Unit = Platform.runLater {
    CheckersGuiApp.updateView(event)
  }
}

object CheckersGuiApp extends JFXApp3 {
  private lazy val moveExecutor = Executors.newFixedThreadPool(1, (r: Runnable) => {
    val t = new Thread(r)
    t.setDaemon(true) // This ensures the thread dies when the app closes
    t
  })
  var inputHandler: InputHandler = uninitialized
  var controller: IController = uninitialized
  var guiInstance: GuiView = uninitialized
  private var canvas: Canvas = uninitialized
  private var redPointsLabel: Label = uninitialized
  private var blackPointsLabel: Label = uninitialized
  private var turnLabel: Label = uninitialized
  var currentBoard: Option[de.htwg.model.Board.Board] = None
  private var selectedSquare: Option[(Int, Int)] = None

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
      minWidth = 140;
      minHeight = 140
      style = "-fx-background-color: linear-gradient(to bottom right, #cc0000, #800000); -fx-background-radius: 20; -fx-border-color: #660000; -fx-border-width: 4; -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 5, 5);"
      children = Seq(new Label("RED") {
        style = "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;"
      }, redPointsLabel)
    }

    val blackScoreCard = new VBox {
      alignment = Pos.Center
      minWidth = 140;
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
      // Side panel semi-transparent to show grass green background
      style = "-fx-background-color: rgba(220, 220, 220, 0.7); -fx-border-color: #aaaaaa; -fx-border-width: 0 0 0 2;"
      children = Seq(new Label("SCOREBOARD") {
        style = "-fx-font-size: 20px; -fx-font-weight: bold;"
      }, redScoreCard, blackScoreCard, turnLabel)
    }

    val bottomControls = new HBox {
      alignment = Pos.Center
      spacing = 25;
      padding = Insets(15)
      style = "-fx-background-color: #f4f4f4;"
      children = Seq(
        new Button("↩ Undo") {
          style = "-fx-font-size: 14px;";
          onAction = _ => inputHandler.submitInput("undo")
        },
        new Button("↪ Redo") {
          style = "-fx-font-size: 14px;";
          onAction = _ => inputHandler.submitInput("redo")
        },
        new Button("ℹ Rules") {
          style = "-fx-font-size: 14px;";
          onAction = _ => showRulesDialog()
        },
        new Button("❌ Quit") {
          style = "-fx-font-size: 14px;";
          onAction = _ => sys.exit(0)
        }
      )
    }

    val centerPane = new StackPane {
      style = "-fx-background-color: #2e7d32;" // Grass Green Background
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
    currentBoard = Some(controller.getBoard)
    setupInput()
    updateScore(controller.getBoard)
    draw()
  }

  def updateView(event: GameEvent): Unit = event match {
    case BoardUpdated(board, _) =>
      currentBoard = Some(board)
      if (redPointsLabel != null) updateScore(board)
      draw()
    case _ =>
  }

  private def updateScore(board: de.htwg.model.Board.Board): Unit = {
    var rLeft = 0;
    var bLeft = 0
    for (row <- board; piece <- row) {
      piece match {
        case Regular(true) | King(true) => rLeft += 1
        case Regular(false) | King(false) => bLeft += 1
        case _ =>
      }
    }
    redPointsLabel.text = s"${12 - bLeft}"
    blackPointsLabel.text = s"${12 - rLeft}"
    val isRed = controller.isRedTurn
    turnLabel.text = if (isRed) "TURN: RED" else "TURN: BLACK"
    turnLabel.style = s"-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: ${if (isRed) "#990000" else "#323232"};"
  }

  private def draw(): Unit = {
    if (canvas == null || canvas.width.value <= 0) return
    val gc = canvas.graphicsContext2D
    val size = Math.min(canvas.width.value, canvas.height.value) * 0.9
    val tileSize = size / 8
    val offsetX = (canvas.width.value - size) / 2
    val offsetY = (canvas.height.value - size) / 2
    val redTurn = controller.isRedTurn

    gc.clearRect(0, 0, canvas.width.value, canvas.height.value)

    // 1. 3D Board Frame
    gc.fill = Color.web("#5d3a1a") // Darker Wood Frame
    gc.fillRoundRect(offsetX - 10, offsetY - 10, size + 20, size + 20, 10, 10)

    // 2. Main Board Squares
    for (row <- 0 until 8; col <- 0 until 8) {
      val x = offsetX + col * tileSize
      val y = offsetY + row * tileSize
      gc.fill = if ((row + col) % 2 == 0) Color.web("#ebcd99") else Color.web("#966333")
      gc.fillRect(x, y, tileSize, tileSize)

      // Bevel effect for 3D look
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

    // 4. Pieces (RE-IMPLEMENTED 3D DESIGN)
    for (row <- 0 until 8; col <- 0 until 8) {
      currentBoard.foreach { b =>
        b(row)(col) match {
          case p: Piece if p != Empty =>
            val isRed = p match {
              case Regular(r) => r;
              case King(r) => r;
              case _ => false
            }
            val x = offsetX + col * tileSize
            val y = offsetY + row * tileSize

            // Glow logic
            if (isRed == redTurn) {
              val moves = GameLogic.getValidMoves(b, row, col)
              if (if (GameLogic.hasJumpsAvailable(b, redTurn)) moves.exists(_._3) else moves.nonEmpty) {
                gc.setStroke(Color.web("#FFFFFF", 0.9));
                gc.setLineWidth(4)
                gc.strokeRoundRect(x + 4, y + 4, tileSize - 8, tileSize - 8, 12, 12)
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
            gc.setStroke(baseColor.darker);
            gc.setLineWidth(1.5)
            gc.strokeOval(x + tileSize * 0.25, y + tileSize * 0.25, tileSize * 0.5, tileSize * 0.5)

            if (p.isInstanceOf[King]) {
              gc.fill = Color.Gold
              gc.font = scalafx.scene.text.Font.font("Serif", scalafx.scene.text.FontWeight.Bold, tileSize * 0.45)
              gc.fillText("♔", x + tileSize * 0.32, y + tileSize * 0.65)
            }
          case _ =>
        }
      }
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
            // First click: Just select the piece
            selectedSquare = Some((row, col))
            draw()
          case Some((sR, sC)) =>
            if (sR == row && sC == col) {
              selectedSquare = None
              draw()
            } else {
              // Second click is a different square: Attempt move in background
              val moveCommand = s"${('a' + sC).toChar}${sR + 1} ${('a' + col).toChar}${row + 1}"

              moveExecutor.submit(new Runnable {
                override def run(): Unit = {
                  try {
                    inputHandler.submitInput(moveCommand)
                  } catch {
                    case ex: Exception => println(s"Controller Error: ${ex.getMessage}")
                  }
                }
              })

              selectedSquare = None
              draw()
            }
        }
      }
    }
  }

  private def showRulesDialog(): Unit = {
    val alert = new Alert(AlertType.Information) {
      initOwner(stage);
      title = "Checkers Rules";
      headerText = "Game Rules"
    }
    val content = new Label {
      text = "1. Movement: Diagonally forward.\n2. Kings: Move diagonally forward/backward.\n3. Jumps: Forced if available.\n4. Winning: Capture all opponent pieces."
      style = "-fx-font-size: 14px; -fx-padding: 10;"
    }
    alert.getDialogPane.setContent(content);
    alert.getDialogPane.setPrefWidth(500);
    alert.showAndWait()
  }
}