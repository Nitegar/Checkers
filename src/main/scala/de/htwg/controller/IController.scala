package de.htwg.controller

trait IController {
  def setInputHandler(handler: InputHandler): Unit

  def add(observer: AnyRef): Unit

  def startGame(): Unit

  def getInputHandler: InputHandler

  def isTuiActive: Boolean
}
