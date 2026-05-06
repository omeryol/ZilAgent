package com.zilagent.app.data.dao

import androidx.room.*
import com.zilagent.app.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusDao {
    // School Classes
    @Query("SELECT * FROM school_classes ORDER BY name ASC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Query("SELECT * FROM school_classes ORDER BY name ASC")
    suspend fun getAllClassesSync(): List<SchoolClass>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass): Long

    @Delete
    suspend fun deleteClass(schoolClass: SchoolClass)

    // School Subjects
    @Query("SELECT * FROM school_subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SchoolSubject>>

    @Query("SELECT * FROM school_subjects ORDER BY name ASC")
    suspend fun getAllSubjectsSync(): List<SchoolSubject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SchoolSubject): Long

    @Delete
    suspend fun deleteSubject(subject: SchoolSubject)

    @Query("SELECT COUNT(*) FROM school_subjects WHERE isSystem = 1")
    suspend fun getSystemSubjectCount(): Int

    // Syllabus Entries
    @Query("SELECT * FROM syllabus_entries WHERE profileId = :profileId AND dayOfWeek = :dayOfWeek")
    fun getSyllabusForDay(profileId: Long, dayOfWeek: Int): Flow<List<SyllabusEntry>>
    
    @Query("SELECT * FROM syllabus_entries WHERE profileId = :profileId AND dayOfWeek = :dayOfWeek")
    suspend fun getSyllabusForDaySync(profileId: Long, dayOfWeek: Int): List<SyllabusEntry>

    @Query("SELECT * FROM syllabus_entries WHERE profileId = :profileId")
    fun getAllSyllabus(profileId: Long): Flow<List<SyllabusEntry>>

    @Query("SELECT * FROM syllabus_entries WHERE profileId = :profileId")
    suspend fun getAllSyllabusSync(profileId: Long): List<SyllabusEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabusEntry(entry: SyllabusEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabusEntries(entries: List<SyllabusEntry>)

    @Query("DELETE FROM syllabus_entries WHERE profileId = :profileId AND dayOfWeek = :dayOfWeek AND lessonOrder = :lessonOrder")
    suspend fun deleteSyllabusEntry(profileId: Long, dayOfWeek: Int, lessonOrder: Int)

    @Query("DELETE FROM syllabus_entries WHERE profileId = :profileId AND dayOfWeek = :dayOfWeek")
    suspend fun deleteSyllabusForDay(profileId: Long, dayOfWeek: Int)

    @Transaction
    @Query("""
        SELECT s.*, c.name as className, c.colorHex as classColor, sub.name as subjectName 
        FROM syllabus_entries s
        LEFT JOIN school_classes c ON s.classId = c.id
        LEFT JOIN school_subjects sub ON s.subjectId = sub.id
        WHERE s.profileId = :profileId AND s.dayOfWeek = :dayOfWeek AND s.lessonOrder = :lessonOrder
    """)
    suspend fun getFullSyllabusEntrySync(profileId: Long, dayOfWeek: Int, lessonOrder: Int): SyllabusFullInfo?
}

data class SyllabusFullInfo(
    val profileId: Long,
    val dayOfWeek: Int,
    val lessonOrder: Int,
    val classId: Long?,
    val subjectId: Long?,
    val className: String?,
    val classColor: String?,
    val subjectName: String?
)
