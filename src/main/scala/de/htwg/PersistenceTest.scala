package de.htwg

import com.google.inject.Guice
import de.htwg.di.{JsonModule, XmlModule}
import de.htwg.model.file.{Book, FileIO}

object PersistenceTest extends App {
  // --- STEP 1: Choose Module (Swap 'XmlModule' for 'JsonModule' here) ---
  val injector = Guice.createInjector(new XmlModule())
  val fileIo = injector.getInstance(classOf[FileIO])

  println(s"Testing with: ${fileIo.getClass.getSimpleName}")

  // --- STEP 2: Save ---
  val myBook = Book("The Scala Guide", "Martin Odersky", 2024)
  fileIo.save(myBook)
  println(s"Saved: $myBook")

  // --- STEP 3: Load and Print ---
  val loadedBook = fileIo.load()
  println(s"Loaded from file: $loadedBook")

  if (myBook == loadedBook) println("SUCCESS: Data matches!")
}