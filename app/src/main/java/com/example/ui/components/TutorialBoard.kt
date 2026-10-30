package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.game.TutorialLessons
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun TutorialBoard(lesson: Int, modifier: Modifier = Modifier, compact: Boolean = false) {
    val frames = remember(lesson) { TutorialLessons.frames(lesson) }
    var index by remember(lesson) { mutableStateOf(0) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lesson, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                delay(if (index == frames.lastIndex) 2600 else 1800)
                index = (index + 1) % frames.size
            }
        }
    }
    val frame = frames[index]
    Card(modifier.testTag("tutorial_board").semantics { stateDescription = "Lesson $lesson frame $index" },
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxSize().padding(if (compact) 6.dp else 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (compact) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(TutorialLessons.titles[lesson], fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                        val brief = listOf(
                            "Nine pieces each. You play white; the AI plays black.",
                            "Place on empty points. Three in a row forms a mill.",
                            "Make a mill, then capture an unprotected opponent piece.",
                            "Slide along a line to an adjacent empty point.",
                            "With three pieces, fly to any empty point.",
                            "Leave the opponent with two pieces or no legal moves.")
                        Text(brief[lesson], fontSize = 10.sp, lineHeight = 12.sp, maxLines = 3)
                    }
                    TutorialCanvas(frame.state, lesson, Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Column(Modifier.fillMaxWidth().height(120.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(TutorialLessons.titles[lesson], style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(TutorialLessons.instructions[lesson], style = MaterialTheme.typography.bodyMedium, maxLines = 4)
                }
                TutorialCanvas(frame.state, lesson, Modifier.fillMaxWidth().weight(1f))
            }
            Text(frame.caption, style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
                maxLines = if (compact) 1 else 2, modifier = Modifier.height(if (compact) 18.dp else 48.dp))
        }
    }
}

@Composable
private fun TutorialCanvas(state: com.example.game.GameState, lesson: Int, modifier: Modifier) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        key(lesson) {
            LusalosBoardView(state, {}, modifier = Modifier.size(minOf(maxWidth, maxHeight)),
                isInteractive = false, animateMoves = true, boardPadding = 0.dp)
        }
    }
}
