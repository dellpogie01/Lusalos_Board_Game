package com.example.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.game.AiDifficulty
import com.example.game.GameMode
import com.example.game.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext

/** Device-only SQLite storage. No account, network, or cloud services are used. */
class LusalosRepository(context: Context) {
    private val helper = LocalDatabase(context.applicationContext)
    private val profile = MutableStateFlow<UserProfile?>(null)
    fun observeProfile() = profile.filterNotNull()

    suspend fun loadProfile(): UserProfile = withContext(Dispatchers.IO) {
        readProfile(helper.readableDatabase).also { profile.value = it }
    }

    suspend fun updateProfile(username: String, avatar: String) = withContext(Dispatchers.IO) {
        require(username.isNotBlank())
        val db = helper.writableDatabase
        db.update("profile", ContentValues().apply {
            put("username", username.trim().take(40)); put("avatar", avatar)
        }, "id = 1", null)
        profile.value = readProfile(db)
    }

    /** Match IDs and a transaction make result recording idempotent. */
    suspend fun recordResult(matchId: String, mode: GameMode, winner: Player?, difficulty: AiDifficulty? = null) = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val inserted = db.insertWithOnConflict("results", null, ContentValues().apply {
                put("id", matchId); put("mode", mode.name)
                put("winner", winner?.name ?: "DRAW"); put("finished_at", System.currentTimeMillis()); put("difficulty", difficulty?.name)
            }, SQLiteDatabase.CONFLICT_IGNORE)
            // Pass-and-play has no assigned profile opponent, so only AI games affect personal stats.
            if (inserted != -1L && mode == GameMode.VS_AI) {
                val win = if (winner == Player.WHITE) 1 else 0
                val loss = if (winner == Player.BLACK) 1 else 0
                val draw = if (winner == null || winner == Player.NONE) 1 else 0
                db.execSQL("UPDATE profile SET wins = wins + ?, losses = losses + ?, draws = draws + ?, matches = matches + 1, points = points + ? WHERE id = 1",
                    arrayOf(win, loss, draw, win * RankSystem.POINTS_PER_WIN))
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        profile.value = readProfile(db)
    }

    private fun readProfile(db: SQLiteDatabase): UserProfile = db.rawQuery("SELECT * FROM profile WHERE id = 1", null).use { c ->
        check(c.moveToFirst())
        fun number(name: String) = c.getLong(c.getColumnIndexOrThrow(name))
        fun string(name: String) = c.getString(c.getColumnIndexOrThrow(name))
        val rank = RankSystem.getRankForPoints(number("points").toInt()).name
        val recent = db.rawQuery("SELECT winner, difficulty FROM results WHERE mode = ? ORDER BY finished_at DESC, rowid DESC LIMIT 20", arrayOf(GameMode.VS_AI.name)).use { results ->
            buildList { while (results.moveToNext()) add(results.getString(0) to results.getString(1)) }.reversed()
        }
        var total = (number("matches") - recent.size).coerceAtLeast(0)
        var wins = (number("wins") - recent.count { it.first == Player.WHITE.name }).coerceAtLeast(0)
        var aiWins = (number("losses") - recent.count { it.first == Player.BLACK.name }).coerceAtLeast(0)
        val trend = if (recent.isEmpty()) emptyList() else buildList {
            add(MatchTrendPoint(total, if (total == 0L) 0f else wins * 100f / total, if (total == 0L) 0f else aiWins * 100f / total))
            recent.forEach { outcome ->
                total++
                if (outcome.first == Player.WHITE.name) wins++
                if (outcome.first == Player.BLACK.name) aiWins++
                add(MatchTrendPoint(total, wins * 100f / total, aiWins * 100f / total, outcome.second?.let { saved -> runCatching { AiDifficulty.valueOf(saved) }.getOrNull() }))
            }
        }
        UserProfile(username = string("username").let { if (it == "Manlalaro") "Player" else it }, avatarId = string("avatar"),
            points = number("points"), wins = number("wins"), losses = number("losses"),
            draws = number("draws"), matchesPlayed = number("matches"), rank = rank, highestRank = rank, winRateHistory = trend)
    }
}

private class LocalDatabase(private val context: Context) : SQLiteOpenHelper(context, "lusalos.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE profile (id INTEGER PRIMARY KEY CHECK(id = 1), username TEXT NOT NULL, avatar TEXT NOT NULL, points INTEGER NOT NULL DEFAULT 0, wins INTEGER NOT NULL DEFAULT 0, losses INTEGER NOT NULL DEFAULT 0, draws INTEGER NOT NULL DEFAULT 0, matches INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE results (id TEXT PRIMARY KEY, mode TEXT NOT NULL, winner TEXT NOT NULL, finished_at INTEGER NOT NULL, difficulty TEXT)")
        val old = context.getSharedPreferences("lusalos_prefs", Context.MODE_PRIVATE)
        db.insertOrThrow("profile", null, ContentValues().apply {
            put("id", 1); put("username", old.getString("guest_username", "Player"))
            put("avatar", old.getString("guest_avatar", "default"))
            put("points", old.getLong("guest_points", 0)); put("wins", old.getLong("guest_wins", 0))
            put("losses", old.getLong("guest_losses", 0)); put("draws", old.getLong("guest_draws", 0))
            put("matches", old.getLong("guest_matches", 0))
        })
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE results ADD COLUMN difficulty TEXT")
    }
}
