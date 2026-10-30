package com.example.game

 data class TutorialFrame(val state: GameState, val caption: String)

/** Curated legal examples keep each lesson focused instead of showing unrelated AI turns. */
object TutorialLessons {
    val titles = listOf("The board", "Place your pieces", "Capture a piece", "Move along a line", "Fly to any empty point", "Win the game")
    val instructions = listOf(
        "Three squares. 24 connected points. You play white; the AI plays black. Each side starts with nine pieces.",
        "Place a piece on an empty point. Three pieces along a marked straight line form a mill.",
        "A new mill lets you capture one opponent piece. Pieces in mills are protected unless all opponent pieces are in mills.",
        "After all pieces are placed, move along a line to an adjacent empty point.",
        "With exactly three pieces left and none in hand, fly to any empty point on the board.",
        "Win by reducing the opponent to fewer than three pieces after placement, or blocking all their moves."
    )

    fun frames(lesson: Int): List<TutorialFrame> {
        fun setup(white: List<Int>, black: List<Int>): GameState {
            val board = IntArray(24)
            white.forEach { board[it] = Player.WHITE.value }
            black.forEach { board[it] = Player.BLACK.value }
            return GameState(board = board, whitePiecesInHand = 0, blackPiecesInHand = 0,
                whitePiecesOnBoard = white.size, blackPiecesOnBoard = black.size,
                phase = if (white.size == 3) GamePhase.FLYING else GamePhase.MOVEMENT,
                action = TurnAction.SELECT_OR_MOVE)
        }
        val result = mutableListOf<TutorialFrame>()
        var state = LusalosEngine.initialGameState()
        fun show(caption: String) { result += TutorialFrame(state, caption) }
        fun place(point: Int, caption: String) { state = LusalosEngine.placePiece(state, point); show(caption) }
        when (lesson) {
            0 -> {
                show("24 points joined by straight lines.")
                place(0, "White pieces belong to you.")
                place(8, "Black pieces belong to the AI.")
                place(1, "Take turns on the same board.")
                place(9, "Each player starts with nine pieces.")
            }
            1 -> {
                show("Choose an empty point.")
                place(0, "White places a piece.")
                place(8, "Black takes a turn.")
                place(1, "Two white pieces line up.")
                place(9, "Black places another piece.")
                place(2, "Three in a row: a mill!")
            }
            2 -> {
                listOf(0, 8, 1, 9).forEach { state = LusalosEngine.placePiece(state, it) }
                show("White is one piece away from a mill.")
                place(2, "Mill! Choose a black piece to capture.")
                state = LusalosEngine.removePiece(state, 8)
                show("One black piece is removed.")
            }
            3 -> {
                state = setup(listOf(0, 1, 3, 5), listOf(8, 10, 12, 14))
                show("With four pieces, move along a line.")
                state = LusalosEngine.movePiece(state, 0, 7)
                show("White slides to an adjacent point.")
                state = LusalosEngine.movePiece(state, 8, 9)
                show("Black slides along a connecting line.")
            }
            4 -> {
                state = setup(listOf(0, 1, 4), listOf(8, 10, 12, 14))
                show("With three pieces, white can fly.")
                state = LusalosEngine.movePiece(state, 4, 6)
                show("Fly to any empty point, even far away.")
            }
            else -> {
                state = setup(listOf(0, 1, 3), listOf(8, 10, 12))
                show("Black has only three pieces left.")
                state = LusalosEngine.movePiece(state, 3, 2)
                show("White flies into a mill.")
                state = LusalosEngine.removePiece(state, 8)
                show("Two black pieces remain. White wins!")
            }
        }
        return result
    }
}
