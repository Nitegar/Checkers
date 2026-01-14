package de.htwg.controller.inputhandler

import de.htwg.model.GameSession

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TuiInputHandler extends InputHandler {
    private var gameSession: GameSession = _

    override def attachSession(session: GameSession): Unit =
        gameSession = session
    
    override def requestInput(): Future[String] = Future {
        val turnAtStart = gameSession.turnCount
        val inputBuilder = new StringBuilder()
        var inputFound = false

        // 2. POLL: Wait for input OR for the turn to change (GUI move)
        while (!inputFound && gameSession.turnCount == turnAtStart) {
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
        if (gameSession.turnCount != turnAtStart) {
            "" // Return empty, MultiInputHandler will ignore this anyway
        } else {
            inputBuilder.toString().trim.toLowerCase
        }
    }
}