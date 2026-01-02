package com.zilagent.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zilagent.app.data.dao.BellDao
import com.zilagent.app.data.dao.HolidayDao
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.data.entity.Profile
import com.zilagent.app.data.entity.Holiday
import com.zilagent.app.data.entity.Quote
import com.zilagent.app.data.dao.QuoteDao

@Database(entities = [Profile::class, BellSchedule::class, Holiday::class, Quote::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bellDao(): BellDao
    abstract fun holidayDao(): HolidayDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zil_agent_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
