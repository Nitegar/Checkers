package de.htwg.model.file

trait FileIO {
  def save(book: Book): Unit
  def load(): Book
}