package com.zilagent.app.data.entity

import androidx.room.*

@Entity(tableName = "school_classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#3F51B5" // Default Indigo
)

@Entity(tableName = "school_subjects")
data class SchoolSubject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isSystem: Boolean = false
)

@Entity(
    tableName = "syllabus_entries",
    primaryKeys = ["profileId", "dayOfWeek", "lessonOrder"],
    foreignKeys = [
        ForeignKey(entity = Profile::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SchoolClass::class, parentColumns = ["id"], childColumns = ["classId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = SchoolSubject::class, parentColumns = ["id"], childColumns = ["subjectId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("profileId"), Index("classId"), Index("subjectId")]
)
data class SyllabusEntry(
    val profileId: Long,
    val dayOfWeek: Int, // 1..7
    val lessonOrder: Int, // 1..N (matches BellSchedule orderIndex for non-breaks)
    val classId: Long?,
    val subjectId: Long?
)
