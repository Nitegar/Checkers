package de.htwg.util

trait Observer[T] {
  def update(event: T): Unit
}
