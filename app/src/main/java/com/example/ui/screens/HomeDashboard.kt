package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.UserProfile
import com.example.game.AiDifficulty
import com.example.ui.components.*

@Composable
fun HomeDashboard(profile: UserProfile, difficulty: AiDifficulty, canResume: Boolean,
    onResume: () -> Unit, onPlay: (AiDifficulty) -> Unit, modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val compact = maxHeight < 400.dp
        AiDemoBoard(Modifier.fillMaxSize(), compact = compact) {
            PlayButton(onClick = { if (canResume) onResume() else onPlay(difficulty) }, resume = canResume, height = if (compact) 48.dp else 52.dp)
            Text("+20 points for every win", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, modifier = Modifier.height(16.dp))
        }
    }
}
