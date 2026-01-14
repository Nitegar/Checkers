package de.htwg

import de.htwg.controller.inputhandler.{GuiInputHandler, MultiInputHandler, TuiInputHandler}
import de.htwg.controller.{GameController, IController}
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
    
    val controller: IController = new GameController(GuiInputHandler)
    controller.add(GuiView)

    new Thread(() => {
      try {
        controller.startGame()
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

    val controller: IController = new GameController(new TuiInputHandler)
    controller.add(TuiView)

    controller.startGame()
  }

  /**
   * Configures the application for parallel mode:
   * GUI handles input and display. TUI only handles display (logging output).
   */
  private def launchParallel(): Unit = {
    println("Starting Checkers in Parallel Mode (GUI Input, GUI & TUI Display)...")

    val controller: IController = new GameController(new MultiInputHandler(GuiInputHandler, new TuiInputHandler))

    controller.add(GuiView)
    controller.add(TuiView)

    new Thread(() => {
      try {
        controller.startGame()
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }).start()
  }
}