package com.example.game

class LusalosEngine {

    companion object {
        fun initialGameState(): GameState = GameState()

        /**
         * Checks if the piece at [point] forms a mill for [player].
         */
        fun formsMill(board: IntArray, point: Int, player: Player): Boolean {
            if (point < 0 || point >= BoardConstants.TOTAL_POINTS) return false
            if (player == Player.NONE) return false

            val mills = BoardConstants.POINT_MILLS[point]
            for (millIdx in mills) {
                val mill = BoardConstants.MILLS[millIdx]
                if (board[mill[0]] == player.value &&
                    board[mill[1]] == player.value &&
                    board[mill[2]] == player.value
                ) {
                    return true
                }
            }
            return false
        }

        /**
         * Returns all point indices involved in mills that pass through [point].
         */
        fun getMillPoints(board: IntArray, point: Int, player: Player): List<Int> {
            val result = mutableSetOf<Int>()
            val mills = BoardConstants.POINT_MILLS[point]
            for (millIdx in mills) {
                val mill = BoardConstants.MILLS[millIdx]
                if (board[mill[0]] == player.value &&
                    board[mill[1]] == player.value &&
                    board[mill[2]] == player.value
                ) {
                    result.add(mill[0])
                    result.add(mill[1])
                    result.add(mill[2])
                }
            }
            return result.toList()
        }

        /**
         * Checks if a point is part of any active mill on the board.
         */
        fun isPartOfAnyMill(board: IntArray, point: Int): Boolean {
            val ownerVal = board[point]
            if (ownerVal == Player.NONE.value) return false
            val player = if (ownerVal == Player.WHITE.value) Player.WHITE else Player.BLACK
            return formsMill(board, point, player)
        }

        /**
         * Checks if a player's piece at [point] can legally be captured/removed.
         * Nine Men's Morris rule: A piece in an active mill cannot be removed
         * UNLESS all the opponent's pieces on the board are currently part of a mill.
         */
        fun canRemovePiece(state: GameState, point: Int): Boolean {
            val opponent = state.currentTurn.opponent()
            if (state.board[point] != opponent.value) return false

            val inMill = isPartOfAnyMill(state.board, point)
            if (!inMill) return true

            // If it is in a mill, check if ALL opponent pieces on the board are in mills
            for (i in 0 until BoardConstants.TOTAL_POINTS) {
                if (state.board[i] == opponent.value && !isPartOfAnyMill(state.board, i)) {
                    // There is at least one piece NOT in a mill, so protected piece cannot be removed
                    return false
                }
            }
            return true
        }

        /**
         * Valid placement destinations: all empty points.
         */
        fun getValidPlacements(state: GameState): List<Int> {
            val list = mutableListOf<Int>()
            for (i in 0 until BoardConstants.TOTAL_POINTS) {
                if (state.board[i] == Player.NONE.value) {
                    list.add(i)
                }
            }
            return list
        }

        /**
         * Returns valid move destinations from a given [from] point.
         */
        fun getValidMoves(state: GameState, from: Int): List<Int> {
            if (from < 0 || from >= BoardConstants.TOTAL_POINTS) return emptyList()
            val player = state.currentTurn
            if (state.board[from] != player.value) return emptyList()

            val piecesOnBoard = state.piecesOnBoard(player)
            val inHand = state.piecesInHand(player)

            // Flying rule: exactly 3 pieces on board and 0 in hand
            val canFly = inHand == 0 && piecesOnBoard == 3

            val valid = mutableListOf<Int>()
            if (canFly) {
                for (i in 0 until BoardConstants.TOTAL_POINTS) {
                    if (state.board[i] == Player.NONE.value) {
                        valid.add(i)
                    }
                }
            } else {
                for (neighbor in BoardConstants.ADJACENT_POINTS[from]) {
                    if (state.board[neighbor] == Player.NONE.value) {
                        valid.add(neighbor)
                    }
                }
            }
            return valid
        }

        /**
         * Checks if the player has any legal moves available.
         */
        fun hasAnyLegalMoves(state: GameState, player: Player): Boolean {
            if (state.piecesInHand(player) > 0) {
                return state.board.any { it == Player.NONE.value }
            }
            for (i in 0 until BoardConstants.TOTAL_POINTS) {
                if (state.board[i] == player.value) {
                    if (getValidMoves(state.copy(currentTurn = player), i).isNotEmpty()) {
                        return true
                    }
                }
            }
            return false
        }

        /**
         * Determines the game phase for the active player.
         */
        fun resolvePhase(whiteInHand: Int, blackInHand: Int, whiteOnBoard: Int, blackOnBoard: Int, currentTurn: Player): GamePhase {
            if (whiteInHand > 0 || blackInHand > 0) {
                return GamePhase.PLACEMENT
            }
            val activeOnBoard = if (currentTurn == Player.WHITE) whiteOnBoard else blackOnBoard
            return if (activeOnBoard == 3) GamePhase.FLYING else GamePhase.MOVEMENT
        }

        /**
         * Places a piece during PLACEMENT phase.
         */
        fun placePiece(state: GameState, point: Int): GameState {
            if (state.isGameOver()) return state
            if (state.action != TurnAction.PLACE_PIECE) return state
            if (point !in 0 until BoardConstants.TOTAL_POINTS) return state
            if (state.board[point] != Player.NONE.value) return state

            val player = state.currentTurn
            val newBoard = state.board.copyOf()
            newBoard[point] = player.value

            val newWhiteHand = if (player == Player.WHITE) state.whitePiecesInHand - 1 else state.whitePiecesInHand
            val newBlackHand = if (player == Player.BLACK) state.blackPiecesInHand - 1 else state.blackPiecesInHand
            val newWhiteBoard = if (player == Player.WHITE) state.whitePiecesOnBoard + 1 else state.whitePiecesOnBoard
            val newBlackBoard = if (player == Player.BLACK) state.blackPiecesOnBoard + 1 else state.blackPiecesOnBoard

            val millFormed = formsMill(newBoard, point, player)
            val millPoints = if (millFormed) getMillPoints(newBoard, point, player) else emptyList()

            val record = MoveRecord(
                player = player,
                from = -1,
                to = point,
                formedMill = millFormed,
                description = "${player.pieceName} placed at $point" + if (millFormed) " (Mill formed!)" else ""
            )

            if (millFormed) {
                return state.copy(
                    board = newBoard,
                    whitePiecesInHand = newWhiteHand,
                    blackPiecesInHand = newBlackHand,
                    whitePiecesOnBoard = newWhiteBoard,
                    blackPiecesOnBoard = newBlackBoard,
                    action = TurnAction.REMOVE_OPPONENT,
                    lastMoveFrom = -1,
                    lastMoveTo = point,
                    highlightedMillPoints = millPoints,
                    moveHistory = state.moveHistory + record,
                    message = "Mill! Choose an opponent piece to remove."
                )
            } else {
                val nextTurn = player.opponent()
                val nextPhase = resolvePhase(newWhiteHand, newBlackHand, newWhiteBoard, newBlackBoard, nextTurn)
                val nextAction = if (newWhiteHand > 0 || newBlackHand > 0) TurnAction.PLACE_PIECE else TurnAction.SELECT_OR_MOVE

                val intermediate = state.copy(
                    board = newBoard,
                    currentTurn = nextTurn,
                    phase = nextPhase,
                    action = nextAction,
                    whitePiecesInHand = newWhiteHand,
                    blackPiecesInHand = newBlackHand,
                    whitePiecesOnBoard = newWhiteBoard,
                    blackPiecesOnBoard = newBlackBoard,
                    selectedPoint = null,
                    lastMoveFrom = -1,
                    lastMoveTo = point,
                    highlightedMillPoints = emptyList(),
                    moveHistory = state.moveHistory + record,
                    message = if (nextAction == TurnAction.PLACE_PIECE)
                        "${nextTurn.displayName}: Place your piece."
                    else
                        "${nextTurn.displayName}: Select a piece to move."
                )
                return checkEndConditions(intermediate)
            }
        }

        /**
         * Selects a piece to move during MOVEMENT or FLYING phase.
         */
        fun selectPiece(state: GameState, point: Int): GameState {
            if (state.isGameOver()) return state
            if (state.action != TurnAction.SELECT_OR_MOVE) return state
            if (point !in 0 until BoardConstants.TOTAL_POINTS) return state

            // If selecting own piece
            if (state.board[point] == state.currentTurn.value) {
                val validMoves = getValidMoves(state, point)
                return state.copy(
                    selectedPoint = point,
                    message = if (validMoves.isEmpty()) "This piece has no legal moves." else "Choose a destination."
                )
            }

            // If tapping on empty point and a piece was selected, try moving
            if (state.board[point] == Player.NONE.value && state.selectedPoint != null) {
                return movePiece(state, state.selectedPoint, point)
            }

            return state
        }

        /**
         * Moves a selected piece from [from] to [to].
         */
        fun movePiece(state: GameState, from: Int, to: Int): GameState {
            if (state.isGameOver()) return state
            if (state.action != TurnAction.SELECT_OR_MOVE) return state
            val player = state.currentTurn
            if (state.board[from] != player.value) return state
            if (state.board[to] != Player.NONE.value) return state

            val validMoves = getValidMoves(state, from)
            if (to !in validMoves) return state

            val newBoard = state.board.copyOf()
            newBoard[from] = Player.NONE.value
            newBoard[to] = player.value

            val millFormed = formsMill(newBoard, to, player)
            val millPoints = if (millFormed) getMillPoints(newBoard, to, player) else emptyList()

            val record = MoveRecord(
                player = player,
                from = from,
                to = to,
                formedMill = millFormed,
                description = "${player.pieceName} moved from $from to $to" + if (millFormed) " (Mill!)" else ""
            )

            if (millFormed) {
                return state.copy(
                    board = newBoard,
                    selectedPoint = null,
                    action = TurnAction.REMOVE_OPPONENT,
                    lastMoveFrom = from,
                    lastMoveTo = to,
                    highlightedMillPoints = millPoints,
                    moveHistory = state.moveHistory + record,
                    message = "Mill! Remove an opponent piece."
                )
            } else {
                val nextTurn = player.opponent()
                val nextPhase = resolvePhase(state.whitePiecesInHand, state.blackPiecesInHand, state.whitePiecesOnBoard, state.blackPiecesOnBoard, nextTurn)

                val intermediate = state.copy(
                    board = newBoard,
                    currentTurn = nextTurn,
                    phase = nextPhase,
                    action = TurnAction.SELECT_OR_MOVE,
                    selectedPoint = null,
                    lastMoveFrom = from,
                    lastMoveTo = to,
                    highlightedMillPoints = emptyList(),
                    moveHistory = state.moveHistory + record,
                    message = "${nextTurn.displayName}: Select and move a piece."
                )
                return checkEndConditions(intermediate)
            }
        }

        /**
         * Removes an opponent piece after a mill is formed.
         */
        fun removePiece(state: GameState, point: Int): GameState {
            if (state.isGameOver()) return state
            if (state.action != TurnAction.REMOVE_OPPONENT) return state
            if (!canRemovePiece(state, point)) return state

            val opponent = state.currentTurn.opponent()
            val newBoard = state.board.copyOf()
            newBoard[point] = Player.NONE.value

            val newWhiteBoard = if (opponent == Player.WHITE) state.whitePiecesOnBoard - 1 else state.whitePiecesOnBoard
            val newBlackBoard = if (opponent == Player.BLACK) state.blackPiecesOnBoard - 1 else state.blackPiecesOnBoard

            val record = MoveRecord(
                player = state.currentTurn,
                from = -1,
                to = point,
                removedPieceAt = point,
                description = "Removed opponent piece at $point"
            )

            val nextTurn = state.currentTurn.opponent()
            val nextPhase = resolvePhase(state.whitePiecesInHand, state.blackPiecesInHand, newWhiteBoard, newBlackBoard, nextTurn)
            val nextAction = if (state.whitePiecesInHand > 0 || state.blackPiecesInHand > 0) TurnAction.PLACE_PIECE else TurnAction.SELECT_OR_MOVE

            val intermediate = state.copy(
                board = newBoard,
                currentTurn = nextTurn,
                phase = nextPhase,
                action = nextAction,
                whitePiecesOnBoard = newWhiteBoard,
                blackPiecesOnBoard = newBlackBoard,
                selectedPoint = null,
                highlightedMillPoints = emptyList(),
                moveHistory = state.moveHistory + record,
                message = if (nextAction == TurnAction.PLACE_PIECE)
                    "${nextTurn.displayName}: Place your piece."
                else
                    "${nextTurn.displayName}: Select a piece to move."
            )
            return checkEndConditions(intermediate)
        }

        /**
         * Checks win and draw conditions.
         */
        fun checkEndConditions(state: GameState): GameState {
            val opponent = state.currentTurn
            val opponentInHand = state.piecesInHand(opponent)
            val opponentOnBoard = state.piecesOnBoard(opponent)

            // Condition 1: In movement/flying phase, opponent has less than 3 pieces left
            if (opponentInHand == 0 && opponentOnBoard < 3 && (state.whitePiecesInHand == 0 && state.blackPiecesInHand == 0)) {
                val winner = opponent.opponent()
                return state.copy(
                    phase = GamePhase.FINISHED,
                    winner = winner,
                    winReason = "The opponent has fewer than 3 pieces remaining.",
                    message = "${winner.displayName} wins!"
                )
            }

            // Condition 2: Opponent has no legal moves (Stalemate / Trapped)
            if (!hasAnyLegalMoves(state, opponent)) {
                val winner = opponent.opponent()
                return state.copy(
                    phase = GamePhase.FINISHED,
                    winner = winner,
                    winReason = "The opponent has no legal moves.",
                    message = "${winner.displayName} wins! The opponent has no legal moves."
                )
            }

            return state
        }
    }
}
