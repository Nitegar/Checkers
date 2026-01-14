package de.htwg.model.file

import de.htwg.model.Board.Board
import de.htwg.model.{Empty, King, Piece, Regular}

import scala.xml.{Node, PrettyPrinter}
import java.io.*

class FileIOXml extends FileIO {
  val path = "data/book.xml"

  override def save(book: Book): Unit = {
    val xml = <book>
      <title>{book.title}</title>
      <author>{book.author}</author>
      <year>{book.year}</year>
    </book>

    val pw = new PrintWriter(new File(path))
    pw.write(new PrettyPrinter(80, 2).format(xml))
    pw.close()
  }

  override def load(): Book = {
    val file = scala.xml.XML.loadFile(path)
    Book(
      (file \ "title").text,
      (file \ "author").text,
      (file \ "year").text.toInt
    )
  }
}