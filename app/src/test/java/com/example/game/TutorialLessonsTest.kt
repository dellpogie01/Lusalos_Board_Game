package com.example.game

import org.junit.Assert.*
import org.junit.Test

class TutorialLessonsTest {
    @Test fun everyLessonContainsLegalAnimatedTransitions() {
        repeat(TutorialLessons.titles.size) { lesson ->
            val frames = TutorialLessons.frames(lesson)
            assertTrue(frames.size > 1)
            frames.forEach { frame ->
                assertEquals(frame.state.whitePiecesOnBoard, frame.state.board.count { it == 1 })
                assertEquals(frame.state.blackPiecesOnBoard, frame.state.board.count { it == 2 })
                assertTrue(frame.caption.isNotBlank())
            }
            frames.zipWithNext().forEach { (before, after) ->
                // A frame with empty history is the explicitly introduced flying setup.
                if (after.state.moveHistory.isNotEmpty()) {
                    val move = after.state.moveHistory.last()
                    val expected = when {
                        move.removedPieceAt >= 0 -> LusalosEngine.removePiece(before.state, move.removedPieceAt)
                        move.from < 0 -> LusalosEngine.placePiece(before.state, move.to)
                        else -> LusalosEngine.movePiece(before.state, move.from, move.to)
                    }
                    assertEquals(expected, after.state)
                }
            }
        }
    }

    @Test fun movementAndFlyingHaveSeparateLegalExamples() {
        assertEquals(6, TutorialLessons.titles.size)
        val moving = TutorialLessons.frames(3)
        assertTrue(moving.all { it.state.whitePiecesOnBoard == 4 })
        val flying = TutorialLessons.frames(4)
        assertEquals(3, flying.first().state.whitePiecesOnBoard)
        val move = flying.last().state.moveHistory.last()
        assertFalse(BoardConstants.ADJACENT_POINTS[move.from].contains(move.to))
        assertEquals(Player.WHITE, move.player)
    }

    @Test fun winningLessonFinishesWithWhiteWinning() {
        val final = TutorialLessons.frames(5).last().state
        assertTrue(final.isGameOver())
        assertEquals(Player.WHITE, final.winner)
        assertEquals(2, final.blackPiecesOnBoard)
    }
}
