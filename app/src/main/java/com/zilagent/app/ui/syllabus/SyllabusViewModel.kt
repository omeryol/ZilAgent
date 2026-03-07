package com.zilagent.app.ui.syllabus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.data.dao.BellDao
import com.zilagent.app.data.dao.SyllabusDao
import com.zilagent.app.data.dao.LessonNoteDao
import com.zilagent.app.util.SubjectConstants
import com.zilagent.app.data.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SyllabusUiState(
    val classes: List<SchoolClass> = emptyList(),
    val subjects: List<SchoolSubject> = emptyList(),
    val selectedDay: Int = 1, // 1..7
    val currentProfileId: Long = -1L,
    val bellSchedules: List<BellSchedule> = emptyList(),
    val isLoading: Boolean = false,
    val showAddClassDialog: Boolean = false,
    val showAddSubjectDialog: Boolean = false
)

class SyllabusViewModel(
    application: Application,
    private val syllabusDao: SyllabusDao,
    private val bellDao: BellDao,
    private val lessonNoteDao: LessonNoteDao
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SyllabusUiState())
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Observe Active Profile and Chain Schedule Observation
            bellDao.getActiveProfile().flatMapLatest { profile ->
                if (profile != null) {
                    _uiState.update { it.copy(currentProfileId = profile.id) }
                    // Combine with selectedDay changes
                    _uiState.map { it.selectedDay }.distinctUntilChanged().flatMapLatest { day ->
                        bellDao.getSchedulesForProfile(profile.id, day)
                    }
                } else {
                    _uiState.update { it.copy(currentProfileId = -1L) }
                    flowOf(emptyList())
                }
            }.collect { schedules ->
                _uiState.update { 
                    it.copy(
                        bellSchedules = schedules.filter { b -> !b.name.contains("Sabah Töreni") },
                        isLoading = false 
                    ) 
                }
            }
        }
        
        viewModelScope.launch {
             // Observe Classes
            syllabusDao.getAllClasses().collect { list ->
                _uiState.update { it.copy(classes = list) }
            }
        }
        
        viewModelScope.launch {
             // Observe Subjects
            syllabusDao.getAllSubjects().collect { list ->
                _uiState.update { it.copy(subjects = list) }
            }
        }
    }

    fun onDaySelected(day: Int) {
        _uiState.update { it.copy(selectedDay = day) }
        // bellSchedules will auto-update due to snapshotFlow in observeData
    }

    // Class Management
    fun addClass(name: String, color: String) {
        viewModelScope.launch {
            syllabusDao.insertClass(SchoolClass(name = name, colorHex = color))
        }
    }
    
    fun updateClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            syllabusDao.insertClass(schoolClass)
        }
    }

    fun deleteClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            syllabusDao.deleteClass(schoolClass)
        }
    }

    // Subject Management
    fun addSubject(name: String) {
        viewModelScope.launch {
            syllabusDao.insertSubject(SchoolSubject(name = name, isSystem = false))
        }
    }
    
    fun updateSubject(subject: SchoolSubject) {
        viewModelScope.launch {
            syllabusDao.insertSubject(subject)
        }
    }

    fun deleteSubject(subject: SchoolSubject) {
        viewModelScope.launch {
            syllabusDao.deleteSubject(subject)
        }
    }

    // Syllabus Management
    fun saveSyllabusEntry(lessonOrder: Int, classId: Long?, subjectId: Long?) {
        val currentProfileId = _uiState.value.currentProfileId
        if (currentProfileId == -1L) return

        viewModelScope.launch {
            if (classId == null && subjectId == null) {
                syllabusDao.deleteSyllabusEntry(currentProfileId, _uiState.value.selectedDay, lessonOrder)
            } else {
                syllabusDao.insertSyllabusEntry(
                    SyllabusEntry(
                        profileId = currentProfileId,
                        dayOfWeek = _uiState.value.selectedDay,
                        lessonOrder = lessonOrder,
                        classId = classId,
                        subjectId = subjectId
                    )
                )
            }
            com.zilagent.app.manager.BellManager(getApplication()).refreshWidgetState()
        }
    }

    fun getSyllabusForDay(day: Int): Flow<List<SyllabusEntry>> {
        // Simplified to just return the flow based on current profile ID
        // Note: Ideally this should also be observed in VM, but for now this works 
        // as long as the ProfileID is updated in uiState (which it is via observeData)
        
        return _uiState.map { it.currentProfileId }.distinctUntilChanged()
            .flatMapLatest { pid ->
                if (pid != -1L) syllabusDao.getSyllabusForDay(pid, day) else flowOf(emptyList())
            }
    }

    fun getNotesForDay(day: Int): Flow<List<LessonNote>> {
        val profileId = _uiState.value.currentProfileId
        if (profileId == -1L) return flowOf(emptyList())
        return lessonNoteDao.getNotesForDay(profileId, day)
    }

    fun copyDayPlan(fromDay: Int, toDay: Int) {
        val profileId = _uiState.value.currentProfileId
        if (profileId == -1L || fromDay == toDay) return

        viewModelScope.launch {
            val source = syllabusDao.getSyllabusForDaySync(profileId, fromDay)
            syllabusDao.deleteSyllabusForDay(profileId, toDay)
            if (source.isNotEmpty()) {
                val mapped = source.map {
                    it.copy(dayOfWeek = toDay)
                }
                syllabusDao.insertSyllabusEntries(mapped)
            }
            com.zilagent.app.manager.BellManager(getApplication()).refreshWidgetState()
        }
    }
    
    fun saveNote(lessonOrder: Int, noteContent: String) {
        val currentProfileId = _uiState.value.currentProfileId
        if (currentProfileId == -1L) return
        
        viewModelScope.launch {
            if (noteContent.isBlank()) {
               // logic to delete if needed, but for now we just save blank or we can query id and delete.
               // simpler to just save empty string or handle it in DAO.
               // actually we should probably support delete.
               // get existing note to find ID?
               val existing = lessonNoteDao.getNoteSync(currentProfileId, _uiState.value.selectedDay, lessonOrder)
               if (existing != null) {
                   lessonNoteDao.delete(existing.id)
               }
            } else {
               val existing = lessonNoteDao.getNoteSync(currentProfileId, _uiState.value.selectedDay, lessonOrder)
               val newNote = existing?.copy(note = noteContent, updatedAt = System.currentTimeMillis())
                   ?: LessonNote(profileId = currentProfileId, dayOfWeek = _uiState.value.selectedDay, lessonOrder = lessonOrder, note = noteContent)
               lessonNoteDao.insert(newNote)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val db = AppDatabase.getDatabase(application)
                SyllabusViewModel(
                    application = application,
                    syllabusDao = db.syllabusDao(),
                    bellDao = db.bellDao(),
                    lessonNoteDao = db.lessonNoteDao()
                )
            }
        }
    }
}
