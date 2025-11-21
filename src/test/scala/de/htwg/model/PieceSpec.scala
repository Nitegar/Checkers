package de.htwg.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PieceSpec extends AnyWordSpec with Matchers {

  "A Regular piece" when {

    "created as red" should {

      "store the red color state correctly" in {
        val redReg = Regular(true)
        redReg.isRed shouldBe true
      }
    }

    "created as black" should {

      "store the black color state correctly" in {
        val blackReg = Regular(false)
        blackReg.isRed shouldBe false
      }
    }
  }

  "A King piece" when {

    "created as red" should {

      "store the red color state correctly" in {
        val redKing = King(true)
        redKing.isRed shouldBe true
      }
    }

    "created as black" should {

      "store the black color state correctly" in {
        val blackKing = King(false)
        blackKing.isRed shouldBe false
      }
    }
  }

  "Empty" should {

    "equal itself as a singleton" in {
      Empty shouldBe Empty
    }

    "not equal Regular pieces" in {
      Empty should not be Regular(true)
      Empty should not be Regular(false)
    }

    "not equal King pieces" in {
      Empty should not be King(true)
      Empty should not be King(false)
    }
  }
}