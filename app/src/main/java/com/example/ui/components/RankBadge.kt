package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RankSystem

@Composable
fun RankBadge(
    rankName: String,
    modifier: Modifier = Modifier,
    points: Int? = null,
    compact: Boolean = false,
    label: String? = null,
    badgeTint: Color? = null
) {
    val tier = RankSystem.TIERS.find { it.name.equals(rankName, ignoreCase = true) }
        ?: RankSystem.TIERS.first()

    val badgeColor = badgeTint ?: tier.badgeColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeColor.copy(alpha = 0.20f))
            .border(1.dp, badgeColor.copy(alpha = 0.70f), RoundedCornerShape(12.dp))
            .padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 4.dp else 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(if (compact) 14.dp else 18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label ?: tier.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 12.sp else 14.sp
            )
            if (points != null && !compact) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${points} pts",
                    color = badgeColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
