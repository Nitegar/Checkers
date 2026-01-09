package de.htwg

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.controller.{GameController, GuiInputHandler, TuiInputHandler, MultiInputHandler}

class CheckersAppSpec extends AnyWordSpec with Matchers {

  "The CheckersApp" when {

    "started with --tui or -t" should {
      "configure the GameController for TUI mode" in {
        CheckersApp.main(Array("--tui"))

        GameController.getInputHandler should be (TuiInputHandler)
      }
    }

    "started with --gui or -g" should {
      "configure the GameController for GUI mode" in {
        CheckersApp.main(Array("--gui"))

        // Wait briefly for the thread to trigger setInputHandler if necessary
        // Though in your code, it's set before the thread starts
        GameController.getInputHandler should be (GuiInputHandler)
      }
    }

    "started with --parallel or -p" should {
      "configure a MultiInputHandler with both TUI and GUI handlers" in {
        CheckersApp.main(Array("--parallel"))

        val handler = GameController.getInputHandler
        handler shouldBe a [MultiInputHandler]
      }
    }

    "started with invalid arguments" should {
      "default to GUI mode" in {
        CheckersApp.main(Array("--unknown"))

        GameController.getInputHandler should be (GuiInputHandler)
      }
    }
  }
}