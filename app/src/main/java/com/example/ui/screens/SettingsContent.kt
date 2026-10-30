package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.AiDifficulty
import com.example.ui.components.FeedbackOptions

@Composable
fun SettingsContent(difficulty: AiDifficulty, onDifficulty: (AiDifficulty) -> Unit,
    feedback: FeedbackOptions, onFeedback: (FeedbackOptions) -> Unit, modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val compact = maxHeight < 400.dp
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 16.dp)) {
            Text("Settings", style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (compact) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsPanel(Modifier.weight(1f), true) { DifficultyButtons(difficulty, onDifficulty, true) }
                SettingsPanel(Modifier.weight(1f), true) { FeedbackControls(feedback, onFeedback, true) }
            } else {
                SettingsPanel(Modifier.fillMaxWidth(), false) { DifficultyButtons(difficulty, onDifficulty, false) }
                SettingsPanel(Modifier.fillMaxWidth(), false) { FeedbackControls(feedback, onFeedback, false) }
            }
        }
    }
}

@Composable
private fun SettingsPanel(modifier: Modifier, compact: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier, shape = RoundedCornerShape(if (compact) 10.dp else 20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(if (compact) 4.dp else 16.dp), verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 8.dp), content = content)
    }
}

@Composable
private fun DifficultyButtons(selected: AiDifficulty, onSelect: (AiDifficulty) -> Unit, compact: Boolean) {
    Text("Game Difficulty", fontSize = if (compact) 11.sp else 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    AiDifficulty.values().toList().chunked(if (compact) 2 else 3).forEach { group ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            group.forEach { level ->
                val pressed = selected == level
                Box(Modifier.weight(1f).height(if (compact) 30.dp else 52.dp)) {
                    Surface(onClick = { onSelect(level) }, modifier = Modifier.fillMaxWidth()
                        .height(if (compact) 27.dp else 46.dp).offset(y = if (pressed) 3.dp else 0.dp)
                        .semantics { this.selected = pressed }.testTag("difficulty_${level.name}"),
                        shape = RoundedCornerShape(if (compact) 6.dp else 12.dp),
                        shadowElevation = if (pressed) 0.dp else 5.dp,
                        color = if (pressed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (pressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(level.label, fontSize = if (compact) 10.sp else 14.sp, fontWeight = if (pressed) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackControls(options: FeedbackOptions, onChange: (FeedbackOptions) -> Unit, compact: Boolean) {
    Text("Sound & Feedback", fontSize = if (compact) 11.sp else 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    listOf("Sounds" to options.sounds, "Vibrate" to options.vibration, "Haptics" to options.haptics).forEachIndexed { index, (label, enabled) ->
        Row(Modifier.fillMaxWidth().height(if (compact) 30.dp else 48.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = if (compact) 10.sp else 14.sp)
            Switch(enabled, { value -> onChange(when (index) { 0 -> options.copy(sounds = value); 1 -> options.copy(vibration = value); else -> options.copy(haptics = value) }) },
                modifier = Modifier.height(if (compact) 28.dp else 48.dp).scale(if (compact) 0.72f else 1f).testTag("feedback_${label.lowercase()}"))
        }
    }
}
