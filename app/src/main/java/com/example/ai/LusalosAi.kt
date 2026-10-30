package com.example.ai

import com.example.game.AiDifficulty
import com.example.game.BoardConstants
import com.example.game.GamePhase
import com.example.game.GameState
import com.example.game.LusalosEngine
import com.example.game.Player
import com.example.game.TurnAction
import kotlin.random.Random

object LusalosAi {

    data class AiMove(
        val placeAt: Int? = null,
        val moveFrom: Int? = null,
        val moveTo: Int? = null,
        val removeAt: Int? = null
    )

    /**
     * Determines the AI move based on difficulty and current game state.
     */
    fun computeMove(state: GameState, difficulty: AiDifficulty): AiMove {
        if (state.isGameOver()) return AiMove()
        if (difficulty == AiDifficulty.VERY_HARD || difficulty == AiDifficulty.HARDEST) return SearchAi.compute(state, difficulty)
        val player = state.currentTurn
        return when (state.action) {
            TurnAction.PLACE_PIECE -> when (difficulty) {
                AiDifficulty.EASY -> computeEasyPlacement(state, player)
                AiDifficulty.MEDIUM -> computeMediumPlacement(state, player)
                AiDifficulty.HARD, AiDifficulty.VERY_HARD, AiDifficulty.HARDEST -> computeHardPlacement(state, player)
            }
            TurnAction.SELECT_OR_MOVE -> when (difficulty) {
                AiDifficulty.EASY -> computeEasyMovement(state, player)
                AiDifficulty.MEDIUM -> computeMediumMovement(state, player)
                AiDifficulty.HARD, AiDifficulty.VERY_HARD, AiDifficulty.HARDEST -> computeHardMovement(state, player)
            }
            TurnAction.REMOVE_OPPONENT -> when (difficulty) {
                AiDifficulty.EASY -> computeEasyRemoval(state)
                AiDifficulty.MEDIUM, AiDifficulty.HARD, AiDifficulty.VERY_HARD, AiDifficulty.HARDEST -> computeSmartRemoval(state, player)
            }
        }
    }

    // --- PLACEMENT STRATEGIES ---

    private fun computeEasyPlacement(state: GameState, player: Player): AiMove {
        val valid = LusalosEngine.getValidPlacements(state)
        if (valid.isEmpty()) return AiMove()

        // 1. Check if any spot forms a mill immediately
        for (pt in valid) {
            if (LusalosEngine.formsMill(state.board.copyOf().apply { this[pt] = player.value }, pt, player)) {
                return AiMove(placeAt = pt)
            }
        }
        // Otherwise random
        return AiMove(placeAt = valid.random())
    }

    private fun computeMediumPlacement(state: GameState, player: Player): AiMove {
        val valid = LusalosEngine.getValidPlacements(state)
        if (valid.isEmpty()) return AiMove()
        val opponent = player.opponent()

        // 1. Priority 1: Form an immediate mill
        for (pt in valid) {
            if (LusalosEngine.formsMill(state.board.copyOf().apply { this[pt] = player.value }, pt, player)) {
                return AiMove(placeAt = pt)
            }
        }

        // 2. Priority 2: Block opponent from completing a mill
        for (pt in valid) {
            if (LusalosEngine.formsMill(state.board.copyOf().apply { this[pt] = opponent.value }, pt, opponent)) {
                return AiMove(placeAt = pt)
            }
        }

        // 3. Priority 3: Cross intersections (points 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23)
        val crossPoints = valid.filter { it % 2 == 1 }
        if (crossPoints.isNotEmpty() && Random.nextFloat() < 0.7f) {
            return AiMove(placeAt = crossPoints.random())
        }

        return AiMove(placeAt = valid.random())
    }

    private fun computeHardPlacement(state: GameState, player: Player): AiMove {
        val valid = LusalosEngine.getValidPlacements(state)
        if (valid.isEmpty()) return AiMove()
        val opponent = player.opponent()

        // Immediate win / mill check
        for (pt in valid) {
            if (LusalosEngine.formsMill(state.board.copyOf().apply { this[pt] = player.value }, pt, player)) {
                return AiMove(placeAt = pt)
            }
        }

        // Immediate block check
        for (pt in valid) {
            if (LusalosEngine.formsMill(state.board.copyOf().apply { this[pt] = opponent.value }, pt, opponent)) {
                return AiMove(placeAt = pt)
            }
        }

        // Minimax evaluation for best placement
        var bestScore = Int.MIN_VALUE
        var bestPoint = valid.first()

        for (pt in valid) {
            val simulated = LusalosEngine.placePiece(state, pt)
            val score = evaluateBoard(simulated, player)
            if (score > bestScore) {
                bestScore = score
                bestPoint = pt
            }
        }

        return AiMove(placeAt = bestPoint)
    }

    // --- MOVEMENT STRATEGIES ---

    private fun computeEasyMovement(state: GameState, player: Player): AiMove {
        val legalMoves = getAllLegalMoves(state, player)
        if (legalMoves.isEmpty()) return AiMove()

        // Try to form mill if possible
        for ((from, to) in legalMoves) {
            val nextBoard = state.board.copyOf().apply {
                this[from] = Player.NONE.value
                this[to] = player.value
            }
            if (LusalosEngine.formsMill(nextBoard, to, player)) {
                return AiMove(moveFrom = from, moveTo = to)
            }
        }

        val (from, to) = legalMoves.random()
        return AiMove(moveFrom = from, moveTo = to)
    }

    private fun computeMediumMovement(state: GameState, player: Player): AiMove {
        val legalMoves = getAllLegalMoves(state, player)
        if (legalMoves.isEmpty()) return AiMove()
        val opponent = player.opponent()

        // 1. Can form mill?
        for ((from, to) in legalMoves) {
            val nextBoard = state.board.copyOf().apply {
                this[from] = Player.NONE.value
                this[to] = player.value
            }
            if (LusalosEngine.formsMill(nextBoard, to, player)) {
                return AiMove(moveFrom = from, moveTo = to)
            }
        }

        // 2. Can block opponent's mill position?
        val opponentThreats = findMillThreatPositions(state.board, opponent)
        for ((from, to) in legalMoves) {
            if (to in opponentThreats) {
                return AiMove(moveFrom = from, moveTo = to)
            }
        }

        // 3. Move that doesn't break an existing mill unless necessary
        val nonMillBreakers = legalMoves.filter { (from, _) ->
            !LusalosEngine.isPartOfAnyMill(state.board, from)
        }
        if (nonMillBreakers.isNotEmpty()) {
            val (from, to) = nonMillBreakers.random()
            return AiMove(moveFrom = from, moveTo = to)
        }

        val (from, to) = legalMoves.random()
        return AiMove(moveFrom = from, moveTo = to)
    }

    private fun computeHardMovement(state: GameState, player: Player): AiMove {
        val legalMoves = getAllLegalMoves(state, player)
        if (legalMoves.isEmpty()) return AiMove()

        var bestScore = Int.MIN_VALUE
        var bestMove = legalMoves.first()

        for ((from, to) in legalMoves) {
            val nextBoard = state.board.copyOf().apply {
                this[from] = Player.NONE.value
                this[to] = player.value
            }
            val millFormed = LusalosEngine.formsMill(nextBoard, to, player)
            var score = 0
            if (millFormed) {
                score += 150
            }

            // Minimax 2-ply evaluation
            score += evaluateBoard(state.copy(board = nextBoard), player)

            if (score > bestScore) {
                bestScore = score
                bestMove = from to to
            }
        }

        return AiMove(moveFrom = bestMove.first, moveTo = bestMove.second)
    }

    // --- REMOVAL STRATEGIES ---

    private fun computeEasyRemoval(state: GameState): AiMove {
        val removable = (0 until BoardConstants.TOTAL_POINTS).filter {
            LusalosEngine.canRemovePiece(state, it)
        }
        return if (removable.isNotEmpty()) AiMove(removeAt = removable.random()) else AiMove()
    }

    private fun computeSmartRemoval(state: GameState, player: Player): AiMove {
        val opponent = player.opponent()
        val removable = (0 until BoardConstants.TOTAL_POINTS).filter {
            LusalosEngine.canRemovePiece(state, it)
        }
        if (removable.isEmpty()) return AiMove()

        // 1. Target pieces that are 1 step away from completing a mill (2-in-a-row)
        for (pt in removable) {
            for (mill in BoardConstants.MILLS) {
                if (mill.contains(pt)) {
                    val countOpp = mill.count { state.board[it] == opponent.value }
                    if (countOpp == 2) {
                        return AiMove(removeAt = pt)
                    }
                }
            }
        }

        // 2. Target high-mobility opponent pieces
        var maxNeighbors = -1
        var bestPt = removable.first()
        for (pt in removable) {
            val emptyNeighbors = BoardConstants.ADJACENT_POINTS[pt].count { state.board[it] == Player.NONE.value }
            if (emptyNeighbors > maxNeighbors) {
                maxNeighbors = emptyNeighbors
                bestPt = pt
            }
        }
        return AiMove(removeAt = bestPt)
    }

    // --- UTILITIES ---

    private fun getAllLegalMoves(state: GameState, player: Player): List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until BoardConstants.TOTAL_POINTS) {
            if (state.board[i] == player.value) {
                val destinations = LusalosEngine.getValidMoves(state.copy(currentTurn = player), i)
                for (dest in destinations) {
                    list.add(i to dest)
                }
            }
        }
        return list
    }

    private fun findMillThreatPositions(board: IntArray, player: Player): Set<Int> {
        val threats = mutableSetOf<Int>()
        for (mill in BoardConstants.MILLS) {
            val playerCount = mill.count { board[it] == player.value }
            val emptyCount = mill.count { board[it] == Player.NONE.value }
            if (playerCount == 2 && emptyCount == 1) {
                val emptyPt = mill.first { board[it] == Player.NONE.value }
                threats.add(emptyPt)
            }
        }
        return threats
    }

    private fun evaluateBoard(state: GameState, player: Player): Int {
        val opponent = player.opponent()
        var score = 0

        val myPieces = state.piecesOnBoard(player) + state.piecesInHand(player)
        val oppPieces = state.piecesOnBoard(opponent) + state.piecesInHand(opponent)
        score += (myPieces - oppPieces) * 25

        // Mill count
        var myMills = 0
        var oppMills = 0
        for (mill in BoardConstants.MILLS) {
            if (mill.all { state.board[it] == player.value }) myMills++
            if (mill.all { state.board[it] == opponent.value }) oppMills++
        }
        score += (myMills - oppMills) * 40

        // 2-in-a-row threats
        val myThreats = findMillThreatPositions(state.board, player).size
        val oppThreats = findMillThreatPositions(state.board, opponent).size
        score += (myThreats - oppThreats) * 20

        return score
    }
}
