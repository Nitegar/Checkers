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

    var tuiThread: Option[Thread] = None
    var guiThread: Option[Thread] = None

    // 2. Start TUI Thread
    if (mode == "--parallel" || mode == "--tui") {
      val tui = new TuiView(inputHandler)
      controller.add(tui)
      val t = new Thread(() => tui.run(), "tui-thread")
      t.start()
      tuiThread = Some(t)
    }

    // 3. Start GUI (This blocks the main thread)
    if (mode == "--parallel" || mode == "--gui") {
      val gui = new GuiViewFx(inputHandler)
      val t = new Thread(() => gui.run(controller), "gui-thread")
      t.start()
      guiThread = Some(t)
    }

    tuiThread.foreach(_.join())
    guiThread.foreach(_.join())
  }
}