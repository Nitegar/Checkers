package de.htwg

import de.htwg.controller.{GameController, GuiInputHandler, MultiInputHandler, TuiInputHandler}
import de.htwg.view.gui.GuiView
import de.htwg.view.tui.TuiView

object CheckersApp {

  def main(args: Array[String]): Unit = {
    // Check for specific launch modes
    args.headOption match {
      case Some("--parallel") | Some("-p") =>
        launchParallel()
      case Some("--gui") | Some("-g") =>
        launchGui()
      case Some("--tui") | Some("-t") =>
        launchTui()
      case _ =>
        // Interactive choice if no arguments are provided
        println("Checkers Game")
        println("Choose interface:")
        println("1. GUI (Graphical)")
        println("2. TUI (Text-based)")
        println("3. Parallel (GUI for input, TUI for logging/display)")
        print("Enter choice (1, 2, or 3): ")
        scala.io.StdIn.readLine().trim match {
          case "1" => launchGui()
          case "2" => launchTui()
          case "3" => launchParallel()
          case _ => println("Invalid choice, defaulting to GUI.")
            launchGui()
        }
    }
  }

  /**
   * Configures the application for GUI input and display only.
   */
  private def launchGui(): Unit = {
    println("Starting Checkers with GUI (Input & Display)...")

    // Set up GUI mode: GUI handles input
    GameController.setInputHandler(GuiInputHandler)
    GameController.add(GuiView)

    // Start the game asynchronously to prevent freezing the environment
    // before the EDT is fully running.
    new Thread(() => {
      try {
        GameController.startGame()
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }).start()
  }

  /**
   * Configures the application for TUI input and display only.
   */
  private def launchTui(): Unit = {
    println("Starting Checkers with TUI (Input & Display)...")

    // Set up TUI mode: TUI handles input
    GameController.setInputHandler(TuiInputHandler)
    GameController.add(TuiView)

    // Start the game synchronously
    GameController.startGame()
  }

  /**
   * Configures the application for parallel mode:
   * GUI handles input and display. TUI only handles display (logging output).
   */
  private def launchParallel(): Unit = {
    println("Starting Checkers in Parallel Mode (GUI Input, GUI & TUI Display)...")

    // 1. Set Input Handler: Prioritize GUI as the primary source for interactive input
    GameController.setInputHandler(
      new MultiInputHandler(GuiInputHandler, TuiInputHandler)
    )

    GameController.add(GuiView)
    GameController.add(TuiView)

    // 3. Start Game: Must be asynchronous to allow the GUI window to render
    // and prevent the environment from freezing.
    new Thread(() => {
      try {
        GameController.startGame()
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }).start()
  }
}