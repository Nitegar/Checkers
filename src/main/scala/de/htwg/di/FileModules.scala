package de.htwg.di

import com.google.inject.AbstractModule
import de.htwg.model.file.{FileIO, FileIOJson, FileIOXml}

class JsonModule extends AbstractModule {
  override def configure(): Unit = bind(classOf[FileIO]).to(classOf[FileIOJson])
}

class XmlModule extends AbstractModule {
  override def configure(): Unit = bind(classOf[FileIO]).to(classOf[FileIOXml])
}