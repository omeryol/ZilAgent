package com.zilagent.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // "Normal", "Cuma", "Sinav"
    val isActive: Boolean = false // Only one should be true
)
