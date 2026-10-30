package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.*
import com.example.ui.components.AutoFitLine
import com.example.ui.components.LusalosBoardView
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val frames = remember {
        val board = IntArray(24)
        listOf(0, 1, 3, 5).forEach { board[it] = 1 }
        listOf(8, 10, 12, 14).forEach { board[it] = 2 }
        var state = GameState(board = board, whitePiecesInHand = 0, blackPiecesInHand = 0,
            whitePiecesOnBoard = 4, blackPiecesOnBoard = 4, phase = GamePhase.MOVEMENT, action = TurnAction.SELECT_OR_MOVE)
        buildList {
            add(state)
            for ((from, to) in listOf(0 to 7, 8 to 9, 7 to 0, 9 to 8)) {
                state = LusalosEngine.movePiece(state, from, to)
                add(state)
            }
        }
    }
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { delay(3000); onSplashComplete() }
    LaunchedEffect(Unit) { while (isActive) { delay(350); index = (index + 1) % frames.size } }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("splash_screen")
        .background(Brush.verticalGradient(listOf(Color(0xFF252044), WoodDarkSurface, WoodDarkBackground))), contentAlignment = Alignment.Center) {
        val boardSize = minOf(320.dp, maxWidth - 32.dp, (maxHeight - 140.dp).coerceAtLeast(80.dp))
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(boardSize).testTag("splash_app_image")) {
                LusalosBoardView(frames[index], {}, isInteractive = false, animateMoves = true, boardPadding = 0.dp, moveAnimationMillis = 160)
            }
            Text("LUSALOS", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            AutoFitLine("Form a line of three. Capture pieces.", maxFontSize = 18f, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}
