package com.zilagent.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bell_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class BellSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String, // e.g., "1. Ders", "Tenefüs"
    val startTime: Int, // Minutes from midnight (e.g., 08:30 -> 510)
    val endTime: Int, // Minutes from midnight
    val isBreak: Boolean = false,
    val orderIndex: Int, // To keep sorting correct
    @androidx.room.ColumnInfo(name = "notify_start") val notifyAtStart: Boolean = true,
    @androidx.room.ColumnInfo(name = "notify_end") val notifyAtEnd: Boolean = true
)
