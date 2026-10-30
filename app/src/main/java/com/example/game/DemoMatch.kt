package com.example.game

import com.example.ai.LusalosAi

/** A separate exhibition match: never records results or touches player progress. */
class DemoMatch {
    var state: GameState = LusalosEngine.initialGameState()
        private set
    private val positions = mutableMapOf<String, Int>()
    private var turns = 0

    fun advance(): GameState {
        if (state.isGameOver()) return state
        val move = LusalosAi.computeMove(state, if (state.currentTurn == Player.WHITE) AiDifficulty.MEDIUM else AiDifficulty.EASY)
        state = when (state.action) {
            TurnAction.PLACE_PIECE -> move.placeAt?.let { LusalosEngine.placePiece(state, it) } ?: state
            TurnAction.SELECT_OR_MOVE -> if (move.moveFrom != null && move.moveTo != null)
                LusalosEngine.movePiece(state, move.moveFrom, move.moveTo) else state
            TurnAction.REMOVE_OPPONENT -> move.removeAt?.let { LusalosEngine.removePiece(state, it) } ?: state
        }
        if (!state.isGameOver() && state.action != TurnAction.REMOVE_OPPONENT) {
            turns++
            val key = "${state.board.joinToString()}:${state.currentTurn}:${state.whitePiecesInHand}:${state.blackPiecesInHand}"
            val visits = (positions[key] ?: 0) + 1
            positions[key] = visits
            if (visits >= 3 || turns >= 180) state = state.copy(
                phase = GamePhase.FINISHED, isDraw = true,
                message = "Draw. A new demonstration starts next.", winReason = "Exhibition draw."
            )
        }
        return state
    }
}
