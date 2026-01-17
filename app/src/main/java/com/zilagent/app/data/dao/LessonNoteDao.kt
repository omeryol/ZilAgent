package com.zilagent.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zilagent.app.data.entity.LessonNote
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonNoteDao {
    @Query("SELECT * FROM lesson_notes WHERE profileId = :profileId AND dayOfWeek = :dayOfWeek AND lessonOrder = :lessonOrder")
    fun getNote(profileId: Long, dayOfWeek: Int, lessonOrder: Int): Flow<LessonNote?>

    @Query("SELECT * FROM lesson_notes WHERE profileId = :profileId AND dayOfWeek = :dayOfWeek AND lessonOrder = :lessonOrder")
    fun getNoteSync(profileId: Long, dayOfWeek: Int, lessonOrder: Int): LessonNote?

    @Query("SELECT * FROM lesson_notes WHERE profileId = :profileId AND dayOfWeek = :dayOfWeek")
    fun getNotesForDay(profileId: Long, dayOfWeek: Int): Flow<List<LessonNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: LessonNote)

    @Query("DELETE FROM lesson_notes WHERE id = :id")
    suspend fun delete(id: Long)
}
