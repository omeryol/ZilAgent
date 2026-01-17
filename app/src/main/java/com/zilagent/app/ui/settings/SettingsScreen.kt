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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    
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
                title = { Text("Ayarlar", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        GradientIcon(Icons.Default.ArrowBack, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showManualDialog = true }) {
                        Icon(Icons.Default.HelpOutline, "Kılavuz", tint = Color.White)
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
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Color.White,
                    divider = {},
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .height(3.dp)
                                .padding(horizontal = 24.dp)
                                .background(Color.White, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("Görünüm", fontWeight = if(pagerState.currentPage==0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("Sistem & Plan", fontWeight = if(pagerState.currentPage==1) FontWeight.Bold else FontWeight.Normal) }
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

    if (showHolidayDialog) HolidayAddDialog(onDismiss = { showHolidayDialog = false }, onConfirm = viewModel::addHoliday)
    if (showQuoteDialog) QuoteManageDialog(uiState.quoteList, onDismiss = { showQuoteDialog = false }, onAdd = viewModel::addQuote, onDelete = viewModel::deleteQuote)
    if (showManualDialog) UserManualDialog(onDismiss = { showManualDialog = false })
    if (showExamGuideDialog) ExamModeGuideDialog(onDismiss = { showExamGuideDialog = false })
    if (showDisclaimerDialog) DisclaimerDialog(onDismiss = { showDisclaimerDialog = false })
    if (showDisclaimerDialog) DisclaimerDialog(onDismiss = { showDisclaimerDialog = false })
    if (showResetDialog) ResetConfirmDialog(onDismiss = { showResetDialog = false }, onConfirm = viewModel::resetAllData, onBackupNow = { exportLauncher.launch("zilagent_backup_emergency.json") })
    
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
    Column(Modifier.fillMaxSize()) {
        // Sticky Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f))
                .padding(vertical = 16.dp) // Removed horizontal padding
        ) {
            WidgetPreviewCard(uiState)
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            SettingsSectionHeader("Uygulama Teması", Icons.Default.Palette, IconGradients.Purple)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val themes = ThemePalette.getAllThemeNames() // Use the new dynamic list
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(themes) { theme ->
                            val isSelected = uiState.themeColorName == theme
                            val palette = ThemePalette.getPalette(theme)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp) // Enlarge for better visibility
                                        .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = palette.first) // Add colored shadow
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Brush.linearGradient(listOf(palette.first, palette.second)))
                                        .border(if (isSelected) 3.dp else 0.dp, Color.White, RoundedCornerShape(20.dp))
                                        .clickable { viewModel.onThemeColorChange(theme) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check, 
                                            contentDescription = null, 
                                            tint = Color.White, 
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    theme, 
                                    fontSize = 13.sp, 
                                    color = if(isSelected) Color.White else Color.White.copy(alpha = 0.6f), 
                                    fontWeight = if(isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Sistem", "Açık", "Koyu").forEachIndexed { index, label ->
                            val isSelected = uiState.themeMode == index
                            FilterChip(
                                selected = isSelected, 
                                onClick = { viewModel.onThemeModeChange(index) }, 
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White.copy(alpha = 0.2f),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color.White.copy(alpha = 0.6f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(borderColor = Color.White.copy(alpha = 0.1f))
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionHeader("Widget Özelleştirme", Icons.Default.Widgets, IconGradients.Sunset)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsToggleRow(Icons.Default.TextFields, IconGradients.Sunset, "Çok Satırlı", "Etiketi iki satıra yay", uiState.multilineEnabled, viewModel::onMultilineEnabledChange)
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
                    SettingsToggleRow(Icons.Default.FormatLineSpacing, IconGradients.Sunset, "İlerleme Çubuğu", "Alt kısımda bar göster", uiState.progressBarEnabled, viewModel::onProgressBarEnabledChange)
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
                    SettingsToggleRow(Icons.Default.ColorLens, IconGradients.Green, "Dinamik Renk", "Bitişe yakın kırmızıya dön", uiState.dynamicColorEnabled, viewModel::onDynamicColorEnabledChange)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Renk Paleti", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Arkaplan Rengi", fontSize = 12.sp, color = Color.Gray)
                    ColorPickerRow(selectedColor = uiState.widgetBgColor, onColorSelect = viewModel::onWidgetBgColorChange)
                    
                    Spacer(Modifier.height(16.dp))
                    Text("Yazı Rengi", fontSize = 12.sp, color = Color.Gray)
                    ColorPickerRow(selectedColor = uiState.widgetTextColor, onColorSelect = viewModel::onWidgetTextColorChange)

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Düzen Ayarları", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Hizalama", fontSize = 12.sp, color = Color.Gray)
                            Row {
                                IconButton(onClick = { viewModel.onWidgetAlignmentChange(0) }) { Icon(Icons.Default.FormatAlignLeft, null, tint = if(uiState.widgetAlignment==0) Color.White else Color.Gray) }
                                IconButton(onClick = { viewModel.onWidgetAlignmentChange(1) }) { Icon(Icons.Default.FormatAlignCenter, null, tint = if(uiState.widgetAlignment==1) Color.White else Color.Gray) }
                                IconButton(onClick = { viewModel.onWidgetAlignmentChange(2) }) { Icon(Icons.Default.FormatAlignRight, null, tint = if(uiState.widgetAlignment==2) Color.White else Color.Gray) }
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Akış Yönü", fontSize = 12.sp, color = Color.Gray)
                            Row {
                                IconButton(onClick = { viewModel.onWidgetFlowDirectionChange(0) }) { Icon(Icons.Default.VerticalAlignBottom, null, tint = if(uiState.widgetFlowDirection==0) Color.White else Color.Gray) }
                                IconButton(onClick = { viewModel.onWidgetFlowDirectionChange(1) }) { Icon(Icons.Default.FormatAlignJustify, null, tint = if(uiState.widgetFlowDirection==1) Color.White else Color.Gray) }
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Sıralama", fontSize = 12.sp, color = Color.Gray)
                            Row {
                                IconButton(onClick = { viewModel.onWidgetElementOrderChange(0) }) { Icon(Icons.Default.Schedule, null, tint = if(uiState.widgetElementOrder==0) Color.White else Color.Gray) }
                                IconButton(onClick = { viewModel.onWidgetElementOrderChange(1) }) { Icon(Icons.Default.ShortText, null, tint = if(uiState.widgetElementOrder==1) Color.White else Color.Gray) }
                            }
                        }
                    }
                    
                    SliderRow("Saat Yazı Boyutu", uiState.widgetTextSize.toFloat(), 12f..64f, { viewModel.onWidgetTextSizeChange(it.toInt()) })
                    SliderRow("Etiket Yazı Boyutu", uiState.widgetLabelSize.toFloat(), 8f..32f, { viewModel.onWidgetLabelSizeChange(it.toInt()) })
                    SliderRow("Eleman Boşluğu", uiState.widgetSpacing.toFloat(), 0f..100f, { viewModel.onWidgetSpacingChange(it.toInt()) })
                    SliderRow("Köşe Yuvarlaklığı", uiState.widgetCornerRadius.toFloat(), 0f..40f, { viewModel.onWidgetCornerRadiusChange(it.toInt()) })
                    SliderRow("Arkaplan Saydamlığı", uiState.widgetBgOpacity.toFloat(), 0f..100f, { viewModel.onWidgetBgOpacityChange(it.toInt()) })
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun ColorPickerRow(selectedColor: String, onColorSelect: (String) -> Unit) {
    val colors = listOf("#FFFFFF", "#111111", "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722")
    
    LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(colors) { hex ->
            val isSelected = selectedColor.uppercase() == hex.uppercase()
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(hex)))
                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable { onColorSelect(hex) }
            )
        }
    }
}

@Composable
fun SystemTab(uiState: SettingsUiState, viewModel: SettingsViewModel, context: android.content.Context, onNavigateToProfiles: () -> Unit, permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>) {
    SettingsSectionHeader("Sistem Ayarları", Icons.Default.Settings, IconGradients.Lava)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ClickableRow(Icons.Default.Person, IconGradients.Purple, "Profilleri Yönet", onNavigateToProfiles)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            SettingsToggleRow(Icons.Default.Notifications, IconGradients.Blue, "Bildirimler", "Zil vakitlerinde bildirim gönder", uiState.notificationsEnabled) { enabled ->
                if (enabled && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else viewModel.onNotificationsEnabledChange(enabled)
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            SettingsToggleRow(Icons.Default.VolumeUp, IconGradients.Green, "Sesli Zil", "Sayaç bittiğinde zil çal", uiState.soundEnabled, viewModel::onSoundEnabledChange)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            SettingsToggleRow(Icons.Default.HourglassEmpty, IconGradients.Sunset, "Saniyeyi Göster", "Sayıcıda saniyeleri göster", uiState.showSeconds, viewModel::onShowSecondsChange)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            SettingsToggleRow(Icons.Default.DoNotDisturbOn, IconGradients.Lava, "Otomatik Sessiz", "Derslerde sessize al", uiState.autoSilentMode, viewModel::onAutoSilentModeChange)
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
    SettingsSectionHeader("Sınav ve Özel Sayaç", Icons.Default.Timer, IconGradients.Blue)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Özel Sayaç", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Bugüne özel tek seferlik sayaç", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = onShowExamGuide) { Icon(Icons.Default.HelpOutline, null, tint = Color.Gray) }
                Switch(checked = uiState.customModeEnabled, onCheckedChange = viewModel::onCustomModeEnabledChange)
            }
            if (uiState.customModeEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.customModeTitle, 
                    onValueChange = viewModel::onCustomModeTitleChange, 
                    label = { Text("Sayaç Başlığı") }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.2f), focusedBorderColor = Color.White)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.customModeTime, 
                    onValueChange = {}, 
                    label = { Text("Bitiş Saati") }, 
                    modifier = Modifier.fillMaxWidth().clickable {
                        launchTimePicker(context, uiState.customModeTime.ifEmpty { "12:00" }, viewModel::onCustomModeTimeChange)
                    }, 
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color.White.copy(alpha = 0.2f), disabledTextColor = Color.White, disabledLabelColor = Color.Gray)
                )
            }
        }
    }
    
    Spacer(Modifier.height(24.dp))
    SettingsSectionHeader("Yedekleme ve Veri", Icons.Default.CloudUpload, IconGradients.Blue)
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
                Text("DOSYA OLARAK YEDEKLE", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            ClickableRow(Icons.Default.Upload, IconGradients.Blue, "Yedekten Geri Yükle", { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) })
            
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
                    Text("QR Paylaş", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                Button(
                    onClick = onShowQrScan,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64A19)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null)
                    Spacer(Modifier.width(8.dp))
                    Text("QR Tara", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))
    SettingsSectionHeader("Tatil ve Günler", Icons.Default.DateRange, IconGradients.Sunset)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Çalışma Günleri", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEachIndexed { index, label ->
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
                            .clickable {
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
            Text("Özel Tatiller", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            uiState.holidayList.forEach { holiday ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text(holiday.name, style = MaterialTheme.typography.bodyMedium, color = Color.White); Text("${holiday.startDate} - ${holiday.endDate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                    IconButton(onClick = { viewModel.deleteHoliday(holiday) }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.4f)) }
                }
            }
            TextButton(onClick = onShowHoliday, modifier = Modifier.align(Alignment.End)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Tatil Ekle") }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val quoteCount = uiState.quoteList.size
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Özlü Sözler ($quoteCount adet)", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                TextButton(onClick = onShowQuote) { Text("Yönet") }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    SettingsSectionHeader("Hakkında", Icons.Default.Info, IconGradients.Lava)
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("ZilAgent v2.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Gelişmiş Okul Zil ve Program Takip Sistemi", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            Text("Tasarım: Ömer Yolcu", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Text("Kodlama: Antigravity AI @ Gemini", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Text("İletişim: omeryol@gmail.com", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            
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
                    Text("Kaynak Kod", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://github.com/omeryol/ZilAgent/blob/main/LICENSE") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Lisans", fontSize = 11.sp)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Button(onClick = onShowDisclaimer, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))) { 
                Text("Yasal Not & Kullanım Koşulları", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp) 
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
    SettingsSectionHeader("Tehlikeli Bölge", Icons.Default.Warning, IconGradients.Lava)
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Bu alandaki işlemler geri alınamaz. Lütfen dikkatli olun.", style = MaterialTheme.typography.bodySmall, color = Color.Red.copy(alpha = 0.7f))
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onShowReset, 
                Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp)
            ) { 
                Icon(Icons.Default.DeleteForever, null)
                Spacer(Modifier.width(8.dp))
                Text("HER ŞEYİ SİL", color = Color.White, fontWeight = FontWeight.Bold) 
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector, gradient: List<Color>) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
        GradientIcon(icon, gradient, size = 32.dp, iconSize = 18.dp)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsToggleRow(icon: ImageVector, gradient: List<Color>, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        GradientIcon(icon, gradient, size = 40.dp, iconSize = 20.dp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color.White.copy(alpha = 0.4f))
        )
    }
}

@Composable
fun ClickableRow(icon: ImageVector, gradient: List<Color>, title: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        GradientIcon(icon, gradient, size = 40.dp, iconSize = 20.dp)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
    }
}

@Composable
fun SliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(Modifier.padding(vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            Text(value.toInt().toString(), style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value, 
            onValueChange = onValueChange, 
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(alpha = 0.2f))
        )
    }
}

@Composable
fun WidgetPreviewCard(uiState: SettingsUiState) {
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
                val timeText = if (uiState.showSeconds) "08:45:22" else "3 Sa 8 Dk"
                Text(
                    text = timeText, 
                    fontSize = uiState.widgetTextSize.sp, 
                    fontWeight = FontWeight.Black, 
                    color = textColor,
                    lineHeight = uiState.widgetTextSize.sp
                )
            }
            val labelContent = @Composable {
                val labelText = if (uiState.multilineEnabled) "⏳ 2. Ders\nBitiş: 09:15" else "⏳ 2. Ders • Bitiş: 09:15"
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
fun HolidayAddDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Yeni Tatil Ekle", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tatil Adı") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { launchDatePicker(context) { start = it; if(end.isEmpty()) end = it } }, modifier = Modifier.weight(1f)) { Text(start.ifEmpty { "Başlangıç" }) }
                    Button(onClick = { launchDatePicker(context) { end = it } }, modifier = Modifier.weight(1f)) { Text(end.ifEmpty { "Bitiş" }) }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Button(onClick = { if (start.isNotEmpty()) { onConfirm(start, end.ifEmpty { start }, name.ifEmpty { "Özel Tatil" }); onDismiss() } }, enabled = start.isNotEmpty()) { Text("Ekle") }
                }
            }
        }
    }
}

@Composable
fun QuoteManageDialog(quotes: List<com.zilagent.app.data.entity.Quote>, onDismiss: () -> Unit, onAdd: (String) -> Unit, onDelete: (com.zilagent.app.data.entity.Quote) -> Unit) {
    var newQuote by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Sözleri Yönet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = newQuote, onValueChange = { newQuote = it }, label = { Text("Yeni Söz") }, modifier = Modifier.weight(1f))
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
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Tamam") }
            }
        }
    }
}


@Composable
fun ExamModeGuideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Text("Kılavuz", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text("Bu mod bugünlük özel bir geri sayım kurmanızı sağlar. Gece yarısı otomatik sıfırlanır.", color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Anladım") }
            }
        }
    }
}

@Composable
fun DisclaimerDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text("Yasal Not", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text("ZilAgent okul süreçlerine yardımcı bir araçtır. Verilerin doğruluğu ve okul zili ile uyumu kullanıcının sorumluluğundadır.", color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Kapat") }
            }
        }
    }
}

@Composable
fun ResetConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, onBackupNow: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Text("HER ŞEYİ SİL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Red)
                Spacer(Modifier.height(16.dp))
                Text("Tüm verileriniz, profilleriniz ve ayarlarınız kalıcı olarak silinecek. Devam etmek istiyor musunuz?", color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(24.dp))
                Button(onClick = onBackupNow, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.6f))) { Text("Önce Yedekle") }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("SİL") }
                }
            }
        }
    }
}
