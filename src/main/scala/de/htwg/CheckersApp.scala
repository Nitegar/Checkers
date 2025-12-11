package de.htwg

import de.htwg.controller.{GameController, GuiInputHandler, TuiInputHandler}
import de.htwg.view.gui.GuiView
import de.htwg.view.tui.TuiView

object CheckersApp {

  def main(args: Array[String]): Unit = {
    // Check if GUI or TUI mode is requested
    val useGui = args.headOption match {
      case Some("--tui") | Some("-t") => false
      case Some("--gui") | Some("-g") => true
      case _ =>
        // Default to GUI, or ask user
        println("Checkers Game")
        println("Choose interface:")
        println("1. GUI (Graphical)")
        println("2. TUI (Text-based)")
        print("Enter choice (1 or 2): ")
        scala.io.StdIn.readLine().trim == "1"
    }

    if (useGui) {
      launchGui()
    } else {
      launchTui()
    }
  }

  private def launchGui(): Unit = {
    println("Starting Checkers with GUI...")

    // Set up GUI mode
    GameController.setInputHandler(GuiInputHandler)
    GameController.add(GuiView)


    // Start the game immediately. This is necessary to fire the initial
    // from freezing the environment before the EDT is fully running.
    new Thread(() => {
      try {
        GameController.startGame()
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }).start()
  }

  private def launchTui(): Unit = {
    println("Starting Checkers with TUI...")

    // Set up TUI mode (default)
    GameController.setInputHandler(TuiInputHandler)
    GameController.add(TuiView)

    // Start the game
    GameController.startGame()
  }
}