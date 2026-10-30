package com.example.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

@Composable
fun AutoFitLine(text: String, modifier: Modifier = Modifier, maxFontSize: Float = 20f, color: Color = Color.Unspecified, textAlign: TextAlign = TextAlign.Start) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val measured = measurer.measure(text, TextStyle(fontSize = maxFontSize.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp), softWrap = false)
        val factor = (constraints.maxWidth.toFloat() / measured.size.width.coerceAtLeast(1)).coerceAtMost(1f)
        Text(text, modifier = Modifier.fillMaxWidth(), textAlign = textAlign, style = TextStyle(fontSize = (maxFontSize * factor * 0.96f).sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp, color = color), maxLines = 1, softWrap = false)
    }
}
