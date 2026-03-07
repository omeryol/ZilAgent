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

class ExamViewModel(application: Application) : AndroidViewModel(application) {
    private val _durationInput = MutableStateFlow("40")
    val durationInput: StateFlow<String> = _durationInput.asStateFlow()

    private val _examDurationMinutes = MutableStateFlow(40)
    val examDurationMinutes: StateFlow<Int> = _examDurationMinutes.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private var pausedElapsedSeconds: Long = 0L
    private var runningStartedRealtimeMs: Long = 0L

    fun onDurationInputChange(input: String) {
        if (input.all { it.isDigit() } && input.length <= 3) {
            _durationInput.value = input
            _examDurationMinutes.value = input.toIntOrNull() ?: 40
            saveState()
        }
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

    fun reset() {
        _isRunning.value = false
        _elapsedSeconds.value = 0L
        pausedElapsedSeconds = 0L
        runningStartedRealtimeMs = 0L
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
                        saveState()
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
        val duration = prefs.getInt(KEY_DURATION_MIN, 40).coerceIn(1, 999)
        val running = prefs.getBoolean(KEY_RUNNING, false)
        val paused = prefs.getLong(KEY_PAUSED_ELAPSED, 0L).coerceAtLeast(0L)
        val startRt = prefs.getLong(KEY_START_RT, 0L).coerceAtLeast(0L)

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
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "exam_mode_prefs"
        private const val KEY_DURATION_MIN = "duration_min"
        private const val KEY_RUNNING = "running"
        private const val KEY_PAUSED_ELAPSED = "paused_elapsed_sec"
        private const val KEY_START_RT = "start_rt_ms"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                ExamViewModel(application)
            }
        }
    }
}
