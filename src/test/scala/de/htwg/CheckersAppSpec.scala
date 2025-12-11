package de.htwg

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.controller.GameController

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

class CheckersAppSpec extends AnyWordSpec with Matchers {

  private def captureOutput(input: String = "")(block: => Unit): String = {
    val outStream = new ByteArrayOutputStream()
    val inStream = new ByteArrayInputStream(input.getBytes)
    Console.withOut(new PrintStream(outStream)) {
      Console.withIn(inStream) {
        block
      }
    }
    outStream.toString
  }

  "CheckersApp" when {

    "the main method is called" should {

      "start the game and display welcome message" in {
        val output = captureOutput("\nq\n") {
          GuiApp.main(Array.empty)
        }
        output should include("WELCOME TO CHECKERS")
      }

      "initialize the game controller successfully" in {
        val output = captureOutput("\nq\n") {
          GuiApp.main(Array.empty)
        }
        output should include("RED")
      }
    }

    "executed with command line arguments" should {

      "ignore arguments and start game normally" in {
        val output = captureOutput("\nq\n") {
          GuiApp.main(Array("arg1", "arg2", "arg3"))
        }
        output should include("WELCOME TO CHECKERS")
      }
    }

    "the application starts" should {

      "delegate to GameController.startGame()" in {
        // This test verifies the integration between CheckersApp and GameController
        val output = captureOutput("\nq\n") {
          GuiApp.main(Array.empty)
        }
        // Should show game elements that only GameController.startGame() produces
        output should include("CHECKERS")
        output should not be empty
      }
    }
  }
}