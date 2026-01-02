package com.zilagent.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.launchTimePicker
import com.zilagent.app.ui.components.launchDatePicker
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showHolidayDialog by remember { mutableStateOf(false) }
    var showQuoteDialog by remember { mutableStateOf(false) }
    var showExamGuide by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onNotificationsEnabledChange(true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar ve Özelleştirme") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (uiState.themeMode == 2) {
                            listOf(Color(0xFF121212), Color(0xFF1E1E1E))
                        } else {
                            listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
                        }
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Görünüm ve Stil
                SettingsSectionHeader("Görünüm ve Stil", Icons.Default.Palette, IconGradients.Blue)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tema Modu", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf("Sistem" to 0, "Açık" to 1, "Koyu" to 2)
                            modes.forEach { (label, mode) ->
                                FilterChip(
                                    selected = uiState.themeMode == mode,
                                    onClick = { viewModel.onThemeModeChange(mode) },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Uygulama Renk Paleti", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        val colorPresets = listOf(
                            "Lavanta" to listOf("#E0C3FC", "#8EC5FC"),
                            "Okyanus" to listOf("#4FACFE", "#00F2FE"),
                            "Orman" to listOf("#43E97B", "#38F9D7"),
                            "Gece" to listOf("#243B55", "#141E30"),
                            "Ateş" to listOf("#F093FB", "#F5576C"),
                            "Güneş" to listOf("#F6D365", "#FDA085")
                        )
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(colorPresets) { (name, colors) ->
                                val isSelected = uiState.themeColorName == name
                                Box(
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 40.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(Brush.verticalGradient(colors.map { Color(android.graphics.Color.parseColor(it)) }))
                                        .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.White else Color.Transparent, MaterialTheme.shapes.small)
                                        .clickable { viewModel.onThemeColorChange(name) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Widget Kişiselleştirme & ÖNİZLEME
                SettingsSectionHeader("Widget Kişiselleştirme", Icons.Default.Dashboard, IconGradients.Blue)
                
                // --- WIDGET LİVE PREVİEW ---
                Text("Anlık Önizleme", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                WidgetPreviewCard(uiState)
                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Dizilim ve Konum", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.widgetFlowDirection == 0,
                                onClick = { viewModel.onWidgetFlowDirectionChange(0) },
                                label = { Text("Dikey") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.widgetFlowDirection == 1,
                                onClick = { viewModel.onWidgetFlowDirectionChange(1) },
                                label = { Text("Yatay") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Hizalama", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Sol" to 0, "Orta" to 1, "Sağ" to 2).forEach { (label, align) ->
                                FilterChip(
                                    selected = uiState.widgetAlignment == align,
                                    onClick = { viewModel.onWidgetAlignmentChange(align) },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Eleman Sırası", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.widgetElementOrder == 0,
                                onClick = { viewModel.onWidgetElementOrderChange(0) },
                                label = { Text(if (uiState.widgetFlowDirection == 0) "Saat Üstte" else "Saat Solda") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.widgetElementOrder == 1,
                                onClick = { viewModel.onWidgetElementOrderChange(1) },
                                label = { Text(if (uiState.widgetFlowDirection == 0) "Yazı Üstte" else "Yazı Solda") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

                        CustomSliderRow("Saat Metin Boyutu", uiState.widgetTextSize, 16f..120f, viewModel::onWidgetTextSizeChange, "sp")
                        CustomSliderRow("Etiket Metin Boyutu", uiState.widgetLabelSize, 8f..64f, viewModel::onWidgetLabelSizeChange, "sp")
                        CustomSliderRow("Eleman Arası Boşluk", uiState.widgetSpacing, 0f..100f, viewModel::onWidgetSpacingChange, "dp")
                        CustomSliderRow("Arka Plan Opaklığı", uiState.widgetBgOpacity, 0f..100f, viewModel::onWidgetBgOpacityChange, "%")
                        CustomSliderRow("Bar Kalınlığı", uiState.widgetBarThickness, 2f..40f, viewModel::onWidgetBarThicknessChange, "dp")
                        CustomSliderRow("Köşe Yuvarlaklığı", uiState.widgetCornerRadius, 0f..60f, viewModel::onWidgetCornerRadiusChange, "dp")

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Arka Plan Rengi", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        ColorPickerRow(uiState.widgetBgColor, viewModel::onWidgetBgColorChange, 
                            listOf("#FFFFFF", "#F5F5F5", "#E3F2FD", "#F1F8E9", "#FFF3E0", "#FFEBEE", "#F3E5F5", "#E8EAF6", "#E0F2F1", "#121212", "#1E1E1E", "#2C2C2C"))

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Metin ve Sayaç Rengi", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        ColorPickerRow(uiState.widgetTextColor, viewModel::onWidgetTextColorChange, 
                            listOf("#111111", "#FFFFFF", "#6200EE", "#3700B3", "#03DAC5", "#F44336", "#E91E63", "#9C27B0", "#2196F3", "#4CAF50", "#FF9800", "#795548"))
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                        
                        SettingsToggleRow(Icons.Default.FormatAlignLeft, IconGradients.Blue, "Çok Satırlı Metin", "Ders ve saati alt alta göster", uiState.multilineEnabled, viewModel::onMultilineEnabledChange)
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
                        SettingsToggleRow(Icons.Default.LinearScale, IconGradients.Purple, "İlerleme Çubuğu", "Widget'ta doluluk oranını göster", uiState.progressBarEnabled, viewModel::onProgressBarEnabledChange)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Sistem ve Denetim
                SettingsSectionHeader("Sistem ve Denetim", Icons.Default.Settings, IconGradients.Lava)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ClickableRow(Icons.Default.Person, IconGradients.Purple, "Profilleri Yönet", onNavigateToProfiles)
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                        
                        SettingsToggleRow(Icons.Default.Notifications, IconGradients.Blue, "Bildirimler", "Zil vakitlerinde bildirim gönder", uiState.notificationsEnabled) { enabled ->
                            if (enabled && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else viewModel.onNotificationsEnabledChange(enabled)
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
                        SettingsToggleRow(Icons.Default.VolumeUp, IconGradients.Green, "Sesli Zil", "Sayaç bittiğinde zil sesi çal", uiState.soundEnabled, viewModel::onSoundEnabledChange)
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
                        SettingsToggleRow(Icons.Default.HourglassEmpty, IconGradients.Sunset, "Saniyeyi Göster", "Sayıcıda saniyeleri göster (Daha fazla pil tüketir)", uiState.showSeconds, viewModel::onShowSecondsChange)

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
                        SettingsToggleRow(Icons.Default.DoNotDisturbOn, IconGradients.Lava, "Otomatik Sessiz Mod", "Derslerde telefonu sessize al", uiState.autoSilentMode, viewModel::onAutoSilentModeChange)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Tatil ve Planlama
                SettingsSectionHeader("Tatil ve Planlama", Icons.Default.DateRange, IconGradients.Sunset)
                
                // One-time mode guide
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Sınav & Özel Sayaç", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("Bugüne özel tek seferlik sayaç", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { showExamGuide = true }) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.Gray)
                            }
                            Switch(checked = uiState.customModeEnabled, onCheckedChange = viewModel::onCustomModeEnabledChange)
                        }

                        if (uiState.customModeEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(uiState.customModeTitle, viewModel::onCustomModeTitleChange, label = { Text("Sayaç Başlığı") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(uiState.customModeTime, {}, label = { Text("Bitiş Saati") }, modifier = Modifier.fillMaxWidth().clickable {
                                launchTimePicker(context, uiState.customModeTime.ifEmpty { "12:00" }, viewModel::onCustomModeTimeChange)
                            }, enabled = false)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Holiday management
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Çalışma Günleri", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                            days.forEachIndexed { index, label ->
                                val isActive = uiState.workingDaysMask.getOrNull(index) == '1'
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (isActive) Color(0xFF6200EE) else Color.White.copy(alpha = 0.1f))
                                            .border(if(isActive) 2.dp else 0.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                            .clickable {
                                                val newMask = uiState.workingDaysMask.toCharArray()
                                                newMask[index] = if (isActive) '0' else '1'
                                                viewModel.onWorkingDaysChange(String(newMask))
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            color = if (isActive) Color.White else Color.White.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))
                        
                        Text("Özel Tatiller", style = MaterialTheme.typography.titleSmall)
                        uiState.holidayList.forEach { holiday ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(holiday.name, style = MaterialTheme.typography.bodyMedium)
                                    val dateStr = if (holiday.startDate == holiday.endDate) holiday.startDate else "${holiday.startDate} - ${holiday.endDate}"
                                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                IconButton(onClick = { viewModel.deleteHoliday(holiday) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.4f))
                                }
                            }
                        }
                        
                        TextButton(onClick = { showHolidayDialog = true }, modifier = Modifier.align(Alignment.End)) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Tatil Ekle")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Quote management
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Düşünce & Özlü Sözler", style = MaterialTheme.typography.titleSmall)
                            Text("${uiState.quoteList.size} Kayıt", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tatil ve gün sonlarında widget'ta rastgele gösterilecek metinleri yönetin.", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        TextButton(onClick = { showQuoteDialog = true }, modifier = Modifier.align(Alignment.End)) {
                            Icon(Icons.Default.FormatQuote, null); Spacer(Modifier.width(4.dp)); Text("Sözleri Yönet")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 5. Hakkında, Yasal & Danger Zone
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                            Text("ZilAgent v1.0.5", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                            Text("Geliştirici: Ömer Yolcu", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        
                        Button(onClick = { showDisclaimerDialog = true }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))) {
                            Text("Yasal Not & Sorumluluk Reddi", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // DANGER ZONE
                Text("Tehlikeli Bölge", style = MaterialTheme.typography.labelSmall, color = Color.Red.copy(alpha = 0.6f), modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(Modifier.fillMaxWidth().border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(16.dp))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Tüm Verileri Sıfırla", color = Color.Red, fontWeight = FontWeight.Bold)
                        Text("Tüm programlar, profiller ve ayarlar kalıcı olarak silinir.", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showResetDialog = true },
                            Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                        ) {
                            Text("HER ŞEYİ SİL", color = Color.White)
                        }
                    }
                }
                
                Spacer(Modifier.height(48.dp))
            }
        }
    }

    // Dialogs
    if (showHolidayDialog) HolidayAddDialog(onDismiss = { showHolidayDialog = false }, onConfirm = viewModel::addHoliday)
    if (showQuoteDialog) QuoteManageDialog(uiState.quoteList, onDismiss = { showQuoteDialog = false }, onAdd = viewModel::addQuote, onDelete = viewModel::deleteQuote)
    if (showExamGuide) ExamModeGuideDialog { showExamGuide = false }
    if (showResetDialog) ResetConfirmDialog(onDismiss = { showResetDialog = false }, onConfirm = { viewModel.resetAllData(); showResetDialog = false })
    if (showDisclaimerDialog) DisclaimerDialog { showDisclaimerDialog = false }
}

@Composable
fun WidgetPreviewCard(uiState: SettingsUiState) {
    val bgColor = try { Color(android.graphics.Color.parseColor(uiState.widgetBgColor)).copy(alpha = uiState.widgetBgOpacity / 100f) } catch(e:Exception) { Color.White }
    val textColor = try { Color(android.graphics.Color.parseColor(uiState.widgetTextColor)) } catch(e:Exception) { Color.Black }
    
    Box(Modifier.fillMaxWidth().height(140.dp).padding(horizontal = 4.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(uiState.widgetCornerRadius.dp))
                .background(bgColor)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(uiState.widgetCornerRadius.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val timeContent = @Composable {
                val timeText = if (uiState.showSeconds) "08:45:22" else "3 Sa 8 Dk"
                Text(timeText, fontSize = (uiState.widgetTextSize * 0.7f).sp, fontWeight = FontWeight.Black, color = textColor)
            }
            val labelContent = @Composable {
                val labelText = if (uiState.multilineEnabled) "⏳ 2. Ders\nBitiş: 09:15" else "⏳ 2. Ders • Bitiş: 09:15"
                Text(
                    text = labelText,
                    fontSize = uiState.widgetLabelSize.sp,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 2,
                    lineHeight = (uiState.widgetLabelSize + 2).sp,
                    textAlign = if (uiState.widgetLayoutType > 1) androidx.compose.ui.text.style.TextAlign.Start else androidx.compose.ui.text.style.TextAlign.Center
                )
            }

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

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = if (uiState.widgetAlignment == 1) Alignment.Center else if (uiState.widgetAlignment == 0) Alignment.CenterStart else Alignment.CenterEnd) {
                if (uiState.widgetFlowDirection == 0) { // Dikey
                    Column(horizontalAlignment = alignment) {
                        if (uiState.widgetElementOrder == 0) {
                            timeContent()
                            Spacer(Modifier.height(uiState.widgetSpacing.dp))
                            labelContent()
                        } else {
                            labelContent()
                            Spacer(Modifier.height(uiState.widgetSpacing.dp))
                            timeContent()
                        }
                    }
                } else { // Yatay
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = arrangement) {
                        if (uiState.widgetElementOrder == 0) {
                            timeContent()
                            Spacer(Modifier.width(uiState.widgetSpacing.dp))
                            labelContent()
                        } else {
                            labelContent()
                            Spacer(Modifier.width(uiState.widgetSpacing.dp))
                            timeContent()
                        }
                    }
                }
            }
            
            if (uiState.progressBarEnabled) {
                Spacer(Modifier.height(8.dp))
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tatil Adı (Opsiyonel)") },
                    placeholder = { Text("Örn: Sömestr Tatili") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { launchDatePicker(context) { start = it; if(end.isEmpty()) end = it } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Başlangıç", fontSize = 10.sp, color = Color.Gray)
                            Text(start.ifEmpty { "Seç" }, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Button(
                        onClick = { launchDatePicker(context) { end = it } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Bitiş", fontSize = 10.sp, color = Color.Gray)
                            Text(end.ifEmpty { "Aynı Gün" }, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (start.isNotEmpty() && end.isNotEmpty() && start != end) {
                    Text("Tatil Aralığı: $start - $end", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (start.isNotEmpty()) {
                    Text("Tek Günlük Tatil: $start", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                
                Spacer(Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            if (start.isNotEmpty()) {
                                onConfirm(start, end.ifEmpty { start }, name.ifEmpty { "Özel Tatil" })
                                onDismiss()
                            }
                        },
                        enabled = start.isNotEmpty()
                    ) { Text("Ekle") }
                }
            }
        }
    }
}

@Composable
fun ExamModeGuideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Text("Sınav Modu Rehberi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text("• Bu mod, mevcut ders programınızı bozmadan bugünlük bir sayaç kurmanızı sağlar.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                Text("• 'Sayaç Başlığı' kısmına sınav adını girin (Örn: TYT Deneme).", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                Text("• 'Bitiş Saati'ni seçin. Widget anlık olarak bu süreyi sayacaktır.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                Text("• Gece 00:00'dan sonra bu mod otomatik olarak kapanır.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                
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
                Text("Yasal Bilgilendirme & Hakkında", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                
                Text("Sorumluluk Reddi:", fontWeight = FontWeight.Bold, color = Color.White)
                Text("ZilAgent, eğitim süreçlerine yardımcı olmak amacıyla geliştirilmiş bir araçtır. Uygulama tarafından sunulan verilerin doğruluğu ve resmi zil saatleri ile uyumluluğu konusunda son sorumluluk kullanıcıya aittir. Teknik aksaklıklar veya cihaz kısıtlamaları nedeniyle oluşabilecek gecikmelerden geliştirici sorumlu tutulamaz.", 
                    fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                
                Spacer(Modifier.height(16.dp))
                Text("Gizlilik Politikası:", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Uygulama %100 çevrimdışı (offline) çalışma prensibiyle tasarlanmıştır. Hiçbir veriniz internete aktarılmaz, analiz edilmez veya üçüncü taraflarla paylaşılmaz. Tüm profil ve ders verileriniz cihazınızın güvenli depolama alanında tutulur.", 
                    fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                
                Spacer(Modifier.height(16.dp))
                Text("Açık Kaynak & Şeffaflık:", fontWeight = FontWeight.Bold, color = Color.White)
                Text("ZilAgent, topluluk gelişimini desteklemek amacıyla açık kaynaklı bir proje olarak geliştirilmektedir. Kaynak kodları yakında resmi kanallar üzerinden paylaşılacaktır.", 
                    fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sürüm 1.0.0", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                    TextButton(onClick = onDismiss) { Text("Kapat") }
                }
            }
        }
    }
}

@Composable
fun ResetConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(16.dp))) {
            Column(Modifier.padding(24.dp)) {
                Text("KALICI SIFIRLAMA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Red)
                Spacer(Modifier.height(16.dp))
                Text("Bu işlem geri alınamaz. Tüm kayıtlı verileriniz (dersler, tatiller, ayarlar) silinecek.", color = Color.White.copy(alpha = 0.8f))
                
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İPTAL", color = Color.White) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("EMİNİM, SİL") }
                }
            }
        }
    }
}

@Composable
fun QuoteManageDialog(
    quotes: List<com.zilagent.app.data.entity.Quote>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (com.zilagent.app.data.entity.Quote) -> Unit
) {
    var newQuote by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Özlü Sözleri Yönet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newQuote,
                        onValueChange = { newQuote = it },
                        label = { Text("Yeni Söz Ekle", color = Color.White.copy(alpha = 0.6f)) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { if(newQuote.isNotBlank()) { onAdd(newQuote); newQuote = "" } }) {
                        Icon(Icons.Default.Add, null, tint = Color.Green)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    quotes.forEach { quote ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(quote.content, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                            if (!quote.isSystem) {
                                IconButton(onClick = { onDelete(quote) }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                                }
                            } else {
                                Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(16.dp).padding(8.dp))
                            }
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Tamam") }
            }
        }
    }
}

@Composable
fun ClickableRow(icon: androidx.compose.ui.graphics.vector.ImageVector, gradient: List<Color>, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GradientIcon(icon, gradient, size = 32.dp, iconSize = 16.dp)
            Spacer(Modifier.width(12.dp)); Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        Icon(Icons.Default.KeyboardArrowRight, null)
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, gradient: List<Color>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
    ) {
        GradientIcon(icon, gradient, size = 32.dp, iconSize = 16.dp)
        Spacer(Modifier.width(12.dp)); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun SettingsToggleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, gradient: List<Color>, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            GradientIcon(icon, gradient, size = 32.dp, iconSize = 16.dp)
            Spacer(Modifier.width(12.dp))
            Column { Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
        }
        Switch(checked, onCheckedChange)
    }
}

@Composable
fun CustomSliderRow(label: String, value: Int, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Int) -> Unit, unit: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text("$label: $value$unit", style = MaterialTheme.typography.bodyMedium, color = Color.White)
        Slider(value.toFloat(), { onValueChange(it.toInt()) }, valueRange = valueRange)
    }
}

@Composable
fun ColorPickerRow(selectedColorHex: String, onColorSelected: (String) -> Unit, colors: List<String>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(colors) { hex ->
            val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
            Box(Modifier.size(36.dp).clip(CircleShape).background(color).border(if (selectedColorHex.equals(hex, true)) 3.dp else 1.dp, if (selectedColorHex.equals(hex, true)) Color.White else Color.Gray.copy(alpha = 0.5f), CircleShape).clickable { onColorSelected(hex) })
        }
    }
}
