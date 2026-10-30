package com.example.game

enum class Player(val value: Int, val displayName: String, val pieceName: String) {
    NONE(0, "None", "Empty"),
    WHITE(1, "Player 1", "White"),
    BLACK(2, "Player 2", "Black");

    fun opponent(): Player = when (this) {
        WHITE -> BLACK
        BLACK -> WHITE
        NONE -> NONE
    }
}

enum class GamePhase {
    PLACEMENT,   // Placing initial 9 pieces
    MOVEMENT,    // Sliding pieces along lines
    FLYING,      // 3 pieces left - can jump anywhere
    FINISHED     // Game over
}

enum class TurnAction {
    PLACE_PIECE,       // Waiting to place on an empty intersection
    SELECT_OR_MOVE,    // Selecting own piece or moving selected piece
    REMOVE_OPPONENT    // Mill formed: must remove an eligible opponent piece
}

enum class GameMode {
    VS_AI,
    LOCAL_PASS_AND_PLAY
}

enum class AiDifficulty(val label: String, val description: String) {
    EASY("Easy", "Uses basic strategy"),
    MEDIUM("Medium", "Blocks mills and attacks"),
    HARD("Hard", "Plans ahead with deeper move analysis"),
    VERY_HARD("Very Hard", "Searches ahead and anticipates your replies"),
    HARDEST("Hardest", "Uses the deepest search and capture planning")
}

data class MoveRecord(
    val player: Player,
    val from: Int, // -1 if placement
    val to: Int,
    val removedPieceAt: Int = -1,
    val formedMill: Boolean = false,
    val description: String = ""
)

data class GameState(
    val board: IntArray = IntArray(BoardConstants.TOTAL_POINTS) { Player.NONE.value },
    val currentTurn: Player = Player.WHITE,
    val phase: GamePhase = GamePhase.PLACEMENT,
    val action: TurnAction = TurnAction.PLACE_PIECE,
    val whitePiecesInHand: Int = BoardConstants.PIECES_PER_PLAYER,
    val blackPiecesInHand: Int = BoardConstants.PIECES_PER_PLAYER,
    val whitePiecesOnBoard: Int = 0,
    val blackPiecesOnBoard: Int = 0,
    val selectedPoint: Int? = null,
    val lastMoveFrom: Int = -1,
    val lastMoveTo: Int = -1,
    val highlightedMillPoints: List<Int> = emptyList(),
    val moveHistory: List<MoveRecord> = emptyList(),
    val winner: Player? = null,
    val winReason: String? = null,
    val isDraw: Boolean = false,
    val message: String = "Place your piece on an empty point."
) {
    fun piecesInHand(player: Player): Int = when (player) {
        Player.WHITE -> whitePiecesInHand
        Player.BLACK -> blackPiecesInHand
        Player.NONE -> 0
    }

    fun piecesOnBoard(player: Player): Int = when (player) {
        Player.WHITE -> whitePiecesOnBoard
        Player.BLACK -> blackPiecesOnBoard
        Player.NONE -> 0
    }

    fun isGameOver(): Boolean = winner != null || isDraw || phase == GamePhase.FINISHED

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!board.contentEquals(other.board)) return false
        if (currentTurn != other.currentTurn) return false
        if (phase != other.phase) return false
        if (action != other.action) return false
        if (whitePiecesInHand != other.whitePiecesInHand) return false
        if (blackPiecesInHand != other.blackPiecesInHand) return false
        if (whitePiecesOnBoard != other.whitePiecesOnBoard) return false
        if (blackPiecesOnBoard != other.blackPiecesOnBoard) return false
        if (selectedPoint != other.selectedPoint) return false
        if (lastMoveFrom != other.lastMoveFrom) return false
        if (lastMoveTo != other.lastMoveTo) return false
        if (highlightedMillPoints != other.highlightedMillPoints) return false
        if (winner != other.winner) return false
        if (winReason != other.winReason) return false
        if (isDraw != other.isDraw) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentHashCode()
        result = 31 * result + currentTurn.hashCode()
        result = 31 * result + phase.hashCode()
        result = 31 * result + action.hashCode()
        result = 31 * result + whitePiecesInHand
        result = 31 * result + blackPiecesInHand
        result = 31 * result + whitePiecesOnBoard
        result = 31 * result + blackPiecesOnBoard
        result = 31 * result + (selectedPoint ?: 0)
        result = 31 * result + lastMoveFrom
        result = 31 * result + lastMoveTo
        result = 31 * result + highlightedMillPoints.hashCode()
        result = 31 * result + (winner?.hashCode() ?: 0)
        result = 31 * result + (winReason?.hashCode() ?: 0)
        result = 31 * result + isDraw.hashCode()
        return result
    }
}
