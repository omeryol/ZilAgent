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
        firstLessonStart: String, // "08:10"
        lessonDurationMinutes: Int,
        breakDurationMinutes: Int,
        lessonCount: Int,
        lunchBreakAfterLesson: Int? = null, // e.g., after 4th lesson
        lunchBreakDurationMinutes: Int = 0,
        morningAssemblyDuration: Int = 0 // If > 0, adds an assembly before 1st lesson
    ): List<BellSchedule> {
        val schedule = mutableListOf<BellSchedule>()
        
        // Parse start time to minutes
        val (startHour, startMin) = firstLessonStart.split(":").map { it.toInt() }
        var currentMinutes = startHour * 60 + startMin

        var order = 0

        // Optional Assembly
        if (morningAssemblyDuration > 0) {
            schedule.add(
                BellSchedule(
                    profileId = profileId,
                    name = "Sabah Töreni",
                    startTime = currentMinutes,
                    endTime = currentMinutes + morningAssemblyDuration,
                    isBreak = false, // It's an activity, not a break technically, or maybe it is? Let's say false.
                    orderIndex = order++
                )
            )
            currentMinutes += morningAssemblyDuration
        }

        for (i in 1..lessonCount) {
            // Lesson
            val lessonStart = currentMinutes
            val lessonEnd = currentMinutes + lessonDurationMinutes
            
            schedule.add(
                BellSchedule(
                    profileId = profileId,
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

                // Variable Break Logic: Break 1 & 2 are 10 minutes
                if (i == 1 || i == 2) {
                    currentBreakDuration = 10
                }

                // Check for Lunch Break (Overrides variable break if it coincides, though unlikely at 1 or 2)
                if (lunchBreakAfterLesson != null && i == lunchBreakAfterLesson) {
                    currentBreakDuration = lunchBreakDurationMinutes
                    breakName = "Öğle Arası"
                }

                val breakStart = currentMinutes
                val breakEnd = currentMinutes + currentBreakDuration
                
                schedule.add(
                    BellSchedule(
                        profileId = profileId,
                        name = breakName,
                        startTime = breakStart,
                        endTime = breakEnd,
                        isBreak = true,
                        orderIndex = order ++
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
