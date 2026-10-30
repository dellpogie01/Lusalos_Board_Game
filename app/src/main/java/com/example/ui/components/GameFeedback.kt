package com.example.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.game.GameState

data class FeedbackOptions(val sounds: Boolean = true, val vibration: Boolean = true, val haptics: Boolean = true)
val LocalFeedbackOptions = staticCompositionLocalOf { FeedbackOptions() }

/** Actual and exhibition moves share the saved sound, vibration, and haptic controls. */
@Composable
fun GameMoveFeedback(state: GameState, demo: Boolean = false) {
    val context = LocalContext.current
    val options = LocalFeedbackOptions.current
    val haptic = LocalHapticFeedback.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var loaded by remember { mutableStateOf(emptySet<Int>()) }
    val pool = remember {
        SoundPool.Builder().setMaxStreams(3).setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    }
    var sample by remember { mutableIntStateOf(0) }
    var millSample by remember { mutableIntStateOf(0) }
    var bonusSample by remember { mutableIntStateOf(0) }
    DisposableEffect(pool) {
        pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) loaded = loaded + id }
        sample = pool.load(context, R.raw.piece_drop, 1)
        millSample = pool.load(context, R.raw.mill_formed, 1)
        bonusSample = pool.load(context, R.raw.mill_bonus, 1)
        onDispose { pool.release() }
    }
    var previousCount by remember { mutableIntStateOf(state.moveHistory.size) }
    LaunchedEffect(state.moveHistory.size) {
        val count = state.moveHistory.size
        if (count > previousCount && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            val cue = if (state.moveHistory.lastOrNull()?.formedMill == true) millSample else sample
            if (options.sounds && cue in loaded) pool.play(cue, 0.7f, 0.7f, 1, 0, 1f)
            if (demo && state.moveHistory.lastOrNull()?.formedMill == true && options.sounds && bonusSample in loaded)
                pool.play(bonusSample, 0.4f, 0.4f, 1, 0, 1f)
            if (demo && options.haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (options.vibration) {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator?.hasVibrator() == true) {
                    val mill = state.moveHistory.lastOrNull()?.formedMill == true
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(if (mill) VibrationEffect.createWaveform(longArrayOf(0, 55, 65, 90), -1)
                            else VibrationEffect.createOneShot(24, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        if (mill) vibrator.vibrate(longArrayOf(0, 55, 65, 90), -1) else vibrator.vibrate(24)
                    }
                }
            }
        }
        previousCount = count
    }
}
