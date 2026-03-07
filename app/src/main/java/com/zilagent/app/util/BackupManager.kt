package com.zilagent.app.util

import android.content.Context
import androidx.room.withTransaction
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.data.entity.Holiday
import com.zilagent.app.data.entity.Profile
import com.zilagent.app.data.entity.Quote
import com.zilagent.app.data.entity.SchoolClass
import com.zilagent.app.data.entity.SchoolSubject
import com.zilagent.app.data.entity.SyllabusEntry
import com.zilagent.app.widget.WidgetStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {
    private const val BACKUP_FORMAT_VERSION = 2
    private const val MAX_BACKUP_JSON_CHARS = 2_000_000
    private const val MAX_LIST_ITEMS = 10_000

    suspend fun createBackup(context: Context): String = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)

        val prefs = context.getSharedPreferences(WidgetStore.PREFS_NAME, Context.MODE_PRIVATE)
        val prefsJson = JSONObject().apply {
            prefs.all.forEach { (key, value) ->
                put(key, value)
            }
        }

        val profiles = db.bellDao().getAllProfilesSync()
        val profilesArray = JSONArray().apply {
            profiles.forEach { profile ->
                put(
                    JSONObject().apply {
                        put("id", profile.id)
                        put("name", profile.name)
                        put("isActive", profile.isActive)
                    },
                )
            }
        }

        val schedulesArray = JSONArray().apply {
            profiles.forEach { profile ->
                for (day in 0..7) {
                    db.bellDao().getSchedulesForProfileSync(profile.id, day).forEach { s ->
                        put(
                            JSONObject().apply {
                                put("id", s.id)
                                put("profileId", s.profileId)
                                put("dayOfWeek", s.dayOfWeek)
                                put("name", s.name)
                                put("startTime", s.startTime)
                                put("endTime", s.endTime)
                                put("isBreak", s.isBreak)
                                put("orderIndex", s.orderIndex)
                                put("notifyAtStart", s.notifyAtStart)
                                put("notifyAtEnd", s.notifyAtEnd)
                            },
                        )
                    }
                }
            }
        }

        val holidaysArray = JSONArray().apply {
            db.holidayDao().getAllHolidaysSync().forEach { h ->
                put(
                    JSONObject().apply {
                        put("id", h.id)
                        put("name", h.name)
                        put("startDate", h.startDate)
                        put("endDate", h.endDate)
                    },
                )
            }
        }

        val classesArray = JSONArray().apply {
            db.syllabusDao().getAllClassesSync().forEach { c ->
                put(
                    JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("colorHex", c.colorHex)
                    },
                )
            }
        }

        val subjectsArray = JSONArray().apply {
            db.syllabusDao().getAllSubjectsSync().forEach { s ->
                put(
                    JSONObject().apply {
                        put("id", s.id)
                        put("name", s.name)
                        put("isSystem", s.isSystem)
                    },
                )
            }
        }

        val syllabusArray = JSONArray().apply {
            profiles.forEach { profile ->
                for (day in 1..7) {
                    db.syllabusDao().getSyllabusForDaySync(profile.id, day).forEach { entry ->
                        put(
                            JSONObject().apply {
                                put("profileId", entry.profileId)
                                put("dayOfWeek", entry.dayOfWeek)
                                put("lessonOrder", entry.lessonOrder)
                                put("classId", entry.classId)
                                put("subjectId", entry.subjectId)
                            },
                        )
                    }
                }
            }
        }

        val quotesArray = JSONArray().apply {
            db.quoteDao().getAllQuotesSync().forEach { q ->
                put(
                    JSONObject().apply {
                        put("id", q.id)
                        put("content", q.content)
                        put("isSystem", q.isSystem)
                    },
                )
            }
        }

        val payload = JSONObject().apply {
            put("prefs", prefsJson)
            put("profiles", profilesArray)
            put("schedules", schedulesArray)
            put("holidays", holidaysArray)
            put("classes", classesArray)
            put("subjects", subjectsArray)
            put("syllabus", syllabusArray)
            put("quotes", quotesArray)
        }

        JSONObject().apply {
            put("formatVersion", BACKUP_FORMAT_VERSION)
            put("appId", "com.zilagent.app")
            put("payload", payload)
        }.toString(2)
    }

    suspend fun restoreBackup(context: Context, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (jsonString.length > MAX_BACKUP_JSON_CHARS) return@withContext false

            val root = JSONObject(jsonString)
            val payload = if (root.has("payload")) root.optJSONObject("payload") ?: return@withContext false else root

            val parsed = parseAndValidatePayload(payload) ?: return@withContext false
            val db = AppDatabase.getDatabase(context)

            db.withTransaction {
                db.clearAllTables()

                parsed.profiles.forEach { db.bellDao().insertProfile(it) }
                db.bellDao().insertSchedules(parsed.schedules)
                parsed.holidays.forEach { db.holidayDao().insertHoliday(it) }
                parsed.classes.forEach { db.syllabusDao().insertClass(it) }
                parsed.subjects.forEach { db.syllabusDao().insertSubject(it) }
                parsed.syllabus.forEach { db.syllabusDao().insertSyllabusEntry(it) }
                parsed.quotes.forEach { db.quoteDao().insertQuote(it) }
            }

            val prefsEditor = context.getSharedPreferences(WidgetStore.PREFS_NAME, Context.MODE_PRIVATE).edit().clear()
            parsed.prefs.keys().forEach { key ->
                val value = parsed.prefs.get(key)
                when (value) {
                    is Boolean -> prefsEditor.putBoolean(key, value)
                    is Int -> prefsEditor.putInt(key, value)
                    is Long -> prefsEditor.putLong(key, value)
                    is Float -> prefsEditor.putFloat(key, value)
                    is String -> prefsEditor.putString(key, value)
                }
            }
            prefsEditor.apply()

            true
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            false
        }
    }

    private data class ParsedBackup(
        val prefs: JSONObject,
        val profiles: List<Profile>,
        val schedules: List<BellSchedule>,
        val holidays: List<Holiday>,
        val classes: List<SchoolClass>,
        val subjects: List<SchoolSubject>,
        val syllabus: List<SyllabusEntry>,
        val quotes: List<Quote>,
    )

    private fun parseAndValidatePayload(payload: JSONObject): ParsedBackup? {
        val prefs = payload.optJSONObject("prefs") ?: JSONObject()
        val profilesJson = payload.optJSONArray("profiles") ?: JSONArray()
        val schedulesJson = payload.optJSONArray("schedules") ?: JSONArray()
        val holidaysJson = payload.optJSONArray("holidays") ?: JSONArray()
        val classesJson = payload.optJSONArray("classes") ?: JSONArray()
        val subjectsJson = payload.optJSONArray("subjects") ?: JSONArray()
        val syllabusJson = payload.optJSONArray("syllabus") ?: JSONArray()
        val quotesJson = payload.optJSONArray("quotes") ?: JSONArray()

        if (
            profilesJson.length() > MAX_LIST_ITEMS ||
            schedulesJson.length() > MAX_LIST_ITEMS ||
            holidaysJson.length() > MAX_LIST_ITEMS ||
            classesJson.length() > MAX_LIST_ITEMS ||
            subjectsJson.length() > MAX_LIST_ITEMS ||
            syllabusJson.length() > MAX_LIST_ITEMS ||
            quotesJson.length() > MAX_LIST_ITEMS
        ) return null

        val profiles = mutableListOf<Profile>()
        for (i in 0 until profilesJson.length()) {
            val p = profilesJson.optJSONObject(i) ?: return null
            val id = p.optLong("id", -1)
            val name = p.optString("name", "")
            val isActive = p.optBoolean("isActive", false)
            if (id < 0 || name.isBlank() || name.length > 80) return null
            profiles += Profile(id = id, name = name, isActive = isActive)
        }

        val schedules = mutableListOf<BellSchedule>()
        for (i in 0 until schedulesJson.length()) {
            val s = schedulesJson.optJSONObject(i) ?: return null
            val id = s.optLong("id", -1)
            val profileId = s.optLong("profileId", -1)
            val dayOfWeek = s.optInt("dayOfWeek", -1)
            val name = s.optString("name", "")
            val startTime = s.optInt("startTime", -1)
            val endTime = s.optInt("endTime", -1)
            val isBreak = s.optBoolean("isBreak", false)
            val orderIndex = s.optInt("orderIndex", -1)
            val notifyAtStart = s.optBoolean("notifyAtStart", true)
            val notifyAtEnd = s.optBoolean("notifyAtEnd", true)

            if (
                id < 0 || profileId < 0 ||
                dayOfWeek !in 0..7 ||
                name.isBlank() || name.length > 80 ||
                startTime !in 0..(24 * 60 - 1) ||
                endTime !in 1..(24 * 60) ||
                startTime >= endTime ||
                orderIndex !in 0..300
            ) return null

            schedules += BellSchedule(
                id = id,
                profileId = profileId,
                dayOfWeek = dayOfWeek,
                name = name,
                startTime = startTime,
                endTime = endTime,
                isBreak = isBreak,
                orderIndex = orderIndex,
                notifyAtStart = notifyAtStart,
                notifyAtEnd = notifyAtEnd,
            )
        }

        val holidays = mutableListOf<Holiday>()
        for (i in 0 until holidaysJson.length()) {
            val h = holidaysJson.optJSONObject(i) ?: return null
            val id = h.optLong("id", -1)
            val name = h.optString("name", "")
            val startDate = h.optString("startDate", "")
            val endDate = h.optString("endDate", "")
            if (id < 0 || name.isBlank() || name.length > 120 || !isIsoDate(startDate) || !isIsoDate(endDate)) return null
            holidays += Holiday(id = id, name = name, startDate = startDate, endDate = endDate)
        }

        val classes = mutableListOf<SchoolClass>()
        for (i in 0 until classesJson.length()) {
            val c = classesJson.optJSONObject(i) ?: return null
            val id = c.optLong("id", -1)
            val name = c.optString("name", "")
            val colorHex = c.optString("colorHex", "")
            if (id < 0 || name.isBlank() || name.length > 80 || !isHexColor(colorHex)) return null
            classes += SchoolClass(id = id, name = name, colorHex = colorHex)
        }

        val subjects = mutableListOf<SchoolSubject>()
        for (i in 0 until subjectsJson.length()) {
            val s = subjectsJson.optJSONObject(i) ?: return null
            val id = s.optLong("id", -1)
            val name = s.optString("name", "")
            val isSystem = s.optBoolean("isSystem", false)
            if (id < 0 || name.isBlank() || name.length > 80) return null
            subjects += SchoolSubject(id = id, name = name, isSystem = isSystem)
        }

        val syllabus = mutableListOf<SyllabusEntry>()
        for (i in 0 until syllabusJson.length()) {
            val e = syllabusJson.optJSONObject(i) ?: return null
            val profileId = e.optLong("profileId", -1)
            val dayOfWeek = e.optInt("dayOfWeek", -1)
            val lessonOrder = e.optInt("lessonOrder", -1)
            val classId = if (e.isNull("classId")) null else e.optLong("classId", -1).takeIf { it >= 0 }
            val subjectId = if (e.isNull("subjectId")) null else e.optLong("subjectId", -1).takeIf { it >= 0 }

            if (profileId < 0 || dayOfWeek !in 1..7 || lessonOrder !in 1..300) return null

            syllabus += SyllabusEntry(
                profileId = profileId,
                dayOfWeek = dayOfWeek,
                lessonOrder = lessonOrder,
                classId = classId,
                subjectId = subjectId,
            )
        }

        val quotes = mutableListOf<Quote>()
        for (i in 0 until quotesJson.length()) {
            val q = quotesJson.optJSONObject(i) ?: return null
            val id = q.optLong("id", -1)
            val content = q.optString("content", "")
            val isSystem = q.optBoolean("isSystem", false)
            if (id < 0 || content.isBlank() || content.length > 500) return null
            quotes += Quote(id = id, content = content, isSystem = isSystem)
        }

        return ParsedBackup(
            prefs = prefs,
            profiles = profiles,
            schedules = schedules,
            holidays = holidays,
            classes = classes,
            subjects = subjects,
            syllabus = syllabus,
            quotes = quotes,
        )
    }

    private fun isIsoDate(value: String): Boolean {
        return Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(value)
    }

    private fun isHexColor(value: String): Boolean {
        return Regex("^#[0-9A-Fa-f]{6}$").matches(value)
    }
}
