package com.zilagent.app.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TimeUtils {

    fun minutesToTime(minutes: Int): String {
        val h = (minutes / 60) % 24
        val m = minutes % 60
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
}
