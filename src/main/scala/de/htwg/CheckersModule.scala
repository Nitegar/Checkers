package de.htwg

import com.google.inject.AbstractModule
import de.htwg.controller.*
import de.htwg.controller.inputhandler.*
import de.htwg.controller.inputhandler.impl.{GuiInputHandler, MultiInputHandler, TuiInputHandler}

class CheckersModule(mode: String) extends AbstractModule {
  override def configure(): Unit = {
    // 1. Create the specific handler instance ONCE
    val handler: InputHandler = mode match {
      case "gui" => new GuiInputHandler()
      case "tui" => new TuiInputHandler()
      case "parallel" =>
        // Important: Parallel needs to share the same GUI handler
        // used by the view and the multi-handler
        val gui = new GuiInputHandler()
        new MultiInputHandler(gui, new TuiInputHandler())
      case _ => new GuiInputHandler()
    }

    // 2. Bind that specific instance as a Singleton
    // This ensures injector.getInstance and Constructor Injection return the same object
    bind(classOf[InputHandler]).toInstance(handler)
    bind(classOf[GameState]).toInstance(AwaitingInputState)

    // 3. Bind the Controller
    bind(classOf[IController]).to(classOf[GameController])
  }
}