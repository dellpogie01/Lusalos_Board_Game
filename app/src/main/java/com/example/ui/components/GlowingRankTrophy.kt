package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import com.example.data.RankTier

@Composable
fun GlowingRankTrophy(tier: RankTier, modifier: Modifier = Modifier) {
    val animation = rememberInfiniteTransition(label = "trophy glow")
    val glow = animation.animateFloat(0.25f, 0.5f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "glow strength")
    Box(modifier.testTag("rank_trophy"), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(tier.badgeColor.copy(alpha = glow.value), Color.Transparent)), radius = size.minDimension / 2f)
        }
        Icon(Icons.Default.EmojiEvents, contentDescription = "${tier.name} trophy", tint = Color.White,
            modifier = Modifier.fillMaxSize(0.8f).drawWithCache {
                val base = tier.badgeColor
                val finish = Brush.linearGradient(listOf(
                    lerp(base, Color.White, 0.65f), base,
                    lerp(base, Color.White, 0.3f), lerp(base, Color.Black, 0.32f), base))
                val paint = Paint()
                onDrawWithContent {
                    drawContext.canvas.saveLayer(Rect(Offset.Zero, size), paint)
                    drawContent()
                    drawRect(finish, blendMode = BlendMode.SrcIn)
                    drawContext.canvas.restore()
                }
            })
    }
}
