package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.AiDifficulty
import com.example.game.GameMode
import com.example.game.Player
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LusalosRepositoryTest {
    private lateinit var context: Context

    @Before fun reset() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase("lusalos.db")
        context.getSharedPreferences("lusalos_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun profileAndResultsSurviveNewRepositoryAndDuplicateResults() = runBlocking {
        val repository = LusalosRepository(context)
        repository.loadProfile()
        repository.updateProfile("  Tala  ", "salakot_mascot")
        repository.recordResult("win", GameMode.VS_AI, Player.WHITE, AiDifficulty.HARD)
        repository.recordResult("win", GameMode.VS_AI, Player.WHITE, AiDifficulty.HARD)
        repository.recordResult("loss", GameMode.VS_AI, Player.BLACK, AiDifficulty.VERY_HARD)
        repository.recordResult("draw", GameMode.VS_AI, null, AiDifficulty.HARDEST)
        repository.recordResult("local", GameMode.LOCAL_PASS_AND_PLAY, Player.WHITE)
        val restored = LusalosRepository(context).loadProfile()
        assertEquals("Tala", restored.username)
        assertEquals(20L, restored.points)
        assertEquals(1L, restored.wins)
        assertEquals(1L, restored.losses)
        assertEquals(1L, restored.draws)
        assertEquals(3L, restored.matchesPlayed)
        assertEquals(listOf(0L, 1L, 2L, 3L), restored.winRateHistory.map { it.matchNumber })
        assertEquals(100f, restored.winRateHistory[1].winRate, 0.01f)
        assertEquals(50f, restored.winRateHistory[2].winRate, 0.01f)
        assertEquals(100f / 3, restored.winRateHistory[3].winRate, 0.01f)
        assertEquals(AiDifficulty.HARD, restored.winRateHistory[1].difficulty)
        assertEquals(AiDifficulty.VERY_HARD, restored.winRateHistory[2].difficulty)
        assertEquals(AiDifficulty.HARDEST, restored.winRateHistory[3].difficulty)
        assertEquals(0f, restored.winRateHistory[1].aiWinRate, 0.01f)
        assertEquals(50f, restored.winRateHistory[2].aiWinRate, 0.01f)
        assertEquals(100f / 3, restored.winRateHistory[3].aiWinRate, 0.01f)
    }

    @Test fun versionOneDatabaseKeepsResultsAndAddsRecordedLevel() = runBlocking {
        context.openOrCreateDatabase("lusalos.db", Context.MODE_PRIVATE, null).use { db ->
            db.execSQL("CREATE TABLE profile (id INTEGER PRIMARY KEY, username TEXT, avatar TEXT, points INTEGER, wins INTEGER, losses INTEGER, draws INTEGER, matches INTEGER)")
            db.execSQL("INSERT INTO profile VALUES (1, 'Existing', 'default', 20, 1, 0, 0, 1)")
            db.execSQL("CREATE TABLE results (id TEXT PRIMARY KEY, mode TEXT, winner TEXT, finished_at INTEGER)")
            db.execSQL("INSERT INTO results VALUES ('old', 'VS_AI', 'WHITE', 1)")
            db.version = 1
        }
        val repository = LusalosRepository(context)
        val migrated = repository.loadProfile()
        assertEquals("Existing", migrated.username)
        assertEquals(1L, migrated.matchesPlayed)
        assertEquals(null, migrated.winRateHistory.last().difficulty)
        repository.recordResult("new", GameMode.VS_AI, Player.BLACK, AiDifficulty.HARDEST)
        val restored = LusalosRepository(context).loadProfile()
        assertEquals(20L, restored.points)
        assertEquals(2L, restored.matchesPlayed)
        assertEquals(AiDifficulty.HARDEST, restored.winRateHistory.last().difficulty)
    }

    @Test fun legacyGuestMigrationRunsOnlyOnce() = runBlocking {
        val old = context.getSharedPreferences("lusalos_prefs", Context.MODE_PRIVATE)
        old.edit().putString("guest_username", "Amihan").putLong("guest_points", 80)
            .putLong("guest_wins", 4).putLong("guest_matches", 4).commit()
        val repository = LusalosRepository(context)
        assertEquals("Amihan", repository.loadProfile().username)
        repository.recordResult("rank-up", GameMode.VS_AI, Player.WHITE)
        old.edit().putLong("guest_points", 0).commit()
        val restored = LusalosRepository(context).loadProfile()
        assertEquals(100L, restored.points)
        assertEquals("Rising Star", restored.rank)
        assertEquals(5L, restored.matchesPlayed)
    }
}
