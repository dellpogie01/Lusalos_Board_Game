package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.game.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
fun AiDemoBoard(modifier: Modifier = Modifier, compact: Boolean = false, controls: @Composable ColumnScope.() -> Unit = {}) {
    var state by remember { mutableStateOf(LusalosEngine.initialGameState()) }
    GameMoveFeedback(state, demo = true)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            var match = DemoMatch()
            state = match.state
            while (isActive) {
                delay(if (state.isGameOver()) 2800 else if (state.action == TurnAction.REMOVE_OPPONENT) 1900 else 1300)
                if (state.isGameOver()) {
                    match = DemoMatch()
                    state = match.state
                } else state = withContext(Dispatchers.Default) { match.advance() }
            }
        }
    }
    val demoTextStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
    val caption = if (compact) when {
        state.isGameOver() -> "Starting again..."
        state.action == TurnAction.REMOVE_OPPONENT -> "Mill! Capture a piece."
        state.phase == GamePhase.FLYING -> "Fly to an empty point."
        state.phase == GamePhase.MOVEMENT -> "Move to a nearby point."
        else -> "Place a piece."
    } else when {
        state.isDraw -> "Draw. Starting again..."
        state.winner != null -> "${if (state.winner == Player.WHITE) "White" else "Black"} wins! Starting again..."
        state.action == TurnAction.REMOVE_OPPONENT -> "Mill! Capture an opponent piece."
        state.phase == GamePhase.FLYING -> "Three pieces left: fly to any empty point."
        state.phase == GamePhase.MOVEMENT -> "Move along a line to an empty point."
        else -> "Take turns placing pieces. Make a line of three."
    }
    BoardStage(modifier.testTag("ai_demo").semantics { stateDescription = "Demo moves: ${state.moveHistory.size}" },
        background = Color.Transparent, expandedHeader = !compact,
        top = {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val infoHeight = if (compact) 52.dp else (maxHeight - 120.dp).coerceIn(40.dp, 100.dp)
                Column(Modifier.fillMaxSize().padding(top = if (compact) 12.dp else 20.dp, bottom = if (compact) 0.dp else 12.dp),
                    verticalArrangement = if (compact) Arrangement.spacedBy(6.dp) else Arrangement.SpaceBetween) {
                    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)) { controls() }
                    Column(Modifier.fillMaxWidth().height(infoHeight)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = if (infoHeight >= 80.dp) 12.dp else 0.dp)) {
                        Text(if (compact) "AI vs AI" else "AI vs AI - Learn by watching", style = demoTextStyle,
                            modifier = Modifier.height(20.dp), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(caption, style = demoTextStyle, fontWeight = FontWeight.Medium,
                            maxLines = if (infoHeight < 52.dp) 1 else 2, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).testTag("demo_caption"))
                    }
                }
            }
        },
        board = { boardModifier ->
            LusalosBoardView(state, {}, modifier = boardModifier.shadow(8.dp, RectangleShape, clip = false),
                isInteractive = false, animateMoves = true, boardPadding = 0.dp)
        }
    )
}
