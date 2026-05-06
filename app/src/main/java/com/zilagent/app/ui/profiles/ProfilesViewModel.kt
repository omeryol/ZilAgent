package com.zilagent.app.ui.profiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.data.dao.BellDao
import com.zilagent.app.data.dao.LessonNoteDao
import com.zilagent.app.data.dao.SyllabusDao
import com.zilagent.app.data.entity.Profile
import com.zilagent.app.manager.BellManager
import com.zilagent.app.widget.WidgetStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfilesUiState(
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = false
)

class ProfilesViewModel(
    application: Application,
    private val bellDao: BellDao,
    private val syllabusDao: SyllabusDao,
    private val lessonNoteDao: LessonNoteDao,
    private val bellManager: BellManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfilesUiState())
    val uiState: StateFlow<ProfilesUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            bellDao.getAllProfiles().collect { list ->
                _uiState.value = _uiState.value.copy(profiles = list, isLoading = false)
            }
        }
    }

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            bellDao.setActiveProfile(profile.id)

            val allSchedules = bellDao.getAllSchedulesForProfileSync(profile.id)
            val selectedDays = allSchedules.map { it.dayOfWeek }.filter { it in 1..7 }.toSet()
            val mask = (1..7).joinToString(separator = "") { day -> if (selectedDays.contains(day)) "1" else "0" }
            WidgetStore.setWorkingDays(getApplication(), mask)
            
            // Refresh Alarms for the new profile
            val today = java.time.LocalDate.now().dayOfWeek.value
            val schedules = bellDao.getSchedulesForProfileSync(profile.id, today)
            bellManager.scheduleDailyAlarms(schedules)
            
            // BellManager.scheduleDailyAlarms already calls refreshWidgetState()
            // which triggers triggerWidgetRefresh() for UI update.
        }
    }

    fun deleteProfile(profile: Profile) {
        if (profile.isActive) return // Don't delete active one for safety
        viewModelScope.launch {
            bellDao.deleteSchedulesForProfile(profile.id)
            bellDao.deleteProfile(profile)
        }
    }

    fun duplicateProfile(profile: Profile) {
        viewModelScope.launch {
            val copyName = buildDuplicateName(
                originalName = profile.name,
                existingNames = bellDao.getAllProfilesSync().map { it.name },
            )
            val newProfileId = bellDao.insertProfile(Profile(name = copyName, isActive = false))

            val schedules = bellDao.getAllSchedulesForProfileSync(profile.id)
                .map { it.copy(id = 0, profileId = newProfileId) }
            if (schedules.isNotEmpty()) {
                bellDao.insertSchedules(schedules)
            }

            val syllabusEntries = syllabusDao.getAllSyllabusSync(profile.id)
                .map { it.copy(profileId = newProfileId) }
            if (syllabusEntries.isNotEmpty()) {
                syllabusDao.insertSyllabusEntries(syllabusEntries)
            }

            val notes = lessonNoteDao.getNotesForProfileSync(profile.id)
                .map { it.copy(id = 0, profileId = newProfileId, updatedAt = System.currentTimeMillis()) }
            if (notes.isNotEmpty()) {
                lessonNoteDao.insertAll(notes)
            }
        }
    }

    private fun buildDuplicateName(originalName: String, existingNames: List<String>): String {
        val existing = existingNames.toSet()
        val base = "$originalName Kopya"
        if (base !in existing) return base
        var index = 2
        while (true) {
            val candidate = "$base $index"
            if (candidate !in existing) return candidate
            index++
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val db = AppDatabase.getDatabase(application)
                ProfilesViewModel(
                    application = application,
                    bellDao = db.bellDao(),
                    syllabusDao = db.syllabusDao(),
                    lessonNoteDao = db.lessonNoteDao(),
                    bellManager = BellManager(application)
                )
            }
        }
    }
}
