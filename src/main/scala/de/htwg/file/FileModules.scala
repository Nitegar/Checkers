package de.htwg.file

import com.google.inject.AbstractModule
import de.htwg.file.controller.FileIO
import de.htwg.file.controller.impl.{FileIOJson, FileIOXml}

class JsonModule extends AbstractModule {
  override def configure(): Unit = bind(classOf[FileIO]).to(classOf[FileIOJson])
}

class XmlModule extends AbstractModule {
  override def configure(): Unit = bind(classOf[FileIO]).to(classOf[FileIOXml])
}