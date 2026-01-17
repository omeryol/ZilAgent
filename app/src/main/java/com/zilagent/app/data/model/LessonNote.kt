package com.zilagent.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_notes")
data class LessonNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val dayOfWeek: Int, // 1=Mon, ..., 7=Sun
    val lessonOrder: Int,
    val note: String,
    val updatedAt: Long = System.currentTimeMillis()
)
