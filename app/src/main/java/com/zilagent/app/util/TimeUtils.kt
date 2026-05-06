package com.zilagent.app.util

import androidx.compose.ui.graphics.Color
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TimeUtils {

    fun minutesToTime(minutes: Int): String {
        val clamped = minutes.coerceIn(0, 1439)
        val h = clamped / 60
        val m = clamped % 60
        return String.format("%02d:%02d", h, m)
    }

    fun getCurrentMinutes(): Int {
        val now = LocalTime.now()
        return now.hour * 60 + now.minute
    }
    
    fun getSecondsToNextMinute(): Int {
        return 60 - LocalTime.now().second
    }

    /**
     * Returns formatted countdown string like "05:23", or "12:10:05"
     */
    fun formatCountdown(secondsLeft: Long, showSeconds: Boolean = true): String {
        val h = secondsLeft / 3600
        val m = (secondsLeft % 3600) / 60
        val s = secondsLeft % 60
        
        if (!showSeconds) {
            val totalMins = h * 60 + m + (if (s > 0) 1 else 0)
            val hours = totalMins / 60
            val mins = totalMins % 60
            return when {
                hours > 0 && mins > 0 -> "$hours Sa $mins Dk"
                hours > 0 -> "$hours Sa"
                else -> "$mins Dk"
            }
        }

        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    fun getCountdownColor(secondsRemaining: Long): Int {
        if (secondsRemaining <= 0) return 0xFFFFFFFF.toInt()
        
        // Linear transition from Green (300s+) to Red (0s)
        val ratio = (secondsRemaining.toFloat() / 300f).coerceIn(0f, 1f)
        // Green: 0xFF4CAF50, Red: 0xFFF44336
        val r = (0xF4 + (0x4C - 0xF4) * ratio).toInt()
        val g = (0x43 + (0xAF - 0x43) * ratio).toInt()
        val b = (0x36 + (0x50 - 0x36) * ratio).toInt()
        
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
}
