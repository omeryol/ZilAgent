package com.zilagent.app.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.receiver.BellReceiver
import com.zilagent.app.widget.WidgetStore
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

class BellManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun t(tr: String, en: String): String = if (WidgetStore.getAppLanguage(context) == "en") en else tr
    fun getAppLanguage(): String = WidgetStore.getAppLanguage(context)

    suspend fun scheduleDailyAlarms(schedules: List<BellSchedule>) {
        if (isHolidayToday()) {
            refreshWidgetState()
            scheduleMinuteTick()
            return
        }
        val now = LocalTime.now()
        val nowMinutes = now.hour * 60 + now.minute

        schedules.forEach { schedule ->
            if (schedule.notifyAtStart && (schedule.startTime - 1) > nowMinutes) {
                scheduleAlarm(schedule.startTime - 1, "${schedule.name} ${t("Başlamasına 1 Dakika Kaldı", "starts in 1 minute")}", schedule.id.toInt() * 10 + 1, false, !schedule.isBreak)
            }
            if (schedule.notifyAtEnd && (schedule.endTime - 1) > nowMinutes) {
                scheduleAlarm(schedule.endTime - 1, "${schedule.name} ${t("Bitmesine 1 Dakika Kaldı", "ends in 1 minute")}", schedule.id.toInt() * 10 + 2, false, null)
            }
            if (schedule.startTime > nowMinutes) {
                scheduleAlarm(schedule.startTime, "Widget Update (Start)", schedule.id.toInt() * 10 + 4, true, null)
            }
            if (schedule.endTime > nowMinutes) {
                scheduleAlarm(schedule.endTime, "Widget Update (End)", schedule.id.toInt() * 10 + 3, true, if (!schedule.isBreak) false else null)
            }
        }

        refreshWidgetState()
        scheduleMinuteTick()
    }

    fun refreshWidgetState() {
        val (customEnabled, _, customTime) = WidgetStore.getCustomCountdown(context)
        val now = LocalTime.now()
        val nowMinutes = now.hour * 60 + now.minute

        if (customEnabled && customTime != -1) {
            if (customTime > nowMinutes) {
                scheduleCustomAlarm(customTime)
            }
        }

        triggerWidgetRefresh()
    }

    private fun triggerWidgetRefresh() {
        val intent = Intent(context, BellReceiver::class.java).apply {
            putExtra("IS_WIDGET_UPDATE", true)
        }
        context.sendBroadcast(intent)
    }

    fun scheduleMinuteTick() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val intent = Intent(context, BellReceiver::class.java).apply {
            putExtra("IS_MINUTE_TICK", true)
        }
        val exactPendingIntent = PendingIntent.getBroadcast(
            context,
            9991,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val repeatingPendingIntent = PendingIntent.getBroadcast(
            context,
            9992,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        setBestEffortAlarm(calendar.timeInMillis, exactPendingIntent)
        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                calendar.timeInMillis,
                60_000L,
                repeatingPendingIntent,
            )
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC,
                calendar.timeInMillis,
                repeatingPendingIntent,
            )
        } catch (e: Exception) {
            Log.e(TAG, "scheduleMinuteTick: inexact repeating alarm failed", e)
        }
    }

    private fun scheduleCustomAlarm(minutesFromMidnight: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutesFromMidnight / 60)
            set(Calendar.MINUTE, minutesFromMidnight % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val intent = Intent(context, BellReceiver::class.java).apply {
            putExtra("BELL_NAME", t("Özel Sayaç", "Custom Timer"))
            putExtra("IS_CUSTOM_MODE_FINISH", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 10011, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        setBestEffortAlarm(calendar.timeInMillis, pendingIntent)
    }

    private fun scheduleAlarm(minutesFromMidnight: Int, title: String, requestCode: Int, isWidgetUpdate: Boolean, enableDnd: Boolean?) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutesFromMidnight / 60)
            set(Calendar.MINUTE, minutesFromMidnight % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis < System.currentTimeMillis()) return
        val intent = Intent(context, BellReceiver::class.java).apply {
            putExtra("BELL_NAME", title)
            putExtra("IS_WIDGET_UPDATE", isWidgetUpdate)
            if (enableDnd != null) putExtra("DND_ACTION", if (enableDnd) 1 else 0)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        setBestEffortAlarm(calendar.timeInMillis, pendingIntent)
    }

    private fun setBestEffortAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (_: SecurityException) {
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (_: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "setBestEffortAlarm: failed to schedule alarm", e)
        }
    }

    suspend fun isHolidayToday(): Boolean {
        val mask = WidgetStore.getWorkingDays(context)
        val dayOfWeek = LocalDate.now().dayOfWeek.value
        if (mask.getOrNull(dayOfWeek - 1) == '0') return true

        val today = LocalDate.now()
        return try {
            val db = AppDatabase.getDatabase(context)
            db.holidayDao().getAllHolidaysSync().any { holiday ->
                val start = LocalDate.parse(holiday.startDate)
                val end = LocalDate.parse(holiday.endDate)
                !today.isBefore(start) && !today.isAfter(end)
            }
        } catch (e: Exception) {
            Log.e(TAG, "isHolidayToday: DB error, defaulting to false", e)
            false
        }
    }

    suspend fun getTodayHolidayName(): String? {
        val today = LocalDate.now()
        val mask = WidgetStore.getWorkingDays(context)
        val dayOfWeek = today.dayOfWeek.value

        if (mask.getOrNull(dayOfWeek - 1) == '0') {
            return t("Haftalık Tatil", "Weekly Day Off")
        }

        return try {
            val db = AppDatabase.getDatabase(context)
            val match = db.holidayDao().getAllHolidaysSync().firstOrNull { holiday ->
                val start = LocalDate.parse(holiday.startDate)
                val end = LocalDate.parse(holiday.endDate)
                !today.isBefore(start) && !today.isAfter(end)
            }
            match?.name
        } catch (e: Exception) {
            Log.e(TAG, "getTodayHolidayName: DB error", e)
            null
        }
    }

    companion object {
        private const val TAG = "BellManager"
    }
}
