package com.example.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LusalosEngineTest {

    @Test
    fun testInitialGameState() {
        val state = LusalosEngine.initialGameState()
        assertEquals(Player.WHITE, state.currentTurn)
        assertEquals(GamePhase.PLACEMENT, state.phase)
        assertEquals(TurnAction.PLACE_PIECE, state.action)
        assertEquals(9, state.whitePiecesInHand)
        assertEquals(9, state.blackPiecesInHand)
        assertEquals(0, state.whitePiecesOnBoard)
        assertEquals(0, state.blackPiecesOnBoard)
        assertTrue(state.board.all { it == Player.NONE.value })
    }

    @Test
    fun testPlacementAndAlternatingTurns() {
        var state = LusalosEngine.initialGameState()
        // White places at 0
        state = LusalosEngine.placePiece(state, 0)
        assertEquals(Player.BLACK, state.currentTurn)
        assertEquals(8, state.whitePiecesInHand)
        assertEquals(1, state.whitePiecesOnBoard)
        assertEquals(Player.WHITE.value, state.board[0])

        // Black places at 1
        state = LusalosEngine.placePiece(state, 1)
        assertEquals(Player.WHITE, state.currentTurn)
        assertEquals(8, state.blackPiecesInHand)
        assertEquals(1, state.blackPiecesOnBoard)
        assertEquals(Player.BLACK.value, state.board[1])
    }

    @Test
    fun testMillDetectionAndPieceRemoval() {
        var state = LusalosEngine.initialGameState()
        // Setup: White places at 0, Black at 8, White at 1, Black at 9
        state = LusalosEngine.placePiece(state, 0)
        state = LusalosEngine.placePiece(state, 8)
        state = LusalosEngine.placePiece(state, 1)
        state = LusalosEngine.placePiece(state, 9)

        // White places at 2 -> Forms Mill (0, 1, 2)
        state = LusalosEngine.placePiece(state, 2)
        assertEquals(TurnAction.REMOVE_OPPONENT, state.action)
        assertEquals(Player.WHITE, state.currentTurn)
        assertTrue(state.highlightedMillPoints.containsAll(listOf(0, 1, 2)))

        // White removes opponent piece at 8
        state = LusalosEngine.removePiece(state, 8)
        assertEquals(Player.NONE.value, state.board[8])
        assertEquals(1, state.blackPiecesOnBoard)
        assertEquals(Player.BLACK, state.currentTurn)
        assertEquals(TurnAction.PLACE_PIECE, state.action)
    }

    @Test
    fun testProtectedMillRule() {
        // A piece in an active mill cannot be removed unless all opponent pieces are in mills
        val board = IntArray(BoardConstants.TOTAL_POINTS) { Player.NONE.value }
        // Black has a mill at (8, 9, 10) and an isolated piece at 12
        board[8] = Player.BLACK.value
        board[9] = Player.BLACK.value
        board[10] = Player.BLACK.value
        board[12] = Player.BLACK.value

        val state = GameState(
            board = board,
            currentTurn = Player.WHITE,
            action = TurnAction.REMOVE_OPPONENT,
            whitePiecesInHand = 0,
            blackPiecesInHand = 0,
            whitePiecesOnBoard = 5,
            blackPiecesOnBoard = 4
        )

        // Cannot remove 8 (it is part of a mill and 12 is available)
        assertFalse(LusalosEngine.canRemovePiece(state, 8))
        // Can remove 12 (not in a mill)
        assertTrue(LusalosEngine.canRemovePiece(state, 12))
    }

    @Test
    fun testFlyingRuleWhenThreePiecesRemain() {
        val board = IntArray(BoardConstants.TOTAL_POINTS) { Player.NONE.value }
        // White has exactly 3 pieces left
        board[0] = Player.WHITE.value
        board[4] = Player.WHITE.value
        board[12] = Player.WHITE.value

        val state = GameState(
            board = board,
            currentTurn = Player.WHITE,
            phase = GamePhase.FLYING,
            action = TurnAction.SELECT_OR_MOVE,
            whitePiecesInHand = 0,
            blackPiecesInHand = 0,
            whitePiecesOnBoard = 3,
            blackPiecesOnBoard = 5
        )

        // Valid moves from 0 should include distant vacant points (e.g., 20)
        val validMoves = LusalosEngine.getValidMoves(state, 0)
        assertTrue(validMoves.contains(20))
        assertTrue(validMoves.contains(16))
    }

    @Test
    fun testWinConditionWhenOpponentHasFewerThanThreePieces() {
        val board = IntArray(BoardConstants.TOTAL_POINTS) { Player.NONE.value }
        board[0] = Player.WHITE.value
        board[1] = Player.WHITE.value
        board[2] = Player.WHITE.value
        board[3] = Player.WHITE.value

        board[8] = Player.BLACK.value
        board[9] = Player.BLACK.value
        // Black only has 2 pieces on board and 0 in hand
        val state = GameState(
            board = board,
            currentTurn = Player.BLACK,
            phase = GamePhase.MOVEMENT,
            action = TurnAction.SELECT_OR_MOVE,
            whitePiecesInHand = 0,
            blackPiecesInHand = 0,
            whitePiecesOnBoard = 4,
            blackPiecesOnBoard = 2
        )

        val endState = LusalosEngine.checkEndConditions(state)
        assertTrue(endState.isGameOver())
        assertEquals(Player.WHITE, endState.winner)
    }
}
