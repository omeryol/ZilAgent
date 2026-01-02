package com.zilagent.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zilagent.app.widget.WidgetStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val dynamicColorEnabled: Boolean = false,
    val customModeEnabled: Boolean = false,
    val customModeTitle: String = "",
    val customModeTime: String = "",
    val profileName: String = "Yükleniyor...",
    // Widget Customization
    val widgetTextSize: Int = 28,
    val widgetLabelSize: Int = 14,
    val widgetBgOpacity: Int = 90,
    val widgetTextColor: String = "#111111",
    val widgetBarThickness: Int = 8,
    val widgetBgColor: String = "#FFFFFF",
    val widgetCornerRadius: Int = 16,
    // Theme & Sound
    val themeMode: Int = 0, // 0: System, 1: Light, 2: Dark
    val themeColorName: String = "Lavanta",
    val soundEnabled: Boolean = true,
    val autoSilentMode: Boolean = false,
    val widgetLayoutType: Int = 0,
    val workingDaysMask: String = "1111100",
    val holidayList: List<com.zilagent.app.data.entity.Holiday> = emptyList(),
    val showSeconds: Boolean = true,
    val multilineEnabled: Boolean = false,
    val progressBarEnabled: Boolean = true,
    // Full Customization
    val widgetFlowDirection: Int = 0, // 0: Vertical, 1: Horizontal
    val widgetAlignment: Int = 1,     // 0: Left, 1: Center, 2: Right
    val widgetSpacing: Int = 8,
    val widgetElementOrder: Int = 0, // 0: Time First, 1: Label First
    val quoteList: List<com.zilagent.app.data.entity.Quote> = emptyList()
)

class SettingsViewModel(
    application: Application,
    private val holidayDao: com.zilagent.app.data.dao.HolidayDao,
    private val quoteDao: com.zilagent.app.data.dao.QuoteDao
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        observeHolidays()
        observeQuotes()
    }

    private fun observeQuotes() {
        viewModelScope.launch {
            quoteDao.getAllQuotes().collect {
                _uiState.value = _uiState.value.copy(quoteList = it)
            }
        }
    }

    private fun observeHolidays() {
        viewModelScope.launch {
            holidayDao.getAllHolidays().collect {
                _uiState.value = _uiState.value.copy(holidayList = it)
            }
        }
    }

    private fun loadSettings() {
        val context = getApplication<Application>()
        val custom = WidgetStore.getCustomCountdown(context)
        
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                notificationsEnabled = WidgetStore.isNotificationsEnabled(context),
                dynamicColorEnabled = WidgetStore.isDynamicColorEnabled(context),
                customModeEnabled = custom.first,
                customModeTitle = custom.second,
                customModeTime = if (custom.third != -1) {
                    val hours = custom.third / 60
                    val mins = custom.third % 60
                    String.format("%02d:%02d", hours, mins)
                } else "",
                widgetTextSize = WidgetStore.getWidgetTextSize(context),
                widgetLabelSize = WidgetStore.getWidgetLabelTextSize(context),
                widgetBgOpacity = WidgetStore.getWidgetBgOpacity(context),
                widgetTextColor = WidgetStore.getWidgetTextColor(context),
                widgetBarThickness = WidgetStore.getWidgetBarThickness(context),
                widgetBgColor = WidgetStore.getWidgetBgColor(context),
                widgetCornerRadius = WidgetStore.getWidgetCornerRadius(context),
                themeMode = WidgetStore.getThemeMode(context),
                themeColorName = WidgetStore.getThemeColorName(context),
                soundEnabled = WidgetStore.isSoundEnabled(context),
                autoSilentMode = WidgetStore.isAutoSilentMode(context),
                widgetLayoutType = WidgetStore.getWidgetLayoutType(context),
                workingDaysMask = WidgetStore.getWorkingDays(context),
                showSeconds = WidgetStore.isShowSeconds(context),
                multilineEnabled = WidgetStore.isMultilineEnabled(context),
                progressBarEnabled = WidgetStore.isProgressBarEnabled(context),
                widgetFlowDirection = WidgetStore.getWidgetFlowDirection(context),
                widgetAlignment = WidgetStore.getWidgetAlignment(context),
                widgetSpacing = WidgetStore.getWidgetSpacing(context),
                widgetElementOrder = WidgetStore.getWidgetElementOrder(context)
            )
        }
    }

    fun onThemeColorChange(colorName: String) {
        WidgetStore.setThemeColorName(getApplication(), colorName)
        _uiState.value = _uiState.value.copy(themeColorName = colorName)
    }

    fun onNotificationsEnabledChange(enabled: Boolean) {
        WidgetStore.setNotificationsEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun onDynamicColorEnabledChange(enabled: Boolean) {
        WidgetStore.setDynamicColorEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(dynamicColorEnabled = enabled)
    }

    // Widget Customization Updates
    fun onWidgetTextSizeChange(size: Int) {
        WidgetStore.setWidgetTextSize(getApplication(), size)
        _uiState.value = _uiState.value.copy(widgetTextSize = size)
        triggerWidgetUpdate()
    }

    fun onWidgetLabelSizeChange(size: Int) {
        WidgetStore.setWidgetLabelTextSize(getApplication(), size)
        _uiState.value = _uiState.value.copy(widgetLabelSize = size)
        triggerWidgetUpdate()
    }

    fun onWidgetBgOpacityChange(opacity: Int) {
        WidgetStore.setWidgetBgOpacity(getApplication(), opacity)
        _uiState.value = _uiState.value.copy(widgetBgOpacity = opacity)
        triggerWidgetUpdate()
    }

    fun onWidgetTextColorChange(colorHex: String) {
        WidgetStore.setWidgetTextColor(getApplication(), colorHex)
        _uiState.value = _uiState.value.copy(widgetTextColor = colorHex)
        triggerWidgetUpdate()
    }

    fun onWidgetBarThicknessChange(dp: Int) {
        WidgetStore.setWidgetBarThickness(getApplication(), dp)
        _uiState.value = _uiState.value.copy(widgetBarThickness = dp)
        triggerWidgetUpdate()
    }

    fun onWidgetBgColorChange(colorHex: String) {
        WidgetStore.setWidgetBgColor(getApplication(), colorHex)
        _uiState.value = _uiState.value.copy(widgetBgColor = colorHex)
        triggerWidgetUpdate()
    }

    fun onWidgetCornerRadiusChange(radius: Int) {
        WidgetStore.setWidgetCornerRadius(getApplication(), radius)
        _uiState.value = _uiState.value.copy(widgetCornerRadius = radius)
        triggerWidgetUpdate()
    }

    fun onThemeModeChange(mode: Int) {
        WidgetStore.setThemeMode(getApplication(), mode)
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun onSoundEnabledChange(enabled: Boolean) {
        WidgetStore.setSoundEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(soundEnabled = enabled)
    }

    private fun triggerWidgetUpdate() {
        val context = getApplication<Application>()
        com.zilagent.app.widget.CountdownWidget.updateAllWidgets(context)
        com.zilagent.app.widget.HorizontalCountdownWidget.updateAll(context)
        com.zilagent.app.widget.ModernCountdownWidget.updateAll(context)
        com.zilagent.app.widget.PanoramicCountdownWidget.updateAll(context)
        com.zilagent.app.widget.CircleCountdownWidget.updateAll(context)
    }

    // Custom Mode logic...
    fun onCustomModeEnabledChange(enabled: Boolean) {
        updateCustomMode(enabled, _uiState.value.customModeTitle, _uiState.value.customModeTime)
        _uiState.value = _uiState.value.copy(customModeEnabled = enabled)
    }

    fun onCustomModeTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(customModeTitle = title)
        updateCustomMode(_uiState.value.customModeEnabled, title, _uiState.value.customModeTime)
    }

    fun onCustomModeTimeChange(time: String) {
        _uiState.value = _uiState.value.copy(customModeTime = time)
        updateCustomMode(_uiState.value.customModeEnabled, _uiState.value.customModeTitle, time)
    }

    private fun updateCustomMode(enabled: Boolean, title: String, timeStr: String) {
        val timeParts = timeStr.split(":")
        var minutes = -1
        if (timeParts.size == 2) {
            try {
                minutes = timeParts[0].toInt() * 60 + timeParts[1].toInt()
            } catch (e: Exception) {}
        }
        WidgetStore.setCustomCountdown(getApplication(), enabled, title, minutes)
    }

    // Holiday Management
    fun onWorkingDaysChange(mask: String) {
        WidgetStore.setWorkingDays(getApplication(), mask)
        _uiState.value = _uiState.value.copy(workingDaysMask = mask)
        triggerWidgetUpdate()
    }

    fun addHoliday(startDate: String, endDate: String, name: String) {
        viewModelScope.launch {
            holidayDao.insertHoliday(com.zilagent.app.data.entity.Holiday(startDate = startDate, endDate = endDate, name = name))
            triggerWidgetUpdate()
        }
    }

    fun deleteHoliday(holiday: com.zilagent.app.data.entity.Holiday) {
        viewModelScope.launch {
            holidayDao.deleteHoliday(holiday)
            triggerWidgetUpdate()
        }
    }

    fun addQuote(content: String) {
        viewModelScope.launch {
            quoteDao.insertQuote(com.zilagent.app.data.entity.Quote(content = content, isSystem = false))
        }
    }

    fun deleteQuote(quote: com.zilagent.app.data.entity.Quote) {
        viewModelScope.launch {
            quoteDao.deleteQuote(quote)
            triggerWidgetUpdate()
        }
    }

    fun onAutoSilentModeChange(enabled: Boolean) {
        WidgetStore.setAutoSilentMode(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(autoSilentMode = enabled)
    }

    fun onWidgetLayoutTypeChange(type: Int) {
        WidgetStore.setWidgetLayoutType(getApplication(), type)
        _uiState.value = _uiState.value.copy(widgetLayoutType = type)
        triggerWidgetUpdate()
    }

    fun onShowSecondsChange(enabled: Boolean) {
        WidgetStore.setShowSeconds(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(showSeconds = enabled)
        triggerWidgetUpdate()
    }

    fun onMultilineEnabledChange(enabled: Boolean) {
        WidgetStore.setMultilineEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(multilineEnabled = enabled)
        triggerWidgetUpdate()
    }

    fun onProgressBarEnabledChange(enabled: Boolean) {
        WidgetStore.setProgressBarEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(progressBarEnabled = enabled)
        triggerWidgetUpdate()
    }

    fun onWidgetFlowDirectionChange(direction: Int) {
        WidgetStore.setWidgetFlowDirection(getApplication(), direction)
        _uiState.value = _uiState.value.copy(widgetFlowDirection = direction)
    }

    fun onWidgetAlignmentChange(alignment: Int) {
        WidgetStore.setWidgetAlignment(getApplication(), alignment)
        _uiState.value = _uiState.value.copy(widgetAlignment = alignment)
    }

    fun onWidgetSpacingChange(spacing: Int) {
        WidgetStore.setWidgetSpacing(getApplication(), spacing)
        _uiState.value = _uiState.value.copy(widgetSpacing = spacing)
    }

    fun onWidgetElementOrderChange(order: Int) {
        WidgetStore.setWidgetElementOrder(getApplication(), order)
        _uiState.value = _uiState.value.copy(widgetElementOrder = order)
    }

    fun resetAllData() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            // Clear SharedPreferences
            context.getSharedPreferences(WidgetStore.PREFS_NAME, android.content.Context.MODE_PRIVATE).edit().clear().apply()
            
            // Clear Database
            val db = com.zilagent.app.data.AppDatabase.getDatabase(context)
            db.clearAllTables()
            
            // Reload
            loadSettings()
            triggerWidgetUpdate()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val db = com.zilagent.app.data.AppDatabase.getDatabase(application)
                SettingsViewModel(application, db.holidayDao(), db.quoteDao())
            }
        }
    }
}
