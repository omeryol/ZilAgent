package com.zilagent.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.domain.ScheduleGenerator
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.ui.components.LocalAppLanguage
import com.zilagent.app.ui.components.premiumClickable
import com.zilagent.app.ui.components.premiumTouchEffect

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
    val appLanguage = LocalAppLanguage.current
    fun trEn(tr: String, en: String): String = if (appLanguage == AppLanguage.EN) en else tr
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) {
            onSaveComplete()
            viewModel.resetSaveComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profileId == -1L) trEn("Yeni Program", "New Schedule") else trEn("Programı Düzenle", "Edit Schedule")) },
                navigationIcon = {
                    IconButton(onClick = onSaveComplete) {
                        GradientIcon(Icons.Default.ArrowBack, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
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
                         Text(trEn("Aktif Profil", "Active Profile"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                         Text(uiState.profileName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                     }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // General Settings Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(trEn("Genel Program", "General Schedule"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(trEn("Uygulanacak Günler", "Apply Days"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val days = if (appLanguage == AppLanguage.EN) listOf("All", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else listOf("Tümü", "Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
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
                            label = { Text(trEn("Profil Adı (Örn: Normal)", "Profile Name (e.g. Normal)")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.morningAssemblyDuration,
                            onValueChange = viewModel::onMorningAssemblyDurationChange,
                            label = { Text(trEn("Sabah Toplanma Süresi (dk)", "Morning Assembly (min)")) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.preBellDuration,
                            onValueChange = viewModel::onPreBellDurationChange,
                            label = { Text(trEn("Ders Öncesi Hazırlık Zili (dk)", "Pre-lesson Prep Bell (min)")) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.startTime,
                            onValueChange = { }, 
                            label = { Text(trEn("İlk Ders Saati (HH:MM)", "First Lesson Time (HH:MM)")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .premiumClickable {
                                    com.zilagent.app.ui.components.launchTimePicker(context, uiState.startTime) { selectedTime ->
                                        viewModel.onStartTimeChange(selectedTime)
                                    }
                                },
                            enabled = false, 
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.lessonDuration,
                            onValueChange = viewModel::onLessonDurationChange,
                            label = { Text(trEn("Ders Süresi (dk)", "Lesson Duration (min)")) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.breakDuration,
                            onValueChange = viewModel::onBreakDurationChange,
                            label = { Text(trEn("Teneffüs Süresi (dk)", "Break Duration (min)")) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.lessonCount,
                            onValueChange = viewModel::onLessonCountChange,
                            label = { Text(trEn("Ders Sayısı", "Lesson Count")) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(trEn("Öğle Arası Ayarları", "Lunch Break Settings"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.lunchBreakAfter,
                            onValueChange = viewModel::onLunchBreakAfterChange,
                            label = { Text(trEn("Kaçıncı Dersten Sonra?", "After Which Lesson?")) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.lunchBreakDuration,
                            onValueChange = viewModel::onLunchBreakDurationChange,
                            label = { Text(trEn("Öğle Arası Süresi (dk)", "Lunch Break Duration (min)")) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .premiumClickable { viewModel.onCountdownColorEnabledChange(!uiState.countdownColorEnabled) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.countdownColorEnabled,
                                onCheckedChange = { viewModel.onCountdownColorEnabledChange(it) }
                            )
                            Text(
                                text = trEn("Geri Sayım Renklensin (Yeşil -> Kırmızı)", "Dynamic Countdown Color (Green -> Red)"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .premiumClickable { viewModel.onLessonStartNotifyEnabledChange(!uiState.lessonStartNotifyEnabled) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.lessonStartNotifyEnabled,
                                onCheckedChange = { viewModel.onLessonStartNotifyEnabledChange(it) }
                            )
                            Text(
                                text = trEn("Ders başlangıcında bildirim gönder", "Notify at lesson start"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                WeeklyGridPreviewCard(
                    uiState = uiState,
                    isEn = appLanguage == AppLanguage.EN
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::generateAndSave,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .premiumTouchEffect(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text(trEn("Oluştur ve Kaydet", "Create and Save"), fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WeeklyGridPreviewCard(
    uiState: CreateScheduleUiState,
    isEn: Boolean
) {
    val dayLabels = if (isEn) listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else listOf("Pzt", "Sal", "Car", "Per", "Cum", "Cmt", "Paz")
    val languageCode = if (isEn) "en" else "tr"
    val preview = remember(uiState, isEn) { buildWeeklyPreview(uiState, languageCode) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isEn) "Weekly Flow Preview" else "Haftalık Akış Önizleme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                (1..7).forEach { day ->
                    val isSelected = uiState.selectedDays.contains(day)
                    val dayItems = preview[day].orEmpty()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 2.dp)
                    ) {
                        Text(
                            text = dayLabels[day - 1],
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (!isSelected) {
                            EmptyDayCard(isEn)
                        } else {
                            dayItems.forEachIndexed { index, lesson ->
                                LessonBlockCard(lesson = lesson, index = index)
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDayCard(isEn: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x26FFFFFF), shape = MaterialTheme.shapes.small)
            .padding(4.dp)
    ) {
        Text(
            text = if (isEn) "-" else "-",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 7.sp,
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun LessonBlockCard(lesson: BellSchedule, index: Int) {
    val palette = listOf(
        Color(0xFFF72585),
        Color(0xFF3A86FF),
        Color(0xFF06D6A0),
        Color(0xFFFFBE0B),
        Color(0xFF8338EC),
        Color(0xFFFB5607)
    )
    val color = palette[index % palette.size].copy(alpha = 0.85f)
    val textColor = Color(0xFF0C111A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, shape = MaterialTheme.shapes.small)
            .padding(2.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = minsToTime(lesson.startTime),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 5.sp,
                lineHeight = 5.sp,
                color = textColor.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = lesson.name,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 5.sp,
                lineHeight = 5.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = minsToTime(lesson.endTime),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 5.sp,
                lineHeight = 5.sp,
                color = textColor.copy(alpha = 0.9f),
                maxLines = 1
            )
        }
    }
}

private fun buildWeeklyPreview(
    uiState: CreateScheduleUiState,
    languageCode: String
): Map<Int, List<BellSchedule>> {
    val lessonCount = uiState.lessonCount.toIntOrNull() ?: 8
    val lessonDuration = uiState.lessonDuration.toIntOrNull() ?: 40
    val breakDuration = uiState.breakDuration.toIntOrNull() ?: 10
    val lunchAfter = uiState.lunchBreakAfter.toIntOrNull()
    val lunchDuration = uiState.lunchBreakDuration.toIntOrNull() ?: 40
    val assemblyDuration = uiState.morningAssemblyDuration.toIntOrNull() ?: 0
    val preBell = uiState.preBellDuration.toIntOrNull() ?: 0

    return (1..7).associateWith { day ->
        if (!uiState.selectedDays.contains(day)) {
            emptyList()
        } else {
            ScheduleGenerator.generateSchedule(
                profileId = 0L,
                dayOfWeek = day,
                languageCode = languageCode,
                firstLessonStart = uiState.startTime,
                lessonDurationMinutes = lessonDuration,
                breakDurationMinutes = breakDuration,
                firstBreakDurationMinutes = null,
                secondBreakDurationMinutes = null,
                lessonCount = lessonCount,
                lunchBreakAfterLesson = lunchAfter,
                lunchBreakDurationMinutes = lunchDuration,
                morningAssemblyDuration = assemblyDuration,
                preBellMinutes = preBell
            ).filter { !it.isBreak }
        }
    }
}

private fun minsToTime(mins: Int): String {
    val h = mins / 60
    val m = mins % 60
    return "%02d:%02d".format(h, m)
}


