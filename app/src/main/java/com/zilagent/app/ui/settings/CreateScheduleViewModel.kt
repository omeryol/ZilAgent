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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateScheduleUiState(
    val editProfileId: Long = -1L,
    val selectedDays: Set<Int> = setOf(1, 2, 3, 4, 5), // Default Mon-Fri
    val profileName: String = "Normal",
    val lessonCount: String = "8",
    val lessonDuration: String = "40",
    val breakDuration: String = "10", // Base default, logic handles specifics
    val firstBreakDuration: String = "",
    val secondBreakDuration: String = "",
    val startTime: String = "08:00",
    val lunchBreakAfter: String = "5",
    val lunchBreakDuration: String = "40",
    val morningAssemblyDuration: String = "10",
    val preBellDuration: String = "0", // 0 mean disabled
    val lessonStartNotifyEnabled: Boolean = true,
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
    private val bellManager: com.zilagent.app.manager.BellManager,
    private val profileId: Long = -1L
) : androidx.lifecycle.AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateScheduleUiState(editProfileId = profileId))
    val uiState: StateFlow<CreateScheduleUiState> = _uiState.asStateFlow()

    init {
        if (profileId != -1L) {
            loadProfileData(profileId)
        }
    }

    private fun loadProfileData(id: Long) {
        viewModelScope.launch {
            val profile = bellDao.getAllProfiles().first().find { it.id == id }
            if (profile != null) {
                val allSchedules = bellDao.getAllSchedulesForProfileSync(id)
                val inferredDays = allSchedules
                    .map { it.dayOfWeek }
                    .filter { it in 1..7 }
                    .toSet()
                    .ifEmpty { setOf(1, 2, 3, 4, 5) }

                // Fetch schedules for Monday (day 1) to infer settings
                val schedules = bellDao.getSchedulesForProfileSync(id, 1)
                
                if (schedules.isNotEmpty()) {
                    val lessons = schedules.filter(::isCountableLesson)
                    val firstLesson = lessons.minByOrNull { it.startTime }
                    val assembly = schedules.find(::isAssembly)
                    
                    val lessonDur = lessons.firstOrNull()?.let { it.endTime - it.startTime } ?: 40
                    val startTime = firstLesson?.startTime ?: 480
                    val assemblyDur = assembly?.let { it.endTime - it.startTime } ?: 0
                    
                    // Try to find normal breaks (exclude lunch and prep)
                    val normalBreaks = schedules
                        .filter { it.isBreak && !isLunchBreak(it) && !isPrepBreak(it) }
                        .sortedBy { it.startTime }
                    val normalBreak = normalBreaks.firstOrNull()
                    val breakDur = normalBreak?.let { it.endTime - it.startTime } ?: 10
                    val firstBreakDur = normalBreaks.getOrNull(0)?.let { it.endTime - it.startTime }
                    val secondBreakDur = normalBreaks.getOrNull(1)?.let { it.endTime - it.startTime }
                    
                    val preBell = schedules.find(::isPrepBreak)
                    val preBellDur = preBell?.let { it.endTime - it.startTime } ?: 0
                    val lessonStartNotify = schedules.any { !it.isBreak && it.notifyAtStart }

                    // Lunch break
                    val lunchBreak = schedules.find(::isLunchBreak)
                    val lunchAfter = if (lunchBreak != null) {
                        schedules.count { isCountableLesson(it) && it.endTime <= lunchBreak.startTime }
                    } else null
                    val lunchDur = lunchBreak?.let { it.endTime - it.startTime } ?: 40

                    _uiState.value = _uiState.value.copy(
                        profileName = profile.name,
                        selectedDays = inferredDays,
                        lessonCount = lessons.size.toString(),
                        lessonDuration = lessonDur.toString(),
                        breakDuration = breakDur.toString(),
                        firstBreakDuration = firstBreakDur?.toString().orEmpty(),
                        secondBreakDuration = secondBreakDur?.toString().orEmpty(),
                        startTime = String.format("%02d:%02d", startTime / 60, startTime % 60),
                        lunchBreakAfter = (lunchAfter ?: 5).toString(),
                        lunchBreakDuration = lunchDur.toString(),
                        morningAssemblyDuration = assemblyDur.toString(),
                        preBellDuration = preBellDur.toString(),
                        lessonStartNotifyEnabled = lessonStartNotify,
                        countdownColorEnabled = com.zilagent.app.widget.WidgetStore.isDynamicColorEnabled(getApplication())
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        profileName = profile.name,
                        selectedDays = inferredDays,
                        countdownColorEnabled = com.zilagent.app.widget.WidgetStore.isDynamicColorEnabled(getApplication())
                    )
                }
            }
        }
    }

    fun onDayToggle(day: Int) {
        // Day 0 = "Tümü" toggle
        if (day == 0) {
            val current = _uiState.value.selectedDays
            if (current.containsAll((1..7).toList())) {
                _uiState.value = _uiState.value.copy(selectedDays = emptySet())
            } else {
                _uiState.value = _uiState.value.copy(selectedDays = (1..7).toSet())
            }
        } else {
            val current = _uiState.value.selectedDays.toMutableSet()
            if (current.contains(day)) {
                current.remove(day)
            } else {
                current.add(day)
            }
            _uiState.value = _uiState.value.copy(selectedDays = current)
        }
    }
    fun onProfileNameChange(value: String) { _uiState.value = _uiState.value.copy(profileName = value) }
    fun onLessonCountChange(value: String) { _uiState.value = _uiState.value.copy(lessonCount = value) }
    fun onLessonDurationChange(value: String) { _uiState.value = _uiState.value.copy(lessonDuration = value) }
    fun onBreakDurationChange(value: String) { _uiState.value = _uiState.value.copy(breakDuration = value) }
    fun onFirstBreakDurationChange(value: String) { _uiState.value = _uiState.value.copy(firstBreakDuration = value) }
    fun onSecondBreakDurationChange(value: String) { _uiState.value = _uiState.value.copy(secondBreakDuration = value) }
    fun onStartTimeChange(value: String) { _uiState.value = _uiState.value.copy(startTime = value) }
    fun onLunchBreakAfterChange(value: String) { _uiState.value = _uiState.value.copy(lunchBreakAfter = value) }
    fun onLunchBreakDurationChange(value: String) { _uiState.value = _uiState.value.copy(lunchBreakDuration = value) }
    fun onMorningAssemblyDurationChange(value: String) { _uiState.value = _uiState.value.copy(morningAssemblyDuration = value) }
    fun onPreBellDurationChange(value: String) { _uiState.value = _uiState.value.copy(preBellDuration = value) }
    fun onLessonStartNotifyEnabledChange(value: Boolean) { _uiState.value = _uiState.value.copy(lessonStartNotifyEnabled = value) }
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
                val appLanguage = com.zilagent.app.widget.WidgetStore.getAppLanguage(getApplication())
                val start = _uiState.value.startTime
                val lunchAfter = _uiState.value.lunchBreakAfter.toIntOrNull()
                val lunchDuration = _uiState.value.lunchBreakDuration.toIntOrNull() ?: 45
                val assemblyDuration = _uiState.value.morningAssemblyDuration.toIntOrNull() ?: 0

                // 1. Create or Update Profile
                val profileId = if (_uiState.value.editProfileId != -1L) {
                    val existing = bellDao.getAllProfiles().first().find { it.id == _uiState.value.editProfileId }
                    if (existing != null) {
                        val updated = existing.copy(name = _uiState.value.profileName)
                        bellDao.updateProfile(updated)
                        existing.id
                    } else {
                        val profile = Profile(name = _uiState.value.profileName, isActive = true)
                        bellDao.insertProfile(profile)
                    }
                } else {
                    val profile = Profile(name = _uiState.value.profileName, isActive = true)
                    bellDao.insertProfile(profile)
                }
                
                // Set as active
                bellDao.setActiveProfile(profileId)

                // 2. Generate and Save Schedule
                // Only generate for selected days
                val daysToGenerate = _uiState.value.selectedDays
                
                val allGeneratedSchedules = mutableListOf<com.zilagent.app.data.entity.BellSchedule>()
                
                // First delete existing schedules for this profile to ensure clean state based on new selection
                // Actually, logic below deletes per day, but if we deselect a day, we must delete it.
                // Safest approach: Delete ALL for profile, then insert selected.
                bellDao.deleteSchedulesForProfile(profileId)

                daysToGenerate.forEach { day ->
                    val daySchedules = ScheduleGenerator.generateSchedule(
                        profileId = profileId,
                        dayOfWeek = day,
                        languageCode = appLanguage,
                        firstLessonStart = start, // Note: ScheduleGenerator now puts Ceremony BEFORE this time
                        lessonDurationMinutes = lessonDuration,
                        breakDurationMinutes = breakDuration,
                        firstBreakDurationMinutes = null,
                        secondBreakDurationMinutes = null,
                        lessonCount = lessonCount,
                        lunchBreakAfterLesson = lunchAfter,
                        lunchBreakDurationMinutes = lunchDuration,
                        morningAssemblyDuration = assemblyDuration,
                        preBellMinutes = _uiState.value.preBellDuration.toIntOrNull() ?: 0
                    ).map { schedule ->
                        if (schedule.isBreak) {
                            schedule.copy(notifyAtStart = false)
                        } else {
                            schedule.copy(notifyAtStart = _uiState.value.lessonStartNotifyEnabled)
                        }
                    }
                    allGeneratedSchedules.addAll(daySchedules)
                }

                // 3. Save All
                bellDao.insertSchedules(allGeneratedSchedules)
                
                // 4. Schedule Alarms for TODAY if today was updated or if we updated all days
                val today = java.time.LocalDate.now().dayOfWeek.value
                val todaySchedule = bellDao.getSchedulesForProfileSync(profileId, today)
                bellManager.scheduleDailyAlarms(todaySchedule)
                
                // Save Widget Preference
                com.zilagent.app.widget.WidgetStore.setDynamicColorEnabled(getApplication(), _uiState.value.countdownColorEnabled)

                // Sync working-days mask with selected profile days
                val mask = buildWorkingDaysMask(daysToGenerate)
                com.zilagent.app.widget.WidgetStore.setWorkingDays(getApplication(), mask)
                
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

    private fun buildWorkingDaysMask(days: Set<Int>): String {
        return (1..7).joinToString(separator = "") { day ->
            if (days.contains(day)) "1" else "0"
        }
    }

    private fun isCountableLesson(item: com.zilagent.app.data.entity.BellSchedule): Boolean {
        if (item.isBreak) return false
        val lowered = normalizeScheduleName(item.name)
        return lowered.contains("ders") || lowered.contains("lesson")
    }

    private fun isLunchBreak(item: com.zilagent.app.data.entity.BellSchedule): Boolean {
        if (!item.isBreak) return false
        val lowered = normalizeScheduleName(item.name)
        return lowered.contains("ogle") || lowered.contains("lunch")
    }

    private fun isPrepBreak(item: com.zilagent.app.data.entity.BellSchedule): Boolean {
        if (!item.isBreak) return false
        val lowered = normalizeScheduleName(item.name)
        return lowered.contains("hazirlik") || lowered.contains("prep")
    }

    private fun isAssembly(item: com.zilagent.app.data.entity.BellSchedule): Boolean {
        val lowered = normalizeScheduleName(item.name)
        return lowered.contains("toren") || lowered.contains("assembly")
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
        fun provideFactory(application: ZilAgentApp, profileId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = AppDatabase.getDatabase(application)
                val bellManager = com.zilagent.app.manager.BellManager(application)
                CreateScheduleViewModel(application, db.bellDao(), bellManager, profileId)
            }
        }

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

