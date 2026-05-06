package com.zilagent.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.ui.components.*
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.ui.theme.ThemePalette
import com.zilagent.app.widget.CountdownWidgetElement
import com.zilagent.app.widget.SyllabusActiveHighlightStyle
import com.zilagent.app.widget.SyllabusWidgetElement
import com.zilagent.app.widget.WidgetAppearance
import com.zilagent.app.widget.WidgetInfoDensity
import com.zilagent.app.widget.WidgetElementScale
import com.zilagent.app.widget.WidgetElementPreferences
import com.zilagent.app.widget.WidgetStyleFamily
import com.zilagent.app.widget.WidgetTypographyPreset
import com.zilagent.app.widget.WidgetVisualPreset
import com.zilagent.app.widget.WidgetStore
import kotlinx.coroutines.launch

data class HolidayTemplate(
    val nameTr: String,
    val nameEn: String,
    val multiDay: Boolean,
    val suggestedRangeTr: String,
    val suggestedRangeEn: String,
) {
    fun name(isEn: Boolean): String = if (isEn) nameEn else nameTr
    fun suggestedRange(isEn: Boolean): String = if (isEn) suggestedRangeEn else suggestedRangeTr
}

val TURKIYE_HOLIDAY_TEMPLATES = listOf(
    HolidayTemplate("Yılbaşı", "New Year's Day", multiDay = false, suggestedRangeTr = "Sabit: 1 Ocak", suggestedRangeEn = "Fixed: Jan 1"),
    HolidayTemplate("23 Nisan Ulusal Egemenlik ve Çocuk Bayramı", "National Sovereignty and Children's Day", multiDay = false, suggestedRangeTr = "Sabit: 23 Nisan", suggestedRangeEn = "Fixed: Apr 23"),
    HolidayTemplate("Emek ve Dayanışma Günü", "Labour and Solidarity Day", multiDay = false, suggestedRangeTr = "Sabit: 1 Mayıs", suggestedRangeEn = "Fixed: May 1"),
    HolidayTemplate("19 Mayıs Atatürk'ü Anma, Gençlik ve Spor Bayramı", "Commemoration of Ataturk, Youth and Sports Day", multiDay = false, suggestedRangeTr = "Sabit: 19 Mayıs", suggestedRangeEn = "Fixed: May 19"),
    HolidayTemplate("Ramazan Bayramı", "Ramadan Feast (Eid al-Fitr)", multiDay = true, suggestedRangeTr = "Yıllık değişir: arife + 3 gün", suggestedRangeEn = "Changes yearly: eve + 3 days"),
    HolidayTemplate("Kurban Bayramı", "Feast of Sacrifice (Eid al-Adha)", multiDay = true, suggestedRangeTr = "Yıllık değişir: arife + 4 gün", suggestedRangeEn = "Changes yearly: eve + 4 days"),
    HolidayTemplate("15 Temmuz Demokrasi ve Milli Birlik Günü", "Democracy and National Unity Day", multiDay = false, suggestedRangeTr = "Sabit: 15 Temmuz", suggestedRangeEn = "Fixed: Jul 15"),
    HolidayTemplate("30 Ağustos Zafer Bayramı", "Victory Day", multiDay = false, suggestedRangeTr = "Sabit: 30 Ağustos", suggestedRangeEn = "Fixed: Aug 30"),
    HolidayTemplate("Cumhuriyet Bayramı", "Republic Day", multiDay = true, suggestedRangeTr = "28 Ekim (yarım) + 29 Ekim", suggestedRangeEn = "Oct 28 (half day) + Oct 29"),
    HolidayTemplate("1. Ara Tatil", "First Midterm Break", multiDay = true, suggestedRangeTr = "MEB takvimine göre", suggestedRangeEn = "Per MEB calendar"),
    HolidayTemplate("Yarıyıl (Sömestr) Tatili", "Semester Break", multiDay = true, suggestedRangeTr = "MEB takvimine göre", suggestedRangeEn = "Per MEB calendar"),
    HolidayTemplate("2. Ara Tatil", "Second Midterm Break", multiDay = true, suggestedRangeTr = "MEB takvimine göre", suggestedRangeEn = "Per MEB calendar"),
    HolidayTemplate("Yaz Tatili", "Summer Break", multiDay = true, suggestedRangeTr = "MEB takvimine göre", suggestedRangeEn = "Per MEB calendar"),
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEn = uiState.appLanguage == "en"
    fun t(tr: String, en: String): String = if (isEn) en else tr
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    
    var showHolidayDialog by remember { mutableStateOf(false) }
    var showQuoteDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var showExamGuideDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showQrShareDialog by remember { mutableStateOf(false) }
    var showQrScanScreen by remember { mutableStateOf(false) }
    var showSpecialReminderDialog by remember { mutableStateOf(false) }
    var selectedHolidayTemplate by remember { mutableStateOf<HolidayTemplate?>(null) }

    val permissionLauncher = androidx.activity.result.contract.ActivityResultContracts.RequestPermission().let { contract ->
        androidx.activity.compose.rememberLauncherForActivityResult(contract) { isGranted ->
            if (isGranted) viewModel.onNotificationsEnabledChange(true)
        }
    }

    val exportLauncher = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json").let { contract ->
        androidx.activity.compose.rememberLauncherForActivityResult(contract) { uri ->
            uri?.let { viewModel.createBackup(context, it) }
        }
    }

    val importLauncher = androidx.activity.result.contract.ActivityResultContracts.OpenDocument().let { contract ->
        androidx.activity.compose.rememberLauncherForActivityResult(contract) { uri ->
            uri?.let { viewModel.restoreBackup(context, it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("Ayarlar", "Settings"), fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        GradientIcon(Icons.Default.ArrowBack, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = onSurface
                ),
                actions = {
                    IconButton(onClick = { showManualDialog = true }) {
                        Icon(Icons.Default.HelpOutline, t("Kılavuz", "Guide"), tint = onSurface)
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        ZilAgentBackground(modifier = Modifier.padding(paddingValues)) {
            Column {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isLightTheme) 0.45f else 0.62f),
                    contentColor = onSurface,
                    divider = {},
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .height(3.dp)
                                .padding(horizontal = 24.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text(t("Tema", "Theme"), fontWeight = if(pagerState.currentPage==0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text(t("Widgetlar", "Widgets"), fontWeight = if(pagerState.currentPage==1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 2,
                        onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                        text = { Text(t("Sistem", "System"), fontWeight = if(pagerState.currentPage==2) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 3,
                        onClick = { scope.launch { pagerState.animateScrollToPage(3) } },
                        text = { Text(t("Plan", "Plan"), fontWeight = if(pagerState.currentPage==3) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondBoundsPageCount = 2,
                    verticalAlignment = Alignment.Top
                ) { page ->
                    if (page == 0) {
                        ThemeStudioTab(uiState, viewModel)
                    } else if (page == 1) {
                        WidgetStudioTab(uiState, viewModel)
                    } else if (page == 2) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)) {
                            SystemTab(uiState, viewModel, context, onNavigateToProfiles, permissionLauncher)
                            Spacer(Modifier.height(80.dp))
                        }
                    } else {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)) {
                            PlanningTab(uiState, viewModel, context, 
                                onShowHoliday = { template ->
                                    selectedHolidayTemplate = template
                                    showHolidayDialog = true
                                },
                                onShowQuote = { showQuoteDialog = true },
                                onShowExamGuide = { showExamGuideDialog = true },
                                onShowDisclaimer = { showDisclaimerDialog = true },
                                onShowReset = { showResetDialog = true },
                                onShowBackup = { }, 
                                onShowImport = { },
                                onShowQrShare = { showQrShareDialog = true },
                                onShowQrScan = { showQrScanScreen = true },
                                onShowSpecialReminderAdd = { showSpecialReminderDialog = true },
                                exportLauncher = exportLauncher,
                                importLauncher = importLauncher
                            )
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    if (showHolidayDialog) {
        HolidayAddDialog(
            onDismiss = {
                showHolidayDialog = false
                selectedHolidayTemplate = null
            },
            onConfirm = viewModel::addHoliday,
            isEn = isEn,
            preset = selectedHolidayTemplate,
        )
    }
    if (showSpecialReminderDialog) {
        SpecialReminderAddDialog(
            onDismiss = { showSpecialReminderDialog = false },
            onConfirm = { name, start, end ->
                viewModel.addCustomSpecialReminder(name, start, end)
                showSpecialReminderDialog = false
            },
            isEn = isEn,
        )
    }
    if (showQuoteDialog) QuoteManageDialog(uiState.quoteList, onDismiss = { showQuoteDialog = false }, onAdd = viewModel::addQuote, onDelete = viewModel::deleteQuote, isEn = isEn)
    if (showManualDialog) UserManualDialog(onDismiss = { showManualDialog = false })
    if (showExamGuideDialog) ExamModeGuideDialog(onDismiss = { showExamGuideDialog = false }, isEn = isEn)
    if (showDisclaimerDialog) DisclaimerDialog(onDismiss = { showDisclaimerDialog = false }, isEn = isEn)
    if (showResetDialog) ResetConfirmDialog(onDismiss = { showResetDialog = false }, onConfirm = viewModel::resetAllData, onBackupNow = { exportLauncher.launch("zilagent_backup_emergency.json") }, isEn = isEn)
    
    if (showQrShareDialog) {
        QrShareDialog(onDismiss = { showQrShareDialog = false }, viewModel = viewModel)
    }

    if (showQrScanScreen) {
        QrScanScreen(
            onDismiss = { showQrScanScreen = false },
            onCodeScanned = { code ->
                showQrScanScreen = false // Close camera
                // Process code
                viewModel.importProfileFromQr(code) { success ->
                   // Toast or Snackbar?
                   // For now just close. UI State sync should happen viewmodel side.
                   // Maybe show a result dialog?
                   // We don't have a generic result dialog handy here but can log or just assume it worked.
                   // Or show a toast. SettingsScreen has no scaffold snackbar host.
                   // I'll leave it as is, ViewModel reloadSettings triggers updates.
                }
            }
        )
    }
}

@Composable
fun ThemeStudioTab(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val isEn = uiState.appLanguage == "en"
    fun t(tr: String, en: String): String = if (isEn) en else tr
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondary = onSurface.copy(alpha = 0.68f)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(t("Sistem", "System"), t("Açık", "Light"), t("Koyu", "Dark")).forEachIndexed { index, label ->
                        FilterChip(
                            selected = uiState.themeMode == index,
                            onClick = { viewModel.onThemeModeChange(index) },
                            label = { Text(label, fontSize = 11.sp) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = uiState.appLanguage == "tr",
                        onClick = { viewModel.onAppLanguageChange("tr") },
                        label = { Text("TR", fontSize = 11.sp) },
                    )
                    FilterChip(
                        selected = uiState.appLanguage == "en",
                        onClick = { viewModel.onAppLanguageChange("en") },
                        label = { Text("EN", fontSize = 11.sp) },
                    )
                }
            }
        }
        SettingsSectionHeader(t("Tema Kimligi", "Theme Identity"), Icons.Default.Palette, IconGradients.Purple)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                val themes = ThemePalette.getAllThemeNames()
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    items(themes) { theme ->
                        val isSelected = theme == uiState.themeColorName
                        val palette = ThemePalette.getPalette(theme)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = palette.first)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Brush.linearGradient(listOf(palette.first, palette.second)))
                                    .premiumClickable { viewModel.onThemeColorChange(theme) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = onSurface, modifier = Modifier.size(36.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                localizedThemeName(theme, isEn),
                                fontSize = 13.sp,
                                color = if (isSelected) onSurface else secondary,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(t("Sistem", "System"), t("Açık", "Light"), t("Koyu", "Dark")).forEachIndexed { index, label ->
                        FilterChip(
                            selected = uiState.themeMode == index,
                            onClick = { viewModel.onThemeModeChange(index) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SettingsSectionHeader(t("Yüzey ve Dil", "Surface & Language"), Icons.Default.Tune, IconGradients.Blue)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(t("Dil", "Language"), color = onSurface, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = uiState.appLanguage == "tr", onClick = { viewModel.onAppLanguageChange("tr") }, label = { Text("TR") })
                    FilterChip(selected = uiState.appLanguage == "en", onClick = { viewModel.onAppLanguageChange("en") }, label = { Text("EN") })
                }
                Text(t("Arka plan stili", "Background style"), color = onSurface, fontWeight = FontWeight.SemiBold)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        t("Dinamik", "Dynamic"),
                        t("Sakin", "Soft"),
                        t("Mesh", "Mesh"),
                        t("Uzay", "Space"),
                        t("Minimal", "Minimal"),
                    ).forEachIndexed { index, label ->
                        FilterChip(
                            selected = uiState.appBackgroundMode == index,
                            onClick = { viewModel.onAppBackgroundModeChange(index) },
                            label = { Text(label) },
                        )
                    }
                }
                Divider()
                SettingsToggleRow(
                    icon = Icons.Default.Animation,
                    gradient = IconGradients.Sunset,
                    title = t("Hareketli dashboard", "Animated dashboard"),
                    subtitle = t("Ana ekrandaki hareket ve derinlik efektlerini koru", "Keep motion and depth effects on the dashboard"),
                    checked = uiState.dashboardMotionEnabled,
                    onCheckedChange = viewModel::onDashboardMotionEnabledChange,
                )
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun WidgetStudioTab(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val isEn = uiState.appLanguage == "en"
    fun t(tr: String, en: String): String = if (isEn) en else tr
    var widgetType by remember { mutableIntStateOf(0) }
    var quickMode by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettingsSectionHeader(t("Widget Stüdyo", "Widget Studio"), Icons.Default.Widgets, IconGradients.Sunset)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = widgetType == 0,
                                onClick = { widgetType = 0 },
                                label = { Text(t("Canlı Akış", "Live Flow"), fontSize = 12.sp) },
                            )
                            FilterChip(
                                selected = widgetType == 1,
                                onClick = { widgetType = 1 },
                                label = { Text(t("Haftalık Akış", "Weekly Flow"), fontSize = 12.sp) },
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = quickMode,
                                onClick = { quickMode = true },
                                label = { Text(t("Hızlı Mod", "Quick Mode"), fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            )
                            FilterChip(
                                selected = !quickMode,
                                onClick = { quickMode = false },
                                label = { Text(t("Detaylı Mod", "Detailed Mode"), fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                text = if (quickMode) {
                                    t(
                                        "Hızlı Mod: Temel ayarlar ve hazır profiller. En kısa yoldan düzenle.",
                                        "Quick Mode: Core options and one-tap presets. Edit in the fastest way.",
                                    )
                                } else {
                                    t(
                                        "Detaylı Mod: Tüm yerleşim, boyut ve tipografi kontrolleri açık.",
                                        "Detailed Mode: Full layout, sizing and typography controls are enabled.",
                                    )
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
                            )
                        }
                        if (widgetType == 0) {
                            CountdownWidgetPreview(uiState)
                        } else {
                            SyllabusWidgetPreview(uiState)
                        }
                        Divider()
                        SliderRow(
                            t("Tüm metin ölçeği", "Global text scale"),
                            uiState.globalWidgetTextScale.toFloat(),
                            80f..130f,
                        ) { viewModel.onGlobalWidgetTextScaleChange(it.toInt()) }
                        Text(
                            text = t("Genel oran: %${uiState.globalWidgetTextScale}", "Overall scale: ${uiState.globalWidgetTextScale}%"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                    }
                }
            }
        }


            item {
                if (!quickMode) {
                    SettingsSectionHeader(t("Tema ve Doku", "Preset & Feel"), Icons.Default.AutoAwesome, IconGradients.Blue)
                }
            }
            item {
                if (quickMode) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = t("Hızlı Profil Uygula", "Apply Quick Profile"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = t(
                                    "Tek dokunuşla önerilen düzenleri uygula. Sonrasında istersen detaylı moddan ince ayar yapabilirsin.",
                                    "Apply recommended layouts with one tap. Fine-tune later from Detailed Mode if needed.",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilledTonalButton(onClick = {
                                    if (widgetType == 0) {
                                        viewModel.onCountdownWidgetPresetChange(WidgetVisualPreset.Dawn)
                                        viewModel.onCountdownWidgetDensityChange(WidgetInfoDensity.Balanced)
                                        viewModel.onCountdownTypographyPresetChange(WidgetTypographyPreset.Strong)
                                        viewModel.onShowSecondsChange(false)
                                        viewModel.onDynamicColorEnabledChange(true)
                                    } else {
                                        viewModel.onSyllabusWidgetPresetChange(WidgetVisualPreset.Dawn)
                                        viewModel.onSyllabusWidgetDensityChange(WidgetInfoDensity.Balanced)
                                        viewModel.onSyllabusTypographyPresetChange(WidgetTypographyPreset.Strong)
                                    }
                                }) {
                                    Text(t("Dengeli", "Balanced"))
                                }
                                FilledTonalButton(onClick = {
                                    if (widgetType == 0) {
                                        viewModel.onCountdownWidgetPresetChange(WidgetVisualPreset.Slate)
                                        viewModel.onCountdownWidgetDensityChange(WidgetInfoDensity.Sparse)
                                        viewModel.onCountdownTypographyPresetChange(WidgetTypographyPreset.Notebook)
                                        viewModel.onShowSecondsChange(false)
                                        viewModel.onDynamicColorEnabledChange(false)
                                    } else {
                                        viewModel.onSyllabusWidgetPresetChange(WidgetVisualPreset.Slate)
                                        viewModel.onSyllabusWidgetDensityChange(WidgetInfoDensity.Sparse)
                                        viewModel.onSyllabusTypographyPresetChange(WidgetTypographyPreset.Notebook)
                                    }
                                }) {
                                    Text(t("Okunaklı", "Readable"))
                                }
                                FilledTonalButton(onClick = {
                                    if (widgetType == 0) {
                                        viewModel.onCountdownWidgetPresetChange(WidgetVisualPreset.Neon)
                                        viewModel.onCountdownWidgetDensityChange(WidgetInfoDensity.Dense)
                                        viewModel.onCountdownTypographyPresetChange(WidgetTypographyPreset.Technical)
                                        viewModel.onShowSecondsChange(true)
                                        viewModel.onDynamicColorEnabledChange(true)
                                    } else {
                                        viewModel.onSyllabusWidgetPresetChange(WidgetVisualPreset.Neon)
                                        viewModel.onSyllabusWidgetDensityChange(WidgetInfoDensity.Dense)
                                        viewModel.onSyllabusTypographyPresetChange(WidgetTypographyPreset.Technical)
                                    }
                                }) {
                                    Text(t("Canlı", "Vivid"))
                                }
                            }
                            Divider()
                            if (widgetType == 0) {
                                SettingsToggleRow(
                                    icon = Icons.Default.Timelapse,
                                    gradient = IconGradients.Purple,
                                    title = t("Saniyeleri göster", "Show seconds"),
                                    subtitle = t("Hızlı görünümde temel sayaç ayarı", "Core timer control for quick mode"),
                                    checked = uiState.showSeconds,
                                    onCheckedChange = viewModel::onShowSecondsChange,
                                )
                                SettingsToggleRow(
                                    icon = Icons.Default.ColorLens,
                                    gradient = IconGradients.Blue,
                                    title = t("Akıllı vurgu", "Smart accent"),
                                    subtitle = t("Süreye göre vurgu rengi", "Adaptive accent based on remaining time"),
                                    checked = uiState.dynamicColorEnabled,
                                    onCheckedChange = viewModel::onDynamicColorEnabledChange,
                                )
                            } else {
                                SettingsToggleRow(
                                    icon = Icons.Default.AccessTime,
                                    gradient = IconGradients.Blue,
                                    title = t("Saatleri göster", "Show times"),
                                    subtitle = t("Başlangıç ve bitiş saatleri", "Show start and end times"),
                                    checked = uiState.syllabusShowTimes,
                                    onCheckedChange = viewModel::onSyllabusShowTimesChange,
                                )
                                SettingsToggleRow(
                                    icon = Icons.Default.Bedtime,
                                    gradient = IconGradients.Sunset,
                                    title = t("Teneffüsleri göster", "Show breaks"),
                                    subtitle = t("Akışta teneffüsleri saklama", "Keep breaks in the weekly flow"),
                                    checked = uiState.syllabusShowBreaks,
                                    onCheckedChange = viewModel::onSyllabusShowBreaksChange,
                                )
                            }
                        }
                    }
                }
            }
            item {
                if (!quickMode) GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val selectedPreset = if (widgetType == 0) uiState.countdownWidgetPreset else uiState.syllabusWidgetPreset
                        val selectedFamily = if (widgetType == 0) uiState.countdownWidgetFamily else uiState.syllabusWidgetFamily
                        val selectedDensity = if (widgetType == 0) uiState.countdownWidgetDensity else uiState.syllabusWidgetDensity
                        val selectedTypography = if (widgetType == 0) uiState.countdownTypographyPreset else uiState.syllabusTypographyPreset
                        WidgetPresetRow(
                            selected = selectedPreset,
                            isEn = isEn,
                            onSelect = { preset ->
                                if (widgetType == 0) {
                                    viewModel.onCountdownWidgetPresetChange(preset)
                                } else {
                                    viewModel.onSyllabusWidgetPresetChange(preset)
                                }
                            },
                        )
                        Divider()
                        Text(
                            text = t("Stil ailesi", "Style family"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            WidgetStyleFamily.values().forEach { family ->
                                FilterChip(
                                    selected = selectedFamily == family,
                                    onClick = {
                                        if (widgetType == 0) {
                                            viewModel.onCountdownWidgetFamilyChange(family)
                                        } else {
                                            viewModel.onSyllabusWidgetFamilyChange(family)
                                        }
                                    },
                                    label = { Text(widgetStyleFamilyTitle(family, isEn)) },
                                )
                            }
                        }
                        Text(
                            text = t("Bilgi yoğunluğu", "Info density"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WidgetInfoDensity.values().forEach { density ->
                                FilterChip(
                                    selected = selectedDensity == density,
                                    onClick = {
                                        if (widgetType == 0) {
                                            viewModel.onCountdownWidgetDensityChange(density)
                                        } else {
                                            viewModel.onSyllabusWidgetDensityChange(density)
                                        }
                                    },
                                    label = { Text(widgetDensityTitle(density, isEn)) },
                                )
                            }
                        }
                        Text(
                            text = t("Tipografi", "Typography"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            WidgetTypographyPreset.values().forEach { typography ->
                                FilterChip(
                                    selected = selectedTypography == typography,
                                    onClick = {
                                        if (widgetType == 0) {
                                            viewModel.onCountdownTypographyPresetChange(typography)
                                        } else {
                                            viewModel.onSyllabusTypographyPresetChange(typography)
                                        }
                                    },
                                    label = { Text(widgetTypographyTitle(typography, isEn)) },
                                )
                            }
                        }
                        SliderRow(
                            t("Köşe Yuvarlaklığı", "Corner Radius"),
                            uiState.widgetCornerRadius.toFloat(),
                            0f..36f,
                        ) { viewModel.onWidgetCornerRadiusChange(it.toInt()) }
                        Divider()
                        if (widgetType == 0) {
                            SettingsToggleRow(
                                icon = Icons.Default.Timelapse,
                                gradient = IconGradients.Purple,
                                title = t("Saniyeleri göster", "Show seconds"),
                                subtitle = t("Geri sayımı daha canlı göster", "Make the timer feel more alive"),
                                checked = uiState.showSeconds,
                                onCheckedChange = viewModel::onShowSecondsChange,
                            )
                            SettingsToggleRow(
                                icon = Icons.Default.ColorLens,
                                gradient = IconGradients.Blue,
                                title = t("Akıllı vurgu", "Smart accent"),
                                subtitle = t("Süre azaldıkça vurgu rengi değişsin", "Shift the accent as time runs low"),
                                checked = uiState.dynamicColorEnabled,
                                onCheckedChange = viewModel::onDynamicColorEnabledChange,
                            )
                            SettingsToggleRow(
                                icon = Icons.Default.AutoStories,
                                gradient = IconGradients.Sunset,
                                title = t("Mikro ikonlar", "Micro icons"),
                                subtitle = t("Durum ve özlü söz modunda küçük semboller kullan", "Use small symbols in status and quote mode"),
                                checked = uiState.countdownMicroIconsEnabled,
                                onCheckedChange = viewModel::onCountdownMicroIconsEnabledChange,
                            )
                            SettingsToggleRow(
                                icon = Icons.Default.LinearScale,
                                gradient = IconGradients.Sunset,
                                title = t("İlerleme çizgisi", "Progress rail"),
                                subtitle = t("Aktif olayda ilerleme göstergesini aç", "Show a progress indicator while an event is active"),
                                checked = uiState.progressBarEnabled,
                                onCheckedChange = viewModel::onProgressBarEnabledChange,
                            )
                            ExpandableSection(
                                title = t("Özlü Söz Görünümü", "Quote Appearance"),
                                icon = Icons.Default.FormatQuote,
                                gradient = IconGradients.Sunset,
                            ) {
                            SettingsToggleRow(
                                icon = Icons.Default.WbSunny,
                                gradient = IconGradients.Blue,
                                title = t("Selamlama satırı", "Greeting line"),
                                subtitle = t("Özlü söz modunda günaydın ve iyi akşamlar gibi selamlamaları göster", "Show greeting lines in quote mode"),
                                checked = uiState.countdownQuoteGreetingEnabled,
                                onCheckedChange = viewModel::onCountdownQuoteGreetingEnabledChange,
                            )
                            SettingsToggleRow(
                                icon = Icons.Default.FormatQuote,
                                gradient = IconGradients.Purple,
                                title = t("Kaynak satırı", "Source line"),
                                subtitle = t("Özlü sözün ait olduğu kişi veya kültürü alt satırda göster", "Show the quote source on the lower line"),
                                checked = uiState.countdownQuoteSourceEnabled,
                                onCheckedChange = viewModel::onCountdownQuoteSourceEnabledChange,
                            )
                            Text(
                                text = t("Söz hizası", "Quote alignment"),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = uiState.countdownQuoteAlignment == 0,
                                    onClick = { viewModel.onCountdownQuoteAlignmentChange(0) },
                                    label = { Text(t("Sol", "Left")) },
                                )
                                FilterChip(
                                    selected = uiState.countdownQuoteAlignment == 1,
                                    onClick = { viewModel.onCountdownQuoteAlignmentChange(1) },
                                    label = { Text(t("Orta", "Center")) },
                                )
                                FilterChip(
                                    selected = uiState.countdownQuoteAlignment == 2,
                                    onClick = { viewModel.onCountdownQuoteAlignmentChange(2) },
                                    label = { Text(t("Sağ", "Right")) },
                                )
                            }
                            if (uiState.countdownQuoteSourceEnabled) {
                                Text(
                                    text = t("Kaynak hizası", "Source alignment"),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = uiState.countdownQuoteSourceAlignment == 0,
                                        onClick = { viewModel.onCountdownQuoteSourceAlignmentChange(0) },
                                        label = { Text(t("Sol", "Left")) },
                                    )
                                    FilterChip(
                                        selected = uiState.countdownQuoteSourceAlignment == 1,
                                        onClick = { viewModel.onCountdownQuoteSourceAlignmentChange(1) },
                                        label = { Text(t("Orta", "Center")) },
                                    )
                                    FilterChip(
                                        selected = uiState.countdownQuoteSourceAlignment == 2,
                                        onClick = { viewModel.onCountdownQuoteSourceAlignmentChange(2) },
                                        label = { Text(t("Sağ", "Right")) },
                                    )
                                }
                            }
                            Text(
                                text = t("Söz rengi", "Quote tone"),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = uiState.countdownQuoteTextTone == 0,
                                    onClick = { viewModel.onCountdownQuoteTextToneChange(0) },
                                    label = { Text(t("Temel", "Base")) },
                                )
                                FilterChip(
                                    selected = uiState.countdownQuoteTextTone == 1,
                                    onClick = { viewModel.onCountdownQuoteTextToneChange(1) },
                                    label = { Text(t("Vurgu", "Accent")) },
                                )
                                FilterChip(
                                    selected = uiState.countdownQuoteTextTone == 2,
                                    onClick = { viewModel.onCountdownQuoteTextToneChange(2) },
                                    label = { Text(t("Yumuşak", "Soft")) },
                                )
                                FilterChip(
                                    selected = uiState.countdownQuoteTextTone == 3,
                                    onClick = { viewModel.onCountdownQuoteTextToneChange(3) },
                                    label = { Text(t("Sıcak", "Warm")) },
                                )
                            }
                            if (uiState.countdownQuoteSourceEnabled) {
                                Text(
                                    text = t("Kaynak rengi", "Source tone"),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = uiState.countdownQuoteSourceTone == 0,
                                        onClick = { viewModel.onCountdownQuoteSourceToneChange(0) },
                                        label = { Text(t("Temel", "Base")) },
                                    )
                                    FilterChip(
                                        selected = uiState.countdownQuoteSourceTone == 1,
                                        onClick = { viewModel.onCountdownQuoteSourceToneChange(1) },
                                        label = { Text(t("Vurgu", "Accent")) },
                                    )
                                    FilterChip(
                                        selected = uiState.countdownQuoteSourceTone == 2,
                                        onClick = { viewModel.onCountdownQuoteSourceToneChange(2) },
                                        label = { Text(t("Yumuşak", "Soft")) },
                                    )
                                    FilterChip(
                                        selected = uiState.countdownQuoteSourceTone == 3,
                                        onClick = { viewModel.onCountdownQuoteSourceToneChange(3) },
                                        label = { Text(t("Sıcak", "Warm")) },
                                    )
                                }
                            }
                            SettingsToggleRow(
                                icon = Icons.Default.FormatBold,
                                gradient = IconGradients.Blue,
                                title = t("Söz kalın", "Bold quote"),
                                subtitle = t("Özlü söz satırını daha güçlü göster", "Make the quote line feel stronger"),
                                checked = uiState.countdownQuoteTextBold,
                                onCheckedChange = viewModel::onCountdownQuoteTextBoldChange,
                            )
                            SettingsToggleRow(
                                icon = Icons.Default.FormatItalic,
                                gradient = IconGradients.Purple,
                                title = t("Söz italik", "Italic quote"),
                                subtitle = t("Özlü söz satırına akışkan bir vurgu ekle", "Add a softer emphasis to the quote"),
                                checked = uiState.countdownQuoteTextItalic,
                                onCheckedChange = viewModel::onCountdownQuoteTextItalicChange,
                            )
                            if (uiState.countdownQuoteSourceEnabled) {
                                SettingsToggleRow(
                                    icon = Icons.Default.FormatBold,
                                    gradient = IconGradients.Blue,
                                    title = t("Kaynak kalın", "Bold source"),
                                    subtitle = t("Kaynak satırını belirginleştir", "Make the source line clearer"),
                                    checked = uiState.countdownQuoteSourceBold,
                                    onCheckedChange = viewModel::onCountdownQuoteSourceBoldChange,
                                )
                                SettingsToggleRow(
                                    icon = Icons.Default.FormatItalic,
                                    gradient = IconGradients.Purple,
                                    title = t("Kaynak italik", "Italic source"),
                                    subtitle = t("Kaynak satırını daha zarif göster", "Give the source line a softer touch"),
                                    checked = uiState.countdownQuoteSourceItalic,
                                    onCheckedChange = viewModel::onCountdownQuoteSourceItalicChange,
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = t("Özlü söz modu düzeni", "Quote mode layout"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = t(
                                            "Ana başlık sözün kendisini, durum satırı selamlamayı, alt satır ise kaynağı gösterir. Söz ve kaynak canlı widgetta sola hizalanır.",
                                            "Main title shows the quote, status line shows the greeting, and the lower line shows the source. Quote alignment can be left, centered, or right.",
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                    )
                                    val quoteTextAlign = when (uiState.countdownQuoteAlignment) {
                                        1 -> androidx.compose.ui.text.style.TextAlign.Center
                                        2 -> androidx.compose.ui.text.style.TextAlign.End
                                        else -> androidx.compose.ui.text.style.TextAlign.Start
                                    }
                                    val quoteSourceAlign = when (uiState.countdownQuoteSourceAlignment) {
                                        1 -> androidx.compose.ui.text.style.TextAlign.Center
                                        2 -> androidx.compose.ui.text.style.TextAlign.End
                                        else -> androidx.compose.ui.text.style.TextAlign.Start
                                    }
                                    val quoteTextColor = when (uiState.countdownQuoteTextTone) {
                                        1 -> MaterialTheme.colorScheme.primary
                                        2 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                                        3 -> Color(0xFFFFC857)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                    val quoteSourceColor = when (uiState.countdownQuoteSourceTone) {
                                        0 -> MaterialTheme.colorScheme.onSurface
                                        1 -> MaterialTheme.colorScheme.primary
                                        2 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f)
                                        3 -> Color(0xFFFFC857)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f)
                                    }
                                    Text(
                                        text = t("Disiplin, en zor günü bile taşınır kılar.", "Discipline makes even the hardest day carryable."),
                                        textAlign = quoteTextAlign,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (uiState.countdownQuoteTextBold) FontWeight.Bold else FontWeight.Normal,
                                        fontStyle = if (uiState.countdownQuoteTextItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                        color = quoteTextColor,
                                    )
                                    if (uiState.countdownQuoteSourceEnabled) {
                                        Text(
                                            text = t("Kafkas anlatısı", "Caucasus saying"),
                                            textAlign = quoteSourceAlign,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (uiState.countdownQuoteSourceBold) FontWeight.Bold else FontWeight.Normal,
                                            fontStyle = if (uiState.countdownQuoteSourceItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                            color = quoteSourceColor,
                                        )
                                    }
                                }
                            }
                            }
                        } else {
                            SettingsToggleRow(Icons.Default.AccessTime, IconGradients.Blue, t("Saatleri göster", "Show times"), t("Akışta başlangıç ve bitiş saatleri görünsün", "Show start and end times in the flow"), uiState.syllabusShowTimes, viewModel::onSyllabusShowTimesChange)
                            SettingsToggleRow(Icons.Default.Bedtime, IconGradients.Sunset, t("Teneffüsleri göster", "Show breaks"), t("Teneffüs ve ara olayları listede tut", "Keep breaks in the weekly flow"), uiState.syllabusShowBreaks, viewModel::onSyllabusShowBreaksChange)
                            SettingsToggleRow(Icons.Default.Label, IconGradients.Purple, t("Sınıf renkleri", "Class colors"), t("Sınıf renklerini akış satırlarına taşı", "Carry class colors into the flow rows"), uiState.syllabusShowClassColors, viewModel::onSyllabusShowClassColorsChange)
                            SettingsToggleRow(Icons.Default.AutoFixHigh, IconGradients.Blue, t("Mikro ikonlar", "Micro icons"), t("Ders, teneffüs ve boş ders için küçük semboller ekle", "Add small symbols for lessons, breaks, and free periods"), uiState.syllabusShowIcons, viewModel::onSyllabusShowIconsChange)
                            SettingsToggleRow(Icons.Default.FormatPaint, IconGradients.Sunset, t("Metni renklendir", "Colorize text"), t("Akış satırlarını tema ve sınıf renkleriyle vurgula", "Tint flow rows with theme and class colors"), uiState.syllabusColorizeText, viewModel::onSyllabusColorizeTextChange)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SyllabusActiveHighlightStyle.values().forEach { style ->
                                    FilterChip(
                                        selected = uiState.syllabusActiveHighlightStyle == style,
                                        onClick = { viewModel.onSyllabusActiveHighlightStyleChange(style) },
                                        label = {
                                            Text(
                                                when (style) {
                                                    SyllabusActiveHighlightStyle.Bold -> t("Kalın", "Bold")
                                                    SyllabusActiveHighlightStyle.Accent -> t("Vurgu", "Accent")
                                                    SyllabusActiveHighlightStyle.Soft -> t("Yumuşak", "Soft")
                                                    SyllabusActiveHighlightStyle.Strong -> t("Güçlü", "Strong")
                                                },
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (!quickMode) {
                item {
                    ExpandableSection(
                        title = t("Eleman Yerleşimi", "Element Layout"),
                        icon = Icons.Default.ViewQuilt,
                        gradient = IconGradients.Purple,
                    ) {
                    if (widgetType == 0) {
                        OutlinedButton(
                            onClick = viewModel::resetCountdownElementSizes,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(t("Boyutları sıfırla", "Reset sizes"))
                        }
                        CountdownWidgetElement.values().forEach { element ->
                            val prefs = uiState.countdownElementPrefs[element]
                                ?: WidgetElementPreferences(element.defaultVisible, element.defaultPosition, element.defaultScale)
                            CountdownWidgetElementCard(
                                title = countdownElementTitle(element, isEn),
                                visible = prefs.visible,
                                positions = countdownPositionLabels(element, isEn),
                                selectedPosition = prefs.position.coerceIn(0, countdownPositionLabels(element, isEn).lastIndex),
                                size = uiState.countdownElementSizes[element] ?: element.defaultSize,
                                isEn = isEn,
                                onVisibilityChange = { viewModel.onCountdownElementVisibilityChange(element, it) },
                                onPositionChange = { viewModel.onCountdownElementPositionChange(element, it) },
                                onSizeChange = { viewModel.onCountdownElementSizeChange(element, it) },
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = viewModel::resetSyllabusElementSizes,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(t("Boyutları sıfırla", "Reset sizes"))
                        }
                        SyllabusWidgetElement.values().forEach { element ->
                            val prefs = uiState.syllabusElementPrefs[element]
                                ?: WidgetElementPreferences(element.defaultVisible, element.defaultPosition, element.defaultScale)
                            SyllabusWidgetElementCard(
                                title = syllabusElementTitle(element, isEn),
                                visible = prefs.visible,
                                positions = syllabusPositionLabels(isEn),
                                selectedPosition = prefs.position.coerceIn(0, syllabusPositionLabels(isEn).lastIndex),
                                size = uiState.syllabusElementSizes[element] ?: element.defaultSize,
                                isEn = isEn,
                                onVisibilityChange = { viewModel.onSyllabusElementVisibilityChange(element, it) },
                                onPositionChange = { viewModel.onSyllabusElementPositionChange(element, it) },
                                onSizeChange = { viewModel.onSyllabusElementSizeChange(element, it) },
                            )
                        }
                    }
                }
            }
            }
        }
}

@Composable
fun ColorPickerRow(selectedColor: String, onColorSelect: (String) -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val colors = listOf("#FFFFFF", "#111111", "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722")

    LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(colors) { hex ->
            val isSelected = selectedColor.uppercase() == hex.uppercase()
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(hex)))
                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) onSurface else onSurface.copy(alpha = 0.2f), CircleShape)
                    .premiumClickable { onColorSelect(hex) }
            )
        }
    }
}

@Composable
fun SystemTab(uiState: SettingsUiState, viewModel: SettingsViewModel, context: android.content.Context, onNavigateToProfiles: () -> Unit, permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>) {
    val isEn = uiState.appLanguage == "en"
    fun t(tr: String, en: String): String = if (isEn) en else tr
    SettingsSectionHeader(t("Sistem Ayarları", "System Settings"), Icons.Default.Settings, IconGradients.Lava)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ClickableRow(Icons.Default.Person, IconGradients.Purple, t("Profilleri Yönet", "Manage Profiles"), onNavigateToProfiles)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            SettingsToggleRow(Icons.Default.Notifications, IconGradients.Blue, t("Bildirimler", "Notifications"), t("Zil vakitlerinde bildirim gönder", "Send notifications at bell times"), uiState.notificationsEnabled) { enabled ->
                if (enabled && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else viewModel.onNotificationsEnabledChange(enabled)
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            SettingsToggleRow(Icons.Default.VolumeUp, IconGradients.Green, t("Sesli Zil", "Sound"), t("Sayaç bittiğinde zil çal", "Play bell sound when timer ends"), uiState.soundEnabled, viewModel::onSoundEnabledChange)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            SettingsToggleRow(Icons.Default.HourglassEmpty, IconGradients.Sunset, t("Saniyeyi Göster", "Show Seconds"), t("Sayıcıda saniyeleri göster", "Show seconds in timer"), uiState.showSeconds, viewModel::onShowSecondsChange)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            SettingsToggleRow(Icons.Default.DoNotDisturbOn, IconGradients.Lava, t("Otomatik Sessiz", "Auto Silent"), t("Derslerde sessize al", "Switch to silent during lessons"), uiState.autoSilentMode, viewModel::onAutoSilentModeChange)
        }
    }
}

@Composable
fun PlanningTab(
    uiState: SettingsUiState, 
    viewModel: SettingsViewModel, 
    context: android.content.Context,
    onShowHoliday: (HolidayTemplate?) -> Unit,
    onShowQuote: () -> Unit,
    onShowExamGuide: () -> Unit,
    onShowDisclaimer: () -> Unit,
    onShowReset: () -> Unit,
    onShowBackup: () -> Unit,
    onShowImport: () -> Unit,
    onShowQrShare: () -> Unit,
    onShowQrScan: () -> Unit,
    onShowSpecialReminderAdd: () -> Unit,
    exportLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    importLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val isEn = uiState.appLanguage == "en"
    fun t(tr: String, en: String): String = if (isEn) en else tr
    val pkgInfo = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
    }
    val appVersionName = pkgInfo?.versionName ?: "-"
    val appVersionCode = if (android.os.Build.VERSION.SDK_INT >= 28) (pkgInfo?.longVersionCode ?: 0L).toString() else (pkgInfo?.versionCode ?: 0).toString()
    SettingsSectionHeader(t("Sınav ve Özel Sayaç", "Exam & Custom Timer"), Icons.Default.Timer, IconGradients.Blue)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("Özel Sayaç", "Custom Timer"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(t("Bugüne özel tek seferlik sayaç", "One-time timer for today"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = onShowExamGuide) { Icon(Icons.Default.HelpOutline, null, tint = Color.Gray) }
                Switch(checked = uiState.customModeEnabled, onCheckedChange = viewModel::onCustomModeEnabledChange)
            }
            if (uiState.customModeEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.customModeTitle, 
                    onValueChange = viewModel::onCustomModeTitleChange, 
                    label = { Text(t("Sayaç Başlığı", "Timer Title")) }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.2f), focusedBorderColor = Color.White)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.customModeTime, 
                    onValueChange = {}, 
                    label = { Text(t("Bitiş Saati", "End Time")) }, 
                    modifier = Modifier.fillMaxWidth().premiumClickable {
                        launchTimePicker(context, uiState.customModeTime.ifEmpty { "12:00" }, viewModel::onCustomModeTimeChange)
                    }, 
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color.White.copy(alpha = 0.2f), disabledTextColor = Color.White, disabledLabelColor = Color.Gray)
                )
            }
        }
    }
    
    Spacer(Modifier.height(24.dp))
    SettingsSectionHeader(t("Yedekleme ve Veri", "Backup & Data"), Icons.Default.CloudUpload, IconGradients.Blue)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = { exportLauncher.launch("zilagent_backup_${System.currentTimeMillis()}.json") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text(t("DOSYA OLARAK YEDEKLE", "BACK UP TO FILE"), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            ClickableRow(Icons.Default.Upload, IconGradients.Blue, t("Yedekten Geri Yükle", "Restore Backup"), { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) })
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onShowQrShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCode, null)
                    Spacer(Modifier.width(8.dp))
                    Text(t("QR Paylaş", "Share QR"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                Button(
                    onClick = onShowQrScan,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64A19)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null)
                    Spacer(Modifier.width(8.dp))
                    Text(t("QR Tara", "Scan QR"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))
    SettingsSectionHeader(t("Tatil ve Günler", "Holidays & Days"), Icons.Default.DateRange, IconGradients.Sunset)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(t("Çalışma Günleri", "Working Days"), style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (if (isEn) listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")).forEachIndexed { index, label ->
                    val isActive = uiState.workingDaysMask.getOrNull(index) == '1'
                    
                    val activeGradient = Brush.linearGradient(
                        colors = listOf(com.zilagent.app.ui.theme.Purple80, com.zilagent.app.ui.theme.Pink80)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isActive) activeGradient else Brush.linearGradient(listOf(Color.White.copy(alpha=0.05f), Color.White.copy(alpha=0.05f)))) // Colorful if active
                            .border(if(isActive) 0.dp else 1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .premiumClickable {
                                val newMask = uiState.workingDaysMask.toCharArray()
                                newMask[index] = if (isActive) '0' else '1'
                                viewModel.onWorkingDaysChange(String(newMask))
                            }, 
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label, 
                            fontSize = 12.sp, 
                            color = if (isActive) Color.White else Color.White.copy(alpha = 0.5f), 
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }
                }
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.05f))
            Text(t("Belirli Gün ve Haftalar", "Special Days & Weeks"), style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                t("Seçtiklerin, tarih yaklaşınca widgette görünür.", "Selected items appear on the widget as date approaches."),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            uiState.specialTemplates.forEach { template ->
                val checked = template.id in uiState.selectedSpecialTemplateIds
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(template.name(isEn), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Text(
                            "${String.format("%02d/%02d", template.startDay, template.startMonth)} - ${String.format("%02d/%02d", template.endDay, template.endMonth)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                    Switch(
                        checked = checked,
                        onCheckedChange = { viewModel.onSpecialTemplateToggled(template.id, it) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            SliderRow(
                label = t("Kaç gün önce görünsün", "Show this many days before"),
                value = uiState.specialReminderLeadDays.toFloat(),
                range = 0f..30f,
                onValueChange = { viewModel.onSpecialReminderLeadDaysChange(it.toInt()) },
            )

            if (uiState.customSpecialReminders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(t("Eklenen Özel Günler", "Custom Added Days"), style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                uiState.customSpecialReminders.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.name, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Text("${entry.startDate} - ${entry.endDate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.removeCustomSpecialReminder(entry.id) }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            TextButton(onClick = onShowSpecialReminderAdd, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(4.dp))
                Text(t("Özel Gün/Hafta Ekle", "Add Special Day/Week"))
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.05f))
            Text(t("Türkiye Yıllık Tatil Şablonları", "Türkiye Annual Holiday Templates"), style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            TURKIYE_HOLIDAY_TEMPLATES.forEach { template ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(template.name(isEn), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Text(
                            template.suggestedRange(isEn),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                    TextButton(onClick = { onShowHoliday(template) }) {
                        Text(t("Tarih Ekle", "Add Date"))
                    }
                }
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.05f))
            Text(t("Özel Tatiller", "Custom Holidays"), style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            uiState.holidayList.forEach { holiday ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text(holiday.name, style = MaterialTheme.typography.bodyMedium, color = Color.White); Text("${holiday.startDate} - ${holiday.endDate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                    IconButton(onClick = { viewModel.deleteHoliday(holiday) }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.4f)) }
                }
            }
            TextButton(onClick = { onShowHoliday(null) }, modifier = Modifier.align(Alignment.End)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text(t("Tatil Ekle", "Add Holiday")) }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val quoteCount = uiState.quoteList.size
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isEn) "Quotes ($quoteCount items)" else "Özlü Sözler ($quoteCount adet)", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                TextButton(onClick = onShowQuote) { Text(t("Yönet", "Manage")) }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    SettingsSectionHeader(t("Hakkında", "About"), Icons.Default.Info, IconGradients.Lava)
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("ZilAgent v$appVersionName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(t("Gelişmiş Okul Zil ve Program Takip Sistemi", "Advanced School Bell & Schedule Tracker"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            Text(t("Tasarım: Ömer Yolcu", "Design: Ömer Yolcu"), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Text("Kodlama: OpenAI Codex", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Text(t("İletişim: omeryol@gmail.com", "Contact: omeryol@gmail.com"), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(8.dp))
            Text("Package: ${context.packageName}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
            Text("Version: $appVersionName ($appVersionCode)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
            Text(t("Çalışma Modu: Tamamen Çevrimdışı", "Mode: Fully Offline"), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
            
            Spacer(Modifier.height(16.dp))
            val uriHandler = LocalUriHandler.current
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://github.com/omeryol/ZilAgent") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Code, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(t("Kaynak Kod", "Source Code"), fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://github.com/omeryol/ZilAgent/blob/main/LICENSE") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(t("Lisans", "License"), fontSize = 11.sp)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Button(onClick = onShowDisclaimer, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))) { 
                Text(t("Yasal Not & Kullanım Koşulları", "Legal Notice & Terms"), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp) 
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
    SettingsSectionHeader(t("Tehlikeli Bölge", "Danger Zone"), Icons.Default.Warning, IconGradients.Lava)
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(t("Bu alandaki işlemler geri alınamaz. Lütfen dikkatli olun.", "Actions in this area cannot be undone. Please be careful."), style = MaterialTheme.typography.bodySmall, color = Color.Red.copy(alpha = 0.7f))
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onShowReset, 
                Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp)
            ) { 
                Icon(Icons.Default.DeleteForever, null)
                Spacer(Modifier.width(8.dp))
                Text(t("HER ŞEYİ SİL", "DELETE ALL"), color = Color.White, fontWeight = FontWeight.Bold) 
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .premiumClickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GradientIcon(icon, gradient, size = 32.dp, iconSize = 18.dp)
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = onSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = onSurface.copy(alpha = 0.6f),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector, gradient: List<Color>) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
        GradientIcon(icon, gradient, size = 32.dp, iconSize = 18.dp)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsToggleRow(icon: ImageVector, gradient: List<Color>, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val subColor = onSurface.copy(alpha = 0.65f)
    Row(modifier = Modifier.fillMaxWidth().premiumClickable { onCheckedChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        GradientIcon(icon, gradient, size = 40.dp, iconSize = 20.dp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = onSurface, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = subColor)
        }
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            )
        )
    }
}

@Composable
fun ClickableRow(icon: ImageVector, gradient: List<Color>, title: String, onClick: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).premiumClickable { onClick() }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        GradientIcon(icon, gradient, size = 40.dp, iconSize = 20.dp)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = onSurface, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = onSurface.copy(alpha = 0.55f))
    }
}

@Composable
fun SliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column(Modifier.padding(vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = onSurface.copy(alpha = 0.8f))
            Text(value.toInt().toString(), style = MaterialTheme.typography.bodySmall, color = onSurface, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value, 
            onValueChange = onValueChange, 
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
            )
        )
    }
}

private fun localizedThemeName(theme: String, isEn: Boolean): String {
    return when (theme) {
        "Ocean" -> if (isEn) "Ocean" else "Okyanus"
        "Forest" -> if (isEn) "Forest" else "Orman"
        "Sunset" -> if (isEn) "Sunset" else "Gün Batımı"
        "Desert" -> if (isEn) "Desert" else "Çöl"
        "Polar" -> if (isEn) "Polar" else "Kutup"
        "Midnight" -> if (isEn) "Midnight" else "Gece Yarısı"
        "Candy" -> if (isEn) "Candy" else "Şeker"
        "Mint" -> if (isEn) "Mint" else "Nane"
        "Lavender" -> if (isEn) "Lavender" else "Lavanta"
        "Peach" -> if (isEn) "Peach" else "Şeftali"
        "Cloud" -> if (isEn) "Cloud" else "Bulut"
        "Fire" -> if (isEn) "Fire" else "Ateş"
        "Sun" -> if (isEn) "Sun" else "Güneş"
        "Cherry" -> if (isEn) "Cherry" else "Kiraz"
        "Electric" -> if (isEn) "Electric" else "Elektrik"
        "Royal" -> if (isEn) "Royal" else "Asil"
        else -> theme
    }
}

@Composable
fun WidgetPresetRow(
    selected: com.zilagent.app.widget.WidgetVisualPreset,
    isEn: Boolean,
    onSelect: (com.zilagent.app.widget.WidgetVisualPreset) -> Unit,
) {
    val presets = com.zilagent.app.widget.WidgetVisualPreset.values().toList()
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(presets) { preset ->
            val palette = WidgetAppearance.palette(preset)
            val labelColor = if (preset == selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(width = 92.dp, height = 58.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(palette.chipBackground),
                                    Color(palette.accent).copy(alpha = 0.82f),
                                ),
                            ),
                        )
                        .border(
                            width = if (preset == selected) 2.dp else 1.dp,
                            color = if (preset == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .premiumClickable { onSelect(preset) },
                )
                Spacer(Modifier.height(8.dp))
                Text(widgetPresetTitle(preset, isEn), color = labelColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun widgetPresetTitle(preset: com.zilagent.app.widget.WidgetVisualPreset, isEn: Boolean): String {
    return when (preset) {
        com.zilagent.app.widget.WidgetVisualPreset.Slate -> if (isEn) "Slate" else "Kayrak"
        com.zilagent.app.widget.WidgetVisualPreset.Paper -> if (isEn) "Paper" else "Kağıt"
        com.zilagent.app.widget.WidgetVisualPreset.Dawn -> if (isEn) "Dawn" else "Şafak"
        com.zilagent.app.widget.WidgetVisualPreset.Grove -> if (isEn) "Grove" else "Koruluk"
        com.zilagent.app.widget.WidgetVisualPreset.Neon -> if (isEn) "Neon" else "Neon"
        com.zilagent.app.widget.WidgetVisualPreset.Lagoon -> if (isEn) "Lagoon" else "Lagün"
        com.zilagent.app.widget.WidgetVisualPreset.Ember -> if (isEn) "Ember" else "Kehribar"
        com.zilagent.app.widget.WidgetVisualPreset.Mono -> if (isEn) "Mono" else "Monokrom"
    }
}

private fun widgetStyleFamilyTitle(family: WidgetStyleFamily, isEn: Boolean): String {
    return when (family) {
        WidgetStyleFamily.Minimal -> if (isEn) "Minimal" else "Minimal"
        WidgetStyleFamily.Academic -> if (isEn) "Academic" else "Akademik"
        WidgetStyleFamily.Agenda -> if (isEn) "Agenda" else "Ajanda"
        WidgetStyleFamily.Energetic -> if (isEn) "Energetic" else "Enerjik"
        WidgetStyleFamily.Night -> if (isEn) "Night" else "Gece"
        WidgetStyleFamily.Mono -> if (isEn) "Mono" else "Monokrom"
    }
}

private fun widgetDensityTitle(density: WidgetInfoDensity, isEn: Boolean): String {
    return when (density) {
        WidgetInfoDensity.Sparse -> if (isEn) "Calm" else "Sade"
        WidgetInfoDensity.Balanced -> if (isEn) "Balanced" else "Dengeli"
        WidgetInfoDensity.Dense -> if (isEn) "Dense" else "Yo\u011Fun"
    }
}

private fun widgetTypographyTitle(preset: WidgetTypographyPreset, isEn: Boolean): String {
    return when (preset) {
        WidgetTypographyPreset.Soft -> if (isEn) "Soft" else "Yumu\u015Fak"
        WidgetTypographyPreset.Strong -> if (isEn) "Strong" else "G\u00FC\u00E7l\u00FC"
        WidgetTypographyPreset.Technical -> if (isEn) "Technical" else "Teknik"
        WidgetTypographyPreset.Notebook -> if (isEn) "Notebook" else "Defter"
    }
}

@Composable
fun CountdownWidgetPreview(uiState: SettingsUiState) {
    val palette = WidgetAppearance.palette(uiState.countdownWidgetPreset)
    val textColor = Color(palette.text)
    val mutedColor = Color(palette.mutedText)
    val chipColor = Color(palette.chipText)
    val accentColor = Color(if (uiState.dynamicColorEnabled) palette.warning else palette.accent)
    val isEn = uiState.appLanguage == "en"
    val family = uiState.countdownWidgetFamily
    val density = uiState.countdownWidgetDensity
    val microIcons = uiState.countdownMicroIconsEnabled
    fun badgeText(kind: String): String {
        val icon = if (microIcons) {
            when (kind) {
                "now" -> "\u25B6 "
                "next" -> "\u2192 "
                else -> ""
            }
        } else {
            ""
        }
        val label = when (kind) {
            "now" -> if (isEn) "Now" else "\u015Eimdi"
            "next" -> if (isEn) "Next" else "Sonra"
            else -> if (isEn) "Status" else "Durum"
        }
        return when (family) {
            WidgetStyleFamily.Minimal -> icon + label.uppercase()
            WidgetStyleFamily.Agenda -> icon + if (isEn) "Plan: $label" else "Plan: $label"
            WidgetStyleFamily.Energetic -> icon + label.uppercase()
            else -> icon + label
        }.trim()
    }
    fun denseMeta(): String {
        return when (density) {
            WidgetInfoDensity.Sparse -> if (isEn) "Countdown" else "Sayaç"
            WidgetInfoDensity.Balanced -> if (uiState.countdownQuoteGreetingEnabled) {
                if (isEn) "Good morning • Counting to end" else "Günaydın • Bitişe sayıyor"
            } else {
                if (isEn) "Counting to end" else "Bitişe sayıyor"
            }
            WidgetInfoDensity.Dense -> if (uiState.countdownQuoteGreetingEnabled) {
                if (isEn) "Good morning • Counting to end • 2/7" else "Günaydın • Bitişe sayıyor • 2/7"
            } else {
                if (isEn) "Counting to end • 2/7" else "Bitişe sayıyor • 2/7"
            }
        }
    }
    val previewData = mapOf(
        CountdownWidgetElement.Badge to badgeText("now"),
        CountdownWidgetElement.Meta to denseMeta(),
        CountdownWidgetElement.Countdown to if (uiState.showSeconds) "08:42" else "08 dk",
        CountdownWidgetElement.Title to when (family) {
            WidgetStyleFamily.Agenda -> "7A - Matematik Atölyesi"
            WidgetStyleFamily.Minimal -> "7A Matematik"
            else -> "7A • Matematik"
        },
        CountdownWidgetElement.Current to when (density) {
            WidgetInfoDensity.Sparse -> "${badgeText("now")} • 7A Matematik"
            WidgetInfoDensity.Balanced -> "${badgeText("now")} • 7A Matematik 08:30-09:10"
            WidgetInfoDensity.Dense -> "${badgeText("now")} • 7A Matematik 08:30-09:10"
        },
        CountdownWidgetElement.Next to if (uiState.countdownQuoteSourceEnabled) {
            when (density) {
                WidgetInfoDensity.Sparse -> "${badgeText("next")} • Teneffüs"
                else -> "${badgeText("next")} • Teneffüs 09:10-09:20"
            }
        } else "",
    )

    WidgetPreviewShell(
        brush = previewFamilyBrush(palette, family, 0.72f),
        cornerRadius = uiState.widgetCornerRadius.dp,
    ) {
        val slots = (0..5).associateWith { mutableListOf<Pair<CountdownWidgetElement, WidgetElementPreferences>>() }
        CountdownWidgetElement.values().filter { it != CountdownWidgetElement.Progress }.forEach { element ->
            val prefs = uiState.countdownElementPrefs[element]
                ?: WidgetElementPreferences(element.defaultVisible, element.defaultPosition, element.defaultScale)
            if (prefs.visible) {
                slots[prefs.position.coerceIn(0, 5)]?.add(element to prefs)
            }
        }
        val progressHeight = countdownProgressHeight(
            (uiState.countdownElementSizes[CountdownWidgetElement.Progress] ?: CountdownWidgetElement.Progress.defaultSize).toFloat(),
        )

        if (uiState.progressBarEnabled && (uiState.countdownElementPrefs[CountdownWidgetElement.Progress]?.visible != false) && (uiState.countdownElementPrefs[CountdownWidgetElement.Progress]?.position == 0)) {
            LinearProgressIndicator(
                progress = 0.62f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(progressHeight),
                color = accentColor,
                trackColor = Color.White.copy(alpha = 0.18f),
            )
            Spacer(Modifier.height(10.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PreviewSlot(slots[0].orEmpty(), previewData, uiState.countdownElementSizes, uiState.countdownTypographyPreset, textColor, mutedColor, chipColor, Modifier.weight(1f))
                PreviewSlot(slots[1].orEmpty(), previewData, uiState.countdownElementSizes, uiState.countdownTypographyPreset, textColor, mutedColor, chipColor, Modifier.weight(1f), alignEnd = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PreviewSlot(slots[2].orEmpty(), previewData, uiState.countdownElementSizes, uiState.countdownTypographyPreset, textColor, mutedColor, chipColor, Modifier.weight(1f))
                PreviewSlot(slots[3].orEmpty(), previewData, uiState.countdownElementSizes, uiState.countdownTypographyPreset, textColor, mutedColor, chipColor, Modifier.weight(1f), alignEnd = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PreviewSlot(slots[4].orEmpty(), previewData, uiState.countdownElementSizes, uiState.countdownTypographyPreset, textColor, mutedColor, chipColor, Modifier.weight(1f))
                PreviewSlot(slots[5].orEmpty(), previewData, uiState.countdownElementSizes, uiState.countdownTypographyPreset, textColor, mutedColor, chipColor, Modifier.weight(1f), alignEnd = true)
            }
            if (uiState.progressBarEnabled && (uiState.countdownElementPrefs[CountdownWidgetElement.Progress]?.visible != false) && (uiState.countdownElementPrefs[CountdownWidgetElement.Progress]?.position != 0)) {
                LinearProgressIndicator(
                    progress = 0.62f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(progressHeight),
                    color = accentColor,
                    trackColor = Color.White.copy(alpha = 0.18f),
                )
            }
        }
    }
}

@Composable
fun SyllabusWidgetPreview(uiState: SettingsUiState) {
    val palette = WidgetAppearance.palette(uiState.syllabusWidgetPreset)
    val textColor = Color(palette.text)
    val mutedColor = Color(palette.mutedText)
    val isEn = uiState.appLanguage == "en"
    val family = uiState.syllabusWidgetFamily
    val density = uiState.syllabusWidgetDensity
    data class PreviewLine(val text: String, val color: Color, val isActive: Boolean = false)

    fun compact(text: String, maxChars: Int): String = if (text.length <= maxChars) text else text.take(maxChars - 3) + "..."
    fun iconToken(type: String): String {
        if (!uiState.syllabusShowIcons) return ""
        return when (type) {
            "lesson" -> "\u270E "
            "break" -> "\u231B "
            "event" -> "\u2726 "
            else -> "\u25CC "
        }
    }
    fun lessonText(subject: String, className: String): String {
        return if (className.isNotBlank()) "$className $subject" else subject
    }
    fun blendColor(first: Color, second: Color, amount: Float): Color {
        val t = amount.coerceIn(0f, 1f)
        return Color(
            red = first.red + ((second.red - first.red) * t),
            green = first.green + ((second.green - first.green) * t),
            blue = first.blue + ((second.blue - first.blue) * t),
            alpha = first.alpha + ((second.alpha - first.alpha) * t),
        )
    }
    fun neutralFlowColor(index: Int): Color {
        if (!uiState.syllabusColorizeText) return textColor
        if (!uiState.syllabusPaletteScaleEnabled) return Color(palette.accent)
        return when (index % 4) {
            0 -> blendColor(Color(palette.accent), textColor, 0.46f)
            1 -> blendColor(Color(palette.success), textColor, 0.52f)
            2 -> blendColor(Color(palette.footer), textColor, 0.38f)
            else -> blendColor(Color(palette.accent), Color(palette.success), 0.58f)
        }
    }
    fun harmonizedClassColor(seed: Color, index: Int): Color {
        if (!uiState.syllabusPaletteScaleEnabled) return seed
        val channelSeed = when {
            seed.red >= seed.green && seed.red >= seed.blue -> Color(palette.warning)
            seed.green >= seed.red && seed.green >= seed.blue -> Color(palette.success)
            else -> Color(palette.accent)
        }
        val themed = blendColor(seed, channelSeed, 0.44f)
        return blendColor(themed, neutralFlowColor(index), 0.22f)
    }
    fun applyFlowDepth(color: Color, index: Int, isSpecial: Boolean = false): Color {
        val alpha = when {
            index == 0 -> 1f
            isSpecial -> 0.96f
            index == 1 -> 0.95f
            index == 2 -> 0.91f
            else -> 0.86f
        }
        return color.copy(alpha = alpha)
    }

    val lessonA = if (uiState.syllabusShowClassColors) harmonizedClassColor(Color(0xFF7BD3EA), 0) else neutralFlowColor(0)
    val lessonB = if (uiState.syllabusShowClassColors) harmonizedClassColor(Color(0xFFFFC857), 3) else neutralFlowColor(3)
    val gapColor = blendColor(Color(palette.warning), textColor, 0.18f)
    val breakColor = if (uiState.syllabusColorizeText) neutralFlowColor(2) else mutedColor
    val flowPreviewSize = uiState.syllabusElementSizes[SyllabusWidgetElement.Flow] ?: SyllabusWidgetElement.Flow.defaultSize
    val statusPreviewSize = uiState.syllabusElementSizes[SyllabusWidgetElement.Status] ?: SyllabusWidgetElement.Status.defaultSize
    val maxRowChars = when {
        flowPreviewSize >= 24 -> if (uiState.syllabusShowTimes) 24 else 30
        flowPreviewSize >= 18 -> if (uiState.syllabusShowTimes) 30 else 38
        else -> if (uiState.syllabusShowTimes) 38 else 48
    } + when (density) {
        WidgetInfoDensity.Sparse -> -4
        WidgetInfoDensity.Balanced -> 0
        WidgetInfoDensity.Dense -> 4
    }
    val lessonWidth = 2
    val classWidth = 2
    val prefixWidth = lessonWidth + (if (uiState.syllabusShowTimes) 7 else 0) + 4 + classWidth
    val subjectWidth = (maxRowChars - prefixWidth).coerceAtLeast(12)
    fun previewFlowRow(lessonNo: String, time: String, className: String, subject: String, type: String): String {
        val subjectText = compact(iconToken(type) + subject, subjectWidth)
        val hasClass = className.isNotBlank()
        return buildString {
            append(lessonNo.padEnd(lessonWidth, ' '))
            if (uiState.syllabusShowTimes) {
                append("  ")
                append(time.padEnd(5, ' '))
            }
            if (hasClass) {
                append("  ")
                append(className.padEnd(classWidth, ' '))
            }
            append(' ')
            append(subjectText)
        }
    }
    val flowLines = buildList {
        add(
            PreviewLine(
                text = previewFlowRow("2.", "08:30", "7A", if (isEn) "Math Workshop" else "Matematik Atölyesi", "lesson"),
                color = applyFlowDepth(lessonA, 0),
                isActive = true,
            ),
        )
        add(
            PreviewLine(
                text = previewFlowRow("", "09:10", "", if (isEn) "Free Period" else "Boş Ders", "gap"),
                color = applyFlowDepth(gapColor, 1, isSpecial = true),
            ),
        )
        if (uiState.syllabusShowBreaks) {
            add(
                PreviewLine(
                    text = previewFlowRow("", "09:15", "", if (isEn) "Break" else "Teneffüs", "break"),
                    color = applyFlowDepth(breakColor, 2, isSpecial = true),
                ),
            )
        }
        add(
            PreviewLine(
                text = previewFlowRow("3.", "09:20", "7A", if (isEn) "Turkish Language" else "Türkçe ve Anlatım", "lesson"),
                color = applyFlowDepth(lessonB, 3),
            ),
        )
        if (density == WidgetInfoDensity.Dense) {
            add(
                PreviewLine(
                    text = previewFlowRow("4.", "10:10", "7A", if (isEn) "Science Lab" else "Fen Laboratuvarı", "lesson"),
                    color = applyFlowDepth(neutralFlowColor(1), 4),
                ),
            )
        }
    }
    val statusText = when (family) {
        WidgetStyleFamily.Agenda -> if (isEn) "Plan: Now • ${lessonText("Science", "7A")}" else "Plan: Şimdi • ${lessonText("Fen Bilimleri", "7A")}"
        WidgetStyleFamily.Minimal -> if (isEn) "NOW • ${lessonText("Science", "7A")}" else "ŞİMDİ • ${lessonText("Fen Bilimleri", "7A")}"
        else -> if (isEn) "Now • ${lessonText("Science", "7A")}" else "Şu an • ${lessonText("Fen Bilimleri", "7A")}"
    }
    val flowData = mapOf(
        SyllabusWidgetElement.Day to when (family) {
            WidgetStyleFamily.Agenda -> if (isEn) "Monday Agenda • 2/7" else "Pazartesi Ajanda • 2/7"
            WidgetStyleFamily.Minimal -> if (isEn) "Monday" else "Pazartesi"
            else -> if (isEn) "Monday • 2/7" else "Pazartesi • 2/7"
        },
        SyllabusWidgetElement.Status to statusText,
        SyllabusWidgetElement.Flow to flowLines.joinToString("\n") { it.text },
        SyllabusWidgetElement.Footer to if (uiState.syllabusShowBreaks) {
            when (family) {
                WidgetStyleFamily.Agenda -> if (isEn) "Planner note • 3 upcoming items" else "Ajanda notu • 3 kalan olay"
                WidgetStyleFamily.Minimal -> if (isEn) "3 upcoming items" else "3 kalan olay"
                else -> if (isEn) "3 upcoming items left" else "3 kalan olay var"
            }
        } else {
            if (isEn) "2 lessons left" else "2 kalan ders var"
        },
    )

    WidgetPreviewShell(
        brush = previewFamilyBrush(palette, family, 0.65f),
        cornerRadius = uiState.widgetCornerRadius.dp,
    ) {
        val slots = (0..3).associateWith { mutableListOf<Pair<SyllabusWidgetElement, WidgetElementPreferences>>() }
        SyllabusWidgetElement.values().forEach { element ->
            val prefs = uiState.syllabusElementPrefs[element]
                ?: WidgetElementPreferences(element.defaultVisible, element.defaultPosition, element.defaultScale)
            if (prefs.visible) {
                slots[prefs.position.coerceIn(0, 3)]?.add(element to prefs)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) { index ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    slots[index].orEmpty().forEach { (element, prefs) ->
                        if (element == SyllabusWidgetElement.Flow) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                flowLines.take(if (density == WidgetInfoDensity.Dense) 6 else 5).forEachIndexed { lineIndex, line ->
                                    val activeBackground = when (uiState.syllabusActiveHighlightStyle) {
                                        SyllabusActiveHighlightStyle.Bold -> Color.Transparent
                                        SyllabusActiveHighlightStyle.Accent -> Color.Transparent
                                        SyllabusActiveHighlightStyle.Soft -> Color(palette.accent).copy(alpha = 0.18f)
                                        SyllabusActiveHighlightStyle.Strong -> Color(palette.success).copy(alpha = 0.28f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (line.isActive) activeBackground else Color.Transparent),
                                    ) {
                                        Text(
                                            text = line.text,
                                            color = if (line.isActive && uiState.syllabusActiveHighlightStyle == SyllabusActiveHighlightStyle.Accent) Color(palette.accent) else line.color,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = if (lineIndex == 0) 4.dp else 2.dp),
                                            fontFamily = previewFontFamily(uiState.syllabusTypographyPreset),
                                            fontSize = previewSyllabusTextSize(SyllabusWidgetElement.Flow, flowPreviewSize).sp,
                                            fontWeight = if (line.isActive || lineIndex == 0) FontWeight.Bold else FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = flowData[element].orEmpty(),
                                color = when (element) {
                                    SyllabusWidgetElement.Status -> if (uiState.syllabusColorizeText) Color(palette.success) else textColor
                                    SyllabusWidgetElement.Footer -> mutedColor
                                    else -> textColor
                                },
                                style = when (element) {
                                    SyllabusWidgetElement.Status -> previewSyllabusTextStyle(element, statusPreviewSize, uiState.syllabusTypographyPreset)
                                    SyllabusWidgetElement.Day -> previewSyllabusTextStyle(element, uiState.syllabusElementSizes[SyllabusWidgetElement.Day] ?: SyllabusWidgetElement.Day.defaultSize, uiState.syllabusTypographyPreset)
                                    SyllabusWidgetElement.Footer -> previewSyllabusTextStyle(element, uiState.syllabusElementSizes[SyllabusWidgetElement.Footer] ?: SyllabusWidgetElement.Footer.defaultSize, uiState.syllabusTypographyPreset)
                                    else -> previewSyllabusTextStyle(element, flowPreviewSize, uiState.syllabusTypographyPreset)
                                },
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetPreviewShell(
    brush: Brush,
    cornerRadius: androidx.compose.ui.unit.Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(cornerRadius))
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}

@Composable
fun PreviewSlot(
    items: List<Pair<CountdownWidgetElement, WidgetElementPreferences>>,
    previewData: Map<CountdownWidgetElement, String>,
    countdownElementSizes: Map<CountdownWidgetElement, Int>,
    typography: WidgetTypographyPreset,
    textColor: Color,
    mutedColor: Color,
    chipColor: Color,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        items.forEach { (element, prefs) ->
            if (element == CountdownWidgetElement.Badge) {
                Surface(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = previewData[element].orEmpty(),
                        color = chipColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = previewCountdownTextStyle(element, countdownElementSizes[element] ?: element.defaultSize, typography),
                    )
                }
            } else {
                Text(
                    text = previewData[element].orEmpty(),
                    color = when (element) {
                        CountdownWidgetElement.Meta, CountdownWidgetElement.Next -> mutedColor
                        CountdownWidgetElement.Countdown -> Color(0xFFFFF2B2)
                        else -> textColor
                    },
                    style = previewCountdownTextStyle(element, countdownElementSizes[element] ?: element.defaultSize, typography),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun CountdownWidgetElementCard(
    title: String,
    visible: Boolean,
    positions: List<String>,
    selectedPosition: Int,
    size: Int,
    isEn: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    onPositionChange: (Int) -> Unit,
    onSizeChange: (Int) -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (isEn) {
                            if (visible) "Visible" else "Hidden"
                        } else {
                            if (visible) "Açık" else "Kapalı"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Switch(checked = visible, onCheckedChange = onVisibilityChange)
            }
            Text(if (isEn) "Position" else "Konum", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                positions.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedPosition == index,
                        onClick = { onPositionChange(index) },
                        label = { Text(label, fontSize = 12.sp) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (isEn) "Size" else "Boyut", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    size.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
            Slider(
                value = size.toFloat(),
                onValueChange = { onSizeChange(it.toInt()) },
                valueRange = 0f..50f,
            )
        }
    }
}

@Composable
fun SyllabusWidgetElementCard(
    title: String,
    visible: Boolean,
    positions: List<String>,
    selectedPosition: Int,
    size: Int,
    isEn: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    onPositionChange: (Int) -> Unit,
    onSizeChange: (Int) -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (isEn) {
                            if (visible) "Visible" else "Hidden"
                        } else {
                            if (visible) "Açık" else "Kapalı"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Switch(checked = visible, onCheckedChange = onVisibilityChange)
            }
            Text(if (isEn) "Position" else "Konum", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                positions.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedPosition == index,
                        onClick = { onPositionChange(index) },
                        label = { Text(label, fontSize = 12.sp) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (isEn) "Size" else "Boyut", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    size.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
            Slider(
                value = size.toFloat(),
                onValueChange = { onSizeChange(it.toInt()) },
                valueRange = 0f..50f,
            )
        }
    }
}

@Composable
fun WidgetElementCard(
    title: String,
    visible: Boolean,
    positions: List<String>,
    selectedPosition: Int,
    scale: WidgetElementScale,
    isEn: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    onPositionChange: (Int) -> Unit,
    onScaleChange: (WidgetElementScale) -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (isEn) {
                            if (visible) "Visible" else "Hidden"
                        } else {
                            if (visible) "Açık" else "Kapalı"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Switch(checked = visible, onCheckedChange = onVisibilityChange)
            }
            Text(if (isEn) "Position" else "Konum", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                positions.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedPosition == index,
                        onClick = { onPositionChange(index) },
                        label = { Text(label, fontSize = 12.sp) },
                    )
                }
            }
            Text(if (isEn) "Size" else "Boyut", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WidgetElementScale.values().forEach { option ->
                    FilterChip(
                        selected = scale == option,
                        onClick = { onScaleChange(option) },
                        label = {
                            Text(
                                when {
                                    isEn && option == WidgetElementScale.Small -> "Small"
                                    isEn && option == WidgetElementScale.Medium -> "Medium"
                                    isEn && option == WidgetElementScale.Large -> "Large"
                                    option == WidgetElementScale.Small -> "Küçük"
                                    option == WidgetElementScale.Medium -> "Orta"
                                    else -> "Büyük"
                                },
                                fontSize = 12.sp,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun previewTextStyle(element: Any, scale: WidgetElementScale): androidx.compose.ui.text.TextStyle {
    val base = when (element) {
        CountdownWidgetElement.Countdown -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
        CountdownWidgetElement.Title -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        CountdownWidgetElement.Badge -> MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        CountdownWidgetElement.Meta -> MaterialTheme.typography.bodySmall
        CountdownWidgetElement.Current, CountdownWidgetElement.Next -> MaterialTheme.typography.bodySmall
        SyllabusWidgetElement.Day -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        SyllabusWidgetElement.Status -> MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        SyllabusWidgetElement.Flow -> MaterialTheme.typography.bodySmall.copy(
            lineHeight = 18.sp,
            fontFamily = FontFamily.Monospace,
        )
        SyllabusWidgetElement.Footer -> MaterialTheme.typography.labelSmall
        else -> MaterialTheme.typography.bodyMedium
    }
    return base.copy(fontSize = base.fontSize * scale.textMultiplier)
}

@Composable
private fun previewCountdownTextStyle(
    element: CountdownWidgetElement,
    sliderValue: Int,
    typography: WidgetTypographyPreset,
): androidx.compose.ui.text.TextStyle {
    val fontSize = countdownElementPreviewSize(element, sliderValue).sp
    val base = when (element) {
        CountdownWidgetElement.Countdown -> MaterialTheme.typography.headlineMedium.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
        )
        CountdownWidgetElement.Title -> MaterialTheme.typography.titleMedium.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
        )
        CountdownWidgetElement.Badge -> MaterialTheme.typography.labelSmall.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
        )
        CountdownWidgetElement.Meta -> MaterialTheme.typography.bodySmall.copy(fontSize = fontSize)
        CountdownWidgetElement.Current,
        CountdownWidgetElement.Next,
        CountdownWidgetElement.Progress -> MaterialTheme.typography.bodySmall.copy(fontSize = fontSize)
    }
    return base.copy(fontFamily = previewFontFamily(typography))
}

private fun countdownElementPreviewSize(element: CountdownWidgetElement, sliderValue: Int): Int {
    val safe = sliderValue.coerceIn(0, 50)
    return when (element) {
        CountdownWidgetElement.Badge -> 9 + (safe * 0.22f).toInt()
        CountdownWidgetElement.Meta -> 10 + (safe * 0.26f).toInt()
        CountdownWidgetElement.Countdown -> 14 + (safe * 0.80f).toInt()
        CountdownWidgetElement.Title -> 11 + (safe * 0.36f).toInt()
        CountdownWidgetElement.Current,
        CountdownWidgetElement.Next -> 10 + (safe * 0.28f).toInt()
        CountdownWidgetElement.Progress -> 10 + (safe * 0.16f).toInt()
    }
}

private fun countdownProgressHeight(sliderValue: Float): androidx.compose.ui.unit.Dp {
    return (4f + (sliderValue.coerceIn(0f, 50f) * 0.26f)).dp
}

@Composable
private fun previewSyllabusTextStyle(
    element: SyllabusWidgetElement,
    sliderValue: Int,
    typography: WidgetTypographyPreset,
): androidx.compose.ui.text.TextStyle {
    val fontSize = previewSyllabusTextSize(element, sliderValue).sp
    val base = when (element) {
        SyllabusWidgetElement.Day -> MaterialTheme.typography.titleMedium.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
        )
        SyllabusWidgetElement.Status -> MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
        )
        SyllabusWidgetElement.Flow -> MaterialTheme.typography.bodySmall.copy(
            fontSize = fontSize,
            lineHeight = (fontSize.value + 2f).sp,
            fontFamily = FontFamily.Monospace,
        )
        SyllabusWidgetElement.Footer -> MaterialTheme.typography.labelSmall.copy(fontSize = fontSize)
    }
    val family = if (element == SyllabusWidgetElement.Flow && typography == WidgetTypographyPreset.Technical) {
        FontFamily.Monospace
    } else {
        previewFontFamily(typography)
    }
    return base.copy(fontFamily = family)
}

private fun previewFontFamily(typography: WidgetTypographyPreset): FontFamily {
    return when (typography) {
        WidgetTypographyPreset.Soft -> FontFamily.SansSerif
        WidgetTypographyPreset.Strong -> FontFamily.SansSerif
        WidgetTypographyPreset.Technical -> FontFamily.Monospace
        WidgetTypographyPreset.Notebook -> FontFamily.Serif
    }
}

private fun previewFamilyBrush(
    palette: com.zilagent.app.widget.WidgetPalette,
    family: WidgetStyleFamily,
    accentAlpha: Float,
): Brush {
    return when (family) {
        WidgetStyleFamily.Agenda -> Brush.linearGradient(
            listOf(
                Color(palette.chipBackground),
                Color(palette.footer).copy(alpha = 0.92f),
                Color(palette.accent).copy(alpha = accentAlpha),
            ),
        )
        WidgetStyleFamily.Minimal -> Brush.linearGradient(
            listOf(
                Color(palette.chipBackground),
                Color(palette.accent).copy(alpha = accentAlpha * 0.68f),
            ),
        )
        WidgetStyleFamily.Energetic -> Brush.linearGradient(
            listOf(
                Color(palette.accent).copy(alpha = 0.42f),
                Color(palette.chipBackground),
                Color(palette.warning).copy(alpha = accentAlpha * 0.82f),
            ),
        )
        else -> Brush.linearGradient(
            listOf(
                Color(palette.chipBackground),
                Color(palette.accent).copy(alpha = accentAlpha),
            ),
        )
    }
}

private fun previewSyllabusTextSize(
    element: SyllabusWidgetElement,
    sliderValue: Int,
): Int {
    val safe = sliderValue.coerceIn(0, 50)
    return when (element) {
        SyllabusWidgetElement.Day -> 11 + (safe * 0.34f).toInt()
        SyllabusWidgetElement.Status -> 10 + (safe * 0.30f).toInt()
        SyllabusWidgetElement.Flow -> 9 + (safe * 0.34f).toInt()
        SyllabusWidgetElement.Footer -> 9 + (safe * 0.24f).toInt()
    }
}

private fun countdownElementTitle(element: CountdownWidgetElement, isEn: Boolean): String {
    return when (element) {
        CountdownWidgetElement.Badge -> if (isEn) "Badge" else "Rota etiketi"
        CountdownWidgetElement.Meta -> if (isEn) "Meta line" else "Durum satırı"
        CountdownWidgetElement.Countdown -> if (isEn) "Countdown" else "Geri sayım"
        CountdownWidgetElement.Title -> if (isEn) "Main title" else "Ana başlık"
        CountdownWidgetElement.Current -> if (isEn) "Current event" else "Şu an olayı"
        CountdownWidgetElement.Next -> if (isEn) "Next event" else "Sıradaki olay"
        CountdownWidgetElement.Progress -> if (isEn) "Progress bar" else "İlerleme çizgisi"
    }
}

private fun syllabusElementTitle(element: SyllabusWidgetElement, isEn: Boolean): String {
    return when (element) {
        SyllabusWidgetElement.Day -> if (isEn) "Day header" else "Gün başlığı"
        SyllabusWidgetElement.Status -> if (isEn) "Status line" else "Durum satırı"
        SyllabusWidgetElement.Flow -> if (isEn) "Flow body" else "Akış gövdesi"
        SyllabusWidgetElement.Footer -> if (isEn) "Footer note" else "Alt not"
    }
}

private fun countdownPositionLabels(element: CountdownWidgetElement, isEn: Boolean): List<String> {
    return if (element == CountdownWidgetElement.Progress) {
        if (isEn) listOf("Top rail", "Bottom rail") else listOf("Üst çizgi", "Alt çizgi")
    } else {
        if (isEn) {
            listOf("Top left", "Top right", "Center left", "Center right", "Bottom left", "Bottom right")
        } else {
            listOf("Sol üst", "Sağ üst", "Sol orta", "Sağ orta", "Sol alt", "Sağ alt")
        }
    }
}

private fun syllabusPositionLabels(isEn: Boolean): List<String> {
    return if (isEn) {
        listOf("Row 1", "Row 2", "Row 3", "Row 4")
    } else {
        listOf("1. sıra", "2. sıra", "3. sıra", "4. sıra")
    }
}

@Composable
fun WidgetPreviewCard(uiState: SettingsUiState) {
    val isEn = uiState.appLanguage == "en"
    val bgColor = try { Color(android.graphics.Color.parseColor(uiState.widgetBgColor)).copy(alpha = uiState.widgetBgOpacity / 100f) } catch(e:Exception) { Color.White.copy(alpha = 0.9f) }
    val textColor = try { Color(android.graphics.Color.parseColor(uiState.widgetTextColor)) } catch(e:Exception) { Color.Black }
    val accentColor = when (uiState.widgetStylePreset) {
        1 -> textColor.copy(alpha = 0.8f)
        2 -> textColor.copy(alpha = 0.45f)
        3 -> textColor.copy(alpha = 0.6f)
        else -> textColor.copy(alpha = 0.52f)
    }
    val textAlign = when(uiState.widgetAlignment) {
        0 -> androidx.compose.ui.text.style.TextAlign.Start
        2 -> androidx.compose.ui.text.style.TextAlign.End
        else -> androidx.compose.ui.text.style.TextAlign.Center
    }

    Box(Modifier.fillMaxWidth().height(188.dp).padding(4.dp), contentAlignment = Alignment.Center) {
        val alignment = when(uiState.widgetAlignment) {
            0 -> Alignment.Start
            2 -> Alignment.End
            else -> Alignment.CenterHorizontally
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(uiState.widgetCornerRadius.dp))
                .background(bgColor)
                .border(1.dp, textColor.copy(alpha = 0.12f), RoundedCornerShape(uiState.widgetCornerRadius.dp))
        ) {
            when (uiState.widgetStylePreset) {
                1 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .background(Brush.horizontalGradient(listOf(accentColor, Color.Transparent)))
                            .align(Alignment.TopCenter),
                    )
                }
                2 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.42f)
                            .background(Brush.verticalGradient(listOf(accentColor, Color.Transparent)))
                            .align(Alignment.TopCenter),
                    )
                }
                3 -> {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomStart)
                            .blur(30.dp)
                            .background(accentColor.copy(alpha = 0.35f), CircleShape),
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(124.dp)
                            .padding(top = 8.dp, end = 6.dp)
                            .align(Alignment.TopEnd)
                            .blur(34.dp)
                            .background(accentColor.copy(alpha = 0.34f), CircleShape),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = alignment,
                verticalArrangement = Arrangement.Center,
            ) {
            val timeContent = @Composable {
                val timeText = if (uiState.showSeconds) "08:45:22" else if (isEn) "3h 8m" else "3 Sa 8 Dk"
                val countdownSize = countdownElementPreviewSize(
                    CountdownWidgetElement.Countdown,
                    uiState.countdownElementSizes[CountdownWidgetElement.Countdown] ?: CountdownWidgetElement.Countdown.defaultSize,
                )
                Text(
                    text = timeText, 
                    fontSize = countdownSize.sp, 
                    fontWeight = FontWeight.Black, 
                    color = textColor,
                    lineHeight = countdownSize.sp
                )
            }
            val labelContent = @Composable {
                val labelText = if (uiState.multilineEnabled) {
                    if (isEn) "Lesson 2\nEnds at 09:15" else "2. Ders\nBitis 09:15"
                } else {
                    if (isEn) "Lesson 2 • Ends at 09:15" else "2. Ders • Bitis 09:15"
                }
                val titleSize = countdownElementPreviewSize(
                    CountdownWidgetElement.Title,
                    uiState.countdownElementSizes[CountdownWidgetElement.Title] ?: CountdownWidgetElement.Title.defaultSize,
                )
                Text(
                    text = labelText,
                    fontSize = titleSize.sp,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 2,
                    lineHeight = (titleSize + 2).sp,
                    textAlign = textAlign,
                )
            }

            val eOrder = uiState.widgetElementOrder

            Text(
                text = if (isEn) "NOW" else "ŞİMDİ",
                color = accentColor.copy(alpha = 0.95f),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.widgetFlowDirection == 0) {
                Column(horizontalAlignment = alignment) {
                    if (eOrder == 0) {
                        timeContent()
                        Spacer(modifier = Modifier.height(uiState.widgetSpacing.dp))
                        labelContent()
                    } else {
                        labelContent()
                        Spacer(modifier = Modifier.height(uiState.widgetSpacing.dp))
                        timeContent()
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = when(uiState.widgetAlignment) {
                        0 -> Arrangement.Start
                        2 -> Arrangement.End
                        else -> Arrangement.Center
                    },
                ) {
                    if (eOrder == 0) {
                        timeContent()
                        Spacer(modifier = Modifier.width(uiState.widgetSpacing.dp))
                        labelContent()
                    } else {
                        labelContent()
                        Spacer(modifier = Modifier.width(uiState.widgetSpacing.dp))
                        timeContent()
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isEn) "Countdown is running" else "Geri sayim calisiyor",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f),
                textAlign = textAlign,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = textColor.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isEn) "Now • 2. Lesson • 08:25-09:05" else "Şu an • 2. Ders • 08:25-09:05",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.92f),
                textAlign = textAlign,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isEn) "Next • Break • 09:05-09:15" else "Sonra • Teneffüs • 09:05-09:15",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f),
                textAlign = textAlign,
            )

            if (uiState.progressBarEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(uiState.widgetBarThickness.dp).clip(CircleShape).background(textColor.copy(alpha = 0.16f))) {
                    Box(Modifier.fillMaxWidth(0.6f).fillMaxHeight().clip(CircleShape).background(accentColor.copy(alpha = 0.95f)))
                }
            }
        }
        }
    }
}

@Composable
fun HolidayAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isEn: Boolean = false,
    preset: HolidayTemplate? = null,
) {
    var name by remember { mutableStateOf(preset?.name(isEn).orEmpty()) }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var isMultiDay by remember { mutableStateOf(preset?.multiDay ?: false) }
    val context = LocalContext.current

    LaunchedEffect(preset, isEn) {
        name = preset?.name(isEn).orEmpty()
        isMultiDay = preset?.multiDay ?: false
        start = ""
        end = ""
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (isEn) "Add Holiday" else "Yeni Tatil Ekle", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                if (preset != null) {
                    Text(
                        text = if (isEn) "Template: ${preset.nameEn}" else "Şablon: ${preset.nameTr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(if (isEn) "Holiday Name" else "Tatil Adı") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isMultiDay,
                        onClick = {
                            isMultiDay = false
                            if (start.isNotEmpty()) end = start
                        },
                        label = { Text(if (isEn) "Single Day" else "Tek Gün") },
                    )
                    FilterChip(
                        selected = isMultiDay,
                        onClick = { isMultiDay = true },
                        label = { Text(if (isEn) "Multi Day" else "Çok Gün") },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            launchDatePicker(context) {
                                start = it
                                if (!isMultiDay || end.isEmpty()) end = it
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(start.ifEmpty { if (isEn) "Start" else "Başlangıç" }) }
                    if (isMultiDay) {
                        Button(onClick = { launchDatePicker(context) { end = it } }, modifier = Modifier.weight(1f)) { Text(end.ifEmpty { if (isEn) "End" else "Bitiş" }) }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "İptal") }
                    Button(
                        onClick = {
                            if (start.isNotEmpty()) {
                                val normalizedEnd = if (isMultiDay) end.ifEmpty { start } else start
                                val (safeStart, safeEnd) = if (normalizedEnd < start) normalizedEnd to start else start to normalizedEnd
                                onConfirm(
                                    safeStart,
                                    safeEnd,
                                    name.ifEmpty { if (isEn) "Custom Holiday" else "Özel Tatil" },
                                )
                                onDismiss()
                            }
                        },
                        enabled = start.isNotEmpty(),
                    ) { Text(if (isEn) "Add" else "Ekle") }
                }
            }
        }
    }
}

@Composable
fun SpecialReminderAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isEn: Boolean = false,
) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    if (isEn) "Add Special Day / Week" else "Özel Gün / Hafta Ekle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isEn) "Name" else "Ad") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { launchDatePicker(context) { start = it; if (end.isEmpty()) end = it } }, modifier = Modifier.weight(1f)) {
                        Text(start.ifEmpty { if (isEn) "Start" else "Başlangıç" })
                    }
                    Button(onClick = { launchDatePicker(context) { end = it } }, modifier = Modifier.weight(1f)) {
                        Text(end.ifEmpty { if (isEn) "End" else "Bitiş" })
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "İptal") }
                    Button(
                        onClick = {
                            if (start.isNotEmpty()) {
                                val safeEnd = end.ifEmpty { start }
                                onConfirm(
                                    name.ifBlank { if (isEn) "Custom Special Day" else "Özel Gün" },
                                    minOf(start, safeEnd),
                                    maxOf(start, safeEnd),
                                )
                            }
                        },
                        enabled = start.isNotEmpty(),
                    ) {
                        Text(if (isEn) "Add" else "Ekle")
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteManageDialog(quotes: List<com.zilagent.app.data.entity.Quote>, onDismiss: () -> Unit, onAdd: (String, String) -> Unit, onDelete: (com.zilagent.app.data.entity.Quote) -> Unit, isEn: Boolean = false) {
    var newQuote by remember { mutableStateOf("") }
    var newSource by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (isEn) "Manage Quotes" else "Sözleri Yönet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newQuote,
                        onValueChange = { newQuote = it },
                        label = { Text(if (isEn) "New Quote" else "Yeni Söz") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newSource,
                            onValueChange = { newSource = it },
                            label = { Text(if (isEn) "Source / Author" else "Kaynak / Kişi") },
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = {
                                if (newQuote.isNotBlank()) {
                                    onAdd(newQuote, newSource)
                                    newQuote = ""
                                    newSource = ""
                                }
                            },
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.Green)
                        }
                    }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    quotes.forEach { quote ->
                        val parsed = com.zilagent.app.util.QuoteConstants.parseStoredQuote(quote.content)
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(parsed.content, fontSize = 14.sp, color = Color.White)
                                if (parsed.source.isNotBlank()) {
                                    Text(
                                        parsed.source,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                    )
                                }
                            }
                            if (!quote.isSystem) IconButton(onClick = { onDelete(quote) }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f)) }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text(if (isEn) "Done" else "Tamam") }
            }
        }
    }
}


@Composable
fun ExamModeGuideDialog(onDismiss: () -> Unit, isEn: Boolean = false) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Text(if (isEn) "Guide" else "Kılavuz", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text(if (isEn) "This mode creates a one-day custom countdown. It resets automatically at midnight." else "Bu mod bugünlük özel bir geri sayım kurmanızı sağlar. Gece yarısı otomatik sıfırlanır.", color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text(if (isEn) "Got it" else "Anladım") }
            }
        }
    }
}

@Composable
fun DisclaimerDialog(onDismiss: () -> Unit, isEn: Boolean = false) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(if (isEn) "Legal Notice" else "Yasal Not", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text(if (isEn) "ZilAgent helps school workflows. Data accuracy and schedule compatibility are user's responsibility." else "ZilAgent okul süreçlerine yardımcı bir araçtır. Verilerin doğruluğu ve okul zili ile uyumu kullanıcının sorumluluğundadır.", color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text(if (isEn) "Close" else "Kapat") }
            }
        }
    }
}

@Composable
fun ResetConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, onBackupNow: () -> Unit, isEn: Boolean = false) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Text(if (isEn) "DELETE ALL" else "HER ŞEYİ SİL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Red)
                Spacer(Modifier.height(16.dp))
                Text(if (isEn) "All your data, profiles and settings will be permanently deleted. Do you want to continue?" else "Tüm verileriniz, profilleriniz ve ayarlarınız kalıcı olarak silinecek. Devam etmek istiyor musunuz?", color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(24.dp))
                Button(onClick = onBackupNow, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.6f))) { Text(if (isEn) "Backup First" else "Önce Yedekle") }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "İptal") }
                    Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text(if (isEn) "DELETE" else "SİL") }
                }
            }
        }
    }
}

