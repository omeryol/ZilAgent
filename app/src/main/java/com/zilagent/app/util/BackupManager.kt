package com.zilagent.app.util

import android.content.Context
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.widget.WidgetStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupManager {

    suspend fun createBackup(context: Context): String = withContext(Dispatchers.IO) {
        val backup = JSONObject()
        val db = AppDatabase.getDatabase(context)

        // 1. SharedPreferences (WidgetStore)
        val prefs = context.getSharedPreferences(WidgetStore.PREFS_NAME, Context.MODE_PRIVATE)
        val prefsJson = JSONObject()
        prefs.all.forEach { (key, value) ->
            prefsJson.put(key, value)
        }
        backup.put("prefs", prefsJson)

        // 2. Profiles
        val profiles = db.bellDao().getAllProfilesSync()
        val profilesArray = JSONArray()
        profiles.forEach { profile ->
            val p = JSONObject()
            p.put("id", profile.id)
            p.put("name", profile.name)
            p.put("isActive", profile.isActive)
            profilesArray.put(p)
        }
        backup.put("profiles", profilesArray)

        // 3. Bell Schedules
        // Note: We need a way to get all schedules easily
        // I'll add a method to BellDao if needed, but for now let's assume we can loop profiles
        val schedulesArray = JSONArray()
        profiles.forEach { profile ->
            val schedules = db.bellDao().getSchedulesForProfileSync(profile.id, 0) // Basic way to get most
            // Actually dayOfWeek 1..7 also needed
            for (day in 0..7) {
                db.bellDao().getSchedulesForProfileSync(profile.id, day).forEach { s ->
                    val sj = JSONObject()
                    sj.put("id", s.id)
                    sj.put("profileId", s.profileId)
                    sj.put("dayOfWeek", s.dayOfWeek)
                    sj.put("name", s.name)
                    sj.put("startTime", s.startTime)
                    sj.put("endTime", s.endTime)
                    sj.put("isBreak", s.isBreak)
                    sj.put("orderIndex", s.orderIndex)
                    sj.put("notifyAtStart", s.notifyAtStart)
                    sj.put("notifyAtEnd", s.notifyAtEnd)
                    schedulesArray.put(sj)
                }
            }
        }
        backup.put("schedules", schedulesArray)

        // 4. Holidays
        val holidaysArr = JSONArray()
        db.holidayDao().getAllHolidaysSync().forEach { h ->
            val hj = JSONObject()
            hj.put("id", h.id)
            hj.put("name", h.name)
            hj.put("startDate", h.startDate)
            hj.put("endDate", h.endDate)
            holidaysArr.put(hj)
        }
        backup.put("holidays", holidaysArr)

        // 5. Classes & Subjects & Syllabus
        val classesArr = JSONArray()
        db.syllabusDao().getAllClassesSync().forEach { c ->
            val cj = JSONObject()
            cj.put("id", c.id)
            cj.put("name", c.name)
            cj.put("colorHex", c.colorHex)
            classesArr.put(cj)
        }
        backup.put("classes", classesArr)

        val subjectsArr = JSONArray()
        db.syllabusDao().getAllSubjectsSync().forEach { s ->
            val sj = JSONObject()
            sj.put("id", s.id)
            sj.put("name", s.name)
            sj.put("isSystem", s.isSystem)
            subjectsArr.put(sj)
        }
        backup.put("subjects", subjectsArr)

        val syllabusArr = JSONArray()
        profiles.forEach { profile ->
            for (day in 1..7) {
                db.syllabusDao().getSyllabusForDaySync(profile.id, day).forEach { entry ->
                    val ej = JSONObject()
                    ej.put("profileId", entry.profileId)
                    ej.put("dayOfWeek", entry.dayOfWeek)
                    ej.put("lessonOrder", entry.lessonOrder)
                    ej.put("classId", entry.classId)
                    ej.put("subjectId", entry.subjectId)
                    syllabusArr.put(ej)
                }
            }
        }
        backup.put("syllabus", syllabusArr)
        
        // 6. Quotes
        val quotesArr = JSONArray()
        db.quoteDao().getAllQuotesSync().forEach { q ->
            val qj = JSONObject()
            qj.put("id", q.id)
            qj.put("content", q.content)
            qj.put("isSystem", q.isSystem)
            quotesArr.put(qj)
        }
        backup.put("quotes", quotesArr)

        backup.toString(4)
    }

    suspend fun restoreBackup(context: Context, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backup = JSONObject(jsonString)
            val db = AppDatabase.getDatabase(context)

            // Clear existing data
            db.clearAllTables()
            context.getSharedPreferences(WidgetStore.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()

            // Restore Prefs
            val prefs = backup.optJSONObject("prefs")
            if (prefs != null) {
                val editor = context.getSharedPreferences(WidgetStore.PREFS_NAME, Context.MODE_PRIVATE).edit()
                prefs.keys().forEach { key ->
                    val value = prefs.get(key)
                    when (value) {
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Float -> editor.putFloat(key, value)
                        is String -> editor.putString(key, value)
                    }
                }
                editor.apply()
            }

            // Restore Database Entities
            // Note: Room IDs might conflict if we use original IDs, but since we cleared tables, it SHOULD be fine.
            // However, it's safer to insert new items and let database generate IDs if they are auto-gen.
            // But references (profileId, classId, etc) need to be preserved.
            
            // Map old IDs to new IDs if necessary, or just use the same IDs if we can insert with IDs.
            // Room @Insert(onConflict = REPLACE) will use the provided IDs.

            // 1. Profiles
            val profiles = backup.optJSONArray("profiles") ?: JSONArray()
            for (i in 0 until profiles.length()) {
                val p = profiles.getJSONObject(i)
                db.bellDao().insertProfile(com.zilagent.app.data.entity.Profile(
                    id = p.getLong("id"),
                    name = p.getString("name"),
                    isActive = p.getBoolean("isActive")
                ))
            }

            // 2. Schedules
            val schedules = backup.optJSONArray("schedules") ?: JSONArray()
            val bellSchedules = mutableListOf<com.zilagent.app.data.entity.BellSchedule>()
            for (i in 0 until schedules.length()) {
                val s = schedules.getJSONObject(i)
                bellSchedules.add(com.zilagent.app.data.entity.BellSchedule(
                    id = s.getLong("id"),
                    profileId = s.getLong("profileId"),
                    dayOfWeek = s.getInt("dayOfWeek"),
                    name = s.getString("name"),
                    startTime = s.getInt("startTime"),
                    endTime = s.getInt("endTime"),
                    isBreak = s.getBoolean("isBreak"),
                    orderIndex = s.getInt("orderIndex"),
                    notifyAtStart = s.getBoolean("notifyAtStart"),
                    notifyAtEnd = s.getBoolean("notifyAtEnd")
                ))
            }
            db.bellDao().insertSchedules(bellSchedules)

            // 3. Holidays
            val holidays = backup.optJSONArray("holidays") ?: JSONArray()
            for (i in 0 until holidays.length()) {
                val h = holidays.getJSONObject(i)
                db.holidayDao().insertHoliday(com.zilagent.app.data.entity.Holiday(
                    id = h.getLong("id"),
                    name = h.getString("name"),
                    startDate = h.getString("startDate"),
                    endDate = h.getString("endDate")
                ))
            }

            // 4. Classes
            val classes = backup.optJSONArray("classes") ?: JSONArray()
            for (i in 0 until classes.length()) {
                val c = classes.getJSONObject(i)
                db.syllabusDao().insertClass(com.zilagent.app.data.entity.SchoolClass(
                    id = c.getLong("id"),
                    name = c.getString("name"),
                    colorHex = c.getString("colorHex")
                ))
            }

            // 5. Subjects
            val subjects = backup.optJSONArray("subjects") ?: JSONArray()
            for (i in 0 until subjects.length()) {
                val s = subjects.getJSONObject(i)
                db.syllabusDao().insertSubject(com.zilagent.app.data.entity.SchoolSubject(
                    id = s.getLong("id"),
                    name = s.getString("name"),
                    isSystem = s.getBoolean("isSystem")
                ))
            }

            // 6. Syllabus Entries
            val syllabus = backup.optJSONArray("syllabus") ?: JSONArray()
            for (i in 0 until syllabus.length()) {
                val e = syllabus.getJSONObject(i)
                db.syllabusDao().insertSyllabusEntry(com.zilagent.app.data.entity.SyllabusEntry(
                    profileId = e.getLong("profileId"),
                    dayOfWeek = e.getInt("dayOfWeek"),
                    lessonOrder = e.getInt("lessonOrder"),
                    classId = if (e.isNull("classId")) null else e.getLong("classId"),
                    subjectId = if (e.isNull("subjectId")) null else e.getLong("subjectId")
                ))
            }
            
            // 7. Quotes
            val quotes = backup.optJSONArray("quotes") ?: JSONArray()
            for (i in 0 until quotes.length()) {
                val q = quotes.getJSONObject(i)
                db.quoteDao().insertQuote(com.zilagent.app.data.entity.Quote(
                    id = q.getLong("id"),
                    content = q.getString("content"),
                    isSystem = q.getBoolean("isSystem")
                ))
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createBackup(context: Context, uri: android.net.Uri) {
        val json = createBackup(context)
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(json.toByteArray())
            }
        }
    }

    suspend fun restoreBackup(context: Context, uri: android.net.Uri): Boolean {
        return try {
            val json = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                }
            }
            if (json != null) restoreBackup(context, json) else false
        } catch (e: Exception) {
            false
        }
    }
}
