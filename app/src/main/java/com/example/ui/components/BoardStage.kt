package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Shared viewport below the 64 dp app bar. Both Home and gameplay use identical board bounds. */
@Composable
fun BoardStage(modifier: Modifier = Modifier, background: Color = Color.Transparent, expandedHeader: Boolean = false,
    top: @Composable () -> Unit, bottom: @Composable () -> Unit = {}, board: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        val compact = maxHeight < 400.dp
        val frameShape = if (compact) RoundedCornerShape(16.dp) else RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        val boardSlot: @Composable (Modifier) -> Unit = { slotModifier ->
            BoxWithConstraints(slotModifier.background(background, frameShape), contentAlignment = Alignment.Center) {
                board(Modifier.size(minOf(maxWidth, maxHeight)).padding(12.dp))
            }
        }
        if (compact) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Box(Modifier.fillMaxWidth().height(140.dp)) { top() }
                    Box(Modifier.fillMaxWidth().height(84.dp)) { bottom() }
                }
                boardSlot(Modifier.weight(1f).fillMaxHeight().padding(bottom = 84.dp))
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().height(140.dp)) { if (!expandedHeader) top() }
                boardSlot(Modifier.fillMaxWidth().weight(1f))
                Box(Modifier.fillMaxWidth().height(84.dp)) { bottom() }
            }
        }
        if (expandedHeader && !compact) {
            val stageHeight = (maxHeight - 224.dp).coerceAtLeast(0.dp)
            val frameSize = minOf(maxWidth, stageHeight)
            val boardTop = 140.dp + (stageHeight - frameSize) / 2 + 12.dp
            Box(Modifier.fillMaxWidth().height(boardTop)) { top() }
        }
    }
}
