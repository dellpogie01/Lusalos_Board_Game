package com.example.game

import org.junit.Assert.*
import org.junit.Test

class DemoMatchTest {
    @Test fun exhibitionUsesLegalTurnsAndAlwaysTerminates() {
        repeat(8) {
            val demo = DemoMatch()
            var steps = 0
            while (!demo.state.isGameOver() && steps < 400) {
                val before = demo.state
                val next = demo.advance()
                assertEquals(before.moveHistory.size + 1, next.moveHistory.size)
                assertEquals(next.whitePiecesOnBoard, next.board.count { it == Player.WHITE.value })
                assertEquals(next.blackPiecesOnBoard, next.board.count { it == Player.BLACK.value })
                assertTrue(next.whitePiecesInHand in 0..9)
                assertTrue(next.blackPiecesInHand in 0..9)
                steps++
            }
            assertTrue("Exhibition must finish so the UI can restart it", demo.state.isGameOver())
            assertEquals(0, DemoMatch().state.moveHistory.size)
        }
    }
}
