package com.example

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.UserProfile
import com.example.game.AiDifficulty
import com.example.ui.screens.MainDashboard
import com.example.ui.theme.LusalosTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.game.BoardConstants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineGameplayTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun tap(point: Int) {
        val (x, y) = BoardConstants.POINT_COORDINATES[point]
        rule.onNodeWithTag("game_board").performTouchInput { click(Offset(width * x, height * y)) }
        rule.waitForIdle()
    }

    private fun screenshot(name: String) {
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        val output = androidx.test.platform.app.InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val directory = if (output != null) java.io.File(output) else rule.activity.getExternalFilesDir(null)!!
        directory.mkdirs()
        val file = java.io.File(directory, "$name.png")
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun homeNavigationAndSettingsAreAvailable() {
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("dashboard").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("LUSALOS").assertIsDisplayed()
        rule.onNodeWithContentDescription("Lusalos logo").assertDoesNotExist()
        rule.onNodeWithText("Form a line of three. Capture pieces.").assertIsDisplayed()
        rule.onNodeWithTag("nav_home").assertIsDisplayed()
        rule.onNodeWithText("Play with a Friend").assertDoesNotExist()
        rule.onNodeWithTag("ai_demo").assertIsDisplayed()
        rule.onNodeWithText("Form a line of three. Capture pieces.").assertIsDisplayed()
        rule.waitUntil(10_000) {
            val value = rule.onNodeWithTag("ai_demo").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.StateDescription]
            value.removePrefix("Demo moves: ").toInt() >= 3
        }
        screenshot("home-modern")
        rule.onNodeWithTag("open_settings").performClick()
        rule.onNodeWithText("Game Difficulty").assertIsDisplayed()
        rule.onNodeWithText("Very Hard").performClick().assertIsSelected()
        rule.onNodeWithText("Hardest").performClick().assertIsSelected()
        rule.onNodeWithText("Hard").performClick().assertIsSelected()
        rule.onNodeWithTag("feedback_sounds").assertIsDisplayed()
        val originalSound = rule.activity.getSharedPreferences("appearance", android.content.Context.MODE_PRIVATE).getBoolean("sounds", true)
        rule.onNodeWithTag("feedback_sounds").performClick()
        org.junit.Assert.assertEquals(!originalSound, rule.activity.getSharedPreferences("appearance", android.content.Context.MODE_PRIVATE).getBoolean("sounds", true))
        rule.onNodeWithTag("feedback_sounds").performClick()
        rule.onNodeWithTag("feedback_vibrate").assertIsDisplayed()
        rule.onNodeWithTag("feedback_haptics").assertIsDisplayed()
        screenshot("settings-controls")
        rule.onNodeWithTag("nav_home").performClick()
        rule.onNodeWithText("Difficulty: Hard").assertDoesNotExist()
        rule.onNodeWithText("Welcome, Player").assertDoesNotExist()
        rule.onNodeWithTag("nav_stats").performClick()
        rule.onNodeWithText("Win rate").assertIsDisplayed()
        rule.onNodeWithTag("stats_graph").assertIsDisplayed()
        screenshot("stats-modern")
        rule.onNodeWithTag("nav_rules").performClick()
        rule.onNodeWithText("The board").assertIsDisplayed()
        rule.onNodeWithTag("tutorial_board").assertIsDisplayed()
        val tutorialBounds = rule.onNodeWithTag("game_board").fetchSemanticsNode().boundsInRoot
        repeat(5) { lesson ->
            rule.onNodeWithTag("tutorial_board").assertIsDisplayed()
            rule.mainClock.advanceTimeBy(2200)
            rule.waitUntil(10000) {
                rule.onNodeWithTag("tutorial_board").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.StateDescription] != "Lesson $lesson frame 0"
            }
            org.junit.Assert.assertEquals(tutorialBounds, rule.onNodeWithTag("game_board").fetchSemanticsNode().boundsInRoot)
            screenshot("tutorial-$lesson")
            rule.onNodeWithText("Next").performClick()
        }
        rule.onNodeWithText("Win the game").assertIsDisplayed()
        org.junit.Assert.assertEquals(tutorialBounds, rule.onNodeWithTag("game_board").fetchSemanticsNode().boundsInRoot)
        screenshot("rules-modern")
        rule.onNodeWithTag("nav_rank").performClick()
        rule.onNodeWithText("Current rank").assertIsDisplayed()
        rule.onNodeWithTag("rank_trophy").assertIsDisplayed()
        rule.onNodeWithContentDescription("Beginner trophy").assertIsDisplayed()
        screenshot("rank-modern")
        rule.onNodeWithText("Next").performClick()
        rule.onNodeWithContentDescription("Rising Star trophy").assertIsDisplayed()
        screenshot("rank-rising-star")
        repeat(5) { rule.onNodeWithText("Next").performClick() }
        rule.onNodeWithContentDescription("Legend trophy").assertIsDisplayed()
        screenshot("rank-legend")
        rule.onNodeWithTag("open_settings").performClick()
        rule.onNodeWithText("Hard").performClick()
        rule.onNodeWithText("Theme").assertDoesNotExist()
    }

    @Test fun compactWindowKeepsNavigationAndSettingsVisibleWithoutScrolling() {
        rule.activity.runOnUiThread {
            rule.activity.setContent {
                var difficulty by remember { mutableStateOf(AiDifficulty.MEDIUM) }
                LusalosTheme(darkTheme = true) {
                    Surface {
                        Box(Modifier.size(320.dp, 320.dp)) {
                            MainDashboard(UserProfile(matchesPlayed = 3, wins = 2, losses = 1,
                                winRateHistory = listOf(com.example.data.MatchTrendPoint(0, 0f), com.example.data.MatchTrendPoint(1, 100f, 0f, AiDifficulty.EASY), com.example.data.MatchTrendPoint(2, 50f, 50f, AiDifficulty.HARD), com.example.data.MatchTrendPoint(3, 66.7f, 33.3f, AiDifficulty.HARDEST))),
                                {}, {}, false, difficulty, { difficulty = it })
                        }
                    }
                }
            }
        }
        rule.onNodeWithText("Play Against AI").assertIsDisplayed()
        rule.onNodeWithTag("ai_demo").assertIsDisplayed()
        screenshot("compact-home")
        rule.onNodeWithTag("open_settings").performClick()
        rule.onNodeWithText("Medium").assertIsDisplayed()
        rule.onNodeWithText("Very Hard").performClick().assertIsSelected()
        rule.onNodeWithText("Hardest").performClick().assertIsSelected()
        rule.onNodeWithText("Hard").performClick().assertIsSelected()
        rule.onNodeWithText("Theme").assertDoesNotExist()
        rule.onNodeWithTag("feedback_sounds").assertIsDisplayed()
        rule.onNodeWithTag("feedback_vibrate").assertIsDisplayed()
        rule.onNodeWithTag("feedback_haptics").assertIsDisplayed()
        screenshot("compact-settings")
        rule.onNodeWithTag("feedback_sounds").assertIsDisplayed()
        rule.onNodeWithTag("feedback_vibrate").assertIsDisplayed()
        rule.onNodeWithTag("feedback_haptics").assertIsDisplayed()
        screenshot("compact-feedback")
        listOf("home", "stats", "rules", "rank").forEach { destination ->
            rule.onNodeWithTag("nav_$destination").performClick().assertIsDisplayed()
            rule.onAllNodes(hasScrollAction()).assertCountEquals(0)
            if (destination == "rules") screenshot("compact-tutorial")
            if (destination == "stats") {
                rule.onNodeWithTag("stats_graph").assertIsDisplayed()
                rule.onNodeWithText("Level: Hardest").assertIsDisplayed()
                screenshot("compact-stats-graph")
                rule.onNodeWithTag("stats_plot").performTouchInput { click(Offset(width / 3f, height / 2f)) }
                rule.onNodeWithText("Level: Easy").assertIsDisplayed()
            }
        }
        screenshot("compact-rank")
    }

    @Test fun splashShowsEnlargedAnimatedBoard() {
        rule.activity.runOnUiThread {
            rule.activity.setContent {
                LusalosTheme(darkTheme = true) {
                    com.example.ui.screens.SplashScreen(onSplashComplete = {})
                }
            }
        }
        rule.onNodeWithTag("splash_app_image").assertIsDisplayed()
        screenshot("splash-large-animation")
    }

    @Test fun aiRespondsAndUndoRestoresHumanTurn() {
        rule.waitUntil(10_000) { runCatching { rule.onAllNodesWithText("Play Against AI").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        rule.onNodeWithTag("open_settings").performClick()
        rule.onNodeWithText("Hardest").performClick().assertIsSelected()
        rule.onNodeWithTag("nav_home").performClick()
        val homeBoardBounds = rule.onNodeWithTag("game_board").fetchSemanticsNode().boundsInRoot
        screenshot("home-spacing")
        rule.onNodeWithText("Play Against AI").performClick()
        val boardBounds = rule.onNodeWithTag("game_board").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertEquals(homeBoardBounds, boardBounds)
        val playerBounds = rule.onNodeWithTag("player_header").fetchSemanticsNode().boundsInRoot
        val aiBounds = rule.onNodeWithTag("ai_header").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertTrue(playerBounds.bottom < aiBounds.top)
        rule.onNodeWithText("You").assertIsDisplayed()
        rule.onNodeWithText("AI").assertIsDisplayed()
        rule.onNodeWithText("Against AI").assertIsDisplayed()
        rule.onNodeWithText("Hardest").assertIsDisplayed()
        rule.onAllNodesWithText("Beginner").assertCountEquals(1)
        screenshot("game-player-top")
        tap(0)
        rule.waitUntil(10_000) { rule.onAllNodesWithText("In hand: 8 | Board: 1").fetchSemanticsNodes().size == 2 }
        org.junit.Assert.assertEquals(boardBounds, rule.onNodeWithTag("game_board").fetchSemanticsNode().boundsInRoot)
        rule.onNodeWithTag("undo_button").performClick()
        rule.onAllNodesWithText("In hand: 9 | Board: 0").assertCountEquals(2)
        tap(2)
        rule.waitUntil(10_000) { rule.onAllNodesWithText("In hand: 8 | Board: 1").fetchSemanticsNodes().size == 2 }
        rule.onNodeWithTag("game_back_button").performClick()
        rule.onNodeWithText("Resume Game").assertIsDisplayed()
        rule.onNodeWithText("Play Against AI").assertDoesNotExist()
        rule.onNodeWithText("Resume Game").performClick()
        rule.onAllNodesWithText("In hand: 8 | Board: 1").assertCountEquals(2)
    }
}

