package com.zilagent.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.ui.theme.ThemePalette
import com.zilagent.app.widget.WidgetStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    onSaveComplete: () -> Unit,
    profileId: Long = -1L,
    viewModel: CreateScheduleViewModel = viewModel(
        factory = CreateScheduleViewModel.provideFactory(
            LocalContext.current.applicationContext as com.zilagent.app.ZilAgentApp,
            profileId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val themeColorName = remember { WidgetStore.getThemeColorName(context) }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val themeMode = remember { WidgetStore.getThemeMode(context) }

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) {
            onSaveComplete()
            viewModel.resetSaveComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profileId == -1L) "Yeni Program" else "Programı Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onSaveComplete) {
                        GradientIcon(Icons.Default.ArrowBack, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        ThemePalette.getPalette("Lavanta") // Trigger import if needed, but we use ZilAgentBackground
        com.zilagent.app.ui.components.ZilAgentBackground(
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Active Profile Header
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                     Column(modifier = Modifier.padding(16.dp)) {
                         Text("Aktif Profil", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                         Text(uiState.profileName, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                     }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // General Settings Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Genel Program", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Uygulanacak Günler", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val days = listOf("Tümü", "Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        ) {
                            days.forEachIndexed { index, day ->
                                val isSelected = if (index == 0) {
                                    val all = (1..7).toList()
                                    uiState.selectedDays.containsAll(all)
                                } else {
                                    uiState.selectedDays.contains(index)
                                }
                                
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onDayToggle(index) },
                                    label = { Text(day) },
                                    modifier = Modifier.padding(end = 4.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (index == 0) Color(0xFF673AB7) else if (index > 5) Color(0xFFE91E63) else Color(0xFF2196F3),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.profileName,
                            onValueChange = viewModel::onProfileNameChange,
                            label = { Text("Profil Adı (Örn: Normal)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.morningAssemblyDuration,
                            onValueChange = viewModel::onMorningAssemblyDurationChange,
                            label = { Text("Sabah Toplanma Süresi (dk)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.preBellDuration,
                            onValueChange = viewModel::onPreBellDurationChange,
                            label = { Text("Ders Öncesi Hazırlık Zili (dk)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.startTime,
                            onValueChange = { }, 
                            label = { Text("İlk Ders Saati (HH:MM)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.zilagent.app.ui.components.launchTimePicker(context, uiState.startTime) { selectedTime ->
                                        viewModel.onStartTimeChange(selectedTime)
                                    }
                                },
                            enabled = false, 
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.White,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.lessonDuration,
                            onValueChange = viewModel::onLessonDurationChange,
                            label = { Text("Ders Süresi (dk)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.breakDuration,
                            onValueChange = viewModel::onBreakDurationChange,
                            label = { Text("Teneffüs Süresi (dk)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.lessonCount,
                            onValueChange = viewModel::onLessonCountChange,
                            label = { Text("Ders Sayısı") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Öğle Arası Ayarları", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.lunchBreakAfter,
                            onValueChange = viewModel::onLunchBreakAfterChange,
                            label = { Text("Kaçıncı Dersten Sonra?") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.lunchBreakDuration,
                            onValueChange = viewModel::onLunchBreakDurationChange,
                            label = { Text("Öğle Arası Süresi (dk)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onCountdownColorEnabledChange(!uiState.countdownColorEnabled) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.countdownColorEnabled,
                                onCheckedChange = { viewModel.onCountdownColorEnabledChange(it) }
                            )
                            Text(
                                text = "Geri Sayım Renklensin (Yeşil -> Kırmızı)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::generateAndSave,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp), color = Color.White)
                    } else {
                        Text("Oluştur ve Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
