package com.zilagent.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.ui.components.*
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.ui.theme.ThemePalette
import com.zilagent.app.ui.theme.Purple80
import com.zilagent.app.ui.theme.Pink80
import com.zilagent.app.widget.WidgetStore
import kotlinx.coroutines.launch

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
    val pagerState = rememberPagerState(pageCount = { 2 })
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
                    containerColor = if (isLightTheme) Color.Black.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.05f),
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
                        text = { Text(t("Görünüm", "Appearance"), fontWeight = if(pagerState.currentPage==0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text(t("Sistem & Plan", "System & Plan"), fontWeight = if(pagerState.currentPage==1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondBoundsPageCount = 2,
                    verticalAlignment = Alignment.Top
                ) { page ->
                    if (page == 0) {
                        AppearanceTabContent(uiState, viewModel)
                    } else {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)) {
                            SystemTab(uiState, viewModel, context, onNavigateToProfiles, permissionLauncher)
                            Spacer(Modifier.height(24.dp))
                            PlanningTab(uiState, viewModel, context, 
                                onShowHoliday = { showHolidayDialog = true },
                                onShowQuote = { showQuoteDialog = true },
                                onShowExamGuide = { showExamGuideDialog = true },
                                onShowDisclaimer = { showDisclaimerDialog = true },
                                onShowReset = { showResetDialog = true },
                                onShowBackup = { }, 
                                onShowImport = { },
                                onShowQrShare = { showQrShareDialog = true },
                                onShowQrScan = { showQrScanScreen = true },
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

    if (showHolidayDialog) HolidayAddDialog(onDismiss = { showHolidayDialog = false }, onConfirm = viewModel::addHoliday, isEn = isEn)
    if (showQuoteDialog) QuoteManageDialog(uiState.quoteList, onDismiss = { showQuoteDialog = false }, onAdd = viewModel::addQuote, onDelete = viewModel::deleteQuote, isEn = isEn)
    if (showManualDialog) UserManualDialog(onDismiss = { showManualDialog = false })
    if (showExamGuideDialog) ExamModeGuideDialog(onDismiss = { showExamGuideDialog = false }, isEn = isEn)
    if (showDisclaimerDialog) DisclaimerDialog(onDismiss = { showDisclaimerDialog = false }, isEn = isEn)
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
fun AppearanceTabContent(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val isEn = uiState.appLanguage == "en"
    fun t(tr: String, en: String): String = if (isEn) en else tr
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondary = onSurface.copy(alpha = 0.68f)
    val dividerColor = onSurface.copy(alpha = 0.08f)
    var selectedWidget by remember { mutableIntStateOf(0) }
    fun canonicalTheme(name: String): String = when (name) {
        "Okyanus" -> "Ocean"
        "Orman" -> "Forest"
        "Gün Batımı" -> "Sunset"
        "Çöl" -> "Desert"
        "Kutup" -> "Polar"
        "Gece Yarısı" -> "Midnight"
        "Şeker" -> "Candy"
        "Nane" -> "Mint"
        "Lavanta" -> "Lavender"
        "Şeftali" -> "Peach"
        "Bulut" -> "Cloud"
        "Ateş" -> "Fire"
        "Güneş" -> "Sun"
        "Kiraz" -> "Cherry"
        "Elektrik" -> "Electric"
        "Asil" -> "Royal"
        else -> name
    }
    fun themeLabel(name: String): String = when (canonicalTheme(name)) {
        "Ocean" -> t("Okyanus", "Ocean")
        "Forest" -> t("Orman", "Forest")
        "Sunset" -> t("Gün Batımı", "Sunset")
        "Desert" -> t("Çöl", "Desert")
        "Polar" -> t("Kutup", "Polar")
        "Cyberpunk" -> "Cyberpunk"
        "Midnight" -> t("Gece Yarısı", "Midnight")
        "Neon Acid" -> t("Neon Asit", "Neon Acid")
        "Deep Space" -> t("Derin Uzay", "Deep Space")
        "Venom" -> t("Zehir", "Venom")
        "Candy" -> t("Şeker", "Candy")
        "Mint" -> t("Nane", "Mint")
        "Lavender" -> t("Lavanta", "Lavender")
        "Peach" -> t("Şeftali", "Peach")
        "Cloud" -> t("Bulut", "Cloud")
        "Fire" -> t("Ateş", "Fire")
        "Sun" -> t("Güneş", "Sun")
        "Cherry" -> t("Kiraz", "Cherry")
        "Electric" -> t("Elektrik", "Electric")
        "Royal" -> t("Asil", "Royal")
        else -> canonicalTheme(name)
    }

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(onSurface.copy(alpha = 0.04f))
                .padding(vertical = 16.dp)
        ) {
            WidgetPreviewCard(uiState)
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            SettingsSectionHeader(t("Uygulama Teması", "App Theme"), Icons.Default.Palette, IconGradients.Purple)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val themes = ThemePalette.getAllThemeNames()
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(themes) { theme ->
                            val isSelected = canonicalTheme(uiState.themeColorName) == canonicalTheme(theme)
                            val palette = ThemePalette.getPalette(theme)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = palette.first)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Brush.linearGradient(listOf(palette.first, palette.second)))
                                        .border(if (isSelected) 3.dp else 0.dp, onSurface, RoundedCornerShape(20.dp))
                                        .premiumClickable { viewModel.onThemeColorChange(canonicalTheme(theme)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = onSurface, modifier = Modifier.size(36.dp))
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(themeLabel(theme), fontSize = 13.sp, color = if (isSelected) onSurface else secondary, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf(t("Sistem", "System"), t("Açık", "Light"), t("Koyu", "Dark")).forEachIndexed { index, label ->
                            val isSelected = uiState.themeMode == index
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onThemeModeChange(index) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                    selectedLabelColor = onSurface,
                                    labelColor = secondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(borderColor = onSurface.copy(alpha = 0.14f))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(if (isEn) "Language" else "Dil / Language", style = MaterialTheme.typography.bodyMedium, color = onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.appLanguage == "tr",
                            onClick = { viewModel.onAppLanguageChange("tr") },
                            label = { Text("🇹🇷 Türkçe") }
                        )
                        FilterChip(
                            selected = uiState.appLanguage == "en",
                            onClick = { viewModel.onAppLanguageChange("en") },
                            label = { Text("🇬🇧 English") }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        t("Arka Plan Stili", "Background Style"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.appBackgroundMode == 0,
                            onClick = { viewModel.onAppBackgroundModeChange(0) },
                            label = { Text(t("Dinamik", "Dynamic")) }
                        )
                        FilterChip(
                            selected = uiState.appBackgroundMode == 1,
                            onClick = { viewModel.onAppBackgroundModeChange(1) },
                            label = { Text(t("Kareli Cam", "Grid Glass")) }
                        )
                        FilterChip(
                            selected = uiState.appBackgroundMode == 2,
                            onClick = { viewModel.onAppBackgroundModeChange(2) },
                            label = { Text(t("Sade", "Simple")) }
                        )
                        FilterChip(
                            selected = uiState.appBackgroundMode == 3,
                            onClick = { viewModel.onAppBackgroundModeChange(3) },
                            label = { Text(t("Aurora", "Aurora")) }
                        )
                        FilterChip(
                            selected = uiState.appBackgroundMode == 4,
                            onClick = { viewModel.onAppBackgroundModeChange(4) },
                            label = { Text(t("Parçacık", "Particles")) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionHeader(t("Widget Özelleştirme", "Widget Customization"), Icons.Default.Widgets, IconGradients.Sunset)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedWidget == 0,
                            onClick = { selectedWidget = 0 },
                            label = { Text(t("Canlı Geri Sayım", "Live Countdown")) },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        )
                        FilterChip(
                            selected = selectedWidget == 1,
                            onClick = { selectedWidget = 1 },
                            label = { Text(t("Ders Akışı", "Lesson Flow")) },
                            leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = dividerColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedWidget == 0) {
                        Text(t("Canlı Geri Sayım Ayarları", "Live Countdown Settings"), style = MaterialTheme.typography.titleSmall, color = onSurface, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsToggleRow(Icons.Default.HourglassBottom, IconGradients.Sunset, t("Saniye Göster", "Show Seconds"), t("Geri sayım saniyeli görünsün", "Show seconds in countdown"), uiState.showSeconds, viewModel::onShowSecondsChange)
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor)
                        SettingsToggleRow(Icons.Default.LinearScale, IconGradients.Green, t("İlerleme Çubuğu", "Progress Bar"), t("Alttaki dolum çubuğunu göster", "Show fill bar at bottom"), uiState.progressBarEnabled, viewModel::onProgressBarEnabledChange)
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor)
                        SettingsToggleRow(Icons.Default.ColorLens, IconGradients.Blue, t("Dinamik Renk", "Dynamic Color"), t("Süre azalınca vurgu rengi değişsin", "Change highlight as time decreases"), uiState.dynamicColorEnabled, viewModel::onDynamicColorEnabledChange)
                        Spacer(modifier = Modifier.height(8.dp))
                        SliderRow(t("Sayaç Yazı Boyutu", "Timer Text Size"), uiState.panoramicTimeTextSize.toFloat(), 20f..54f) { viewModel.onPanoramicTimeTextSizeChange(it.toInt()) }
                        SliderRow(t("Başlık Yazı Boyutu", "Title Text Size"), uiState.panoramicTitleTextSize.toFloat(), 11f..24f) { viewModel.onPanoramicTitleTextSizeChange(it.toInt()) }
                    } else {
                        Text(t("Ders Akışı Ayarları", "Lesson Flow Settings"), style = MaterialTheme.typography.titleSmall, color = onSurface, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsToggleRow(Icons.Default.HourglassBottom, IconGradients.Sunset, t("Teneffüsleri Göster", "Show Breaks"), t("Akışta teneffüs satırlarını göster", "Show break rows in flow"), uiState.syllabusShowBreaks, viewModel::onSyllabusShowBreaksChange)
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor)
                        SettingsToggleRow(Icons.Default.Schedule, IconGradients.Blue, t("Saatleri Göster", "Show Times"), t("Ders saat aralığını satırlarda göster", "Show lesson time ranges"), uiState.syllabusShowTimes, viewModel::onSyllabusShowTimesChange)
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor)
                        SettingsToggleRow(Icons.Default.FormatColorText, IconGradients.Green, t("Yazıları Renklendir", "Colorize Text"), t("Ders/teneffüs satırlarını renkli göster", "Color lesson/break rows"), uiState.syllabusColorizeText, viewModel::onSyllabusColorizeTextChange)
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor)
                        SettingsToggleRow(Icons.Default.Palette, IconGradients.Purple, t("Sınıf Renk Noktası", "Class Color Dot"), t("Sınıf rengini satır başında göster", "Show class color at line start"), uiState.syllabusShowClassColors, viewModel::onSyllabusShowClassColorsChange)
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor)
                        SettingsToggleRow(Icons.Default.EmojiEmotions, IconGradients.Sunset, t("Etkinlik Simgesi", "Event Icon"), t("Ders/teneffüs simgelerini göster", "Show lesson/break icons"), uiState.syllabusShowIcons, viewModel::onSyllabusShowIconsChange)
                        Spacer(modifier = Modifier.height(8.dp))
                        SliderRow(t("Akış Yazı Boyutu", "Flow Text Size"), uiState.syllabusFlowTextSize.toFloat(), 12f..22f) { viewModel.onSyllabusFlowTextSizeChange(it.toInt()) }
                        SliderRow(t("Durum Yazı Boyutu", "Status Text Size"), uiState.syllabusStatusTextSize.toFloat(), 12f..20f) { viewModel.onSyllabusStatusTextSizeChange(it.toInt()) }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = dividerColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(t("Ana Sayfa Efekti", "Home Screen Effect"), style = MaterialTheme.typography.titleSmall, color = onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingsToggleRow(
                        Icons.Default.AutoAwesome,
                        IconGradients.Blue,
                        t("Kaydırma Efekti", "Scroll Effect"),
                        t("Üsttekiler küçülsün, alttan gelenler büyüsün", "Top shrinks, incoming items grow"),
                        uiState.dashboardMotionEnabled,
                        viewModel::onDashboardMotionEnabledChange,
                    )
                    SliderRow(t("Efekt Gücü", "Effect Strength"), uiState.dashboardMotionStrength.toFloat(), 5f..60f) { viewModel.onDashboardMotionStrengthChange(it.toInt()) }
                    SliderRow(t("Geri Sayım Boyutu", "Countdown Size"), uiState.dashboardCountdownTextSize.toFloat(), 40f..120f) { viewModel.onDashboardCountdownTextSizeChange(it.toInt()) }
                    SliderRow(t("Kart Kenarlık Kalınlığı", "Card Border Width"), uiState.dashboardCardBorderWidth.toFloat(), 1f..8f) { viewModel.onDashboardCardBorderWidthChange(it.toInt()) }
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = dividerColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(t("Dokunma Animasyonları", "Touch Animations"), style = MaterialTheme.typography.titleSmall, color = onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingsToggleRow(
                        Icons.Default.TouchApp,
                        IconGradients.Purple,
                        t("Premium Dokunma Efekti", "Premium Touch Effect"),
                        t("Buton ve kart dokunuşlarına canlı tepki ekle", "Add lively feedback on button and card taps"),
                        uiState.touchAnimationsEnabled,
                        viewModel::onTouchAnimationsEnabledChange,
                    )
                    SliderRow(t("Animasyon Şiddeti", "Animation Intensity"), uiState.touchAnimationIntensity.toFloat(), 10f..100f) {
                        viewModel.onTouchAnimationIntensityChange(it.toInt())
                    }
                    Text(
                        t("Animasyon Stili", "Animation Style"),
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurface.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.touchAnimationStyle == 0,
                            onClick = { viewModel.onTouchAnimationStyleChange(0) },
                            label = { Text(t("Yaylı", "Spring")) }
                        )
                        FilterChip(
                            selected = uiState.touchAnimationStyle == 1,
                            onClick = { viewModel.onTouchAnimationStyleChange(1) },
                            label = { Text(t("Sıçrama", "Bounce")) }
                        )
                        FilterChip(
                            selected = uiState.touchAnimationStyle == 2,
                            onClick = { viewModel.onTouchAnimationStyleChange(2) },
                            label = { Text(t("Akıcı", "Smooth")) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
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
    onShowHoliday: () -> Unit,
    onShowQuote: () -> Unit,
    onShowExamGuide: () -> Unit,
    onShowDisclaimer: () -> Unit,
    onShowReset: () -> Unit,
    onShowBackup: () -> Unit,
    onShowImport: () -> Unit,
    onShowQrShare: () -> Unit,
    onShowQrScan: () -> Unit,
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
            Text(t("Özel Tatiller", "Custom Holidays"), style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            uiState.holidayList.forEach { holiday ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text(holiday.name, style = MaterialTheme.typography.bodyMedium, color = Color.White); Text("${holiday.startDate} - ${holiday.endDate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                    IconButton(onClick = { viewModel.deleteHoliday(holiday) }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.4f)) }
                }
            }
            TextButton(onClick = onShowHoliday, modifier = Modifier.align(Alignment.End)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text(t("Tatil Ekle", "Add Holiday")) }
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

@Composable
fun WidgetPreviewCard(uiState: SettingsUiState) {
    val isEn = uiState.appLanguage == "en"
    val bgColor = try { Color(android.graphics.Color.parseColor(uiState.widgetBgColor)).copy(alpha = uiState.widgetBgOpacity / 100f) } catch(e:Exception) { Color.White.copy(alpha = 0.9f) }
    val textColor = try { Color(android.graphics.Color.parseColor(uiState.widgetTextColor)) } catch(e:Exception) { Color.Black }
    
    Box(Modifier.fillMaxWidth().height(160.dp).padding(4.dp), contentAlignment = Alignment.Center) {
        val alignment = when(uiState.widgetAlignment) {
            0 -> Alignment.Start
            2 -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
        val arrangement = when(uiState.widgetAlignment) {
            0 -> Arrangement.Start
            2 -> Arrangement.End
            else -> Arrangement.Center
        }

        Column(
            Modifier
                .fillMaxWidth() // Changed from fixed width to fillMaxWidth
                .padding(horizontal = 16.dp) // Add horizontal padding for visual balance inside the full width container if needed, or remove to go full edge. User asked for "device edges". Let's try fillMaxWidth with small margin or no margin.
                // Re-reading request: "genişlet sağ ve solda cihaz çerçevesine sığsın" -> Fit to edges implies fillMaxWidth.
                .clip(RoundedCornerShape(uiState.widgetCornerRadius.dp))
                .background(bgColor)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(uiState.widgetCornerRadius.dp))
                .padding(16.dp),
            horizontalAlignment = alignment,
            verticalArrangement = Arrangement.Center
        ) {
            val timeContent = @Composable {
                val timeText = if (uiState.showSeconds) "08:45:22" else if (isEn) "3h 8m" else "3 Sa 8 Dk"
                Text(
                    text = timeText, 
                    fontSize = uiState.widgetTextSize.sp, 
                    fontWeight = FontWeight.Black, 
                    color = textColor,
                    lineHeight = uiState.widgetTextSize.sp
                )
            }
            val labelContent = @Composable {
                val labelText = if (uiState.multilineEnabled) {
                    if (isEn) "⏳ Lesson 2\nEnds: 09:15" else "⏳ 2. Ders\nBitiş: 09:15"
                } else {
                    if (isEn) "⏳ Lesson 2 • Ends: 09:15" else "⏳ 2. Ders • Bitiş: 09:15"
                }
                Text(
                    text = labelText,
                    fontSize = uiState.widgetLabelSize.sp,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 2,
                    lineHeight = (uiState.widgetLabelSize + 2).sp,
                    textAlign = when(uiState.widgetAlignment) {
                        0 -> androidx.compose.ui.text.style.TextAlign.Start
                        2 -> androidx.compose.ui.text.style.TextAlign.End
                        else -> androidx.compose.ui.text.style.TextAlign.Center
                    }
                )
            }

            val eOrder = uiState.widgetElementOrder

            if (uiState.widgetFlowDirection == 0) { // Dikey
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
            } else { // Yatay
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = arrangement) {
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
            
            if (uiState.progressBarEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(uiState.widgetBarThickness.dp).clip(CircleShape).background(textColor.copy(alpha = 0.2f))) {
                    Box(Modifier.fillMaxWidth(0.6f).fillMaxHeight().clip(CircleShape).background(textColor))
                }
            }
        }
    }
}

@Composable
fun HolidayAddDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit, isEn: Boolean = false) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (isEn) "Add Holiday" else "Yeni Tatil Ekle", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(if (isEn) "Holiday Name" else "Tatil Adı") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { launchDatePicker(context) { start = it; if(end.isEmpty()) end = it } }, modifier = Modifier.weight(1f)) { Text(start.ifEmpty { if (isEn) "Start" else "Başlangıç" }) }
                    Button(onClick = { launchDatePicker(context) { end = it } }, modifier = Modifier.weight(1f)) { Text(end.ifEmpty { if (isEn) "End" else "Bitiş" }) }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "İptal") }
                    Button(onClick = { if (start.isNotEmpty()) { onConfirm(start, end.ifEmpty { start }, name.ifEmpty { if (isEn) "Custom Holiday" else "Özel Tatil" }); onDismiss() } }, enabled = start.isNotEmpty()) { Text(if (isEn) "Add" else "Ekle") }
                }
            }
        }
    }
}

@Composable
fun QuoteManageDialog(quotes: List<com.zilagent.app.data.entity.Quote>, onDismiss: () -> Unit, onAdd: (String) -> Unit, onDelete: (com.zilagent.app.data.entity.Quote) -> Unit, isEn: Boolean = false) {
    var newQuote by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (isEn) "Manage Quotes" else "Sözleri Yönet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = newQuote, onValueChange = { newQuote = it }, label = { Text(if (isEn) "New Quote" else "Yeni Söz") }, modifier = Modifier.weight(1f))
                    IconButton(onClick = { if(newQuote.isNotBlank()) { onAdd(newQuote); newQuote = "" } }) { Icon(Icons.Default.Add, null, tint = Color.Green) }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    quotes.forEach { quote ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(quote.content, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.White)
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

