package de.htwg.controller

import de.htwg.util.Observer

import scala.collection.mutable.ListBuffer

class TestController extends IController {
  val addedObservers = ListBuffer[Observer[GameEvent]]()

  override def add(observer: Observer[GameEvent]): Unit = {
    addedObservers += observer
    super.add(observer) // optional, but keeps behavior correct
  }

  override def startGame(): Unit = ()
}
