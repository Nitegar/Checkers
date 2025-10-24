package de.htwg

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.{Future, Await, TimeoutException}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class CheckersAppTest extends AnyWordSpec with Matchers {

  // TODO add more complex tests
  "CheckersApp" should {
    "block indefinitely on user input" in {
      val future = Future {
        CheckersApp.main(Array.empty)
      }

      intercept[TimeoutException] {
        Await.result(future, 1.second)
      }

      future.isCompleted shouldBe false
    }
  }
}