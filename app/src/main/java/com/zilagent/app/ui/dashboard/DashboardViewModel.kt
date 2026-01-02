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
import com.zilagent.app.manager.BellManager
import com.zilagent.app.util.TimeUtils
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
    val activeItemId: Long? = null
)

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
            val profile = bellDao.getActiveProfileSync()
            if (profile == null) {
                // Create default profile if none exists
                val defaultProfile = Profile(name = "Varsayılan", isActive = true)
                val profileId = bellDao.insertProfile(defaultProfile)
                
                // Generate default schedule
                val schedules = com.zilagent.app.domain.ScheduleGenerator.generateSchedule(
                    profileId = profileId,
                    firstLessonStart = "08:00",
                    lessonDurationMinutes = 40,
                    breakDurationMinutes = 10,
                    lessonCount = 8,
                    lunchBreakAfterLesson = 4,
                    lunchBreakDurationMinutes = 40,
                    morningAssemblyDuration = 10
                )
                bellDao.insertSchedules(schedules)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            bellDao.getActiveProfile()
                .distinctUntilChanged()
                .flatMapLatest { profile ->
                    if (profile != null) {
                        _uiState.value = _uiState.value.copy(currentProfile = profile)
                        bellDao.getSchedulesForProfile(profile.id)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            currentProfile = null,
                            schedule = emptyList(),
                            currentStatusText = "Profil Oluşturuluyor..."
                        )
                        flowOf(emptyList<BellSchedule>())
                    }
                }
                .distinctUntilChanged()
                .collect { schedules ->
                    _uiState.value = _uiState.value.copy(schedule = schedules)
                    calculateStatus(schedules)
                }
        }
    }

    // New function to handle manual time update
    // Updated function to handle manual time AND duration update
    fun updateItem(item: BellSchedule, newStartTime: Int, newDuration: Int, notifyStart: Boolean, notifyEnd: Boolean) {
        val schedules = _uiState.value.schedule
        val index = schedules.indexOfFirst { it.id == item.id }
        if (index == -1) return

        // Calculate new end time based on NEW duration
        val newEndTime = newStartTime + newDuration

        var updatedSchedules = com.zilagent.app.domain.ScheduleGenerator.updateScheduleFromIndex(
            currentSchedule = schedules,
            index = index,
            newStartTime = newStartTime,
            newEndTime = newEndTime
        )
        
        // Update flags for the modified item
        updatedSchedules = updatedSchedules.toMutableList().apply {
            this[index] = this[index].copy(notifyAtStart = notifyStart, notifyAtEnd = notifyEnd)
        }

        viewModelScope.launch {
            // Update DB
            bellDao.insertSchedules(updatedSchedules)
            
            // Reschedule Alarms
            bellManager.scheduleDailyAlarms(updatedSchedules)
            
            // Optimistic UI update
            _uiState.value = _uiState.value.copy(schedule = updatedSchedules)
        }
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

    private fun calculateStatus(schedules: List<BellSchedule>) {
        if (bellManager.isHolidayToday()) {
            if (currentHolidayQuote == null) {
                currentHolidayQuote = com.zilagent.app.util.QuoteConstants.getRandomQuote()
            }
            _uiState.value = _uiState.value.copy(
                nextBell = null,
                secondsRemaining = 0,
                currentStatusText = currentHolidayQuote!!,
                isEndOfDay = true,
                activeItemId = null
            )
            return
        }
        currentHolidayQuote = null
        val now = TimeUtils.getCurrentMinutes()
        val nowTime = LocalTime.now()
        val nowSecondsTotal = nowTime.toSecondOfDay()

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
                activeItemId = activeEvent.id
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
                    activeItemId = null
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
                        activeItemId = null
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
                            activeItemId = null
                        )
                    }
                }
            }
        }
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
