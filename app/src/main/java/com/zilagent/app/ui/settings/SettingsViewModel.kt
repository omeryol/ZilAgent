package com.zilagent.app.ui.settings

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zilagent.app.widget.WidgetStore
import com.zilagent.app.widget.CountdownWidgetElement
import com.zilagent.app.widget.SyllabusActiveHighlightStyle
import com.zilagent.app.widget.SyllabusWidgetElement
import com.zilagent.app.widget.WidgetInfoDensity
import com.zilagent.app.widget.WidgetElementPreferences
import com.zilagent.app.widget.WidgetElementScale
import com.zilagent.app.widget.WidgetStyleFamily
import com.zilagent.app.widget.WidgetTypographyPreset
import com.zilagent.app.widget.WidgetVisualPreset
import com.zilagent.app.data.model.TransferData
import com.zilagent.app.util.SpecialDaysCatalog
import com.zilagent.app.util.QrUtils
import com.zilagent.app.data.entity.Profile
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
    val profileName: String = "Loading...",
    // Widget Customization
    val widgetTextSize: Int = 28,
    val widgetLabelSize: Int = 14,
    val widgetBgOpacity: Int = 90,
    val widgetTextColor: String = "#111111",
    val widgetBarThickness: Int = 8,
    val widgetBgColor: String = "#FFFFFF",
    val widgetCornerRadius: Int = 16,
    val widgetStylePreset: Int = 0,
    // Theme & Sound
    val themeMode: Int = 0, // 0: System, 1: Light, 2: Dark
    val themeColorName: String = "Lavanta",
    val soundEnabled: Boolean = true,
    val autoSilentMode: Boolean = false,
    val widgetLayoutType: Int = 0,
    val countdownWidgetPreset: WidgetVisualPreset = WidgetVisualPreset.Slate,
    val syllabusWidgetPreset: WidgetVisualPreset = WidgetVisualPreset.Paper,
    val countdownWidgetFamily: WidgetStyleFamily = WidgetStyleFamily.Minimal,
    val syllabusWidgetFamily: WidgetStyleFamily = WidgetStyleFamily.Agenda,
    val countdownWidgetDensity: WidgetInfoDensity = WidgetInfoDensity.Balanced,
    val syllabusWidgetDensity: WidgetInfoDensity = WidgetInfoDensity.Balanced,
    val countdownTypographyPreset: WidgetTypographyPreset = WidgetTypographyPreset.Strong,
    val syllabusTypographyPreset: WidgetTypographyPreset = WidgetTypographyPreset.Notebook,
    val globalWidgetTextScale: Int = 100,
    val countdownElementPrefs: Map<CountdownWidgetElement, WidgetElementPreferences> = defaultCountdownElementPrefs(),
    val countdownElementSizes: Map<CountdownWidgetElement, Int> = defaultCountdownElementSizes(),
    val syllabusElementPrefs: Map<SyllabusWidgetElement, WidgetElementPreferences> = defaultSyllabusElementPrefs(),
    val syllabusElementSizes: Map<SyllabusWidgetElement, Int> = defaultSyllabusElementSizes(),
    val workingDaysMask: String = "1111100",
    val holidayList: List<com.zilagent.app.data.entity.Holiday> = emptyList(),
    val specialTemplates: List<com.zilagent.app.util.AnnualSpecialTemplate> = SpecialDaysCatalog.templates,
    val selectedSpecialTemplateIds: Set<String> = emptySet(),
    val specialReminderLeadDays: Int = 7,
    val customSpecialReminders: List<WidgetStore.SpecialReminderEntry> = emptyList(),
    val showSeconds: Boolean = true,
    val multilineEnabled: Boolean = false,
    val progressBarEnabled: Boolean = true,
    val countdownQuoteGreetingEnabled: Boolean = true,
    val countdownQuoteSourceEnabled: Boolean = true,
    val countdownQuoteAlignment: Int = 0,
    val countdownQuoteSourceAlignment: Int = 0,
    val countdownQuoteTextTone: Int = 0,
    val countdownQuoteSourceTone: Int = 2,
    val countdownQuoteTextBold: Boolean = true,
    val countdownQuoteTextItalic: Boolean = false,
    val countdownQuoteSourceBold: Boolean = false,
    val countdownQuoteSourceItalic: Boolean = false,
    val countdownMicroIconsEnabled: Boolean = true,
    // Full Customization
    val widgetFlowDirection: Int = 0, // 0: Vertical, 1: Horizontal
    val widgetAlignment: Int = 1,     // 0: Left, 1: Center, 2: Right
    val widgetSpacing: Int = 8,
    val widgetElementOrder: Int = 0, // 0: Time First, 1: Label First
    val panoramicTimeTextSize: Int = 30,
    val panoramicTitleTextSize: Int = 15,
    val syllabusFlowTextSize: Int = 15,
    val syllabusStatusTextSize: Int = 15,
    val syllabusShowIcons: Boolean = true,
    val syllabusShowClassColors: Boolean = true,
    val syllabusShowBreaks: Boolean = true,
    val syllabusShowTimes: Boolean = true,
    val syllabusColorizeText: Boolean = true,
    val syllabusPaletteScaleEnabled: Boolean = true,
    val syllabusActiveHighlightStyle: SyllabusActiveHighlightStyle = SyllabusActiveHighlightStyle.Soft,
    val dashboardMotionEnabled: Boolean = true,
    val dashboardMotionStrength: Int = 26,
    val dashboardCountdownTextSize: Int = 62,
    val dashboardCardBorderWidth: Int = 2,
    val touchAnimationsEnabled: Boolean = true,
    val touchAnimationIntensity: Int = 60,
    val touchAnimationStyle: Int = 0,
    val appBackgroundMode: Int = 0,
    val appLanguage: String = "tr",
    val quoteList: List<com.zilagent.app.data.entity.Quote> = emptyList()
)

private fun defaultCountdownElementPrefs(): Map<CountdownWidgetElement, WidgetElementPreferences> {
    return CountdownWidgetElement.values().associateWith { element ->
        WidgetElementPreferences(
            visible = element.defaultVisible,
            position = element.defaultPosition,
            scale = element.defaultScale,
        )
    }
}

private fun defaultCountdownElementSizes(): Map<CountdownWidgetElement, Int> {
    return CountdownWidgetElement.values().associateWith { element -> element.defaultSize }
}

private fun defaultSyllabusElementPrefs(): Map<SyllabusWidgetElement, WidgetElementPreferences> {
    return SyllabusWidgetElement.values().associateWith { element ->
        WidgetElementPreferences(
            visible = element.defaultVisible,
            position = element.defaultPosition,
            scale = element.defaultScale,
        )
    }
}

private fun defaultSyllabusElementSizes(): Map<SyllabusWidgetElement, Int> {
    return SyllabusWidgetElement.values().associateWith { element -> element.defaultSize }
}

class SettingsViewModel(
    application: Application,
    private val holidayDao: com.zilagent.app.data.dao.HolidayDao,
    private val quoteDao: com.zilagent.app.data.dao.QuoteDao,
    private val bellDao: com.zilagent.app.data.dao.BellDao,
    private val syllabusDao: com.zilagent.app.data.dao.SyllabusDao
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private fun isEn(): Boolean = WidgetStore.getAppLanguage(getApplication()) == "en"

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
                widgetStylePreset = WidgetStore.getWidgetStylePreset(context),
                themeMode = WidgetStore.getThemeMode(context),
                themeColorName = WidgetStore.getThemeColorName(context),
                soundEnabled = WidgetStore.isSoundEnabled(context),
                autoSilentMode = WidgetStore.isAutoSilentMode(context),
                widgetLayoutType = WidgetStore.getWidgetLayoutType(context),
                countdownWidgetPreset = WidgetStore.getCountdownWidgetPreset(context),
                syllabusWidgetPreset = WidgetStore.getSyllabusWidgetPreset(context),
                countdownWidgetFamily = WidgetStore.getCountdownWidgetFamily(context),
                syllabusWidgetFamily = WidgetStore.getSyllabusWidgetFamily(context),
                countdownWidgetDensity = WidgetStore.getCountdownWidgetDensity(context),
                syllabusWidgetDensity = WidgetStore.getSyllabusWidgetDensity(context),
                countdownTypographyPreset = WidgetStore.getCountdownTypographyPreset(context),
                syllabusTypographyPreset = WidgetStore.getSyllabusTypographyPreset(context),
                globalWidgetTextScale = WidgetStore.getGlobalWidgetTextScale(context),
                countdownElementPrefs = CountdownWidgetElement.values().associateWith { element ->
                    WidgetStore.getCountdownElementPreferences(context, element)
                },
                countdownElementSizes = CountdownWidgetElement.values().associateWith { element ->
                    WidgetStore.getCountdownElementSize(context, element)
                },
                syllabusElementPrefs = SyllabusWidgetElement.values().associateWith { element ->
                    WidgetStore.getSyllabusElementPreferences(context, element)
                },
                syllabusElementSizes = SyllabusWidgetElement.values().associateWith { element ->
                    WidgetStore.getSyllabusElementSize(context, element)
                },
                workingDaysMask = WidgetStore.getWorkingDays(context),
                selectedSpecialTemplateIds = WidgetStore.getSelectedSpecialTemplateIds(context),
                specialReminderLeadDays = WidgetStore.getSpecialDaysLeadDays(context),
                customSpecialReminders = WidgetStore.getCustomSpecialReminders(context),
                showSeconds = WidgetStore.isShowSeconds(context),
                multilineEnabled = WidgetStore.isMultilineEnabled(context),
                progressBarEnabled = WidgetStore.isProgressBarEnabled(context),
                countdownQuoteGreetingEnabled = WidgetStore.isCountdownQuoteGreetingEnabled(context),
                countdownQuoteSourceEnabled = WidgetStore.isCountdownQuoteSourceEnabled(context),
                countdownQuoteAlignment = WidgetStore.getCountdownQuoteAlignment(context),
                countdownQuoteSourceAlignment = WidgetStore.getCountdownQuoteSourceAlignment(context),
                countdownQuoteTextTone = WidgetStore.getCountdownQuoteTextTone(context),
                countdownQuoteSourceTone = WidgetStore.getCountdownQuoteSourceTone(context),
                countdownQuoteTextBold = WidgetStore.isCountdownQuoteTextBold(context),
                countdownQuoteTextItalic = WidgetStore.isCountdownQuoteTextItalic(context),
                countdownQuoteSourceBold = WidgetStore.isCountdownQuoteSourceBold(context),
                countdownQuoteSourceItalic = WidgetStore.isCountdownQuoteSourceItalic(context),
                countdownMicroIconsEnabled = WidgetStore.isCountdownMicroIconsEnabled(context),
                widgetFlowDirection = WidgetStore.getWidgetFlowDirection(context),
                widgetAlignment = WidgetStore.getWidgetAlignment(context),
                widgetSpacing = WidgetStore.getWidgetSpacing(context),
                widgetElementOrder = WidgetStore.getWidgetElementOrder(context),
                panoramicTimeTextSize = WidgetStore.getPanoramicTimeTextSize(context),
                panoramicTitleTextSize = WidgetStore.getPanoramicTitleTextSize(context),
                syllabusFlowTextSize = WidgetStore.getSyllabusFlowTextSize(context),
                syllabusStatusTextSize = WidgetStore.getSyllabusStatusTextSize(context),
                syllabusShowIcons = WidgetStore.isSyllabusShowIcons(context),
                syllabusShowClassColors = WidgetStore.isSyllabusShowClassColors(context),
                syllabusShowBreaks = WidgetStore.isSyllabusShowBreaks(context),
                syllabusShowTimes = WidgetStore.isSyllabusShowTimes(context),
                syllabusColorizeText = WidgetStore.isSyllabusColorizeText(context),
                syllabusPaletteScaleEnabled = WidgetStore.isSyllabusPaletteScaleEnabled(context),
                syllabusActiveHighlightStyle = WidgetStore.getSyllabusActiveHighlightStyle(context),
                dashboardMotionEnabled = WidgetStore.isDashboardMotionEnabled(context),
                dashboardMotionStrength = WidgetStore.getDashboardMotionStrength(context),
                dashboardCountdownTextSize = WidgetStore.getDashboardCountdownTextSize(context),
                dashboardCardBorderWidth = WidgetStore.getDashboardCardBorderWidth(context),
                touchAnimationsEnabled = WidgetStore.isTouchAnimationsEnabled(context),
                touchAnimationIntensity = WidgetStore.getTouchAnimationIntensity(context),
                touchAnimationStyle = WidgetStore.getTouchAnimationStyle(context),
                appBackgroundMode = WidgetStore.getAppBackgroundMode(context),
                appLanguage = WidgetStore.getAppLanguage(context),
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
        triggerWidgetUpdate()
    }

    fun onGlobalWidgetTextScaleChange(scale: Int) {
        val safe = scale.coerceIn(80, 130)
        WidgetStore.setGlobalWidgetTextScale(getApplication(), safe)
        _uiState.value = _uiState.value.copy(globalWidgetTextScale = safe)
        triggerWidgetUpdate()
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

    fun onWidgetStylePresetChange(preset: Int) {
        val safe = preset.coerceIn(0, 3)
        WidgetStore.setWidgetStylePreset(getApplication(), safe)
        _uiState.value = _uiState.value.copy(widgetStylePreset = safe)
        triggerWidgetUpdate()
    }

    fun onThemeModeChange(mode: Int) {
        WidgetStore.setThemeMode(getApplication(), mode)
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun onCountdownWidgetPresetChange(preset: WidgetVisualPreset) {
        WidgetStore.setCountdownWidgetPreset(getApplication(), preset)
        _uiState.value = _uiState.value.copy(countdownWidgetPreset = preset)
        triggerWidgetUpdate()
    }

    fun onSyllabusWidgetPresetChange(preset: WidgetVisualPreset) {
        WidgetStore.setSyllabusWidgetPreset(getApplication(), preset)
        _uiState.value = _uiState.value.copy(syllabusWidgetPreset = preset)
        triggerWidgetUpdate()
    }

    fun onCountdownWidgetFamilyChange(family: WidgetStyleFamily) {
        WidgetStore.setCountdownWidgetFamily(getApplication(), family)
        _uiState.value = _uiState.value.copy(countdownWidgetFamily = family)
        triggerWidgetUpdate()
    }

    fun onSyllabusWidgetFamilyChange(family: WidgetStyleFamily) {
        WidgetStore.setSyllabusWidgetFamily(getApplication(), family)
        _uiState.value = _uiState.value.copy(syllabusWidgetFamily = family)
        triggerWidgetUpdate()
    }

    fun onCountdownWidgetDensityChange(density: WidgetInfoDensity) {
        WidgetStore.setCountdownWidgetDensity(getApplication(), density)
        _uiState.value = _uiState.value.copy(countdownWidgetDensity = density)
        triggerWidgetUpdate()
    }

    fun onSyllabusWidgetDensityChange(density: WidgetInfoDensity) {
        WidgetStore.setSyllabusWidgetDensity(getApplication(), density)
        _uiState.value = _uiState.value.copy(syllabusWidgetDensity = density)
        triggerWidgetUpdate()
    }

    fun onCountdownTypographyPresetChange(preset: WidgetTypographyPreset) {
        WidgetStore.setCountdownTypographyPreset(getApplication(), preset)
        _uiState.value = _uiState.value.copy(countdownTypographyPreset = preset)
        triggerWidgetUpdate()
    }

    fun onSyllabusTypographyPresetChange(preset: WidgetTypographyPreset) {
        WidgetStore.setSyllabusTypographyPreset(getApplication(), preset)
        _uiState.value = _uiState.value.copy(syllabusTypographyPreset = preset)
        triggerWidgetUpdate()
    }

    fun onCountdownElementVisibilityChange(element: CountdownWidgetElement, visible: Boolean) {
        mutateCountdownElement(element) { it.copy(visible = visible) }
    }

    fun onCountdownElementPositionChange(element: CountdownWidgetElement, position: Int) {
        mutateCountdownElement(element) { it.copy(position = position) }
    }

    fun onCountdownElementScaleChange(element: CountdownWidgetElement, scale: WidgetElementScale) {
        mutateCountdownElement(element) { it.copy(scale = scale) }
    }

    fun onCountdownElementSizeChange(element: CountdownWidgetElement, size: Int) {
        val safe = size.coerceIn(0, 50)
        WidgetStore.setCountdownElementSize(getApplication(), element, safe)
        _uiState.value = _uiState.value.copy(
            countdownElementSizes = _uiState.value.countdownElementSizes.toMutableMap().apply {
                put(element, safe)
            },
        )
        triggerWidgetUpdate()
    }

    fun resetCountdownElementSizes() {
        WidgetStore.resetCountdownElementSizes(getApplication())
        _uiState.value = _uiState.value.copy(
            countdownElementSizes = defaultCountdownElementSizes(),
        )
        triggerWidgetUpdate()
    }

    fun onSyllabusElementVisibilityChange(element: SyllabusWidgetElement, visible: Boolean) {
        mutateSyllabusElement(element) { it.copy(visible = visible) }
    }

    fun onSyllabusElementPositionChange(element: SyllabusWidgetElement, position: Int) {
        mutateSyllabusElement(element) { it.copy(position = position) }
    }

    fun onSyllabusElementScaleChange(element: SyllabusWidgetElement, scale: WidgetElementScale) {
        mutateSyllabusElement(element) { it.copy(scale = scale) }
    }

    fun onSyllabusElementSizeChange(element: SyllabusWidgetElement, size: Int) {
        val safe = size.coerceIn(0, 50)
        WidgetStore.setSyllabusElementSize(getApplication(), element, safe)
        _uiState.value = _uiState.value.copy(
            syllabusElementSizes = _uiState.value.syllabusElementSizes.toMutableMap().apply {
                put(element, safe)
            },
            syllabusFlowTextSize = if (element == SyllabusWidgetElement.Flow) safe else _uiState.value.syllabusFlowTextSize,
            syllabusStatusTextSize = if (element == SyllabusWidgetElement.Status) safe else _uiState.value.syllabusStatusTextSize,
        )
        triggerWidgetUpdate()
    }

    fun resetSyllabusElementSizes() {
        WidgetStore.resetSyllabusElementSizes(getApplication())
        _uiState.value = _uiState.value.copy(
            syllabusElementSizes = defaultSyllabusElementSizes(),
            syllabusFlowTextSize = SyllabusWidgetElement.Flow.defaultSize,
            syllabusStatusTextSize = SyllabusWidgetElement.Status.defaultSize,
        )
        triggerWidgetUpdate()
    }

    fun onSoundEnabledChange(enabled: Boolean) {
        WidgetStore.setSoundEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(soundEnabled = enabled)
    }

    private fun triggerWidgetUpdate() {
        val context = getApplication<Application>()
        com.zilagent.app.widget.PanoramicCountdownWidget.updateAll(context)
        com.zilagent.app.widget.SyllabusWidget.updateAll(context)
    }

    private fun mutateCountdownElement(
        element: CountdownWidgetElement,
        transform: (WidgetElementPreferences) -> WidgetElementPreferences,
    ) {
        val current = _uiState.value.countdownElementPrefs[element]
            ?: WidgetElementPreferences(element.defaultVisible, element.defaultPosition, element.defaultScale)
        val updated = transform(current)
        WidgetStore.setCountdownElementPreferences(getApplication(), element, updated)
        _uiState.value = _uiState.value.copy(
            countdownElementPrefs = _uiState.value.countdownElementPrefs.toMutableMap().apply {
                put(element, updated)
            },
        )
        triggerWidgetUpdate()
    }

    private fun mutateSyllabusElement(
        element: SyllabusWidgetElement,
        transform: (WidgetElementPreferences) -> WidgetElementPreferences,
    ) {
        val current = _uiState.value.syllabusElementPrefs[element]
            ?: WidgetElementPreferences(element.defaultVisible, element.defaultPosition, element.defaultScale)
        val updated = transform(current)
        WidgetStore.setSyllabusElementPreferences(getApplication(), element, updated)
        _uiState.value = _uiState.value.copy(
            syllabusElementPrefs = _uiState.value.syllabusElementPrefs.toMutableMap().apply {
                put(element, updated)
            },
        )
        triggerWidgetUpdate()
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

    fun onSpecialTemplateToggled(templateId: String, selected: Boolean) {
        WidgetStore.setSelectedSpecialTemplate(getApplication(), templateId, selected)
        _uiState.value = _uiState.value.copy(
            selectedSpecialTemplateIds = WidgetStore.getSelectedSpecialTemplateIds(getApplication()),
        )
        triggerWidgetUpdate()
    }

    fun onSpecialReminderLeadDaysChange(days: Int) {
        WidgetStore.setSpecialDaysLeadDays(getApplication(), days)
        _uiState.value = _uiState.value.copy(specialReminderLeadDays = days.coerceIn(0, 30))
        triggerWidgetUpdate()
    }

    fun addCustomSpecialReminder(name: String, startDate: String, endDate: String) {
        WidgetStore.addCustomSpecialReminder(getApplication(), name, startDate, endDate)
        _uiState.value = _uiState.value.copy(
            customSpecialReminders = WidgetStore.getCustomSpecialReminders(getApplication()),
        )
        triggerWidgetUpdate()
    }

    fun removeCustomSpecialReminder(id: String) {
        WidgetStore.removeCustomSpecialReminder(getApplication(), id)
        _uiState.value = _uiState.value.copy(
            customSpecialReminders = WidgetStore.getCustomSpecialReminders(getApplication()),
        )
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

    fun addQuote(content: String, source: String) {
        viewModelScope.launch {
            quoteDao.insertQuote(
                com.zilagent.app.data.entity.Quote(
                    content = com.zilagent.app.util.QuoteConstants.encodeStoredQuote(content, source),
                    isSystem = false,
                ),
            )
            triggerWidgetUpdate()
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

    fun onCountdownQuoteGreetingEnabledChange(enabled: Boolean) {
        WidgetStore.setCountdownQuoteGreetingEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(countdownQuoteGreetingEnabled = enabled)
        triggerWidgetUpdate()
    }

    fun onCountdownQuoteSourceEnabledChange(enabled: Boolean) {
        WidgetStore.setCountdownQuoteSourceEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(countdownQuoteSourceEnabled = enabled)
        triggerWidgetUpdate()
    }

    fun onCountdownQuoteAlignmentChange(alignment: Int) {
        WidgetStore.setCountdownQuoteAlignment(getApplication(), alignment)
        _uiState.value = _uiState.value.copy(countdownQuoteAlignment = alignment)
        triggerWidgetUpdate()
    }

    fun onCountdownQuoteSourceAlignmentChange(alignment: Int) {
        WidgetStore.setCountdownQuoteSourceAlignment(getApplication(), alignment)
        _uiState.value = _uiState.value.copy(countdownQuoteSourceAlignment = alignment)
        triggerWidgetUpdate()
    }

    fun onCountdownQuoteTextToneChange(tone: Int) {
        WidgetStore.setCountdownQuoteTextTone(getApplication(), tone)
        _uiState.value = _uiState.value.copy(countdownQuoteTextTone = tone)
        triggerWidgetUpdate()
    }

    fun onCountdownQuoteSourceToneChange(tone: Int) {
        WidgetStore.setCountdownQuoteSourceTone(getApplication(), tone)
        _uiState.value = _uiState.value.copy(countdownQuoteSourceTone = tone)
        triggerWidgetUpdate()
    }

    fun onCountdownQuoteTextBoldChange(enabled: Boolean) {
        WidgetStore.setCountdownQuoteTextBold(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(countdownQuoteTextBold = enabled)
        triggerWidgetUpdate()
    }

    fun onCountdownQuoteTextItalicChange(enabled: Boolean) {
        WidgetStore.setCountdownQuoteTextItalic(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(countdownQuoteTextItalic = enabled)
        triggerWidgetUpdate()
    }

    fun onCountdownQuoteSourceBoldChange(enabled: Boolean) {
        WidgetStore.setCountdownQuoteSourceBold(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(countdownQuoteSourceBold = enabled)
        triggerWidgetUpdate()
    }

    fun onCountdownQuoteSourceItalicChange(enabled: Boolean) {
        WidgetStore.setCountdownQuoteSourceItalic(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(countdownQuoteSourceItalic = enabled)
        triggerWidgetUpdate()
    }

    fun onCountdownMicroIconsEnabledChange(enabled: Boolean) {
        WidgetStore.setCountdownMicroIconsEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(countdownMicroIconsEnabled = enabled)
        triggerWidgetUpdate()
    }

    fun onWidgetFlowDirectionChange(direction: Int) {
        WidgetStore.setWidgetFlowDirection(getApplication(), direction)
        _uiState.value = _uiState.value.copy(widgetFlowDirection = direction)
        triggerWidgetUpdate()
    }

    fun onWidgetAlignmentChange(alignment: Int) {
        WidgetStore.setWidgetAlignment(getApplication(), alignment)
        _uiState.value = _uiState.value.copy(widgetAlignment = alignment)
        triggerWidgetUpdate()
    }

    fun onWidgetSpacingChange(spacing: Int) {
        WidgetStore.setWidgetSpacing(getApplication(), spacing)
        _uiState.value = _uiState.value.copy(widgetSpacing = spacing)
        triggerWidgetUpdate()
    }

    fun onWidgetElementOrderChange(order: Int) {
        WidgetStore.setWidgetElementOrder(getApplication(), order)
        _uiState.value = _uiState.value.copy(widgetElementOrder = order)
        triggerWidgetUpdate()
    }

    fun onPanoramicTimeTextSizeChange(size: Int) {
        WidgetStore.setPanoramicTimeTextSize(getApplication(), size)
        _uiState.value = _uiState.value.copy(panoramicTimeTextSize = size)
        triggerWidgetUpdate()
    }

    fun onPanoramicTitleTextSizeChange(size: Int) {
        WidgetStore.setPanoramicTitleTextSize(getApplication(), size)
        _uiState.value = _uiState.value.copy(panoramicTitleTextSize = size)
        triggerWidgetUpdate()
    }

    fun onSyllabusFlowTextSizeChange(size: Int) {
        WidgetStore.setSyllabusFlowTextSize(getApplication(), size)
        WidgetStore.setSyllabusElementSize(getApplication(), SyllabusWidgetElement.Flow, size)
        _uiState.value = _uiState.value.copy(
            syllabusFlowTextSize = size,
            syllabusElementSizes = _uiState.value.syllabusElementSizes.toMutableMap().apply {
                put(SyllabusWidgetElement.Flow, size.coerceIn(0, 50))
            },
        )
        triggerWidgetUpdate()
    }

    fun onSyllabusStatusTextSizeChange(size: Int) {
        WidgetStore.setSyllabusStatusTextSize(getApplication(), size)
        WidgetStore.setSyllabusElementSize(getApplication(), SyllabusWidgetElement.Status, size)
        _uiState.value = _uiState.value.copy(
            syllabusStatusTextSize = size,
            syllabusElementSizes = _uiState.value.syllabusElementSizes.toMutableMap().apply {
                put(SyllabusWidgetElement.Status, size.coerceIn(0, 50))
            },
        )
        triggerWidgetUpdate()
    }

    fun onSyllabusShowIconsChange(enabled: Boolean) {
        WidgetStore.setSyllabusShowIcons(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(syllabusShowIcons = enabled)
        triggerWidgetUpdate()
    }

    fun onSyllabusShowClassColorsChange(enabled: Boolean) {
        WidgetStore.setSyllabusShowClassColors(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(syllabusShowClassColors = enabled)
        triggerWidgetUpdate()
    }

    fun onSyllabusShowBreaksChange(enabled: Boolean) {
        WidgetStore.setSyllabusShowBreaks(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(syllabusShowBreaks = enabled)
        triggerWidgetUpdate()
    }

    fun onSyllabusShowTimesChange(enabled: Boolean) {
        WidgetStore.setSyllabusShowTimes(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(syllabusShowTimes = enabled)
        triggerWidgetUpdate()
    }

    fun onSyllabusColorizeTextChange(enabled: Boolean) {
        WidgetStore.setSyllabusColorizeText(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(syllabusColorizeText = enabled)
        triggerWidgetUpdate()
    }

    fun onSyllabusPaletteScaleEnabledChange(enabled: Boolean) {
        WidgetStore.setSyllabusPaletteScaleEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(syllabusPaletteScaleEnabled = enabled)
        triggerWidgetUpdate()
    }

    fun onSyllabusActiveHighlightStyleChange(style: SyllabusActiveHighlightStyle) {
        WidgetStore.setSyllabusActiveHighlightStyle(getApplication(), style)
        _uiState.value = _uiState.value.copy(syllabusActiveHighlightStyle = style)
        triggerWidgetUpdate()
    }

    fun onDashboardMotionEnabledChange(enabled: Boolean) {
        WidgetStore.setDashboardMotionEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(dashboardMotionEnabled = enabled)
    }

    fun onDashboardMotionStrengthChange(strength: Int) {
        WidgetStore.setDashboardMotionStrength(getApplication(), strength)
        _uiState.value = _uiState.value.copy(dashboardMotionStrength = strength.coerceIn(5, 60))
    }

    fun onDashboardCountdownTextSizeChange(size: Int) {
        WidgetStore.setDashboardCountdownTextSize(getApplication(), size)
        _uiState.value = _uiState.value.copy(dashboardCountdownTextSize = size.coerceIn(40, 120))
    }

    fun onDashboardCardBorderWidthChange(width: Int) {
        WidgetStore.setDashboardCardBorderWidth(getApplication(), width)
        _uiState.value = _uiState.value.copy(dashboardCardBorderWidth = width.coerceIn(1, 8))
    }

    fun onTouchAnimationsEnabledChange(enabled: Boolean) {
        WidgetStore.setTouchAnimationsEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(touchAnimationsEnabled = enabled)
    }

    fun onTouchAnimationIntensityChange(intensity: Int) {
        val safe = intensity.coerceIn(10, 100)
        WidgetStore.setTouchAnimationIntensity(getApplication(), safe)
        _uiState.value = _uiState.value.copy(touchAnimationIntensity = safe)
    }

    fun onTouchAnimationStyleChange(style: Int) {
        val safe = style.coerceIn(0, 2)
        WidgetStore.setTouchAnimationStyle(getApplication(), safe)
        _uiState.value = _uiState.value.copy(touchAnimationStyle = safe)
    }

    fun onAppBackgroundModeChange(mode: Int) {
        val safe = mode.coerceIn(0, 4)
        WidgetStore.setAppBackgroundMode(getApplication(), safe)
        _uiState.value = _uiState.value.copy(appBackgroundMode = safe)
    }

    fun onAppLanguageChange(languageCode: String) {
        val safe = if (languageCode.lowercase() == "en") "en" else "tr"
        WidgetStore.setAppLanguage(getApplication(), safe)
        _uiState.value = _uiState.value.copy(appLanguage = safe)
        triggerWidgetUpdate()
        viewModelScope.launch {
            reseedSystemSubjectsForLanguage(safe)
            reseedSystemQuotesForLanguage(safe)
            triggerWidgetUpdate()
        }
    }

    private suspend fun reseedSystemSubjectsForLanguage(languageCode: String) {
        val samples = if (languageCode == "en") {
            com.zilagent.app.util.SubjectConstants.MIDDLE_SCHOOL_SUBJECTS_EN
        } else {
            com.zilagent.app.util.SubjectConstants.MIDDLE_SCHOOL_SUBJECTS_TR
        }
        val existingSystemSubjects = syllabusDao.getAllSubjectsSync()
            .filter { it.isSystem }
            .sortedBy { it.id }

        existingSystemSubjects.zip(samples).forEach { (subject, localizedName) ->
            if (subject.name != localizedName) {
                syllabusDao.insertSubject(subject.copy(name = localizedName, isSystem = true))
            }
        }

        if (samples.size > existingSystemSubjects.size) {
            samples.drop(existingSystemSubjects.size).forEach { localizedName ->
                syllabusDao.insertSubject(com.zilagent.app.data.entity.SchoolSubject(name = localizedName, isSystem = true))
            }
        }

        repairDanglingSystemSubjectLinks()
    }

    private suspend fun repairDanglingSystemSubjectLinks() {
        val allSubjects = syllabusDao.getAllSubjectsSync()
        val currentSystemSubjects = allSubjects
            .filter { it.isSystem }
            .sortedBy { it.id }
        if (currentSystemSubjects.isEmpty()) return

        val existingIds = allSubjects.map { it.id }.toSet()
        val missingBlocks = buildMissingSubjectBlocks(existingIds, currentSystemSubjects.size)
        if (missingBlocks.isEmpty()) return

        bellDao.getAllProfilesSync().forEach { profile ->
            syllabusDao.getAllSyllabusSync(profile.id).forEach { entry ->
                val oldSubjectId = entry.subjectId ?: return@forEach
                if (oldSubjectId in existingIds) return@forEach
                val replacementId = remapDanglingSystemSubjectId(oldSubjectId, missingBlocks, currentSystemSubjects)
                    ?: return@forEach
                syllabusDao.insertSyllabusEntry(entry.copy(subjectId = replacementId))
            }
        }
    }

    private fun buildMissingSubjectBlocks(existingIds: Set<Long>, expectedBlockSize: Int): List<LongRange> {
        if (expectedBlockSize <= 0) return emptyList()
        val maxKnownId = (existingIds.maxOrNull() ?: return emptyList()).toInt()
        val blocks = mutableListOf<LongRange>()
        var blockStart: Int? = null

        for (id in 1..maxKnownId) {
            if (id.toLong() !in existingIds) {
                if (blockStart == null) blockStart = id
            } else if (blockStart != null) {
                val blockEnd = id - 1
                if (blockEnd - blockStart + 1 >= expectedBlockSize) {
                    blocks += blockStart.toLong()..blockEnd.toLong()
                }
                blockStart = null
            }
        }

        if (blockStart != null && maxKnownId - blockStart + 1 >= expectedBlockSize) {
            blocks += blockStart.toLong()..maxKnownId.toLong()
        }

        return blocks
    }

    private fun remapDanglingSystemSubjectId(
        oldSubjectId: Long,
        missingBlocks: List<LongRange>,
        currentSystemSubjects: List<com.zilagent.app.data.entity.SchoolSubject>,
    ): Long? {
        val matchingBlock = missingBlocks.firstOrNull { block ->
            oldSubjectId in block && (oldSubjectId - block.first).toInt() in currentSystemSubjects.indices
        } ?: return null

        val index = (oldSubjectId - matchingBlock.first).toInt()
        return currentSystemSubjects.getOrNull(index)?.id
    }

    private suspend fun reseedSystemQuotesForLanguage(languageCode: String) {
        quoteDao.deleteAllSystemQuotes()
        val quotes = com.zilagent.app.util.QuoteConstants.systemQuotes(languageCode)
        quoteDao.insertQuotes(quotes.map { com.zilagent.app.data.entity.Quote(content = it, isSystem = true) })
    }

    fun createBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            com.zilagent.app.util.BackupManager.createBackup(context, uri)
        }
    }

    fun restoreBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val success = com.zilagent.app.util.BackupManager.restoreBackup(context, uri)
            if (success) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    loadSettings()
                    triggerWidgetUpdate()
                }
            }
        }
    }

    fun exportBackup(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val json = com.zilagent.app.util.BackupManager.createBackup(getApplication())
            onComplete(json)
        }
    }

    fun importBackup(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = com.zilagent.app.util.BackupManager.restoreBackup(getApplication(), json)
            if (success) {
                loadSettings()
                triggerWidgetUpdate()
            }
            onComplete(success)
        }
    }

    fun resetAllData() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val context = getApplication<Application>()
            // Clear SharedPreferences
            context.getSharedPreferences(WidgetStore.PREFS_NAME, android.content.Context.MODE_PRIVATE).edit().clear().apply()
            
            // Clear Database
            val db = com.zilagent.app.data.AppDatabase.getDatabase(context)
            db.clearAllTables()
            
            // Reload on UI thread
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                loadSettings()
                triggerWidgetUpdate()
            }
        }
    }

    fun exportProfileToQr(onResult: (Bitmap?) -> Unit) {
        viewModelScope.launch {
            val profile = bellDao.getActiveProfileSync()
            if (profile != null) {
                val schedules = bellDao.getAllSchedulesForProfileSync(profile.id)
                val syllabus = syllabusDao.getAllSyllabusSync(profile.id)
                // Assuming Profile entity has 'name' field
                val data = TransferData(profile.name, schedules, syllabus)
                // Serialize in IO
                val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val compressed = QrUtils.compressAndSerialize(data)
                        QrUtils.generateQrBitmap(compressed)
                    } catch (_: Exception) {
                        null
                    }
                }
                onResult(bitmap)
            } else {
                onResult(null)
            }
        }
    }

    fun importProfileFromQr(scannedData: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val data = QrUtils.decompressAndDeserialize(scannedData, TransferData::class.java)
                if (data != null && isValidTransferData(data)) {
                    // Create new profile
                    val importedSuffix = if (isEn()) "(Imported)" else "(İçe Aktarılan)"
                    val newId = bellDao.insertProfile(Profile(name = "${data.profileName} $importedSuffix"))
                    
                    // Process schedules
                    data.bellSchedules.forEach { 
                        bellDao.insertSchedule(it.copy(id = 0, profileId = newId)) 
                    }
                    
                    // Process syllabus
                    data.syllabusEntries.forEach { 
                        syllabusDao.insertSyllabusEntry(it.copy(profileId = newId)) 
                    }
                    true
                } else {
                    false
                }
            }
            onResult(success)
        }
    }

    private fun isValidTransferData(data: TransferData): Boolean {
        if (data.profileName.isBlank() || data.profileName.length > 80) return false
        if (data.bellSchedules.size > 500) return false
        if (data.syllabusEntries.size > 500) return false

        val schedulesOk = data.bellSchedules.all { s ->
            s.dayOfWeek in 0..7 &&
                s.startTime in 0..(24 * 60 - 1) &&
                s.endTime in 1..(24 * 60) &&
                s.startTime < s.endTime &&
                s.orderIndex in 0..200 &&
                s.name.length <= 80
        }
        if (!schedulesOk) return false

        return data.syllabusEntries.all { e ->
            e.dayOfWeek in 1..7 &&
                e.lessonOrder in 1..200
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val db = com.zilagent.app.data.AppDatabase.getDatabase(application)
                SettingsViewModel(application, db.holidayDao(), db.quoteDao(), db.bellDao(), db.syllabusDao())
            }
        }
    }
}

