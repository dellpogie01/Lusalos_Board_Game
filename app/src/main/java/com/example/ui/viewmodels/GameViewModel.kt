package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.LusalosAi
import com.example.data.LusalosRepository
import com.example.game.AiDifficulty
import com.example.game.GameMode
import com.example.game.GameState
import com.example.game.LusalosEngine
import com.example.game.Player
import com.example.game.TurnAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: LusalosRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(LusalosEngine.initialGameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.VS_AI)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    private val _aiDifficulty = MutableStateFlow(AiDifficulty.HARD)
    val aiDifficulty: StateFlow<AiDifficulty> = _aiDifficulty.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val historyStack = mutableListOf<GameState>()
    private var aiJob: Job? = null
    private var matchId = java.util.UUID.randomUUID().toString()
    private var resultRecorded = false
    private val _saveError = MutableStateFlow<String?>(null)
    val saveError = _saveError.asStateFlow()

    fun startNewGame(mode: GameMode = GameMode.VS_AI, difficulty: AiDifficulty = AiDifficulty.HARD) {
        aiJob?.cancel()
        _isAiThinking.value = false
        matchId = java.util.UUID.randomUUID().toString()
        resultRecorded = false
        _saveError.value = null
        _gameMode.value = mode
        _aiDifficulty.value = difficulty
        historyStack.clear()
        _gameState.value = LusalosEngine.initialGameState()
    }

    fun onPointClicked(point: Int) {
        val current = _gameState.value
        if (current.isGameOver() || _isAiThinking.value) return

        // In VS_AI mode, only Human (Player.WHITE) should click directly
        if (_gameMode.value == GameMode.VS_AI && current.currentTurn != Player.WHITE) return

        val nextState = when (current.action) {
            TurnAction.PLACE_PIECE -> LusalosEngine.placePiece(current, point)
            TurnAction.SELECT_OR_MOVE -> LusalosEngine.selectPiece(current, point)
            TurnAction.REMOVE_OPPONENT -> LusalosEngine.removePiece(current, point)
        }

        if (nextState == current) return
        // One checkpoint for each human board action; AI replies belong to that action.
        if (!current.board.contentEquals(nextState.board)) {
            historyStack.add(current)
            if (historyStack.size > 20) historyStack.removeAt(0)
        }
        _gameState.value = nextState
        checkGameCompletion(nextState)

        // Check if AI should move
        if (_gameMode.value == GameMode.VS_AI && !nextState.isGameOver() && nextState.currentTurn == Player.BLACK) {
            triggerAiMove()
        }
    }

    fun undoMove() {
        if (_gameState.value.isGameOver()) return
        if (historyStack.isEmpty()) return

        aiJob?.cancel()
        _isAiThinking.value = false

        _gameState.value = historyStack.removeAt(historyStack.lastIndex)
    }

    fun resignGame() {
        val current = _gameState.value
        if (current.isGameOver()) return
        aiJob?.cancel()
        _isAiThinking.value = false
        val winner = if (_gameMode.value == GameMode.VS_AI) Player.BLACK else current.currentTurn.opponent()
        _gameState.value = current.copy(
            winner = winner,
            winReason = "${winner.opponent().displayName} resigned.",
            message = "${winner.displayName} wins by resignation."
        )
        checkGameCompletion(_gameState.value)
    }

    private fun triggerAiMove() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _isAiThinking.value = true
            delay(500) // Natural thinking pause

            var current = _gameState.value
            while (current.currentTurn == Player.BLACK && !current.isGameOver()) {
                val aiMove = withContext(Dispatchers.Default) { LusalosAi.computeMove(current, _aiDifficulty.value) }

                val next = when (current.action) {
                    TurnAction.PLACE_PIECE -> {
                        aiMove.placeAt?.let { LusalosEngine.placePiece(current, it) } ?: current
                    }
                    TurnAction.SELECT_OR_MOVE -> {
                        if (aiMove.moveFrom != null && aiMove.moveTo != null) {
                            val selected = LusalosEngine.selectPiece(current, aiMove.moveFrom)
                            LusalosEngine.movePiece(selected, aiMove.moveFrom, aiMove.moveTo)
                        } else current
                    }
                    TurnAction.REMOVE_OPPONENT -> {
                        aiMove.removeAt?.let { LusalosEngine.removePiece(current, it) } ?: current
                    }
                }

                _gameState.value = next
                current = next
                checkGameCompletion(next)

                if (current.action == TurnAction.REMOVE_OPPONENT) {
                    delay(400) // Brief pause before AI removes opponent piece
                }
            }

            _isAiThinking.value = false
        }
    }

    fun retrySaveResult() { checkGameCompletion(_gameState.value) }

    private fun checkGameCompletion(state: GameState) {
        if (!state.isGameOver() || resultRecorded) return
        resultRecorded = true
        val finishedId = matchId
        val finishedMode = _gameMode.value
        val finishedDifficulty = _aiDifficulty.value
        viewModelScope.launch {
            try {
                repository.recordResult(finishedId, finishedMode, state.winner, finishedDifficulty)
                _saveError.value = null
            } catch (e: Exception) {
                if (matchId == finishedId) {
                    resultRecorded = false
                    _saveError.value = "Could not save the result. Please try again."
                }
            }
        }
    }
}
