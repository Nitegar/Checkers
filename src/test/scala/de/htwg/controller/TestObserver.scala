package de.htwg.controller

import de.htwg.util.Observer
import scala.collection.mutable.ListBuffer

class TestObserver extends Observer[GameEvent] {
  val events = ListBuffer[GameEvent]()

  override def update(event: GameEvent): Unit =
    events += event
}
