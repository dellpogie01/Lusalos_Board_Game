package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UserProfile

@Composable
fun StatsContent(profile: UserProfile, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val compact = maxHeight < 330.dp
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 12.dp)) {
            Text("Your Stats", style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (compact) {
                Text("${profile.matchesPlayed} played | ${profile.wins} wins | ${profile.losses} losses | ${profile.draws} draws", style = MaterialTheme.typography.labelSmall)
            } else Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Matches played" to profile.matchesPlayed.toString(), "Wins" to profile.wins.toString(),
                        "Losses" to profile.losses.toString(), "Draws" to profile.draws.toString(), "Win rate" to "${"%.0f".format(profile.winRate)}%")
                        .forEach { (label, value) ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(label); Text(value, fontWeight = FontWeight.Bold)
                            }
                        }
                }
            }
            WinRateGraph(profile, Modifier.fillMaxWidth().weight(1f), compact)
        }
    }
}

@Composable
private fun WinRateGraph(profile: UserProfile, modifier: Modifier, compact: Boolean) {
    val colors = MaterialTheme.colorScheme
    val aiColor = Color(0xFFE5793E)
    var selectedPoint by remember(profile.winRateHistory) { mutableIntStateOf(profile.winRateHistory.lastIndex) }
    val selectedLevel = profile.winRateHistory.getOrNull(selectedPoint)?.difficulty?.label ?: "Not recorded"
    Card(modifier.testTag("stats_graph").semantics { contentDescription = "You and AI win rates over the last ${profile.winRateHistory.size.minus(1).coerceAtLeast(0)} saved AI matches" },
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
        Column(Modifier.fillMaxSize().padding(if (compact) 6.dp else 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Win rate over time", style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("You", style = MaterialTheme.typography.labelSmall, color = colors.primary, fontWeight = FontWeight.Bold)
                    Text("AI", style = MaterialTheme.typography.labelSmall, color = aiColor, fontWeight = FontWeight.Bold)
                }
                Text("Level: $selectedLevel", style = MaterialTheme.typography.labelSmall, modifier = Modifier.testTag("graph_level"))
            }
            if (profile.winRateHistory.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        for (i in 0..4) drawLine(colors.outline.copy(alpha = 0.15f), Offset(0f, size.height * i / 4), Offset(size.width, size.height * i / 4), 1.dp.toPx())
                    }
                    Text("Complete an AI match to start your graph.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    Column(Modifier.fillMaxHeight().width(30.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("100%", style = MaterialTheme.typography.labelSmall)
                        Text("0%", style = MaterialTheme.typography.labelSmall)
                    }
                    Canvas(Modifier.fillMaxSize().padding(4.dp).testTag("stats_plot").pointerInput(profile.winRateHistory) {
                        detectTapGestures { point ->
                            selectedPoint = ((point.x / size.width.coerceAtLeast(1)) * (profile.winRateHistory.size - 1)).roundToInt().coerceIn(0, profile.winRateHistory.lastIndex)
                        }
                    }) {
                        for (i in 0..4) drawLine(colors.outline.copy(alpha = 0.3f), Offset(0f, size.height * i / 4), Offset(size.width, size.height * i / 4), 1.dp.toPx())
                        val points = profile.winRateHistory.mapIndexed { index, point ->
                            Offset(size.width * index / (profile.winRateHistory.size - 1).coerceAtLeast(1), size.height * (1 - point.winRate.coerceIn(0f, 100f) / 100))
                        }
                        val line = Path().apply { points.forEachIndexed { i, point -> if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) } }
                        val area = Path().apply { addPath(line); lineTo(size.width, size.height); lineTo(0f, size.height); close() }
                        drawPath(area, Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.28f), colors.primary.copy(alpha = 0f))))
                        drawPath(line, colors.primary, style = Stroke(2.dp.toPx()))
                        points.forEach { drawCircle(colors.primary, 3.dp.toPx(), it) }
                        val aiPoints = profile.winRateHistory.mapIndexed { index, point ->
                            Offset(size.width * index / (profile.winRateHistory.size - 1).coerceAtLeast(1), size.height * (1 - point.aiWinRate.coerceIn(0f, 100f) / 100))
                        }
                        val aiLine = Path().apply { aiPoints.forEachIndexed { i, point -> if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) } }
                        drawPath(aiLine, aiColor, style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 4.dp.toPx()))))
                        aiPoints.forEach { drawCircle(aiColor, 3.dp.toPx(), it) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Match ${profile.winRateHistory.first().matchNumber}", style = MaterialTheme.typography.labelSmall)
                    Text("Match ${profile.winRateHistory.last().matchNumber}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
