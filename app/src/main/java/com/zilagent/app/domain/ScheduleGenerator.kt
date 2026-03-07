package com.zilagent.app.domain

import com.zilagent.app.data.entity.BellSchedule

object ScheduleGenerator {

    fun generateSchedule(
        profileId: Long,
        dayOfWeek: Int = 0,
        languageCode: String = "tr",
        firstLessonStart: String,
        lessonDurationMinutes: Int,
        breakDurationMinutes: Int,
        firstBreakDurationMinutes: Int? = null,
        secondBreakDurationMinutes: Int? = null,
        lessonCount: Int,
        lunchBreakAfterLesson: Int? = null,
        lunchBreakDurationMinutes: Int = 0,
        morningAssemblyDuration: Int = 0,
        preBellMinutes: Int = 0,
    ): List<BellSchedule> {
        val schedule = mutableListOf<BellSchedule>()
        val isEn = languageCode.lowercase() == "en"
        val labelCeremony = if (isEn) "Morning Assembly" else "Sabah Töreni"
        val labelPrep = if (isEn) "Prep" else "Hazırlık"
        val labelLesson = if (isEn) "Lesson" else "Ders"
        val labelBreak = if (isEn) "Break" else "Teneffüs"
        val labelLunch = if (isEn) "Lunch Break" else "Öğle Arası"

        val (startHour, startMin) = firstLessonStart.split(":").map { it.toInt() }
        var currentMinutes = startHour * 60 + startMin
        var order = 0

        if (morningAssemblyDuration > 0) {
            schedule.add(
                BellSchedule(
                    profileId = profileId,
                    dayOfWeek = dayOfWeek,
                    name = labelCeremony,
                    startTime = currentMinutes - morningAssemblyDuration,
                    endTime = currentMinutes,
                    isBreak = false,
                    orderIndex = order++,
                ),
            )
        }

        for (i in 1..lessonCount) {
            if (preBellMinutes > 0) {
                schedule.add(
                    BellSchedule(
                        profileId = profileId,
                        dayOfWeek = dayOfWeek,
                        name = "$i. $labelLesson $labelPrep",
                        startTime = currentMinutes - preBellMinutes,
                        endTime = currentMinutes,
                        isBreak = true,
                        orderIndex = order++,
                    ),
                )
            }

            val lessonStart = currentMinutes
            val lessonEnd = currentMinutes + lessonDurationMinutes
            schedule.add(
                BellSchedule(
                    profileId = profileId,
                    dayOfWeek = dayOfWeek,
                    name = "$i. $labelLesson",
                    startTime = lessonStart,
                    endTime = lessonEnd,
                    isBreak = false,
                    orderIndex = order++,
                ),
            )
            currentMinutes = lessonEnd

            if (i < lessonCount) {
                var currentBreakDuration = breakDurationMinutes
                var breakName = "$i. $labelBreak"

                if (i == 1 && firstBreakDurationMinutes != null) {
                    currentBreakDuration = firstBreakDurationMinutes
                } else if (i == 2 && secondBreakDurationMinutes != null) {
                    currentBreakDuration = secondBreakDurationMinutes
                }

                if (lunchBreakAfterLesson != null && i == lunchBreakAfterLesson) {
                    currentBreakDuration = lunchBreakDurationMinutes
                    breakName = labelLunch
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
                        orderIndex = order++,
                    ),
                )
                currentMinutes = breakEnd
            }
        }
        return schedule
    }

    fun updateScheduleFromIndex(
        currentSchedule: List<BellSchedule>,
        index: Int,
        newStartTime: Int,
        newEndTime: Int,
    ): List<BellSchedule> {
        if (index !in currentSchedule.indices) return currentSchedule

        val updatedSchedule = currentSchedule.map { it.copy() }.toMutableList()
        val originalItem = updatedSchedule[index]
        updatedSchedule[index] = originalItem.copy(startTime = newStartTime, endTime = newEndTime)

        var previousEndTime = newEndTime
        for (i in (index + 1) until updatedSchedule.size) {
            val item = updatedSchedule[i]
            val duration = item.endTime - item.startTime
            val newItemStart = previousEndTime
            val newItemEnd = newItemStart + duration
            updatedSchedule[i] = item.copy(startTime = newItemStart, endTime = newItemEnd)
            previousEndTime = newItemEnd
        }
        return updatedSchedule
    }
}
