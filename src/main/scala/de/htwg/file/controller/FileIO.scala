package de.htwg.file.controller

import de.htwg.file.model.Book

trait FileIO {
  def save(book: Book): Unit
  def load(): Book
}