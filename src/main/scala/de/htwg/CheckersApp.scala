package de.htwg

import com.google.inject.Guice
import de.htwg.controller.IController
import de.htwg.controller.inputhandler.InputHandler
import de.htwg.view.gui.GuiView
import de.htwg.view.tui.TuiView

object CheckersApp {
  var injectorFactory: String => com.google.inject.Injector =
    mode => Guice.createInjector(new CheckersModule(mode))

  var blockOnThreads: Boolean = true // 👈 ADD THIS

  def main(args: Array[String]): Unit = {
    val mode = args.headOption.getOrElse("--parallel")
    val injector = injectorFactory(mode)

    val controller = injector.getInstance(classOf[IController])
    val inputHandler = injector.getInstance(classOf[InputHandler])

    val logicThread = new Thread(() => controller.startGame())
    logicThread.setDaemon(true)
    logicThread.start()

    var tuiThread: Option[Thread] = None
    var guiThread: Option[Thread] = None

    if (mode == "--parallel" || mode == "--tui") {
      val tui = new TuiView(inputHandler)
      controller.add(tui)
      val t = new Thread(() => tui.run(), "tui-thread")
      t.start()
      tuiThread = Some(t)
    }

    if (mode == "--parallel" || mode == "--gui") {
      val gui = new GuiView(inputHandler)
      controller.add(gui)
      val t = new Thread(() => gui.run(), "gui-thread")
      t.start()
      guiThread = Some(t)
    }

    if (blockOnThreads) {
      tuiThread.foreach(_.join())
      guiThread.foreach(_.join())
    }
  }
}