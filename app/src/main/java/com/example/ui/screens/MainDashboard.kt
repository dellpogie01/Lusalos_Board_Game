package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowingRankTrophy
import com.example.ui.components.FeedbackOptions
import com.example.data.RankSystem
import com.example.data.UserProfile
import com.example.game.AiDifficulty
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(userProfile: UserProfile, onStartVsAi: (AiDifficulty) -> Unit,
    onResume: () -> Unit, canResume: Boolean,
    difficulty: AiDifficulty, onDifficultyChange: (AiDifficulty) -> Unit,
    feedback: FeedbackOptions = FeedbackOptions(), onFeedbackChange: (FeedbackOptions) -> Unit = {}
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var settings by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = settings || tab != 0) { if (settings) settings = false else tab = 0 }
    val titles = listOf("Home", "Stats", "How to Play", "Player Rank")
    val tags = listOf("home", "stats", "rules", "rank")
    val icons = listOf(Icons.Default.Home, Icons.Default.BarChart, Icons.Default.MenuBook, Icons.Default.EmojiEvents)
    val colors = MaterialTheme.colorScheme
    val home = !settings && tab == 0
    Scaffold(modifier = Modifier.fillMaxSize().testTag("dashboard"), containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(modifier = Modifier.shadow(10.dp).zIndex(1f), title = {
                Column {
                    AppTitle()
                    com.example.ui.components.AutoFitLine("Form a line of three. Capture pieces.", maxFontSize = 14f)
                }
            }, actions = {
                IconButton(onClick = { settings = !settings }, modifier = Modifier.testTag("open_settings")) {
                    Icon(if (settings) Icons.Default.Close else Icons.Default.Settings,
                        contentDescription = if (settings) "Close Settings" else "Settings")
                }
            }, windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WoodDarkSurface, titleContentColor = Color.White, actionIconContentColor = Color.White))
        },
        bottomBar = {
            NavigationBar(containerColor = colors.surface, windowInsets = WindowInsets(0, 0, 0, 0)) {
                titles.forEachIndexed { index, title ->
                    NavigationBarItem(selected = tab == index && !settings, onClick = { tab = index; settings = false },
                        icon = { Icon(icons[index], contentDescription = null) }, label = { Text(title, fontSize = 10.sp, maxLines = 1) },
                        modifier = Modifier.testTag("nav_${tags[index]}"))
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding(),
            bottom = if (home) 0.dp else padding.calculateBottomPadding())
            .background(Brush.verticalGradient(listOf(colors.primaryContainer.copy(alpha = 0.25f), colors.background)))) {
            val compact = maxHeight < 400.dp
            if (home) HomeDashboard(userProfile, difficulty, canResume, onResume, onStartVsAi)
            else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = 560.dp).fillMaxSize().padding(if (compact) 12.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 16.dp)) {
                    when {
                        settings -> SettingsContent(difficulty, onDifficultyChange, feedback, onFeedbackChange, Modifier.weight(1f))
                        tab == 1 -> StatsContent(userProfile, Modifier.weight(1f))
                        tab == 2 -> HowToPlayContent(Modifier.weight(1f))
                        else -> RankPages(userProfile, compact)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTitle() {
    Text("LUSALOS", maxLines = 1, color = Color.White, fontSize = 28.sp,
        fontWeight = FontWeight.Black, letterSpacing = 1.sp)
}

@Composable
private fun DashboardCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(colors.primaryContainer.copy(alpha = 0.3f), colors.surfaceVariant))).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ColumnScope.RankPages(profile: UserProfile, compact: Boolean) {
    var page by rememberSaveable { mutableStateOf(0) }
    val rank = RankSystem.getRankForPoints(profile.points.toInt())
    val tier = RankSystem.TIERS[page]
    if (compact) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${rank.name} | ${profile.points} pts", style = MaterialTheme.typography.labelMedium)
            Text(tier.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    } else {
    Text("Player Rank", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    DashboardCard {
        StatRow("Current rank", rank.name)
        StatRow("Points", profile.points.toString())
        LinearProgressIndicator(progress = { RankSystem.getProgressToNextRank(profile.points.toInt()) }, modifier = Modifier.fillMaxWidth())
        Text(RankSystem.getNextRank(rank)?.let { "${RankSystem.pointsToNextRank(profile.points.toInt())} points to ${it.name}" } ?: "Highest rank reached", style = MaterialTheme.typography.bodySmall)
    }
    DashboardCard {
        Text(tier.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("${tier.minPoints}${if (tier.maxPoints == Int.MAX_VALUE) "+" else " - ${tier.maxPoints}"} points", style = MaterialTheme.typography.bodyMedium)
        Text(tier.description, style = MaterialTheme.typography.bodySmall)
    }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = { page-- }, enabled = page > 0) { Text("Previous") }
        Text("${page + 1} / ${RankSystem.TIERS.size}", style = MaterialTheme.typography.labelMedium)
        TextButton(onClick = { page++ }, enabled = page < RankSystem.TIERS.lastIndex) { Text("Next") }
    }
    BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        GlowingRankTrophy(tier, Modifier.size(minOf(maxHeight, maxWidth, 180.dp)))
    }
}

