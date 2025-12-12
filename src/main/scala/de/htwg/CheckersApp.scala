package de.htwg

import de.htwg.controller.{GameController, GuiInputHandler, TuiInputHandler}
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
   * Both GUI and TUI can provide input. Both GUI and TUI display updates.
   */
  private def launchParallel(): Unit = {
    println("Starting Checkers in Parallel Mode (GUI & TUI Input, GUI & TUI Display)...")

    // 1. Set Input Handler: Use GUI as the primary mechanism driver.
    // The GameController's internal loop will call GuiInputHandler.requestInput(), 
    // which usually waits on an internal queue (fed by GUI clicks).
    GameController.setInputHandler(GuiInputHandler)

    // 2. Add BOTH Views as Observers: Both will receive updates from the Controller
    GameController.add(GuiView)
    GameController.add(TuiView)

    // 3. Start a dedicated TUI input thread.
    // This thread will continuously read from the console (TUI is blocking)
    // and submit the input directly to the GameController's input queue via setInput.
    new Thread(() => {
      try {
        // Run forever, listening for console input
        while (true) {
          // Blocking call to read console input
          val input = scala.io.StdIn.readLine()

          // Submit the input directly to the GameController's queue.
          // This allows TUI commands to be processed in parallel with GUI clicks.
          GameController.setInput(input)
        }
      } catch {
        case e: Exception =>
          // Log interruption or error if the thread stops unexpectedly
          println(s"TUI input listener stopped: ${e.getMessage}")
      }
    }).start()

    // 4. Start Game: Must be asynchronous to allow the GUI window to render 
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