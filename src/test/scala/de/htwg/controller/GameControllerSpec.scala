package de.htwg.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.controller.inputhandler.TestInputHandler

class GameControllerSpec extends AnyWordSpec with Matchers {

  "GameController" should {

    "publish StartGame and RequestInput events on startGame() with TestGameState" in {
      val inputHandler = new TestInputHandler
      val controller = new GameController(inputHandler, ChangeBoardState)

      val observer = new TestObserver
      controller.add(observer)

      // Inject test state to avoid infinite loop
      controller.startGame()

      observer.events.collect { case _: StartGame => true }.nonEmpty shouldBe true
      observer.events.collect { case _: RequestInput => true }.nonEmpty shouldBe true

      inputHandler.sessionAttached shouldBe true
    }

    "publish StartGame and RequestInput events on startGame() with GameOverState" in {
      val inputHandler = new TestInputHandler
      val controller = new GameController(inputHandler, DoNothingState)

      val observer = new TestObserver
      controller.add(observer)

      controller.startGame()

      inputHandler.sessionAttached shouldBe true
    }
  }
}
