package com.zilagent.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holidays")
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: String, // Format: YYYY-MM-DD
    val endDate: String,   // Format: YYYY-MM-DD
    val name: String
)
