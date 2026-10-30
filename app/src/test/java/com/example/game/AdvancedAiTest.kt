package com.example.game

import com.example.ai.LusalosAi
import org.junit.Assert.*
import org.junit.Test

class AdvancedAiTest {
    @Test fun advancedLevelsBlockAnImmediateMill() {
        val board = IntArray(24).apply { this[0] = 1; this[1] = 1; this[8] = 2 }
        val state = GameState(board = board, currentTurn = Player.BLACK,
            whitePiecesInHand = 7, blackPiecesInHand = 8, whitePiecesOnBoard = 2, blackPiecesOnBoard = 1)
        for (level in listOf(AiDifficulty.VERY_HARD, AiDifficulty.HARDEST)) {
            assertEquals("$level should block white's mill", 2, LusalosAi.computeMove(state, level).placeAt)
        }
    }

    @Test fun advancedLevelsPlanTheCaptureThatWins() {
        val board = IntArray(24).apply {
            listOf(0, 1, 3).forEach { this[it] = 1 }
            listOf(8, 10, 12).forEach { this[it] = 2 }
        }
        val state = GameState(board = board, whitePiecesInHand = 0, blackPiecesInHand = 0,
            whitePiecesOnBoard = 3, blackPiecesOnBoard = 3, phase = GamePhase.FLYING, action = TurnAction.SELECT_OR_MOVE)
        for (level in listOf(AiDifficulty.VERY_HARD, AiDifficulty.HARDEST)) {
            val move = LusalosAi.computeMove(state, level)
            assertEquals(3, move.moveFrom)
            assertEquals(2, move.moveTo)
            val mill = LusalosEngine.movePiece(state, move.moveFrom!!, move.moveTo!!)
            val capture = LusalosAi.computeMove(mill, level).removeAt!!
            assertTrue(LusalosEngine.canRemovePiece(mill, capture))
            assertEquals(Player.WHITE, LusalosEngine.removePiece(mill, capture).winner)
        }
    }
}
