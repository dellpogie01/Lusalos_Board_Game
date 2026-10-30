package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.ui.components.LocalFeedbackOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.AiDifficulty
import com.example.game.GameMode
import com.example.game.GamePhase
import com.example.game.GameState
import com.example.game.Player
import com.example.game.TurnAction
import com.example.ui.components.BoardStage
import com.example.ui.components.GameMoveFeedback
import com.example.ui.components.LusalosBoardView
import com.example.ui.components.RankBadge
import com.example.ui.theme.CapizPearl
import com.example.ui.theme.CapizPearlBorder
import com.example.ui.theme.CoconutShell
import com.example.ui.theme.CoconutShellBorder
import com.example.ui.theme.WoodDarkSurface
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.MillHighlightGlow
import com.example.ui.theme.NarraGoldDark

@Composable
fun GameScreen(
    gameState: GameState,
    gameMode: GameMode,
    aiDifficulty: AiDifficulty,
    isAiThinking: Boolean,
    whitePlayerName: String,
    whitePlayerRank: String,
    blackPlayerName: String,
    blackPlayerRank: String,
    onPointClicked: (Int) -> Unit,
    onUndoClicked: () -> Unit,
    onNewGameClicked: () -> Unit,
    onResignClicked: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    GameMoveFeedback(gameState)
    val touchFeedback = LocalHapticFeedback.current
    val feedbackOptions = LocalFeedbackOptions.current
    var showResignConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_screen"),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WoodDarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToMenu,
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = when (gameMode) {
                            GameMode.VS_AI -> "Against AI"
                            GameMode.LOCAL_PASS_AND_PLAY -> "Pass & Play (Local)"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Row {
                    if (gameMode == GameMode.VS_AI) {
                        IconButton(
                            onClick = onUndoClicked,
                            enabled = !gameState.isGameOver() && !isAiThinking,
                            modifier = Modifier.testTag("undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = { showResignConfirm = true },
                        enabled = !gameState.isGameOver(),
                        modifier = Modifier.testTag("resign_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Resign",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoardStage(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            top = {
                Column {
                    PlayerInfoCard(whitePlayerName, whitePlayerRank, Modifier.testTag("player_header"), true,
                        gameState.whitePiecesInHand, gameState.whitePiecesOnBoard,
                        gameState.currentTurn == Player.WHITE && !gameState.isGameOver(), false)
                    TurnStatusBanner(gameState, gameMode, isAiThinking)
                }
            },
            bottom = {
                Column {
                    PlayerInfoCard(blackPlayerName, blackPlayerRank, Modifier.testTag("ai_header"), false,
                        gameState.blackPiecesInHand, gameState.blackPiecesOnBoard,
                        gameState.currentTurn == Player.BLACK && !gameState.isGameOver(),
                        isAiThinking && gameMode == GameMode.VS_AI)
                    Spacer(Modifier.height(8.dp))
                }
            },
            board = { boardModifier ->
                LusalosBoardView(gameState, {
                    if (feedbackOptions.haptics) touchFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPointClicked(it)
                }, modifier = boardModifier, isInteractive = !gameState.isGameOver() && !isAiThinking, boardPadding = 0.dp)
            }
        )
    }

    // Resign Confirmation Dialog
    if (showResignConfirm) {
        AlertDialog(
            onDismissRequest = { showResignConfirm = false },
            title = { Text("Confirm Resignation", color = MaterialTheme.colorScheme.primary) },
            text = { Text("Are you sure you want to resign this game?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                Button(
                    onClick = {
                        showResignConfirm = false
                        onResignClicked()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson)
                ) {
                    Text("Yes, Resign", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResignConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Game Over Dialog
    if (gameState.isGameOver()) {
        val isWin = (gameState.winner == Player.WHITE)
        val isDraw = gameState.isDraw

        AlertDialog(
            onDismissRequest = { /* Require button click */ },
            title = {
                Text(
                    text = if (isDraw) "It's a Draw!" else if (gameMode == GameMode.LOCAL_PASS_AND_PLAY) "${gameState.winner?.displayName} Wins!" else if (isWin) "Victory! You Win!" else "You Lost!",
                    color = if (isWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = playerMessage(gameState.winReason ?: gameState.message, gameMode),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    if (gameMode == GameMode.VS_AI && isWin) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "+20 Rank Points!",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onNewGameClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Play Again", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onBackToMenu) {
                    Text("Back to Dashboard", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun PlayerInfoCard(
    playerName: String,
    rankName: String,
    modifier: Modifier = Modifier,
    isWhite: Boolean,
    piecesInHand: Int,
    piecesOnBoard: Int,
    isCurrentTurn: Boolean,
    isThinking: Boolean
) {
    val pieceColor = if (isWhite) CapizPearl else CoconutShell
    val borderColor = if (isWhite) CapizPearlBorder else CoconutShellBorder

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrentTurn) MaterialTheme.colorScheme.surfaceVariant else Color(0x66111B35))
            .border(
                width = if (isCurrentTurn) 2.dp else 1.dp,
                color = if (isCurrentTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (maxWidth < 220.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(16.dp).clip(CircleShape).background(pieceColor).border(1.dp, borderColor, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text(playerName, modifier = Modifier.weight(1f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                if (isWhite) RankBadge(rankName, compact = true)
                else RankBadge("Beginner", compact = true, label = rankName, badgeTint = MaterialTheme.colorScheme.primary)
                Text("Hand: $piecesInHand | Board: $piecesOnBoard", fontSize = 10.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                // Piece sample
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(pieceColor)
                        .border(2.dp, borderColor, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = playerName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (isThinking) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(Thinking...)",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (isWhite) RankBadge(rankName = rankName, compact = true)
                    else RankBadge(rankName = "Beginner", compact = true, label = rankName,
                        badgeTint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            // Piece Counts
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "In hand: $piecesInHand | Board: $piecesOnBoard",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                // Graphical dots of pieces in hand
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    for (i in 0 until 9) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(1.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i < piecesInHand) borderColor else Color(0x33888888)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TurnStatusBanner(
    gameState: GameState,
    gameMode: GameMode,
    isAiThinking: Boolean
) {
    val isMillAction = gameState.action == TurnAction.REMOVE_OPPONENT

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isMillAction) MillHighlightGlow.copy(alpha = 0.20f) else Color(0x44111B35))
            .border(
                1.dp,
                if (isMillAction) MillHighlightGlow else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isAiThinking) "AI is thinking..." else playerMessage(gameState.message, gameMode),
            color = if (isMillAction) MillHighlightGlow else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun playerMessage(text: String, mode: GameMode): String = if (mode == GameMode.VS_AI) {
    text.replace("Player 1 wins", "You win").replace("Player 1", "You").replace("Player 2", "AI")
} else text
