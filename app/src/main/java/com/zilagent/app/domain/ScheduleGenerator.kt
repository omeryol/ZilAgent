package com.zilagent.app.domain

import com.zilagent.app.data.entity.BellSchedule
import java.time.LocalTime

object ScheduleGenerator {

    /**
     * Generates a full day schedule based on inputs.
     * All times are in minutes from midnight.
     */
    fun generateSchedule(
        profileId: Long,
        dayOfWeek: Int = 0, // 0 means daily/all days
        firstLessonStart: String, // "08:10"
        lessonDurationMinutes: Int,
        breakDurationMinutes: Int,
        lessonCount: Int,
        lunchBreakAfterLesson: Int? = null, // e.g., after 4th lesson
        lunchBreakDurationMinutes: Int = 0,
        morningAssemblyDuration: Int = 0, // If > 0, adds an assembly before 1st lesson
        preBellMinutes: Int = 0 // If > 0, adds a warning bell before each lesson
    ): List<BellSchedule> {
        val schedule = mutableListOf<BellSchedule>()
        
        // Parse start time to minutes - this is strictly the 1. Lesson Start
        val (startHour, startMin) = firstLessonStart.split(":").map { it.toInt() }
        var currentMinutes = startHour * 60 + startMin

        var order = 0

        // Optional Assembly (Shift backwards)
        if (morningAssemblyDuration > 0) {
            schedule.add(
                BellSchedule(
                    profileId = profileId,
                    dayOfWeek = dayOfWeek,
                    name = "Sabah Töreni",
                    startTime = currentMinutes - morningAssemblyDuration,
                    endTime = currentMinutes,
                    isBreak = false,
                    orderIndex = order++
                )
            )
            // No need to increment currentMinutes, as it IS the start of Lesson 1
        }

        for (i in 1..lessonCount) {
            // Pre-bell (Warning)
            if (preBellMinutes > 0) {
                schedule.add(
                    BellSchedule(
                        profileId = profileId,
                        dayOfWeek = dayOfWeek,
                        name = "$i. Ders Hazırlık",
                        startTime = currentMinutes - preBellMinutes,
                        endTime = currentMinutes,
                        isBreak = true,
                        orderIndex = order++
                    )
                )
            }

            // Lesson
            val lessonStart = currentMinutes
            val lessonEnd = currentMinutes + lessonDurationMinutes
            
            schedule.add(
                BellSchedule(
                    profileId = profileId,
                    dayOfWeek = dayOfWeek,
                    name = "$i. Ders",
                    startTime = lessonStart,
                    endTime = lessonEnd,
                    isBreak = false,
                    orderIndex = order++
                )
            )
            currentMinutes = lessonEnd

            // Break (if not last lesson)
            if (i < lessonCount) {
                var currentBreakDuration = breakDurationMinutes
                var breakName = "$i. Teneffüs"

                // Variable Break Logic
                if (i == 1 || i == 2) {
                    currentBreakDuration = 10
                }

                // Check for Lunch Break
                if (lunchBreakAfterLesson != null && i == lunchBreakAfterLesson) {
                    currentBreakDuration = lunchBreakDurationMinutes
                    breakName = "Öğle Arası"
                }

                val breakStart = currentMinutes
                val breakEnd = currentMinutes + currentBreakDuration
                
                schedule.add(
                    BellSchedule(
                        profileId = profileId,
                        dayOfWeek = dayOfWeek,
                        name = breakName,
                        startTime = breakStart,
                        endTime = breakEnd,
                        isBreak = true,
                        orderIndex = order++
                    )
                )
                currentMinutes = breakEnd
            }
        }

        return schedule
    }
    /**
     * Shifts the schedule starting from a specific index.
     * Takes the existing schedule, the index of the item that changed, and its new start/end times.
     * Re-calculates subsequent items based on their original durations.
     */
    fun updateScheduleFromIndex(
        currentSchedule: List<BellSchedule>,
        index: Int,
        newStartTime: Int,
        newEndTime: Int
    ): List<BellSchedule> {
        // If index is out of bounds, return original
        if (index !in currentSchedule.indices) return currentSchedule

        val updatedSchedule = currentSchedule.map { it.copy() }.toMutableList()
        val originalItem = updatedSchedule[index]

        // Update the target item
        updatedSchedule[index] = originalItem.copy(
            startTime = newStartTime,
            endTime = newEndTime
        )

        var previousEndTime = newEndTime
        
        for (i in (index + 1) until updatedSchedule.size) {
            val item = updatedSchedule[i]
            val duration = item.endTime - item.startTime
            
            val newItemStart = previousEndTime
            val newItemEnd = newItemStart + duration
            
            updatedSchedule[i] = item.copy(
                startTime = newItemStart,
                endTime = newItemEnd
            )
            
            previousEndTime = newItemEnd
        }
        
        return updatedSchedule
    }
}
