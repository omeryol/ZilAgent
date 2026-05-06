package com.zilagent.app.ui.exam

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

class ExamViewModel(application: Application) : AndroidViewModel(application) {
    data class ButterflyGroupConfig(
        val name: String,
        val startHour: Int,
        val startMinute: Int,
        val manualDurationMinutes: Int?,
    )

    data class ButterflySessionConfig(
        val name: String,
        val endHour: Int,
        val endMinute: Int,
        val groups: List<ButterflyGroupConfig>,
    )

    data class SessionStatusInfo(
        val label: String,
        val minutesUntilStart: Int?,
        val isActionDue: Boolean,
    )

    data class GroupCountdownInfo(
        val sessionIndex: Int,
        val groupIndex: Int,
        val sessionName: String,
        val groupName: String,
        val remainingSeconds: Long,
    )

    data class GroupStartCountdownInfo(
        val sessionIndex: Int,
        val groupIndex: Int,
        val sessionName: String,
        val groupName: String,
        val secondsUntilStart: Long,
    )

    data class SessionRunPlan(
        val sessionIndex: Int,
        val groupOffsetsMinutes: Map<Int, Int>,
        val groupDurationsMinutes: Map<Int, Int>,
    )

    private val _durationInput = MutableStateFlow("40")
    val durationInput: StateFlow<String> = _durationInput.asStateFlow()

    private val _examDurationMinutes = MutableStateFlow(40)
    val examDurationMinutes: StateFlow<Int> = _examDurationMinutes.asStateFlow()

    private val _endTimeHourInput = MutableStateFlow("")
    val endTimeHourInput: StateFlow<String> = _endTimeHourInput.asStateFlow()

    private val _endTimeMinuteInput = MutableStateFlow("")
    val endTimeMinuteInput: StateFlow<String> = _endTimeMinuteInput.asStateFlow()

    private val _sessionEndHourInput = MutableStateFlow("")
    val sessionEndHourInput: StateFlow<String> = _sessionEndHourInput.asStateFlow()

    private val _sessionEndMinuteInput = MutableStateFlow("")
    val sessionEndMinuteInput: StateFlow<String> = _sessionEndMinuteInput.asStateFlow()

    private val _groupAStartHourInput = MutableStateFlow("")
    val groupAStartHourInput: StateFlow<String> = _groupAStartHourInput.asStateFlow()

    private val _groupAStartMinuteInput = MutableStateFlow("")
    val groupAStartMinuteInput: StateFlow<String> = _groupAStartMinuteInput.asStateFlow()

    private val _groupANameInput = MutableStateFlow("")
    val groupANameInput: StateFlow<String> = _groupANameInput.asStateFlow()

    private val _groupAManualDurationInput = MutableStateFlow("")
    val groupAManualDurationInput: StateFlow<String> = _groupAManualDurationInput.asStateFlow()

    private val _groupBStartHourInput = MutableStateFlow("")
    val groupBStartHourInput: StateFlow<String> = _groupBStartHourInput.asStateFlow()

    private val _groupBStartMinuteInput = MutableStateFlow("")
    val groupBStartMinuteInput: StateFlow<String> = _groupBStartMinuteInput.asStateFlow()

    private val _groupBNameInput = MutableStateFlow("")
    val groupBNameInput: StateFlow<String> = _groupBNameInput.asStateFlow()

    private val _groupBManualDurationInput = MutableStateFlow("")
    val groupBManualDurationInput: StateFlow<String> = _groupBManualDurationInput.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _butterflySessions = MutableStateFlow(defaultButterflySessions())
    val butterflySessions: StateFlow<List<ButterflySessionConfig>> = _butterflySessions.asStateFlow()

    data class ActiveGroupKey(val sessionIndex: Int, val groupIndex: Int)

    private val _activeGroups = MutableStateFlow<Set<ActiveGroupKey>>(emptySet())
    val activeGroups: StateFlow<Set<ActiveGroupKey>> = _activeGroups.asStateFlow()

    private val _sessionRunPlan = MutableStateFlow<SessionRunPlan?>(null)
    val sessionRunPlan: StateFlow<SessionRunPlan?> = _sessionRunPlan.asStateFlow()

    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private var pausedElapsedSeconds: Long = 0L
    private var runningStartedRealtimeMs: Long = 0L

    fun onDurationInputChange(input: String) {
        if (input.all { it.isDigit() } && input.length <= 4) {
            _durationInput.value = input
            _examDurationMinutes.value = (input.toIntOrNull() ?: 40).coerceIn(1, MAX_DURATION_MINUTES)
            saveState()
        }
    }

    fun increaseDuration(step: Int = 1) {
        val updated = (_examDurationMinutes.value + step).coerceIn(1, MAX_DURATION_MINUTES)
        setQuickDuration(updated)
    }

    fun decreaseDuration(step: Int = 1) {
        val updated = (_examDurationMinutes.value - step).coerceIn(1, MAX_DURATION_MINUTES)
        setQuickDuration(updated)
    }

    fun onEndTimeHourInputChange(input: String) {
        if (input.all { it.isDigit() } && input.length <= 2) {
            _endTimeHourInput.value = input
            saveState()
        }
    }

    fun onEndTimeMinuteInputChange(input: String) {
        if (input.all { it.isDigit() } && input.length <= 2) {
            _endTimeMinuteInput.value = input
            saveState()
        }
    }

    fun setEndTime(hour: Int, minute: Int) {
        if (hour !in 0..23 || minute !in 0..59) return
        val now = LocalTime.now()
        val target = LocalTime.of(hour, minute)
        var diffSeconds = target.toSecondOfDay() - now.toSecondOfDay()
        if (diffSeconds <= 0) diffSeconds += 24 * 60 * 60
        val durationMinutes = ((diffSeconds + 59) / 60).coerceIn(1, MAX_DURATION_MINUTES)
        _endTimeHourInput.value = "%02d".format(hour)
        _endTimeMinuteInput.value = "%02d".format(minute)
        _examDurationMinutes.value = durationMinutes
        _durationInput.value = durationMinutes.toString()
        saveState()
    }

    fun updateButterflySessionName(sessionIndex: Int, input: String) {
        updateButterflySession(sessionIndex) { session ->
            session.copy(name = input.take(MAX_SESSION_NAME_LENGTH))
        }
    }

    fun setButterflySessionEndTime(sessionIndex: Int, hour: Int, minute: Int) {
        if (hour !in 0..23 || minute !in 0..59) return
        updateButterflySession(sessionIndex) { session ->
            session.copy(endHour = hour, endMinute = minute)
        }
    }

    fun updateButterflyGroupName(sessionIndex: Int, groupIndex: Int, input: String) {
        updateButterflyGroup(sessionIndex, groupIndex) { group ->
            group.copy(name = input.take(MAX_SESSION_NAME_LENGTH))
        }
    }

    fun setButterflyGroupStartTime(sessionIndex: Int, groupIndex: Int, hour: Int, minute: Int) {
        if (hour !in 0..23 || minute !in 0..59) return
        updateButterflyGroup(sessionIndex, groupIndex) { group ->
            group.copy(startHour = hour, startMinute = minute)
        }
    }

    fun updateButterflyGroupManualDuration(sessionIndex: Int, groupIndex: Int, input: String) {
        if (input.any { !it.isDigit() } || input.length > 4) return
        updateButterflyGroup(sessionIndex, groupIndex) { group ->
            group.copy(manualDurationMinutes = input.toIntOrNull()?.coerceIn(1, MAX_DURATION_MINUTES))
        }
    }

    fun getButterflyEffectiveDuration(sessionIndex: Int, groupIndex: Int): Int? {
        val session = _butterflySessions.value.getOrNull(sessionIndex) ?: return null
        val group = session.groups.getOrNull(groupIndex) ?: return null
        group.manualDurationMinutes?.let { return it.coerceIn(1, MAX_DURATION_MINUTES) }
        val startSeconds = group.startHour * 3600 + group.startMinute * 60
        var endSeconds = session.endHour * 3600 + session.endMinute * 60
        if (endSeconds <= startSeconds) endSeconds += 24 * 3600
        return ((endSeconds - startSeconds) / 60).coerceIn(1, MAX_DURATION_MINUTES)
    }

    fun getButterflyStatus(sessionIndex: Int, groupIndex: Int, now: LocalTime = LocalTime.now()): SessionStatusInfo {
        val session = _butterflySessions.value.getOrNull(sessionIndex)
            ?: return SessionStatusInfo("Hazir", null, false)
        val group = session.groups.getOrNull(groupIndex)
            ?: return SessionStatusInfo("Hazir", null, false)
        val effectiveDuration = getButterflyEffectiveDuration(sessionIndex, groupIndex)
            ?: return SessionStatusInfo("Sure eksik", null, false)

        val startMinutes = group.startHour * 60 + group.startMinute
        val nowMinutes = now.hour * 60 + now.minute
        val minutesUntilStart = startMinutes - nowMinutes
        return when {
            minutesUntilStart > 0 -> SessionStatusInfo("$minutesUntilStart dk sonra", minutesUntilStart, false)
            minutesUntilStart == 0 -> SessionStatusInfo("Baslama zamani geldi", 0, true)
            -minutesUntilStart < effectiveDuration -> SessionStatusInfo("Aktif aralik", minutesUntilStart, true)
            else -> SessionStatusInfo("Tamamlandi/Hazir", minutesUntilStart, false)
        }
    }

    fun startButterflyGroup(sessionIndex: Int, groupIndex: Int): Boolean {
        val durationMinutes = getButterflyEffectiveDuration(sessionIndex, groupIndex) ?: return false
        setQuickDuration(durationMinutes)
        _elapsedSeconds.value = 0L
        pausedElapsedSeconds = 0L
        _isRunning.value = true
        runningStartedRealtimeMs = SystemClock.elapsedRealtime()
        _sessionRunPlan.value = null
        _activeGroups.value = _activeGroups.value + ActiveGroupKey(sessionIndex, groupIndex)
        saveState()
        return true
    }

    fun startButterflySession(sessionIndex: Int): Boolean {
        val session = _butterflySessions.value.getOrNull(sessionIndex) ?: return false
        if (session.groups.isEmpty()) return false

        val groupDurations = session.groups.mapIndexedNotNull { groupIndex, _ ->
            getButterflyEffectiveDuration(sessionIndex, groupIndex)?.let { groupIndex to it }
        }.toMap()
        if (groupDurations.size != session.groups.size) return false

        // Common-start means groups begin at different moments so they finish together.
        val masterDuration = groupDurations.values.maxOrNull() ?: return false
        val groupOffsets = session.groups.indices.associateWith { groupIndex ->
            val duration = groupDurations[groupIndex] ?: masterDuration
            (masterDuration - duration).coerceAtLeast(0)
        }

        setQuickDuration(masterDuration.coerceIn(1, MAX_DURATION_MINUTES))
        _elapsedSeconds.value = 0L
        pausedElapsedSeconds = 0L
        _isRunning.value = true
        runningStartedRealtimeMs = SystemClock.elapsedRealtime()

        _sessionRunPlan.value = SessionRunPlan(
            sessionIndex = sessionIndex,
            groupOffsetsMinutes = groupOffsets,
            groupDurationsMinutes = groupDurations,
        )
        _activeGroups.value = session.groups.indices
            .filter { (groupOffsets[it] ?: 0) == 0 }
            .map { ActiveGroupKey(sessionIndex, it) }
            .toSet()
        saveState()
        return true
    }

    fun getActiveGroupCountdowns(elapsedSeconds: Long): List<GroupCountdownInfo> {
        val elapsedSafe = elapsedSeconds.coerceAtLeast(0L)
        val sessions = _butterflySessions.value
        val plan = _sessionRunPlan.value
        return _activeGroups.value.mapNotNull { key ->
            val session = sessions.getOrNull(key.sessionIndex) ?: return@mapNotNull null
            val group = session.groups.getOrNull(key.groupIndex) ?: return@mapNotNull null
            val remaining = if (plan != null && plan.sessionIndex == key.sessionIndex) {
                val duration = plan.groupDurationsMinutes[key.groupIndex] ?: return@mapNotNull null
                val offset = plan.groupOffsetsMinutes[key.groupIndex] ?: 0
                val adjustedElapsed = (elapsedSafe - offset * 60L).coerceAtLeast(0L)
                (duration * 60L - adjustedElapsed).coerceAtLeast(0L)
            } else {
                val duration = getButterflyEffectiveDuration(key.sessionIndex, key.groupIndex) ?: return@mapNotNull null
                (duration * 60L - elapsedSafe).coerceAtLeast(0L)
            }
            GroupCountdownInfo(
                sessionIndex = key.sessionIndex,
                groupIndex = key.groupIndex,
                sessionName = session.name,
                groupName = group.name,
                remainingSeconds = remaining,
            )
        }.sortedWith(compareBy<GroupCountdownInfo> { it.remainingSeconds }.thenBy { it.sessionIndex }.thenBy { it.groupIndex })
    }

    fun getPendingGroupStartCountdowns(elapsedSeconds: Long): List<GroupStartCountdownInfo> {
        val plan = _sessionRunPlan.value ?: return emptyList()
        val session = _butterflySessions.value.getOrNull(plan.sessionIndex) ?: return emptyList()
        val elapsedSafe = elapsedSeconds.coerceAtLeast(0L)
        return session.groups.mapIndexedNotNull { groupIndex, group ->
            val key = ActiveGroupKey(plan.sessionIndex, groupIndex)
            if (key in _activeGroups.value) return@mapIndexedNotNull null
            val offsetMinutes = plan.groupOffsetsMinutes[groupIndex] ?: return@mapIndexedNotNull null
            val remaining = offsetMinutes * 60L - elapsedSafe
            if (remaining <= 0L) return@mapIndexedNotNull null
            GroupStartCountdownInfo(
                sessionIndex = plan.sessionIndex,
                groupIndex = groupIndex,
                sessionName = session.name,
                groupName = group.name,
                secondsUntilStart = remaining,
            )
        }.sortedBy { it.secondsUntilStart }
    }

    fun getPendingGroups(now: LocalTime = LocalTime.now()): List<Triple<Int, Int, Int>> {
        val pendingGroups = mutableListOf<Triple<Int, Int, Int>>()
        _butterflySessions.value.forEachIndexed { sessionIndex, session ->
            session.groups.forEachIndexed { groupIndex, group ->
                val key = ActiveGroupKey(sessionIndex, groupIndex)
                if (key !in _activeGroups.value) {
                    val startMinutes = group.startHour * 60 + group.startMinute
                    val nowMinutes = now.hour * 60 + now.minute
                    val minutesUntilStart = startMinutes - nowMinutes
                    if (minutesUntilStart > 0) {
                        pendingGroups.add(Triple(sessionIndex, groupIndex, minutesUntilStart))
                    }
                }
            }
        }
        return pendingGroups.sortedBy { it.third }
    }

    fun areAllGroupsStarted(): Boolean {
        val totalGroups = _butterflySessions.value.sumOf { it.groups.size }
        return _activeGroups.value.size >= totalGroups
    }

    fun setSessionEndTime(hour: Int, minute: Int) {
        if (hour !in 0..23 || minute !in 0..59) return
        _sessionEndHourInput.value = "%02d".format(hour)
        _sessionEndMinuteInput.value = "%02d".format(minute)
        saveState()
    }

    fun setGroupStartTime(group: Int, hour: Int, minute: Int) {
        if (hour !in 0..23 || minute !in 0..59) return
        when (group) {
            GROUP_A -> {
                _groupAStartHourInput.value = "%02d".format(hour)
                _groupAStartMinuteInput.value = "%02d".format(minute)
            }
            GROUP_B -> {
                _groupBStartHourInput.value = "%02d".format(hour)
                _groupBStartMinuteInput.value = "%02d".format(minute)
            }
        }
        saveState()
    }

    fun onSessionNameChange(group: Int, input: String) {
        val sanitized = input.take(MAX_SESSION_NAME_LENGTH)
        when (group) {
            GROUP_A -> _groupANameInput.value = sanitized
            GROUP_B -> _groupBNameInput.value = sanitized
            else -> return
        }
        saveState()
    }

    fun onSessionManualDurationChange(group: Int, input: String) {
        if (input.any { !it.isDigit() } || input.length > 4) return
        when (group) {
            GROUP_A -> _groupAManualDurationInput.value = input
            GROUP_B -> _groupBManualDurationInput.value = input
            else -> return
        }
        saveState()
    }

    fun getEffectiveDurationMinutes(group: Int): Int? {
        val manual = when (group) {
            GROUP_A -> _groupAManualDurationInput.value
            GROUP_B -> _groupBManualDurationInput.value
            else -> return null
        }.toIntOrNull()?.coerceIn(1, MAX_DURATION_MINUTES)
        if (manual != null) return manual

        val (startHour, startMinute) = getGroupStart(group) ?: return null
        val (endHour, endMinute) = getSharedEnd() ?: return null
        val startSeconds = startHour * 3600 + startMinute * 60
        var endSeconds = endHour * 3600 + endMinute * 60
        if (endSeconds <= startSeconds) endSeconds += 24 * 3600
        return ((endSeconds - startSeconds) / 60).coerceIn(1, MAX_DURATION_MINUTES)
    }

    fun getSessionStatus(group: Int, now: LocalTime = LocalTime.now()): SessionStatusInfo {
        val (startHour, startMinute) = getGroupStart(group)
            ?: return SessionStatusInfo(label = "Saat eksik", minutesUntilStart = null, isActionDue = false)
        val effectiveDuration = getEffectiveDurationMinutes(group)
            ?: return SessionStatusInfo(label = "Sure eksik", minutesUntilStart = null, isActionDue = false)

        val startMinutes = startHour * 60 + startMinute
        val nowMinutes = now.hour * 60 + now.minute
        val minutesUntilStart = startMinutes - nowMinutes
        val isManualDuration = when (group) {
            GROUP_A -> _groupAManualDurationInput.value.isNotBlank()
            GROUP_B -> _groupBManualDurationInput.value.isNotBlank()
            else -> false
        }

        return when {
            minutesUntilStart > 0 -> SessionStatusInfo(
                label = "$minutesUntilStart dk sonra",
                minutesUntilStart = minutesUntilStart,
                isActionDue = false,
            )
            minutesUntilStart == 0 -> SessionStatusInfo(
                label = "Baslama zamani geldi",
                minutesUntilStart = 0,
                isActionDue = true,
            )
            -minutesUntilStart < effectiveDuration -> SessionStatusInfo(
                label = if (isManualDuration) "Manuel sure aktif" else "Oturum devam araliginda",
                minutesUntilStart = minutesUntilStart,
                isActionDue = true,
            )
            else -> SessionStatusInfo(
                label = if (isManualDuration) "Manuel sure ayarli" else "Hazir",
                minutesUntilStart = minutesUntilStart,
                isActionDue = false,
            )
        }
    }

    fun startSessionForGroup(group: Int): Boolean {
        val durationMinutes = getEffectiveDurationMinutes(group) ?: return false
        _examDurationMinutes.value = durationMinutes
        _durationInput.value = durationMinutes.toString()
        _elapsedSeconds.value = 0L
        pausedElapsedSeconds = 0L
        _isRunning.value = true
        runningStartedRealtimeMs = SystemClock.elapsedRealtime()
        saveState()
        return true
    }

    fun toggleRunning() {
        if (_isRunning.value) {
            refreshElapsedFromClock()
            _isRunning.value = false
            pausedElapsedSeconds = _elapsedSeconds.value
        } else {
            _isRunning.value = true
            runningStartedRealtimeMs = SystemClock.elapsedRealtime()
        }
        saveState()
    }

    fun startFromMinutesInput() {
        val minutes = (_durationInput.value.toIntOrNull() ?: _examDurationMinutes.value).coerceIn(1, MAX_DURATION_MINUTES)
        setQuickDuration(minutes)
        _elapsedSeconds.value = 0L
        pausedElapsedSeconds = 0L
        _isRunning.value = true
        runningStartedRealtimeMs = SystemClock.elapsedRealtime()
        _sessionRunPlan.value = null
        _activeGroups.value = emptySet()
        saveState()
    }

    fun startFromEndTimeInput(): Boolean {
        val hour = _endTimeHourInput.value.toIntOrNull() ?: return false
        val minute = _endTimeMinuteInput.value.toIntOrNull() ?: return false
        if (hour !in 0..23 || minute !in 0..59) return false

        val now = LocalTime.now()
        val target = LocalTime.of(hour, minute)
        var diffSeconds = target.toSecondOfDay() - now.toSecondOfDay()
        if (diffSeconds <= 0) diffSeconds += 24 * 60 * 60

        val durationMinutes = ((diffSeconds + 59) / 60).coerceIn(1, MAX_DURATION_MINUTES)
        setQuickDuration(durationMinutes)
        _elapsedSeconds.value = 0L
        pausedElapsedSeconds = 0L
        _isRunning.value = true
        runningStartedRealtimeMs = SystemClock.elapsedRealtime()
        _sessionRunPlan.value = null
        _activeGroups.value = emptySet()
        saveState()
        return true
    }

    fun reset() {
        _isRunning.value = false
        _elapsedSeconds.value = 0L
        pausedElapsedSeconds = 0L
        runningStartedRealtimeMs = 0L
        _sessionRunPlan.value = null
        _activeGroups.value = emptySet()
        saveState()
    }

    fun resetAndAlignToNow() {
        _isRunning.value = false
        _elapsedSeconds.value = 0L
        pausedElapsedSeconds = 0L
        runningStartedRealtimeMs = 0L
        _sessionRunPlan.value = null
        _activeGroups.value = emptySet()

        val now = LocalTime.now().withSecond(0).withNano(0)
        val quickEnd = now.plusMinutes(_examDurationMinutes.value.toLong())
        _endTimeHourInput.value = "%02d".format(quickEnd.hour)
        _endTimeMinuteInput.value = "%02d".format(quickEnd.minute)

        val currentSessions = _butterflySessions.value
        val effectiveDurations = currentSessions.mapIndexed { sessionIndex, session ->
            session.groups.mapIndexed { groupIndex, _ ->
                getButterflyEffectiveDuration(sessionIndex, groupIndex) ?: _examDurationMinutes.value
            }
        }

        _butterflySessions.value = currentSessions.mapIndexed { sessionIndex, session ->
            val sessionDuration = effectiveDurations.getOrNull(sessionIndex)
                ?.maxOrNull()
                ?.coerceIn(1, MAX_DURATION_MINUTES)
                ?: _examDurationMinutes.value
            val sessionEnd = now.plusMinutes(sessionDuration.toLong())
            session.copy(
                endHour = sessionEnd.hour,
                endMinute = sessionEnd.minute,
                groups = session.groups.map { group ->
                    group.copy(startHour = now.hour, startMinute = now.minute)
                },
            )
        }

        saveState()
    }

    fun setElapsed(seconds: Long) {
        _elapsedSeconds.value = seconds
        pausedElapsedSeconds = seconds
        saveState()
    }

    init {
        loadState()
        viewModelScope.launch {
            while (true) {
                if (_isRunning.value) {
                    refreshElapsedFromClock()
                    val max = _examDurationMinutes.value * 60L
                    if (_elapsedSeconds.value >= max) {
                        _elapsedSeconds.value = max
                        pausedElapsedSeconds = max
                        _isRunning.value = false
                        _sessionRunPlan.value = null
                        _activeGroups.value = emptySet()
                        saveState()
                    }

                    val plan = _sessionRunPlan.value
                    if (plan != null) {
                        val elapsedSafe = _elapsedSeconds.value.coerceAtLeast(0L)
                        val readyGroups = plan.groupOffsetsMinutes
                            .filter { (_, offsetMinutes) -> elapsedSafe >= offsetMinutes * 60L }
                            .keys
                            .map { ActiveGroupKey(plan.sessionIndex, it) }
                            .toSet()
                        if (readyGroups.isNotEmpty() && !_activeGroups.value.containsAll(readyGroups)) {
                            _activeGroups.value = _activeGroups.value + readyGroups
                            saveState()
                        }
                    }

                    val now = LocalTime.now()
                    val pending = getPendingGroups(now)
                    pending.forEach { (sessionIndex, groupIndex, minutesUntilStart) ->
                        if (minutesUntilStart <= 0) {
                            val key = ActiveGroupKey(sessionIndex, groupIndex)
                            if (key !in _activeGroups.value) {
                                _activeGroups.value = _activeGroups.value + key
                                saveState()
                            }
                        }
                    }
                }
                delay(250)
            }
        }
    }

    private fun refreshElapsedFromClock() {
        if (!_isRunning.value) {
            _elapsedSeconds.value = pausedElapsedSeconds
            return
        }
        val delta = ((SystemClock.elapsedRealtime() - runningStartedRealtimeMs) / 1000L).coerceAtLeast(0L)
        _elapsedSeconds.value = pausedElapsedSeconds + delta
    }

    private fun loadState() {
        val duration = prefs.getInt(KEY_DURATION_MIN, 40).coerceIn(1, MAX_DURATION_MINUTES)
        val running = prefs.getBoolean(KEY_RUNNING, false)
        val paused = prefs.getLong(KEY_PAUSED_ELAPSED, 0L).coerceAtLeast(0L)
        val startRt = prefs.getLong(KEY_START_RT, 0L).coerceAtLeast(0L)
        val savedHour = prefs.getString(KEY_END_TIME_HOUR, "").orEmpty()
        val savedMinute = prefs.getString(KEY_END_TIME_MINUTE, "").orEmpty()
        val savedSessionEndHour = prefs.getString(KEY_SESSION_END_HOUR, "").orEmpty()
        val savedSessionEndMinute = prefs.getString(KEY_SESSION_END_MINUTE, "").orEmpty()
        val savedGroupAStartHour = prefs.getString(KEY_GROUP_A_START_HOUR, "").orEmpty()
        val savedGroupAStartMinute = prefs.getString(KEY_GROUP_A_START_MINUTE, "").orEmpty()
        val savedGroupAName = prefs.getString(KEY_GROUP_A_NAME, "").orEmpty()
        val savedGroupAManualDuration = prefs.getString(KEY_GROUP_A_MANUAL_DURATION, "").orEmpty()
        val savedGroupBStartHour = prefs.getString(KEY_GROUP_B_START_HOUR, "").orEmpty()
        val savedGroupBStartMinute = prefs.getString(KEY_GROUP_B_START_MINUTE, "").orEmpty()
        val savedGroupBName = prefs.getString(KEY_GROUP_B_NAME, "").orEmpty()
        val savedGroupBManualDuration = prefs.getString(KEY_GROUP_B_MANUAL_DURATION, "").orEmpty()

        if (savedHour.isNotBlank() && savedMinute.isNotBlank()) {
            _endTimeHourInput.value = savedHour
            _endTimeMinuteInput.value = savedMinute
        } else {
            val defaultEnd = LocalTime.now().plusMinutes(duration.toLong())
            _endTimeHourInput.value = "%02d".format(defaultEnd.hour)
            _endTimeMinuteInput.value = "%02d".format(defaultEnd.minute)
        }

        if (savedSessionEndHour.isNotBlank() && savedSessionEndMinute.isNotBlank()) {
            _sessionEndHourInput.value = savedSessionEndHour
            _sessionEndMinuteInput.value = savedSessionEndMinute
        } else {
            _sessionEndHourInput.value = "10"
            _sessionEndMinuteInput.value = "45"
        }

        if (savedGroupAStartHour.isNotBlank() && savedGroupAStartMinute.isNotBlank()) {
            _groupAStartHourInput.value = savedGroupAStartHour
            _groupAStartMinuteInput.value = savedGroupAStartMinute
        } else {
            _groupAStartHourInput.value = "09"
            _groupAStartMinuteInput.value = "30"
        }
        _groupANameInput.value = savedGroupAName.ifBlank { "1. Oturum" }
        _groupAManualDurationInput.value = savedGroupAManualDuration

        if (savedGroupBStartHour.isNotBlank() && savedGroupBStartMinute.isNotBlank()) {
            _groupBStartHourInput.value = savedGroupBStartHour
            _groupBStartMinuteInput.value = savedGroupBStartMinute
        } else {
            _groupBStartHourInput.value = "09"
            _groupBStartMinuteInput.value = "45"
        }
        _groupBNameInput.value = savedGroupBName.ifBlank { "2. Oturum" }
        _groupBManualDurationInput.value = savedGroupBManualDuration

        _butterflySessions.value = loadButterflySessionsFromPrefs()

        _examDurationMinutes.value = duration
        _durationInput.value = duration.toString()
        pausedElapsedSeconds = paused
        runningStartedRealtimeMs = startRt
        _isRunning.value = running

        refreshElapsedFromClock()
        val max = duration * 60L
        if (_elapsedSeconds.value > max) {
            _elapsedSeconds.value = max
            pausedElapsedSeconds = max
            _isRunning.value = false
            saveState()
        }
    }

    private fun saveState() {
        prefs.edit()
            .putInt(KEY_DURATION_MIN, _examDurationMinutes.value)
            .putBoolean(KEY_RUNNING, _isRunning.value)
            .putLong(KEY_PAUSED_ELAPSED, pausedElapsedSeconds)
            .putLong(KEY_START_RT, runningStartedRealtimeMs)
            .putString(KEY_END_TIME_HOUR, _endTimeHourInput.value)
            .putString(KEY_END_TIME_MINUTE, _endTimeMinuteInput.value)
                .putString(KEY_SESSION_END_HOUR, _sessionEndHourInput.value)
                .putString(KEY_SESSION_END_MINUTE, _sessionEndMinuteInput.value)
                .putString(KEY_GROUP_A_START_HOUR, _groupAStartHourInput.value)
                .putString(KEY_GROUP_A_START_MINUTE, _groupAStartMinuteInput.value)
                .putString(KEY_GROUP_A_NAME, _groupANameInput.value)
                .putString(KEY_GROUP_A_MANUAL_DURATION, _groupAManualDurationInput.value)
                .putString(KEY_GROUP_B_START_HOUR, _groupBStartHourInput.value)
                .putString(KEY_GROUP_B_START_MINUTE, _groupBStartMinuteInput.value)
                .putString(KEY_GROUP_B_NAME, _groupBNameInput.value)
                .putString(KEY_GROUP_B_MANUAL_DURATION, _groupBManualDurationInput.value)
                .apply {
                    _butterflySessions.value.forEachIndexed { sessionIndex, session ->
                        putString("butterfly_session_${sessionIndex}_name", session.name)
                        putInt("butterfly_session_${sessionIndex}_end_hour", session.endHour)
                        putInt("butterfly_session_${sessionIndex}_end_minute", session.endMinute)
                        session.groups.forEachIndexed { groupIndex, group ->
                            putString("butterfly_session_${sessionIndex}_group_${groupIndex}_name", group.name)
                            putInt("butterfly_session_${sessionIndex}_group_${groupIndex}_start_hour", group.startHour)
                            putInt("butterfly_session_${sessionIndex}_group_${groupIndex}_start_minute", group.startMinute)
                            if (group.manualDurationMinutes != null) {
                                putInt("butterfly_session_${sessionIndex}_group_${groupIndex}_manual_duration", group.manualDurationMinutes)
                            } else {
                                remove("butterfly_session_${sessionIndex}_group_${groupIndex}_manual_duration")
                            }
                        }
                    }
                }
            .apply()
    }

    private fun setQuickDuration(minutes: Int) {
        val safeMinutes = minutes.coerceIn(1, MAX_DURATION_MINUTES)
        _examDurationMinutes.value = safeMinutes
        _durationInput.value = safeMinutes.toString()
        val end = LocalTime.now().plusMinutes(safeMinutes.toLong())
        _endTimeHourInput.value = "%02d".format(end.hour)
        _endTimeMinuteInput.value = "%02d".format(end.minute)
        saveState()
    }

    private fun updateButterflySession(sessionIndex: Int, transform: (ButterflySessionConfig) -> ButterflySessionConfig) {
        _butterflySessions.value = _butterflySessions.value.mapIndexed { index, session ->
            if (index == sessionIndex) transform(session) else session
        }
        saveState()
    }

    private fun updateButterflyGroup(
        sessionIndex: Int,
        groupIndex: Int,
        transform: (ButterflyGroupConfig) -> ButterflyGroupConfig,
    ) {
        updateButterflySession(sessionIndex) { session ->
            session.copy(groups = session.groups.mapIndexed { index, group ->
                if (index == groupIndex) transform(group) else group
            })
        }
    }

    private fun loadButterflySessionsFromPrefs(): List<ButterflySessionConfig> {
        val defaults = defaultButterflySessions()
        return defaults.mapIndexed { sessionIndex, defaultSession ->
            val name = prefs.getString("butterfly_session_${sessionIndex}_name", defaultSession.name).orEmpty()
            val endHour = prefs.getInt("butterfly_session_${sessionIndex}_end_hour", defaultSession.endHour)
            val endMinute = prefs.getInt("butterfly_session_${sessionIndex}_end_minute", defaultSession.endMinute)
            val hasLegacyFourGroupLayout = prefs.contains("butterfly_session_${sessionIndex}_group_2_name") ||
                prefs.contains("butterfly_session_${sessionIndex}_group_3_name") ||
                prefs.contains("butterfly_session_${sessionIndex}_group_2_start_hour") ||
                prefs.contains("butterfly_session_${sessionIndex}_group_3_start_hour")
            defaultSession.copy(
                name = name,
                endHour = endHour,
                endMinute = endMinute,
                groups = defaultSession.groups.mapIndexed { groupIndex, defaultGroup ->
                    ButterflyGroupConfig(
                        name = if (hasLegacyFourGroupLayout) {
                            defaultGroup.name
                        } else {
                            prefs.getString("butterfly_session_${sessionIndex}_group_${groupIndex}_name", defaultGroup.name).orEmpty()
                        },
                        startHour = if (hasLegacyFourGroupLayout) defaultGroup.startHour else prefs.getInt("butterfly_session_${sessionIndex}_group_${groupIndex}_start_hour", defaultGroup.startHour),
                        startMinute = if (hasLegacyFourGroupLayout) defaultGroup.startMinute else prefs.getInt("butterfly_session_${sessionIndex}_group_${groupIndex}_start_minute", defaultGroup.startMinute),
                        manualDurationMinutes = if (hasLegacyFourGroupLayout) {
                            defaultGroup.manualDurationMinutes
                        } else if (prefs.contains("butterfly_session_${sessionIndex}_group_${groupIndex}_manual_duration")) {
                            prefs.getInt("butterfly_session_${sessionIndex}_group_${groupIndex}_manual_duration", defaultGroup.manualDurationMinutes ?: 0)
                                .takeIf { it > 0 }
                        } else {
                            defaultGroup.manualDurationMinutes
                        },
                    )
                },
            )
        }
    }

    private fun defaultButterflySessions(): List<ButterflySessionConfig> {
        return listOf(
            ButterflySessionConfig(
                name = "Sayisal",
                endHour = 10,
                endMinute = 45,
                groups = listOf(
                    ButterflyGroupConfig("8. sinif", 9, 30, null),
                    ButterflyGroupConfig("5-6-7. siniflar", 9, 45, null),
                ),
            ),
            ButterflySessionConfig(
                name = "Sozel",
                endHour = 14,
                endMinute = 15,
                groups = listOf(
                    ButterflyGroupConfig("8. sinif", 13, 0, null),
                    ButterflyGroupConfig("5-6-7. siniflar", 13, 15, null),
                ),
            ),
        )
    }

    private fun getSharedEnd(): Pair<Int, Int>? {
        val hour = _sessionEndHourInput.value.toIntOrNull() ?: return null
        val minute = _sessionEndMinuteInput.value.toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour to minute
    }

    private fun getGroupStart(group: Int): Pair<Int, Int>? {
        val (hourText, minuteText) = when (group) {
            GROUP_A -> _groupAStartHourInput.value to _groupAStartMinuteInput.value
            GROUP_B -> _groupBStartHourInput.value to _groupBStartMinuteInput.value
            else -> return null
        }
        val hour = hourText.toIntOrNull() ?: return null
        val minute = minuteText.toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour to minute
    }

    companion object {
        private const val PREFS_NAME = "exam_mode_prefs"
        private const val KEY_DURATION_MIN = "duration_min"
        private const val KEY_RUNNING = "running"
        private const val KEY_PAUSED_ELAPSED = "paused_elapsed_sec"
        private const val KEY_START_RT = "start_rt_ms"
        private const val KEY_END_TIME_HOUR = "end_time_hour"
        private const val KEY_END_TIME_MINUTE = "end_time_minute"
        private const val KEY_SESSION_END_HOUR = "session_end_hour"
        private const val KEY_SESSION_END_MINUTE = "session_end_minute"
        private const val KEY_GROUP_A_START_HOUR = "group_a_start_hour"
        private const val KEY_GROUP_A_START_MINUTE = "group_a_start_minute"
        private const val KEY_GROUP_A_NAME = "group_a_name"
        private const val KEY_GROUP_A_MANUAL_DURATION = "group_a_manual_duration"
        private const val KEY_GROUP_B_START_HOUR = "group_b_start_hour"
        private const val KEY_GROUP_B_START_MINUTE = "group_b_start_minute"
        private const val KEY_GROUP_B_NAME = "group_b_name"
        private const val KEY_GROUP_B_MANUAL_DURATION = "group_b_manual_duration"
        private const val MAX_DURATION_MINUTES = 1440
        private const val MAX_SESSION_NAME_LENGTH = 32
        const val GROUP_A = 1
        const val GROUP_B = 2

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                ExamViewModel(application)
            }
        }
    }
}
