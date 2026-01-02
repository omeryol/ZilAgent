package com.zilagent.app.data.dao

import androidx.room.*
import com.zilagent.app.data.entity.Holiday
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY startDate ASC")
    fun getAllHolidays(): Flow<List<Holiday>>

    @Query("SELECT * FROM holidays ORDER BY startDate ASC")
    suspend fun getAllHolidaysSync(): List<Holiday>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: Holiday)

    @Delete
    suspend fun deleteHoliday(holiday: Holiday)

    @Query("DELETE FROM holidays WHERE startDate = :startDate AND endDate = :endDate")
    suspend fun deleteHolidayByRange(startDate: String, endDate: String)
}
