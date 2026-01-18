package de.htwg.file.controller.impl

import de.htwg.file.controller.FileIO
import de.htwg.file.model.Book
import de.htwg.model.Board.Board
import de.htwg.model.{King, Piece, Regular}
import upickle.default.*

import java.io.*

class FileIOJson extends FileIO {
  val path = "data/book.json"

  override def save(book: Book): Unit = {
    val json = s"""{
      "title": "${book.title}",
      "author": "${book.author}",
      "year": ${book.year}
    }"""
    val pw = new PrintWriter(new File(path))
    pw.write(json)
    pw.close()
  }

  override def load(): Book = {
    val source = scala.io.Source.fromFile(path)
    val content = source.getLines().mkString
    source.close()

    // Simple regex to pull values out without a heavy library
    val title = """(?s)"title":\s*"([^"]*)"""".r.findFirstMatchIn(content).get.group(1)
    val author = """(?s)"author":\s*"([^"]*)"""".r.findFirstMatchIn(content).get.group(1)
    val year = """(?s)"year":\s*(\d+)""".r.findFirstMatchIn(content).get.group(1).toInt

    Book(title, author, year)
  }
}