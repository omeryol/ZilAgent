package com.zilagent.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zilagent.app.ZilAgentApp
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.data.dao.BellDao
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.data.entity.Profile
import com.zilagent.app.domain.ScheduleGenerator
import com.zilagent.app.manager.BellManager
import com.zilagent.app.util.TimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime

data class DashboardUiState(
    val currentProfile: Profile? = null,
    val schedule: List<BellSchedule> = emptyList(),
    val nextBell: BellSchedule? = null,
    val secondsRemaining: Long = 0,
    val currentStatusText: String = "Loading...", // "3. Ders", "Tenefüs", etc.
    val isEndOfDay: Boolean = false,
    val activeItemId: Long? = null,
    val hasAnyScheduleForProfile: Boolean = false,
    val summary: DashboardSummary = DashboardSummary(),
)

data class DashboardSummary(
    val totalLessons: Int = 0,
    val completedLessons: Int = 0,
    val remainingLessons: Int = 0,
    val totalBreaks: Int = 0,
    val dayWindow: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val bellDao: BellDao,
    private val bellManager: BellManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var currentHolidayQuote: String? = null

    init {
        checkAndCreateDefaultProfile()
        loadData()
        startTimer()
    }

    private fun checkAndCreateDefaultProfile() {
        viewModelScope.launch {
            val profiles = bellDao.getAllProfilesSync()
            var defaultProfile = profiles.find { it.name == "Varsayılan" }

            if (defaultProfile == null && profiles.isEmpty()) {
                // Create default profile ONLY if no profiles exist
                defaultProfile = Profile(name = "Varsayılan", isActive = true)
                val profileId = bellDao.insertProfile(defaultProfile)
                
                // Generate default schedule
                generateDefaultScheduleForProfile(profileId)
            } else if (defaultProfile != null) {
                // If default exists, check if it has schedules AND if they are legacy (day=0)
                val schedules = bellDao.getAllSchedulesForProfileSync(defaultProfile.id)
                val hasGlobalSchedule = schedules.any { it.dayOfWeek == 0 }
                
                if (schedules.isEmpty() || hasGlobalSchedule) {
                    // Regenerate to fix "Weekend showing as workday" issue
                    generateDefaultScheduleForProfile(defaultProfile.id)
                }
                
                // Ensure active profile
                if (profiles.none { it.isActive }) {
                    bellDao.setActiveProfile(defaultProfile.id)
                }
            }
        }
    }

    private suspend fun generateDefaultScheduleForProfile(profileId: Long) {
        // Clear potential garbage before inserting
        bellDao.deleteSchedulesForProfile(profileId)
        
        val allSchedules = mutableListOf<BellSchedule>()
        
        // Generate separate schedules for Monday(1) to Friday(5)
        for (day in 1..5) {
            val daySchedules = com.zilagent.app.domain.ScheduleGenerator.generateSchedule(
                profileId = profileId,
                dayOfWeek = day,
                languageCode = "tr",
                firstLessonStart = "08:00",
                lessonDurationMinutes = 40,
                breakDurationMinutes = 10,
                firstBreakDurationMinutes = null,
                secondBreakDurationMinutes = null,
                lessonCount = 8,
                lunchBreakAfterLesson = 5,
                lunchBreakDurationMinutes = 40,
                morningAssemblyDuration = 10
            )
            allSchedules.addAll(daySchedules)
        }
        
        bellDao.insertSchedules(allSchedules)
    }

    private fun loadData() {
        viewModelScope.launch {
            bellDao.getActiveProfile()
                .distinctUntilChanged()
                .flatMapLatest { profile ->
                    if (profile != null) {
                        _uiState.value = _uiState.value.copy(currentProfile = profile)
                        val today = java.time.LocalDate.now().dayOfWeek.value
                        combine(
                            bellDao.getSchedulesForProfile(profile.id, today),
                            bellDao.getAllSchedulesForProfile(profile.id),
                        ) { todaySchedules, allSchedules ->
                            todaySchedules to allSchedules.any { !it.isBreak }
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            currentProfile = null,
                            schedule = emptyList(),
                            currentStatusText = "Profil Oluşturuluyor...",
                            hasAnyScheduleForProfile = false,
                            summary = DashboardSummary(),
                        )
                        flowOf(emptyList<BellSchedule>() to false)
                    }
                }
                .distinctUntilChanged()
                .collect { (schedules, hasAnySchedule) ->
                    _uiState.value = _uiState.value.copy(
                        schedule = schedules,
                        hasAnyScheduleForProfile = hasAnySchedule,
                    )
                    calculateStatus(schedules, hasAnySchedule)
                }
        }
    }

    // New function to handle manual time update
    // Updated function to handle manual time AND duration update
    fun updateItem(item: BellSchedule, newStartTime: Int, newDuration: Int, notifyStart: Boolean, notifyEnd: Boolean) {
        val schedules = _uiState.value.schedule
        val index = schedules.indexOfFirst { it.id == item.id }
        if (index == -1) return

        val updatedTodaySchedules = applyItemUpdate(
            schedules = schedules,
            index = index,
            newStartTime = newStartTime,
            newDuration = newDuration,
            notifyStart = notifyStart,
            notifyEnd = notifyEnd,
        )
        val startShift = newStartTime - item.startTime

        viewModelScope.launch {
            val allSchedules = bellDao.getAllSchedulesForProfileSync(item.profileId)
            val updatedSchedulesByDay = allSchedules
                .groupBy { it.dayOfWeek }
                .mapValues { (_, daySchedules) ->
                    val sortedSchedules = daySchedules.sortedBy { it.orderIndex }
                    val matchingIndex = findMatchingScheduleIndex(
                        schedules = sortedSchedules,
                        targetItem = item,
                        fallbackIndex = index,
                    )

                    if (matchingIndex == -1) {
                        sortedSchedules
                    } else {
                        val dayItem = sortedSchedules[matchingIndex]
                        val targetStartTime = if (dayItem.id == item.id) {
                            newStartTime
                        } else {
                            dayItem.startTime + startShift
                        }
                        applyItemUpdate(
                            schedules = sortedSchedules,
                            index = matchingIndex,
                            newStartTime = targetStartTime,
                            newDuration = newDuration,
                            notifyStart = notifyStart,
                            notifyEnd = notifyEnd,
                        )
                    }
                }

            bellDao.insertSchedules(updatedSchedulesByDay.values.flatten())

            val today = java.time.LocalDate.now().dayOfWeek.value
            val todaySchedule = buildVisibleTodaySchedule(updatedSchedulesByDay, today)
            bellManager.scheduleDailyAlarms(todaySchedule)

            _uiState.value = _uiState.value.copy(
                schedule = if (todaySchedule.isNotEmpty()) todaySchedule else updatedTodaySchedules,
            )
        }
    }

    private fun applyItemUpdate(
        schedules: List<BellSchedule>,
        index: Int,
        newStartTime: Int,
        newDuration: Int,
        notifyStart: Boolean,
        notifyEnd: Boolean,
    ): List<BellSchedule> {
        val newEndTime = newStartTime + newDuration
        val updatedSchedules = ScheduleGenerator.updateScheduleFromIndex(
            currentSchedule = schedules,
            index = index,
            newStartTime = newStartTime,
            newEndTime = newEndTime,
        )

        return updatedSchedules.toMutableList().apply {
            this[index] = this[index].copy(
                notifyAtStart = notifyStart,
                notifyAtEnd = notifyEnd,
            )
        }
    }

    private fun findMatchingScheduleIndex(
        schedules: List<BellSchedule>,
        targetItem: BellSchedule,
        fallbackIndex: Int,
    ): Int {
        val targetName = comparableScheduleName(targetItem.name)

        return schedules.indexOfFirst {
            it.orderIndex == targetItem.orderIndex && it.isBreak == targetItem.isBreak
        }.takeIf { it >= 0 }
            ?: schedules.indexOfFirst {
                it.isBreak == targetItem.isBreak && comparableScheduleName(it.name) == targetName
            }.takeIf { it >= 0 }
            ?: fallbackIndex.takeIf {
                it in schedules.indices && schedules[it].isBreak == targetItem.isBreak
            }
            ?: -1
    }

    private fun comparableScheduleName(name: String): String {
        return normalizeScheduleName(name)
            .replace(".", "")
            .replace(" ", "")
            .filterNot(Char::isDigit)
    }

    private fun buildVisibleTodaySchedule(
        schedulesByDay: Map<Int, List<BellSchedule>>,
        today: Int,
    ): List<BellSchedule> {
        return (schedulesByDay[today].orEmpty() + schedulesByDay[0].orEmpty())
            .sortedBy { it.orderIndex }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                // Update every second
                val schedules = _uiState.value.schedule
                if (schedules.isNotEmpty()) {
                    calculateStatus(schedules)
                }
                delay(1000)
            }
        }
    }

    private suspend fun calculateStatus(
        schedules: List<BellSchedule>,
        hasAnySchedule: Boolean = _uiState.value.hasAnyScheduleForProfile,
    ) {
        if (bellManager.isHolidayToday()) {
            if (currentHolidayQuote == null) {
                currentHolidayQuote = com.zilagent.app.util.QuoteConstants.getRandomQuoteDisplayText(
                    bellManager.getAppLanguage(),
                )
            }
            _uiState.value = _uiState.value.copy(
                nextBell = null,
                secondsRemaining = 0,
                currentStatusText = currentHolidayQuote!!,
                isEndOfDay = true,
                activeItemId = null,
                summary = buildSummary(schedules, TimeUtils.getCurrentMinutes()),
            )
            return
        }
        currentHolidayQuote = null

        if (schedules.isEmpty()) {
            val emptyStatus = when {
                _uiState.value.currentProfile == null -> "Profil Oluşturuluyor..."
                hasAnySchedule -> "Bugün ders yok"
                else -> "Bu profil için henüz ders programı yok"
            }
            _uiState.value = _uiState.value.copy(
                nextBell = null,
                secondsRemaining = 0,
                currentStatusText = emptyStatus,
                isEndOfDay = true,
                activeItemId = null,
                summary = DashboardSummary(),
            )
            return
        }

        val now = TimeUtils.getCurrentMinutes()
        val nowTime = LocalTime.now()
        val nowSecondsTotal = nowTime.toSecondOfDay()
        val summary = buildSummary(schedules, now)

        // Find current or next event
        // 1. Is it currently during a lesson/break?
        // 2. Is it in between today's start and end but in a void? (Shouldn't happen with our logic usually)
        // 3. Is it before first bell?
        // 4. Is it after last bell?

        val activeEvent = schedules.find { now >= it.startTime && now < it.endTime }
        
        if (activeEvent != null) {
            // We are IN an event
            val endSecondsTotal = activeEvent.endTime * 60
            val remaining = endSecondsTotal - nowSecondsTotal
            
            // Correction for negative crossing
            val finalRemaining = if (remaining < 0) 0 else remaining

            _uiState.value = _uiState.value.copy(
                nextBell = activeEvent,
                secondsRemaining = finalRemaining.toLong(),
                currentStatusText = activeEvent.name,
                isEndOfDay = false,
                activeItemId = activeEvent.id,
                summary = summary,
            )
        } else {
            // Check if before first
            val first = schedules.firstOrNull()
            if (first != null && now < first.startTime) {
                 val startSecondsTotal = first.startTime * 60
                 val remaining = startSecondsTotal - nowSecondsTotal
                 _uiState.value = _uiState.value.copy(
                    nextBell = first,
                    secondsRemaining = remaining.toLong(),
                    currentStatusText = "Sıradaki: ${first.name}",
                    isEndOfDay = false,
                    activeItemId = null,
                    summary = summary
                )
            } else {
                // Check if after last
                val last = schedules.lastOrNull()
                if (last != null && now >= last.endTime) {
                    _uiState.value = _uiState.value.copy(
                        nextBell = null,
                        secondsRemaining = 0,
                        currentStatusText = "Gün Bitti",
                        isEndOfDay = true,
                        activeItemId = null,
                        summary = summary
                    )
                } else {
                    val next = schedules.firstOrNull { it.startTime > now }
                    if (next != null) {
                        val startSecondsTotal = next.startTime * 60
                        val remaining = startSecondsTotal - nowSecondsTotal
                        _uiState.value = _uiState.value.copy(
                            nextBell = next,
                            secondsRemaining = remaining.toLong(),
                            currentStatusText = "Sıradaki: ${next.name}",
                            isEndOfDay = false,
                            activeItemId = null,
                            summary = summary
                        )
                    }
                }
            }
        }
    }

    private fun buildSummary(
        schedules: List<BellSchedule>,
        nowMinutes: Int,
    ): DashboardSummary {
        if (schedules.isEmpty()) return DashboardSummary()
        val lessons = schedules.filter(::isCountableLesson)
        val breaks = schedules.count(::isStandardBreak)
        val totalLessons = lessons.size
        val completedLessons = lessons.count { nowMinutes >= it.endTime }
        val remainingLessons = lessons.count { nowMinutes < it.endTime }
        val firstStart = schedules.minOfOrNull { it.startTime }
        val lastEnd = schedules.maxOfOrNull { it.endTime }
        val dayWindow = if (firstStart != null && lastEnd != null) {
            "${TimeUtils.minutesToTime(firstStart)} - ${TimeUtils.minutesToTime(lastEnd)}"
        } else {
            ""
        }
        return DashboardSummary(
            totalLessons = totalLessons,
            completedLessons = completedLessons,
            remainingLessons = remainingLessons,
            totalBreaks = breaks,
            dayWindow = dayWindow,
        )
    }

    private fun isCountableLesson(item: BellSchedule): Boolean {
        if (item.isBreak) return false
        val lowered = normalizeScheduleName(item.name)
        return lowered.contains("ders") || lowered.contains("lesson")
    }

    private fun isStandardBreak(item: BellSchedule): Boolean {
        if (!item.isBreak) return false
        return !isLunchBreak(item) && !isPrepBreak(item)
    }

    private fun isLunchBreak(item: BellSchedule): Boolean {
        val lowered = normalizeScheduleName(item.name)
        return lowered.contains("ogle") || lowered.contains("lunch")
    }

    private fun isPrepBreak(item: BellSchedule): Boolean {
        val lowered = normalizeScheduleName(item.name)
        return lowered.contains("hazirlik") || lowered.contains("prep")
    }

    private fun normalizeScheduleName(name: String): String {
        return name
            .lowercase()
            .replace("ö", "o")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ş", "s")
            .replace("ü", "u")
            .replace("Ã¶", "o")
            .replace("ÄŸ", "g")
            .replace("Ä±", "i")
            .replace("ÅŸ", "s")
            .replace("Ã¼", "u")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ZilAgentApp)
                val db = AppDatabase.getDatabase(application)
                DashboardViewModel(
                    bellDao = db.bellDao(),
                    bellManager = BellManager(application)
                )
            }
        }
    }
}
