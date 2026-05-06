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
import com.zilagent.app.data.dao.LessonNoteDao
import com.zilagent.app.data.dao.SyllabusDao
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.data.entity.LessonNote
import com.zilagent.app.data.entity.SchoolClass
import com.zilagent.app.data.entity.SchoolSubject
import com.zilagent.app.data.entity.SyllabusEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

data class WeeklyLessonPlan(
    val dayOfWeek: Int,
    val lessonOrder: Int,
    val isLunchBreak: Boolean = false,
    val bell: BellSchedule,
    val entry: SyllabusEntry? = null,
    val note: LessonNote? = null,
)

data class WeeklyDayPlan(
    val dayOfWeek: Int,
    val lessons: List<WeeklyLessonPlan> = emptyList(),
)

data class SyllabusUiState(
    val classes: List<SchoolClass> = emptyList(),
    val subjects: List<SchoolSubject> = emptyList(),
    val currentProfileId: Long = -1L,
    val weekPlans: List<WeeklyDayPlan> = (1..7).map { WeeklyDayPlan(dayOfWeek = it) },
    val health: ProgramHealth = ProgramHealth(),
    val isLoading: Boolean = false,
)

data class ProgramHealth(
    val missingAssignmentCount: Int = 0,
    val notedLessonCount: Int = 0,
    val emptyDayCount: Int = 0,
    val overlapCount: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SyllabusViewModel(
    application: Application,
    private val syllabusDao: SyllabusDao,
    private val bellDao: BellDao,
    private val lessonNoteDao: LessonNoteDao,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SyllabusUiState())
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            bellDao.getActiveProfile()
                .distinctUntilChanged()
                .flatMapLatest { profile ->
                    if (profile != null) {
                        _uiState.update { it.copy(currentProfileId = profile.id) }
                        combine(
                            bellDao.getAllSchedulesForProfile(profile.id),
                            syllabusDao.getAllSyllabus(profile.id),
                            lessonNoteDao.getNotesForProfile(profile.id),
                        ) { schedules, entries, notes ->
                            Triple(schedules, entries, notes)
                        }
                    } else {
                        _uiState.update { it.copy(currentProfileId = -1L) }
                        flowOf(Triple(emptyList(), emptyList(), emptyList()))
                    }
                }
                .collect { (schedules, entries, notes) ->
                    _uiState.update {
                        it.copy(
                            weekPlans = buildWeekPlans(schedules, entries, notes),
                            health = buildProgramHealth(schedules, entries, notes),
                            isLoading = false,
                        )
                    }
                }
        }

        viewModelScope.launch {
            syllabusDao.getAllClasses().collect { list ->
                _uiState.update { it.copy(classes = list) }
            }
        }

        viewModelScope.launch {
            syllabusDao.getAllSubjects().collect { list ->
                _uiState.update { it.copy(subjects = list) }
            }
        }
    }

    private fun buildWeekPlans(
        schedules: List<BellSchedule>,
        entries: List<SyllabusEntry>,
        notes: List<LessonNote>,
    ): List<WeeklyDayPlan> {
        val visibleSchedules = schedules.filterNot { isHiddenSchedule(it) }
        return (1..7).map { day ->
            var lessonOrderCounter = 1
            val dayLessons = visibleSchedules
                .filter { (!it.isBreak || isLunchBreak(it)) && (it.dayOfWeek == day || it.dayOfWeek == 0) }
                .sortedBy { it.orderIndex }
                .map { bell ->
                    val isLunch = isLunchBreak(bell)
                    val currentOrder = if (isLunch) -1 else lessonOrderCounter++
                    WeeklyLessonPlan(
                        dayOfWeek = day,
                        lessonOrder = currentOrder,
                        isLunchBreak = isLunch,
                        bell = bell,
                        entry = if (isLunch) null else entries.firstOrNull { it.dayOfWeek == day && it.lessonOrder == currentOrder },
                        note = if (isLunch) null else notes.firstOrNull { it.dayOfWeek == day && it.lessonOrder == currentOrder },
                    )
                }
            WeeklyDayPlan(dayOfWeek = day, lessons = dayLessons)
        }
    }

    private fun isLunchBreak(bell: BellSchedule): Boolean {
        val n = bell.name.lowercase()
        return bell.isBreak && (n.contains("lunch") || n.contains("öğle") || n.contains("ogle"))
    }

    private fun isHiddenSchedule(schedule: BellSchedule): Boolean {
        val n = schedule.name.lowercase()
        return n.contains("sabah töreni") || n.contains("sabah toreni") || n.contains("assembly")
    }

    private fun buildProgramHealth(
        schedules: List<BellSchedule>,
        entries: List<SyllabusEntry>,
        notes: List<LessonNote>,
    ): ProgramHealth {
        val visibleSchedules = schedules.filterNot { isHiddenSchedule(it) }
        val lessonSlotsByDay = (1..7).associateWith { day ->
            visibleSchedules
                .filter { !it.isBreak && (it.dayOfWeek == day || it.dayOfWeek == 0) }
                .sortedBy { it.orderIndex }
        }
        val missingAssignments = lessonSlotsByDay.entries.sumOf { (day, daySchedules) ->
            daySchedules.mapIndexed { index, _ ->
                val lessonOrder = index + 1
                val entry = entries.firstOrNull { it.dayOfWeek == day && it.lessonOrder == lessonOrder }
                entry?.classId == null || entry.subjectId == null
            }.count { it }
        }
        val overlapCount = visibleSchedules
            .groupBy { it.dayOfWeek }
            .values
            .sumOf { daySchedules ->
                val ordered = daySchedules.sortedBy { it.startTime }
                ordered.zipWithNext().count { (left, right) -> right.startTime < left.endTime }
            }
        return ProgramHealth(
            missingAssignmentCount = missingAssignments,
            notedLessonCount = notes.count { it.note.isNotBlank() },
            emptyDayCount = lessonSlotsByDay.count { it.value.isEmpty() },
            overlapCount = overlapCount,
        )
    }

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

    fun saveSyllabusEntry(dayOfWeek: Int, lessonOrder: Int, classId: Long?, subjectId: Long?) {
        val profileId = _uiState.value.currentProfileId
        if (profileId == -1L) return

        viewModelScope.launch {
            if (classId == null && subjectId == null) {
                syllabusDao.deleteSyllabusEntry(profileId, dayOfWeek, lessonOrder)
            } else {
                syllabusDao.insertSyllabusEntry(
                    SyllabusEntry(
                        profileId = profileId,
                        dayOfWeek = dayOfWeek,
                        lessonOrder = lessonOrder,
                        classId = classId,
                        subjectId = subjectId,
                    ),
                )
            }
            com.zilagent.app.manager.BellManager(getApplication()).refreshWidgetState()
        }
    }

    fun copyDayPlan(fromDay: Int, toDay: Int) {
        val profileId = _uiState.value.currentProfileId
        if (profileId == -1L || fromDay == toDay) return

        viewModelScope.launch {
            val source = syllabusDao.getSyllabusForDaySync(profileId, fromDay)
            syllabusDao.deleteSyllabusForDay(profileId, toDay)
            if (source.isNotEmpty()) {
                syllabusDao.insertSyllabusEntries(source.map { it.copy(dayOfWeek = toDay) })
            }
            com.zilagent.app.manager.BellManager(getApplication()).refreshWidgetState()
        }
    }

    fun saveNote(dayOfWeek: Int, lessonOrder: Int, noteContent: String) {
        val profileId = _uiState.value.currentProfileId
        if (profileId == -1L) return

        viewModelScope.launch {
            if (noteContent.isBlank()) {
                val existing = lessonNoteDao.getNoteSync(profileId, dayOfWeek, lessonOrder)
                if (existing != null) {
                    lessonNoteDao.delete(existing.id)
                }
            } else {
                val existing = lessonNoteDao.getNoteSync(profileId, dayOfWeek, lessonOrder)
                val newNote = existing?.copy(note = noteContent, updatedAt = System.currentTimeMillis())
                    ?: LessonNote(
                        profileId = profileId,
                        dayOfWeek = dayOfWeek,
                        lessonOrder = lessonOrder,
                        note = noteContent,
                    )
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
                    lessonNoteDao = db.lessonNoteDao(),
                )
            }
        }
    }
}
