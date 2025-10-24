package de.htwg.model

import org.scalatest.funsuite.AnyFunSuite

class PieceTest extends AnyFunSuite {

  test("Regular and King pieces store correct color state") {
    val redReg = Regular(true)
    val blackReg = Regular(false)
    val redKing = King(true)
    val blackKing = King(false)

    assert(redReg.isRed)
    assert(!blackReg.isRed)
    assert(redKing.isRed)
    assert(!blackKing.isRed)
  }

  test("Empty is a singleton and equals only itself") {
    assert(Empty == Empty)
    assert(Empty != Regular(true))
    assert(Empty != King(false))
  }
}
