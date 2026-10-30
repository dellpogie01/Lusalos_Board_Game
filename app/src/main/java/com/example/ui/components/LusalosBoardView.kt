package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.testTag
import com.example.game.BoardConstants
import com.example.game.GameState
import com.example.game.LusalosEngine
import com.example.game.Player
import com.example.game.TurnAction
import com.example.ui.theme.BanigSand
import com.example.ui.theme.CapizPearl
import com.example.ui.theme.CapizPearlBorder
import com.example.ui.theme.CoconutShell
import com.example.ui.theme.CoconutShellBorder
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.MillHighlightGlow
import com.example.ui.theme.NarraGoldPrimary
import com.example.ui.theme.SelectedPieceGlow
import com.example.ui.theme.ValidMoveGlow
import kotlin.math.sqrt
import kotlin.math.sin

@Composable
fun LusalosBoardView(
    gameState: GameState,
    onPointClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    animateMoves: Boolean = false,
    boardPadding: Dp = 12.dp,
    moveAnimationMillis: Int = 600
) {
    val moveProgress = remember { Animatable(1f) }
    LaunchedEffect(gameState.moveHistory.size, animateMoves) {
        if (animateMoves && gameState.moveHistory.isNotEmpty()) {
            moveProgress.snapTo(0f)
            moveProgress.animateTo(1f, tween(moveAnimationMillis, easing = FastOutSlowInEasing))
        } else moveProgress.snapTo(1f)
    }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Calculate valid moves if a piece is selected
    val validMoves = if (gameState.selectedPoint != null && gameState.action == TurnAction.SELECT_OR_MOVE) {
        LusalosEngine.getValidMoves(gameState, gameState.selectedPoint)
    } else emptyList()

    // Calculate removable points if in REMOVE_OPPONENT action
    val removablePoints = if (gameState.action == TurnAction.REMOVE_OPPONENT) {
        (0 until BoardConstants.TOTAL_POINTS).filter { LusalosEngine.canRemovePiece(gameState, it) }
    } else emptyList()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .padding(boardPadding)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_board")
                .pointerInput(isInteractive, gameState) {
                    if (isInteractive) {
                        detectTapGestures { tapOffset ->
                            val boardSize = size.width.toFloat()
                            val tapRadius = boardSize * 0.08f // ~35dp touch radius

                            var closestPt = -1
                            var minDist = Float.MAX_VALUE

                            for (i in 0 until BoardConstants.TOTAL_POINTS) {
                                val (nx, ny) = BoardConstants.POINT_COORDINATES[i]
                                val px = nx * boardSize
                                val py = ny * boardSize
                                val dx = tapOffset.x - px
                                val dy = tapOffset.y - py
                                val dist = sqrt(dx * dx + dy * dy)

                                if (dist < tapRadius && dist < minDist) {
                                    minDist = dist
                                    closestPt = i
                                }
                            }

                            if (closestPt != -1) {
                                onPointClicked(closestPt)
                            }
                        }
                    }
                }
        ) {
            val boardDim = size.width
            val lineStroke = boardDim * 0.012f

            // 1. Draw Narra Wood Board Base
            drawLightWoodBackground(boardDim)

            // 2. Draw Board Lines (The 3 concentric squares + cross lines)
            for ((from, to) in BoardConstants.BOARD_LINES) {
                val (fx, fy) = BoardConstants.POINT_COORDINATES[from]
                val (tx, ty) = BoardConstants.POINT_COORDINATES[to]

                drawLine(
                    color = Color(0x667D8FB5),
                    start = Offset(fx * boardDim, fy * boardDim),
                    end = Offset(tx * boardDim, ty * boardDim),
                    strokeWidth = lineStroke + 2f
                )
                drawLine(
                    color = Color(0xFF445276),
                    start = Offset(fx * boardDim, fy * boardDim),
                    end = Offset(tx * boardDim, ty * boardDim),
                    strokeWidth = lineStroke
                )
            }

            // 3. Draw Highlighted Mills if formed
            if (gameState.highlightedMillPoints.isNotEmpty()) {
                for (pt in gameState.highlightedMillPoints) {
                    val (nx, ny) = BoardConstants.POINT_COORDINATES[pt]
                    drawCircle(
                        color = MillHighlightGlow.copy(alpha = pulseAlpha),
                        radius = boardDim * 0.065f,
                        center = Offset(nx * boardDim, ny * boardDim)
                    )
                }
            }

            val record = if (animateMoves) gameState.moveHistory.lastOrNull() else null
            val progress = if (animateMoves) moveProgress.value else 1f
            // 4. Draw Intersections & Pieces
            for (i in 0 until BoardConstants.TOTAL_POINTS) {
                val (nx, ny) = BoardConstants.POINT_COORDINATES[i]
                val center = Offset(nx * boardDim, ny * boardDim)
                val nodeRadius = boardDim * 0.022f
                val pieceRadius = boardDim * 0.048f

                val occupant = gameState.board[i]
                val isSelected = (gameState.selectedPoint == i)
                val isValidMove = i in validMoves
                val isRemovable = i in removablePoints
                val isLastMove = (i == gameState.lastMoveTo)

                // Background intersection spot
                drawCircle(
                    color = Color(0xFF344366),
                    radius = nodeRadius + 2f,
                    center = center
                )
                drawCircle(
                    color = BanigSand,
                    radius = nodeRadius,
                    center = center
                )

                // Draw valid move destination halo
                if (isValidMove) {
                    drawCircle(
                        color = ValidMoveGlow.copy(alpha = pulseAlpha),
                        radius = pieceRadius * 1.15f,
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = pieceRadius * 0.35f,
                        center = center
                    )
                }

                // Draw removable target halo (Warning red pulse)
                if (isRemovable) {
                    drawCircle(
                        color = DangerCrimson.copy(alpha = pulseAlpha),
                        radius = pieceRadius * 1.35f,
                        center = center
                    )
                }

                // Draw Last Move Indicator Ring
                if (isLastMove) {
                    drawCircle(
                        color = NarraGoldPrimary,
                        radius = pieceRadius * 1.25f,
                        center = center,
                        style = Stroke(width = 3f)
                    )
                }

                val pieceCenter = if (record != null && record.removedPieceAt < 0 && record.from >= 0 && record.to == i) {
                    val (sx, sy) = BoardConstants.POINT_COORDINATES[record.from]
                    Offset(sx * boardDim + (center.x - sx * boardDim) * progress,
                        sy * boardDim + (center.y - sy * boardDim) * progress)
                } else center
                val animatedRadius = if (record != null && record.removedPieceAt < 0 && record.from < 0 && record.to == i)
                    pieceRadius * progress else pieceRadius
                if (record?.removedPieceAt == i && progress < 1f) {
                    drawSphere(center, pieceRadius * (1f - progress), record.player.opponent() == Player.WHITE, false, pulseAlpha)
                }
                // Draw Pieces
                when (occupant) {
                    Player.WHITE.value -> {
                        // Capiz Pearl Piece
                        drawCapizPearlPiece(pieceCenter, animatedRadius.coerceAtLeast(0.1f), isSelected, pulseAlpha)
                    }
                    Player.BLACK.value -> {
                        // Coconut Shell Piece
                        drawCoconutShellPiece(pieceCenter, animatedRadius.coerceAtLeast(0.1f), isSelected, pulseAlpha)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawLightWoodBackground(dim: Float) {
    // Outer border & frame
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF6E5), Color(0xFFEBD9B9)),
            center = Offset(dim / 2f, dim / 2f),
            radius = dim * 0.8f
        ),
        topLeft = Offset.Zero,
        size = Size(dim, dim)
    )

    // Fine, low-contrast grain keeps the pale maple surface readable.
    for (grain in 0..36) {
        val path = Path()
        val baseline = dim * grain / 36f
        path.moveTo(0f, baseline)
        for (step in 1..32) {
            val x = dim * step / 32f
            val wave = sin(step * 0.25f + grain * 0.8f) * dim * 0.004f +
                sin(step * 0.7f + grain) * dim * 0.0015f
            path.lineTo(x, baseline + wave)
        }
        drawPath(path, Color(0x14957951), style = Stroke(width = (dim * 0.0015f).coerceAtLeast(0.5f)))
    }


}

private fun DrawScope.drawCapizPearlPiece(center: Offset, radius: Float, isSelected: Boolean, pulseAlpha: Float) {
    drawSphere(center, radius, true, isSelected, pulseAlpha)
}

private fun DrawScope.drawCoconutShellPiece(center: Offset, radius: Float, isSelected: Boolean, pulseAlpha: Float) {
    drawSphere(center, radius, false, isSelected, pulseAlpha)
}

private fun DrawScope.drawSphere(center: Offset, radius: Float, white: Boolean, selected: Boolean, pulseAlpha: Float) {
    if (selected) drawCircle(SelectedPieceGlow.copy(alpha = pulseAlpha), radius * 1.35f, center)
    drawCircle(Color.Black.copy(alpha = 0.45f), radius * 1.04f, center + Offset(radius * 0.12f, radius * 0.22f))
    val light = center - Offset(radius * 0.35f, radius * 0.38f)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = if (white) arrayOf(0f to Color.White, 0.35f to Color(0xFFF5F7FF), 0.7f to Color(0xFFB7C3DC), 1f to Color(0xFF66738F))
                else arrayOf(0f to Color(0xFF8993A7), 0.25f to Color(0xFF414B60), 0.65f to Color(0xFF111722), 1f to Color(0xFF02040A)),
            center = light, radius = radius * 1.5f
        ), radius = radius, center = center
    )
    drawCircle(brush = Brush.radialGradient(listOf(Color.White.copy(alpha = if (white) 0.9f else 0.55f), Color.Transparent), center = light, radius = radius * 0.35f), radius = radius * 0.35f, center = light)
}
