package de.htwg.controller.inputhandler

import de.htwg.controller.inputhandler.impl.{GuiInputHandler, MultiInputHandler, TuiInputHandler}
import de.htwg.model.GameSession
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.ByteArrayInputStream
import scala.concurrent.Await
import scala.concurrent.duration.*

class InputHandlerSpec extends AnyWordSpec with Matchers {

  "A GuiInputHandler" should {
    "return a future that completes when submitInput is called" in {
      val handler = new GuiInputHandler()
      val futureInput = handler.requestInput()

      futureInput.isCompleted shouldBe false

      handler.submitInput("a3 b4")

      Await.result(futureInput, 100.millis) shouldBe "a3 b4"
    }
  }

  "A TuiInputHandler" should {
    "read input from System.in when available" in {
      val session = GameSession()
      val handler = new TuiInputHandler()
      handler.attachSession(session)

      val testInput = "e6 f5\n"
      val in = new ByteArrayInputStream(testInput.getBytes)

      System.setIn(in)
      val future = handler.requestInput()

      Await.result(future, 1.second) shouldBe "e6 f5"
    }

    "return an empty string if the turn changes while waiting" in {
      val session = GameSession()
      val handler = new TuiInputHandler()
      handler.attachSession(session)

      val future = handler.requestInput()
      Thread.sleep(20)
      session.incrementTurn()

      Await.result(future, 1.second) shouldBe ""
    }
  }

  "A MultiInputHandler" should {
    "relay the session to all sub-handlers" in {
      val gui = new GuiInputHandler()
      val multi = new MultiInputHandler(gui)
      val session = GameSession()

      multi.attachSession(session)
      // Implicitly check if gui has session if your GuiInputHandler used it
    }

    "complete the future with the first non-empty input that arrives" in {
      val session = GameSession()
      val gui = new GuiInputHandler()
      val tui = new TuiInputHandler()
      val multi = new MultiInputHandler(gui, tui)
      multi.attachSession(session)

      val future = multi.requestInput()

      multi.submitInput("b2 c3")

      Await.result(future, 200.millis) shouldBe "b2 c3"
    }

    "ignore inputs if the turn has already changed" in {
      val session = GameSession()
      val gui = new GuiInputHandler()
      val multi = new MultiInputHandler(gui)
      multi.attachSession(session)

      val future = multi.requestInput()

      // Turn increments BEFORE input arrives
      session.incrementTurn()
      gui.submitInput("a3 b4")

      // The promise should not be fulfilled by this input
      intercept[java.util.concurrent.TimeoutException] {
        Await.result(future, 200.millis)
      }
    }
  }
}