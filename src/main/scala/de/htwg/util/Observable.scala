package de.htwg.util

trait Observable[T] {
  private var observers: List[Observer[T]] = Nil

  def add(observer: Observer[T]): Unit = observers = observer :: observers

  def remove(observer: Observer[T]): Unit = observers = observers.filter(_ != observer)

  def notifyObservers(event: T): Unit = observers.foreach(_.update(event))
}