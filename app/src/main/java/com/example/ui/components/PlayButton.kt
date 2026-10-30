package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayButton(onClick: () -> Unit, modifier: Modifier = Modifier, resume: Boolean = false, height: Dp = 52.dp) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val labelSize = if (maxWidth < 180.dp) 14.sp else 18.sp
        Button(onClick, Modifier.fillMaxWidth().height(height),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurface), contentPadding = PaddingValues(horizontal = 8.dp)) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (resume) "Resume Game" else "Play Against AI", fontSize = labelSize, maxLines = 1)
        }
    }
}
