package com.example.ai

import com.example.game.*
import kotlin.math.max
import kotlin.math.min

/** Bounded iterative deepening; mill captures stay part of the same turn. */
internal object SearchAi {
    private class SearchLimit : RuntimeException()
    private data class Candidate(val move: LusalosAi.AiMove, val state: GameState)

    fun compute(state: GameState, difficulty: AiDifficulty): LusalosAi.AiMove {
        val player = state.currentTurn
        val hardest = difficulty == AiDifficulty.HARDEST
        val deadline = System.nanoTime() + if (hardest) 900_000_000L else 250_000_000L
        val maxNodes = if (hardest) 30_000 else 6_000
        var nodes = 0
        fun checkLimit() { if (++nodes > maxNodes || System.nanoTime() >= deadline || Thread.currentThread().isInterrupted) throw SearchLimit() }
        fun search(position: GameState, depth: Int, alphaIn: Int, betaIn: Int): Int {
            checkLimit()
            if (position.isGameOver()) return evaluate(position, player)
            if (depth <= 0 && position.action != TurnAction.REMOVE_OPPONENT) return evaluate(position, player)
            var alpha = alphaIn
            var beta = betaIn
            val maximizing = position.currentTurn == player
            var best = if (maximizing) -1_000_000 else 1_000_000
            val options = candidates(position).sortedByDescending { if (maximizing) evaluate(it.state, player) else -evaluate(it.state, player) }
            if (options.isEmpty()) return evaluate(LusalosEngine.checkEndConditions(position), player)
            for (candidate in options) {
                val nextDepth = depth - if (candidate.state.currentTurn != position.currentTurn) 1 else 0
                val score = search(candidate.state, nextDepth, alpha, beta)
                if (maximizing) { best = max(best, score); alpha = max(alpha, best) }
                else { best = min(best, score); beta = min(beta, best) }
                if (alpha >= beta) break
            }
            return best
        }
        val root = candidates(state).sortedByDescending { evaluate(it.state, player) }
        if (root.isEmpty()) return LusalosAi.AiMove()
        var chosen = root.first().move
        for (depth in 1..if (hardest) 4 else 2) {
            try {
                var best = -1_000_000
                var iterationMove = chosen
                for (candidate in root) {
                    val remaining = depth - if (candidate.state.currentTurn != player) 1 else 0
                    val score = search(candidate.state, remaining, -1_000_000, 1_000_000)
                    if (score > best) { best = score; iterationMove = candidate.move }
                }
                chosen = iterationMove
                if (best >= 100_000) break
            } catch (_: SearchLimit) { break }
        }
        return chosen
    }

    private fun candidates(state: GameState): List<Candidate> {
        val position = state.copy(moveHistory = emptyList(), selectedPoint = null)
        return when (state.action) {
            TurnAction.PLACE_PIECE -> LusalosEngine.getValidPlacements(position).map {
                Candidate(LusalosAi.AiMove(placeAt = it), LusalosEngine.placePiece(position, it))
            }
            TurnAction.REMOVE_OPPONENT -> (0 until 24).filter { LusalosEngine.canRemovePiece(position, it) }.map {
                Candidate(LusalosAi.AiMove(removeAt = it), LusalosEngine.removePiece(position, it))
            }
            TurnAction.SELECT_OR_MOVE -> (0 until 24).filter { position.board[it] == position.currentTurn.value }.flatMap { from ->
                LusalosEngine.getValidMoves(position, from).map { to ->
                    Candidate(LusalosAi.AiMove(moveFrom = from, moveTo = to), LusalosEngine.movePiece(position, from, to))
                }
            }
        }
    }

    private fun evaluate(state: GameState, player: Player): Int {
        if (state.winner == player) return 100_000
        if (state.winner == player.opponent()) return -100_000
        if (state.isDraw) return 0
        val opponent = player.opponent()
        var score = ((state.piecesOnBoard(player) + state.piecesInHand(player)) -
            (state.piecesOnBoard(opponent) + state.piecesInHand(opponent))) * 120
        for (mill in BoardConstants.MILLS) {
            val mine = mill.count { state.board[it] == player.value }
            val theirs = mill.count { state.board[it] == opponent.value }
            if (mine == 3) score += 65
            if (theirs == 3) score -= 65
            if (mine == 2 && theirs == 0) score += 35
            if (theirs == 2 && mine == 0) score -= 40
        }
        for (point in 0 until 24) {
            val mobility = BoardConstants.ADJACENT_POINTS[point].count { state.board[it] == 0 }
            if (state.board[point] == player.value) score += mobility * 3
            if (state.board[point] == opponent.value) score -= mobility * 3
        }
        return score
    }
}
