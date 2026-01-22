package de.htwg

import com.google.inject.AbstractModule
import de.htwg.controller.inputhandler.{InputHandler, TestInputHandler}
import de.htwg.controller.{IController, TestController}

class TestCheckersModule extends AbstractModule {
  var controller: IController = new TestController

  override def configure(): Unit = {
    bind(classOf[IController]).toInstance(controller)
    bind(classOf[InputHandler]).toInstance(new TestInputHandler)
  }
}
