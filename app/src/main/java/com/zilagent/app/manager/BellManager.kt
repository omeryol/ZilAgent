package com.zilagent.app.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.receiver.BellReceiver
import com.zilagent.app.widget.WidgetStore
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

class BellManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDailyAlarms(schedules: List<BellSchedule>) {
        if (isHolidayToday()) {
            refreshWidgetState()
            return
        }
        val now = LocalTime.now()
        val nowMinutes = now.hour * 60 + now.minute

        schedules.forEach { schedule ->
            if (schedule.notifyAtStart && schedule.startTime > nowMinutes) {
                scheduleAlarm(schedule.startTime, "${schedule.name} Başlıyor", schedule.id.toInt() * 10 + 1, false, !schedule.isBreak)
            }
            if (schedule.notifyAtEnd && (schedule.endTime - 1) > nowMinutes) {
                scheduleAlarm(schedule.endTime - 1, "${schedule.name} Sona Eriyor", schedule.id.toInt() * 10 + 2, false, null)
            }
            if (schedule.endTime > nowMinutes) {
                scheduleAlarm(schedule.endTime, "Widget Update", schedule.id.toInt() * 10 + 3, true, if (!schedule.isBreak) false else null)
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
        
        // Trigger a full recalculation via Broadcast to ensure DB data is synced to WidgetStore
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
            set(Calendar.SECOND, 1) // 1 second offset to avoid race conditions
            set(Calendar.MILLISECOND, 0)
        }
        val intent = Intent(context, BellReceiver::class.java).apply {
            putExtra("IS_WIDGET_UPDATE", true)
            putExtra("IS_MINUTE_TICK", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 9991, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun scheduleCustomAlarm(minutesFromMidnight: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutesFromMidnight / 60)
            set(Calendar.MINUTE, minutesFromMidnight % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val intent = Intent(context, BellReceiver::class.java).apply {
            putExtra("BELL_NAME", "Özel Sayaç")
            putExtra("IS_CUSTOM_MODE_FINISH", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 10011, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: Exception) { e.printStackTrace() }
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
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun isHolidayToday(): Boolean {
        // 1. Weekly Check
        val mask = WidgetStore.getWorkingDays(context)
        val dayOfWeek = LocalDate.now().dayOfWeek.value // 1 (Mon) to 7 (Sun)
        if (mask.getOrNull(dayOfWeek - 1) == '0') return true

        // 2. Database Check (Special Date Ranges)
        val today = LocalDate.now()
        return try {
            val db = AppDatabase.getDatabase(context)
            // Note: runBlocking is used here for simplicity in a non-suspend context
            kotlinx.coroutines.runBlocking { 
                db.holidayDao().getAllHolidaysSync().any { holiday ->
                    val start = LocalDate.parse(holiday.startDate)
                    val end = LocalDate.parse(holiday.endDate)
                    !today.isBefore(start) && !today.isAfter(end)
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}
