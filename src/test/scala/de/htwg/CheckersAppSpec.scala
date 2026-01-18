package de.htwg

import com.google.inject.Guice
import de.htwg.controller.TestController
import de.htwg.di.TestCheckersModule
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

class CheckersAppSpec extends AnyWordSpec with Matchers {
  val testCheckersModule = new TestCheckersModule

  def withStdIn(input: String)(test: => Unit): Unit = {
    val in = new ByteArrayInputStream(input.getBytes)
    Console.withIn(in)(test)
  }

  def withStdOut(test: => Unit): String = {
    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out))(test)
    out.toString
  }

  "CheckersApp.main" should {

    "start GUI mode via argument" in {
      val testController = new TestController
      testCheckersModule.controller = testController
      CheckersApp.injectorFactory = _ =>
        Guice.createInjector(testCheckersModule)

      CheckersApp.main(Array("--gui"))
      Thread.sleep(50)

      testController.addedObservers.size shouldBe 1
    }

    "start TUI mode via argument" in {
      val testController = new TestController
      testCheckersModule.controller = testController
      CheckersApp.injectorFactory = _ =>
        Guice.createInjector(testCheckersModule)

      CheckersApp.main(Array("--tui"))

      testController.addedObservers.size shouldBe 1
    }

    "start parallel mode via argument" in {
      val testController = new TestController
      testCheckersModule.controller = testController
      CheckersApp.injectorFactory = _ =>
        Guice.createInjector(testCheckersModule)

      CheckersApp.main(Array("--parallel"))
      Thread.sleep(50)

      testController.addedObservers.size shouldBe 2
    }

    "use menu input when no arguments provided" in {
      val testController = new TestController
      testCheckersModule.controller = testController
      CheckersApp.injectorFactory = _ =>
        Guice.createInjector(testCheckersModule)

      withStdIn("2\n") {
        val output = withStdOut {
          CheckersApp.main(Array.empty)
        }
        output should include("Checkers Game")
      }
    }

    "default to GUI on invalid menu input" in {
      val testController = new TestController
      testCheckersModule.controller = testController
      CheckersApp.injectorFactory = _ =>
        Guice.createInjector(testCheckersModule)

      withStdIn("invalid\n") {
        CheckersApp.main(Array.empty)
      }

      testController.addedObservers.size shouldBe 1
    }
  }
}
