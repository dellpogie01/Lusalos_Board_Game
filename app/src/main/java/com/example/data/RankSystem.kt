package com.example.data

import androidx.compose.ui.graphics.Color

data class RankTier(
    val name: String,
    val title: String,
    val minPoints: Int,
    val maxPoints: Int,
    val badgeColor: Color,
    val description: String
)

object RankSystem {
    const val POINTS_PER_WIN = 20
    const val POINTS_PER_LOSS = 0
    const val POINTS_PER_DRAW = 0

    val TIERS = listOf(
        RankTier(
            name = "Beginner",
            title = "Beginner",
            minPoints = 0,
            maxPoints = 99,
            badgeColor = Color(0xFF9CA3AF),
            description = "Beginning your Lusalos journey"
        ),
        RankTier(
            name = "Rising Star",
            title = "Rising Star",
            minPoints = 100,
            maxPoints = 249,
            badgeColor = Color(0xFF9B5CFF),
            description = "Building confidence on the board"
        ),
        RankTier(
            name = "Warrior",
            title = "Warrior",
            minPoints = 250,
            maxPoints = 499,
            badgeColor = Color(0xFF2794FF),
            description = "Ready to face every challenge"
        ),
        RankTier(
            name = "Expert",
            title = "Expert",
            minPoints = 500,
            maxPoints = 999,
            badgeColor = Color(0xFF26C76F),
            description = "Skilled at creating and blocking mills"
        ),
        RankTier(
            name = "Hero",
            title = "Hero",
            minPoints = 1000,
            maxPoints = 1999,
            badgeColor = Color(0xFFFFDC35),
            description = "A respected Lusalos player"
        ),
        RankTier(
            name = "Champion",
            title = "Champion",
            minPoints = 2000,
            maxPoints = 3999,
            badgeColor = Color(0xFFFF8A24),
            description = "A leader in matches and tournaments"
        ),
        RankTier(
            name = "Legend",
            title = "Legend",
            minPoints = 4000,
            maxPoints = Int.MAX_VALUE,
            badgeColor = Color(0xFFFF303F),
            description = "A legendary master of Lusalos"
        )
    )

    fun getRankForPoints(points: Int): RankTier {
        val safePoints = points.coerceAtLeast(0)
        return TIERS.find { safePoints in it.minPoints..it.maxPoints } ?: TIERS.last()
    }

    fun getNextRank(currentRank: RankTier): RankTier? {
        val idx = TIERS.indexOf(currentRank)
        return if (idx >= 0 && idx < TIERS.size - 1) TIERS[idx + 1] else null
    }

    fun getProgressToNextRank(points: Int): Float {
        val currentTier = getRankForPoints(points)
        val nextTier = getNextRank(currentTier) ?: return 1.0f
        val range = (nextTier.minPoints - currentTier.minPoints).toFloat()
        if (range <= 0) return 1.0f
        val progress = (points - currentTier.minPoints).toFloat() / range
        return progress.coerceIn(0f, 1f)
    }

    fun pointsToNextRank(points: Int): Int {
        val currentTier = getRankForPoints(points)
        val nextTier = getNextRank(currentTier) ?: return 0
        return (nextTier.minPoints - points).coerceAtLeast(0)
    }
}
