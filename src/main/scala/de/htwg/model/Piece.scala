package de.htwg.model

sealed trait Piece
case object Empty extends Piece
case class Regular(isRed: Boolean) extends Piece
case class King(isRed: Boolean) extends Piece