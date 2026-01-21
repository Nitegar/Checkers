package de.htwg

import com.google.inject.Guice
import de.htwg.controller.IController
import de.htwg.controller.inputhandler.InputHandler
import de.htwg.view.gui.GuiViewFx
import de.htwg.view.tui.TuiView

object CheckersApp {
  def main(args: Array[String]): Unit = {
    val mode = args.headOption.getOrElse("--parallel")
    val injector = Guice.createInjector(new CheckersModule(mode))

    val controller = injector.getInstance(classOf[IController])
    val inputHandler = injector.getInstance(classOf[InputHandler])

    // 1. Start Logic Thread (The Boss)
    val logicThread = new Thread(() => controller.startGame())
    logicThread.setDaemon(true)
    logicThread.start()

    // 2. Start TUI Thread
    if (mode == "--parallel" || mode == "--tui") {
      val tui = new TuiView(inputHandler)
      controller.add(tui)
      val tuiThread = new Thread(() => tui.run())
      tuiThread.setDaemon(true)
      tuiThread.start()
    }

    // 3. Start GUI (This blocks the main thread)
    if (mode == "--parallel" || mode == "--gui") {
      val gui = new GuiViewFx(inputHandler)
      // We don't wrap this in a thread; we let it own the main thread
      gui.run(controller)
    }
    while (true) {
      Thread.sleep(1000)
    }
  }
}