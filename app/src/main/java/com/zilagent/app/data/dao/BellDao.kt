package com.zilagent.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.data.entity.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface BellDao {
    // --- Profile Operations ---
    @Query("SELECT * FROM profiles")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<Profile?>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfileSync(): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile): Long

    @Update
    suspend fun updateProfile(profile: Profile)

    @Delete
    suspend fun deleteProfile(profile: Profile)

    // Helper to set active profile
    @Transaction
    suspend fun setActiveProfile(profileId: Long) {
        clearActiveProfiles()
        setActiveProfileById(profileId)
    }

    @Query("UPDATE profiles SET isActive = 0")
    suspend fun clearActiveProfiles()

    @Query("UPDATE profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun setActiveProfileById(profileId: Long)


    // --- Schedule Operations ---
    @Query("SELECT * FROM bell_schedules WHERE profileId = :profileId ORDER BY orderIndex ASC")
    fun getSchedulesForProfile(profileId: Long): Flow<List<BellSchedule>>

    @Query("SELECT * FROM bell_schedules WHERE profileId = :profileId ORDER BY orderIndex ASC")
    suspend fun getSchedulesForProfileSync(profileId: Long): List<BellSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BellSchedule)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<BellSchedule>)

    @Delete
    suspend fun deleteSchedule(schedule: BellSchedule)

    @Query("DELETE FROM bell_schedules WHERE profileId = :profileId")
    suspend fun deleteSchedulesForProfile(profileId: Long)
}
