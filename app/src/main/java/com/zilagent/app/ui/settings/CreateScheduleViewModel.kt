package com.zilagent.app.ui.settings

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
import com.zilagent.app.data.entity.Profile
import com.zilagent.app.domain.ScheduleGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateScheduleUiState(
    val profileName: String = "Normal",
    val lessonCount: String = "8",
    val lessonDuration: String = "40",
    val breakDuration: String = "10", // Base default, logic handles specifics
    val startTime: String = "08:00",
    val lunchBreakAfter: String = "5",
    val lunchBreakDuration: String = "40",
    val morningAssemblyDuration: String = "10",
    val countdownColorEnabled: Boolean = false,
    // Custom Countdown
    val customModeEnabled: Boolean = false,
    val customModeTitle: String = "",
    val customModeTime: String = "", // formatted HH:mm
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false
)

class CreateScheduleViewModel(
    application: android.app.Application,
    private val bellDao: BellDao,
    private val bellManager: com.zilagent.app.manager.BellManager
) : androidx.lifecycle.AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateScheduleUiState())
    val uiState: StateFlow<CreateScheduleUiState> = _uiState.asStateFlow()

    fun onProfileNameChange(value: String) { _uiState.value = _uiState.value.copy(profileName = value) }
    fun onLessonCountChange(value: String) { _uiState.value = _uiState.value.copy(lessonCount = value) }
    fun onLessonDurationChange(value: String) { _uiState.value = _uiState.value.copy(lessonDuration = value) }
    fun onBreakDurationChange(value: String) { _uiState.value = _uiState.value.copy(breakDuration = value) }
    fun onStartTimeChange(value: String) { _uiState.value = _uiState.value.copy(startTime = value) }
    fun onLunchBreakAfterChange(value: String) { _uiState.value = _uiState.value.copy(lunchBreakAfter = value) }
    fun onLunchBreakDurationChange(value: String) { _uiState.value = _uiState.value.copy(lunchBreakDuration = value) }
    fun onMorningAssemblyDurationChange(value: String) { _uiState.value = _uiState.value.copy(morningAssemblyDuration = value) }
    fun onCountdownColorEnabledChange(value: Boolean) { _uiState.value = _uiState.value.copy(countdownColorEnabled = value) }
    
    // Custom Mode Updates
    fun onCustomModeEnabledChange(value: Boolean) { _uiState.value = _uiState.value.copy(customModeEnabled = value) }
    fun onCustomModeTitleChange(value: String) { _uiState.value = _uiState.value.copy(customModeTitle = value) }
    fun onCustomModeTimeChange(value: String) { _uiState.value = _uiState.value.copy(customModeTime = value) }

    fun generateAndSave() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                // Parse inputs (Basic validation assumed for now)
                val lessonCount = _uiState.value.lessonCount.toIntOrNull() ?: 8
                val lessonDuration = _uiState.value.lessonDuration.toIntOrNull() ?: 40
                val breakDuration = _uiState.value.breakDuration.toIntOrNull() ?: 10
                val start = _uiState.value.startTime
                val lunchAfter = _uiState.value.lunchBreakAfter.toIntOrNull()
                val lunchDuration = _uiState.value.lunchBreakDuration.toIntOrNull() ?: 45
                val assemblyDuration = _uiState.value.morningAssemblyDuration.toIntOrNull() ?: 0

                // 1. Create Profile
                val profile = Profile(name = _uiState.value.profileName, isActive = true)
                
                // Set as active (logic inside Dao will clear others)
                val profileId = bellDao.insertProfile(profile)
                bellDao.setActiveProfile(profileId)

                // 2. Generate Schedule
                val schedules = ScheduleGenerator.generateSchedule(
                    profileId = profileId,
                    firstLessonStart = start,
                    lessonDurationMinutes = lessonDuration,
                    breakDurationMinutes = breakDuration,
                    lessonCount = lessonCount,
                    lunchBreakAfterLesson = lunchAfter,
                    lunchBreakDurationMinutes = lunchDuration,
                    morningAssemblyDuration = assemblyDuration
                )

                // 3. Save Schedule
                bellDao.deleteSchedulesForProfile(profileId) // Cleanup if any
                bellDao.insertSchedules(schedules)
                
                // 4. Schedule Alarms
                bellManager.scheduleDailyAlarms(schedules)
                
                // Save Widget Preference
                com.zilagent.app.widget.WidgetStore.setDynamicColorEnabled(getApplication(), _uiState.value.countdownColorEnabled)
                
                // Save Custom Countdown
                val customTimeParts = _uiState.value.customModeTime.split(":")
                var customMinutes = -1
                if (customTimeParts.size == 2) {
                     try {
                        customMinutes = customTimeParts[0].toInt() * 60 + customTimeParts[1].toInt()
                     } catch (e: Exception) { customMinutes = -1 }
                }
                
                com.zilagent.app.widget.WidgetStore.setCustomCountdown(
                    getApplication(),
                    _uiState.value.customModeEnabled,
                    _uiState.value.customModeTitle,
                    customMinutes
                )
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false, 
                    saveComplete = true
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }
    
    fun resetSaveComplete() {
        _uiState.value = _uiState.value.copy(saveComplete = false)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ZilAgentApp)
                val db = AppDatabase.getDatabase(application)
                val bellManager = com.zilagent.app.manager.BellManager(application)
                CreateScheduleViewModel(application, db.bellDao(), bellManager)
            }
        }
    }
}
