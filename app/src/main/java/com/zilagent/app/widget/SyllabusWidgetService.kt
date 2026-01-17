package com.zilagent.app.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.zilagent.app.R
import com.zilagent.app.data.AppDatabase
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class SyllabusWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return SyllabusWidgetFactory(this.applicationContext)
    }
}

class SyllabusWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var lessons = listOf<SyllabusItem>()

    data class SyllabusItem(
        val order: Int,
        val name: String,
        val time: String,
        val color: String?,
        val hasNote: Boolean = false
    )

    override fun onCreate() {}

    override fun onDataSetChanged() = runBlocking {
        try {
            val db = AppDatabase.getDatabase(context)
            val profile = db.bellDao().getActiveProfileSync()
            if (profile != null) {
                val calendar = Calendar.getInstance()
                // Check if today has remaining lessons
                val rawDay = calendar.get(Calendar.DAY_OF_WEEK)
                val dayOfWeek = if (rawDay == Calendar.SUNDAY) 7 else rawDay - 1
                val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

                var schedules = db.bellDao().getSchedulesForProfileSync(profile.id, dayOfWeek)
                val remainingLessons = schedules.filter { !it.isBreak && it.endTime > currentMinutes }

                val newList = mutableListOf<SyllabusItem>()

                if (remainingLessons.isNotEmpty()) {
                    // Show Today's remaining
                    addLessons(db, profile.id, dayOfWeek, remainingLessons, newList)
                } else {
                    // Look for next days
                    newList.add(SyllabusItem(0, "Bugün bitti", "--:--", null, false))
                    
                    for (i in 1..7) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        val nextRawDay = calendar.get(Calendar.DAY_OF_WEEK)
                        val nextDayOfWeek = if (nextRawDay == Calendar.SUNDAY) 7 else nextRawDay - 1
                        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                        val dayName = SimpleDateFormat("EEEE", Locale("tr")).format(calendar.time)

                        // Check Official Holiday
                        val holiday = db.holidayDao().getAllHolidaysSync().find { 
                            it.startDate <= dateStr && it.endDate >= dateStr 
                        }

                        if (holiday != null) {
                            newList.add(SyllabusItem(-1, "$dayName - ${holiday.name}", "Tatil", "#FF5252", false))
                            continue
                        }

                        // Check Schedule
                        val nextSchedules = db.bellDao().getSchedulesForProfileSync(profile.id, nextDayOfWeek)
                        // Filter for ACTUAL lessons (not ceremonies or breaks) to decide if day is empty
                        val lessonCount = nextSchedules.count { !it.isBreak && !it.name.contains("Töreni") }

                        if (lessonCount == 0) {
                            // If it's Weekend (Sat/Sun) and no lessons -> Holiday
                            if (nextDayOfWeek == 6 || nextDayOfWeek == 7) {
                                newList.add(SyllabusItem(-1, "$dayName - Haftasonu", "Tatil", "#9E9E9E", false))
                            } else {
                                // If it's Weekday and no lessons -> Empty Day
                                newList.add(SyllabusItem(-1, "$dayName - Ders Yok", "Boş Gün", "#607D8B", false))
                            }
                            continue
                        }

                        // Found Day with Lessons
                        newList.add(SyllabusItem(-1, "$dayName Programı", "Yarın", "#4CAF50", false)) // Header
                        addLessons(db, profile.id, nextDayOfWeek, nextSchedules.filter{ !it.isBreak }, newList)
                        break
                    }
                }
                lessons = newList
            } else {
                lessons = listOf(SyllabusItem(0, "Aktif Profil Yok", "Ayarlardan seçin", null, false))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lessons = listOf(SyllabusItem(0, "Hata", e.message ?: "Bilinmeyen Hata", null, false))
        }
    }


    
    private suspend fun addLessons(
        db: AppDatabase, 
        profileId: Long, 
        day: Int, 
        schedules: List<com.zilagent.app.data.entity.BellSchedule>, 
        list: MutableList<SyllabusItem>
    ) {
        // Assume 'schedules' contains valid lessons (non-break) in order.
        // We need to match them to their SyllabusEntry.
        // Since we don't have the original 'lessonCount' here easily if filtered, 
        // we should probably fetch ALL schedules for that day to establish the index.
        
        val allSchedules = db.bellDao().getSchedulesForProfileSync(profileId, day)
        val syllabusEntries = db.syllabusDao().getSyllabusForDaySync(profileId, day)
        
        var lessonCounter = 0
        allSchedules.forEach { s ->
            if (!s.isBreak && !s.name.contains("Töreni")) {
                lessonCounter++
                // Check if this schedule 's' is in our 'schedules' list (the one to display)
                if (schedules.any { it.id == s.id }) {
                    val fullEntry = db.syllabusDao().getFullSyllabusEntrySync(profileId, day, lessonCounter)
                    val note = db.lessonNoteDao().getNoteSync(profileId, day, lessonCounter)
                    val hasNote = !note?.note.isNullOrBlank()

                    val name = if (fullEntry != null) {
                        "${fullEntry.className ?: ""} ${fullEntry.subjectName ?: ""}".trim().ifEmpty { s.name }
                    } else s.name
                    
                    list.add(SyllabusItem(
                        order = lessonCounter,
                        name = name,
                        time = "${formatTime(s.startTime)} - ${formatTime(s.endTime)}",
                        color = fullEntry?.classColor,
                        hasNote = hasNote
                    ))
                }
            }
        }
    }

    private fun formatTime(mins: Int): String {
        val h = mins / 60
        val m = mins % 60
        return String.format("%02d:%02d", h, m)
    }

    override fun onDestroy() {}

    override fun getCount(): Int = lessons.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = lessons[position]
        val views = RemoteViews(context.packageName, R.layout.widget_syllabus_item)
        
        views.setTextViewText(R.id.lesson_name, item.name)
        views.setTextViewText(R.id.lesson_time, item.time)
        // Removed duplicate line
        
        // Handle Header/Holiday styling
        if (item.order <= 0) { // Header or Message
            views.setViewVisibility(R.id.lesson_order, android.view.View.GONE)
             // Maybe change text color/style for headers?
             // For now, re-use existing fields.
        } else {
            views.setViewVisibility(R.id.lesson_order, android.view.View.VISIBLE)
            views.setTextViewText(R.id.lesson_order, item.order.toString())
        }
        
        if (item.hasNote) {
            views.setViewVisibility(R.id.note_icon, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.note_icon, android.view.View.GONE)
        }
        
        if (item.color != null) {
            try {
                views.setInt(R.id.class_color_strip, "setBackgroundColor", android.graphics.Color.parseColor(item.color))
            } catch (e: Exception) {}
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
