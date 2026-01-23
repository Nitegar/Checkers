package de.htwg

import com.google.inject.Guice
import de.htwg.controller.TestController
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

class CheckersAppSpec
  extends AnyWordSpec
    with Matchers
    with BeforeAndAfterEach {

  // These are re-created before each test
  private var testController: TestController = _
  private var testModule: TestCheckersModule = _

  // ----------------------------
  // Test lifecycle hooks
  // ----------------------------

  override def beforeEach(): Unit = {
    testController = new TestController
    testModule = new TestCheckersModule
    testModule.controller = testController

    CheckersApp.injectorFactory = _ =>
      Guice.createInjector(testModule)

    // IMPORTANT: do not block on threads in tests
    CheckersApp.blockOnThreads = false
  }

  override def afterEach(): Unit = {
    // Restore production defaults to avoid leakage
    CheckersApp.blockOnThreads = true
    CheckersApp.injectorFactory =
      mode => Guice.createInjector(new CheckersModule(mode))
  }

  // ----------------------------
  // StdIn / StdOut helpers
  // ----------------------------

  private def withStdIn(input: String)(test: => Unit): Unit = {
    val in = new ByteArrayInputStream(input.getBytes)
    Console.withIn(in)(test)
  }

  private def withStdOut(test: => Unit): String = {
    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out))(test)
    out.toString
  }

  // ----------------------------
  // Tests
  // ----------------------------

  "CheckersApp.main" should {

    "start GUI mode via argument" in {
      CheckersApp.main(Array("--gui"))
      testController.addedObservers.size shouldBe 1
    }

    "start TUI mode via argument" in {
      CheckersApp.main(Array("--tui"))
      testController.addedObservers.size shouldBe 1
    }

    "start parallel mode via argument" in {
      CheckersApp.main(Array("--parallel"))
      testController.addedObservers.size shouldBe 2
    }
  }
}
