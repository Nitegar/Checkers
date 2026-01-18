package de.htwg

import com.google.inject.Guice
import de.htwg.controller.IController
import de.htwg.view.gui.GuiView
import de.htwg.view.tui.TuiView
import de.htwg.controller.inputhandler.InputHandler

object CheckersApp {
  var injectorFactory: String => com.google.inject.Injector =
    mode => Guice.createInjector(new CheckersModule(mode))

  def main(args: Array[String]): Unit = {
    val mode = args.headOption match {
      case Some("--parallel") | Some("-p") => "parallel"
      case Some("--gui") | Some("-g")      => "gui"
      case Some("--tui") | Some("-t")      => "tui"
      case _ =>
        println("Checkers Game\n1. GUI\n2. TUI\n3. Parallel")
        scala.io.StdIn.readLine().trim match {
          case "1" => "gui"
          case "2" => "tui"
          case "3" => "parallel"
          case _   => "gui"
        }
    }

    launch(mode)
  }

  private def launch(mode: String): Unit = {
    // 1. Initialize Guice
    val injector = Guice.createInjector(new CheckersModule(mode))

    // 2. Get injected instances
    val controller = injector.getInstance(classOf[IController])
    val inputHandler = injector.getInstance(classOf[InputHandler])

    // 3. Attach Views based on mode
    mode match {
      case "gui" =>
        controller.add(new GuiView(inputHandler))
      case "tui" =>
        controller.add(new TuiView)
      case "parallel" =>
        controller.add(new GuiView(inputHandler))
        controller.add(new TuiView)
    }

    // 4. Run Game
    if (mode == "tui") {
      controller.startGame()
    } else {
      new Thread(() => controller.startGame()).start()
    }
  }
}