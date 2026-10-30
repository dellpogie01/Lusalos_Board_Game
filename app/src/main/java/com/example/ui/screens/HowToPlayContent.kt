package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.TutorialLessons
import com.example.ui.components.TutorialBoard

@Composable
fun HowToPlayContent(modifier: Modifier = Modifier) {
    var page by rememberSaveable { mutableStateOf(0) }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val compact = maxHeight < 330.dp
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 10.dp)) {
            if (!compact) Text("How to Play", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TutorialBoard(page, Modifier.fillMaxWidth().weight(1f), compact = compact)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { page-- }, enabled = page > 0) { Text("Previous") }
                Text("${page + 1} / ${TutorialLessons.titles.size}", style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = { page++ }, enabled = page < TutorialLessons.titles.lastIndex) { Text("Next") }
            }
        }
    }
}
