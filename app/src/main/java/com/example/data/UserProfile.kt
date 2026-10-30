package com.example.data

import com.example.game.AiDifficulty

data class MatchTrendPoint(val matchNumber: Long, val winRate: Float, val aiWinRate: Float = 0f, val difficulty: AiDifficulty? = null)

data class UserProfile(
    val id: String = "local",
    val userId: String = "local",
    val username: String = "Player",
    val avatarId: String = "default",
    val rank: String = "Beginner",
    val points: Long = 0,
    val wins: Long = 0,
    val losses: Long = 0,
    val draws: Long = 0,
    val matchesPlayed: Long = 0,
    val highestRank: String = "Beginner",
    val winRateHistory: List<MatchTrendPoint> = emptyList()
) {
    val winRate: Float get() = if (matchesPlayed > 0) wins.toFloat() / matchesPlayed * 100 else 0f
}
