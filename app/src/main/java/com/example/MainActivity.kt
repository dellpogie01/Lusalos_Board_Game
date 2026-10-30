package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.components.FeedbackOptions
import com.example.ui.components.LocalFeedbackOptions
import com.example.game.AiDifficulty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.LusalosRepository
import com.example.game.GameMode
import com.example.ui.screens.*
import com.example.ui.theme.LusalosTheme
import com.example.ui.viewmodels.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = LusalosRepository(applicationContext)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
                modelClass.isAssignableFrom(UserViewModel::class.java) -> UserViewModel(repository) as T
                modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel(repository) as T
                else -> error("Unknown ViewModel")
            }
        }
        setContent {
            val preferences = remember {
                getSharedPreferences("appearance", MODE_PRIVATE).apply {
                    if (!getBoolean("hard_default_applied", false)) edit().putString("difficulty", "HARD")
                        .putBoolean("hard_default_applied", true).apply()
                }
            }
            var defaultDifficulty by remember { mutableStateOf(runCatching { AiDifficulty.valueOf(preferences.getString("difficulty", "HARD")!!) }.getOrDefault(AiDifficulty.HARD)) }
            var feedback by remember { mutableStateOf(FeedbackOptions(
                sounds = preferences.getBoolean("sounds", true),
                vibration = preferences.getBoolean("vibration", true),
                haptics = preferences.getBoolean("haptics", true))) }
            val dark = isSystemInDarkTheme()
            LusalosTheme(darkTheme = dark) {
                val user: UserViewModel = viewModel(factory = factory)
                val game: GameViewModel = viewModel(factory = factory)
                CompositionLocalProvider(LocalFeedbackOptions provides feedback) {
                LusalosApp(user, game, defaultDifficulty, {
                    defaultDifficulty = it; preferences.edit().putString("difficulty", it.name).apply()
                }, feedback, {
                    feedback = it
                    preferences.edit().putBoolean("sounds", it.sounds).putBoolean("vibration", it.vibration).putBoolean("haptics", it.haptics).apply()
                })
                }
            }
        }
    }
}

@Composable
fun LusalosApp(userViewModel: UserViewModel, gameViewModel: GameViewModel,
    defaultDifficulty: AiDifficulty, onDifficultyChange: (AiDifficulty) -> Unit,
    feedback: FeedbackOptions, onFeedbackChange: (FeedbackOptions) -> Unit
) {
    var screen by rememberSaveable { mutableStateOf("splash") }
    val profile by userViewModel.currentUser.collectAsState()
    val error by userViewModel.error.collectAsState()
    val saveError by gameViewModel.saveError.collectAsState()
    val state by gameViewModel.gameState.collectAsState()
    val mode by gameViewModel.gameMode.collectAsState()
    val difficulty by gameViewModel.aiDifficulty.collectAsState()
    val thinking by gameViewModel.isAiThinking.collectAsState()
    BackHandler(enabled = screen != "home" && screen != "splash") { screen = "home" }
    if (screen == "splash") {
        SplashScreen(onSplashComplete = { screen = "home" })
        return
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            if (error != null) {
                Text(error!!, Modifier.padding(16.dp))
                TextButton(onClick = { userViewModel.load() }) { Text("Try Again") }
            }
            if (saveError != null) {
                Text(saveError!!, Modifier.padding(16.dp))
                TextButton(onClick = { gameViewModel.retrySaveResult() }) { Text("Save Again") }
            }
            val player = profile
            if (player == null) {
                if (error == null) CircularProgressIndicator(Modifier.padding(24.dp))
            } else when (screen) {
                "game" -> GameScreen(
                    gameState = state, gameMode = mode, aiDifficulty = difficulty,
                    isAiThinking = thinking, whitePlayerName = "You",
                    whitePlayerRank = player.rank,
                    blackPlayerName = if (mode == GameMode.VS_AI) "AI" else "Player 2",
                    blackPlayerRank = if (mode == GameMode.VS_AI) difficulty.label else "Local",
                    onPointClicked = gameViewModel::onPointClicked,
                    onUndoClicked = gameViewModel::undoMove,
                    onNewGameClicked = { gameViewModel.startNewGame(mode, difficulty) },
                    onResignClicked = gameViewModel::resignGame,
                    onBackToMenu = { screen = "home" }
                )
                else -> MainDashboard(
                    userProfile = player,
                    onStartVsAi = { gameViewModel.startNewGame(GameMode.VS_AI, it); screen = "game" },
                    canResume = state.moveHistory.isNotEmpty() && !state.isGameOver(),
                    onResume = { screen = "game" },
                    difficulty = defaultDifficulty, onDifficultyChange = onDifficultyChange,
                    feedback = feedback, onFeedbackChange = onFeedbackChange
                )
            }
        }
    }
}
