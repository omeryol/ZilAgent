package com.zilagent.app.ui.exam

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExamViewModel : ViewModel() {
    private val _durationInput = MutableStateFlow("40")
    val durationInput: StateFlow<String> = _durationInput.asStateFlow()

    private val _examDurationMinutes = MutableStateFlow(40)
    val examDurationMinutes: StateFlow<Int> = _examDurationMinutes.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    fun onDurationInputChange(input: String) {
        if (input.all { it.isDigit() } && input.length <= 3) {
            _durationInput.value = input
            _examDurationMinutes.value = input.toIntOrNull() ?: 40
        }
    }

    fun toggleRunning() {
        _isRunning.value = !_isRunning.value
    }

    fun reset() {
        _isRunning.value = false
        _elapsedSeconds.value = 0L
    }

    fun setElapsed(seconds: Long) {
        _elapsedSeconds.value = seconds
    }

    init {
        viewModelScope.launch {
            while (true) {
                if (_isRunning.value && _elapsedSeconds.value < (_examDurationMinutes.value * 60L)) {
                    _elapsedSeconds.value += 1
                }
                delay(1000)
            }
        }
    }
}
