package de.htwg.controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn

object TuiInputHandler extends InputHandler {
    override def requestInput(): Future[String] = Future {
        val turnAtStart = GameController.getCurrentTurn
        val inputBuilder = new StringBuilder()
        var inputFound = false

        // 1. CLEAR: Wipe the buffer of old keystrokes from previous turns
        while (System.in.available() > 0) { System.in.read() }

        // 2. POLL: Wait for input OR for the turn to change (GUI move)
        while (!inputFound && GameController.getCurrentTurn == turnAtStart) {
            if (System.in.available() > 0) {
                val char = System.in.read().toChar
                if (char == '\n' || char == '\r') {
                    inputFound = true
                } else {
                    inputBuilder.append(char)
                }
            } else {
                Thread.sleep(20) // Don't peg the CPU
            }
        }

        // 3. RESULT: If the turn changed while we were typing, return a dummy
        if (GameController.getCurrentTurn != turnAtStart) {
            "" // Return empty, MultiInputHandler will ignore this anyway
        } else {
            inputBuilder.toString().trim.toLowerCase
        }
    }
}