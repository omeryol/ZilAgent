package com.zilagent.app.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zilagent.app.R
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.ui.components.LocalAppLanguage
import com.zilagent.app.ui.components.UserManualDialog
import com.zilagent.app.ui.components.ZilAgentBackground
import com.zilagent.app.ui.components.premiumClickable
import com.zilagent.app.ui.components.premiumTouchEffect
import com.zilagent.app.ui.theme.ThemePalette
import com.zilagent.app.util.TimeUtils
import com.zilagent.app.widget.WidgetStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCreate: (Long) -> Unit,
    onNavigateToExamMode: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val appLanguage = LocalAppLanguage.current
    val context = LocalContext.current
    fun trEn(tr: String, en: String): String = if (appLanguage == AppLanguage.EN) en else tr
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondary = onSurface.copy(alpha = 0.72f)
    val themePair = ThemePalette.getPalette(WidgetStore.getThemeColorName(context))
    val dashboardAccent = themePair.first
    val editingItem = remember { mutableStateOf<BellSchedule?>(null) }
    val todayListState = rememberLazyListState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    var showManual by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = selectedTabIndex != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    if (showManual) {
        UserManualDialog(onDismiss = { showManual = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ZilAgentBackground(modifier = Modifier.fillMaxSize()) {}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ZilAgent",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = onSurface,
                        )
                        Text(
                            text = "${trEn("Profil", "Profile")}: ${uiState.currentProfile?.name ?: trEn("Varsayılan", "Default")}",
                            style = MaterialTheme.typography.labelMedium,
                            color = secondary,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = { showManual = true }) {
                            GradientIcon(Icons.Default.HelpOutline, IconGradients.Blue, size = 40.dp, iconSize = 20.dp)
                        }
                        IconButton(onClick = onNavigateToProfiles) {
                            GradientIcon(Icons.Default.Groups, IconGradients.Purple, size = 40.dp, iconSize = 20.dp)
                        }
                        IconButton(onClick = onNavigateToExamMode) {
                            GradientIcon(Icons.Default.Timer, IconGradients.Blue, size = 40.dp, iconSize = 20.dp)
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            GradientIcon(Icons.Default.Settings, IconGradients.Lava, size = 40.dp, iconSize = 20.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = onSurface,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = dashboardAccent,
                        )
                    }
                },
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(trEn("Bugün", "Today"), fontWeight = FontWeight.Bold) },
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(trEn("Ders Programı", "Syllabus"), fontWeight = FontWeight.Bold) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                beyondBoundsPageCount = 1,
            ) { page ->
                if (page == 0) {
                    Column {
                        TodayContent(
                            uiState = uiState,
                            editingItem = editingItem,
                            listState = todayListState,
                            onNavigateToCreate = { onNavigateToCreate(uiState.currentProfile?.id ?: -1L) },
                            hasProfile = uiState.currentProfile != null
                        )
                    }
                } else {
                    com.zilagent.app.ui.syllabus.SyllabusTabView()
                }
            }
        }

        if (editingItem.value != null) {
            EditScheduleDialog(
                item = editingItem.value!!,
                onDismiss = { editingItem.value = null },
                onConfirm = { newStart, newDuration, notifyStart, notifyEnd ->
                    viewModel.updateItem(editingItem.value!!, newStart, newDuration, notifyStart, notifyEnd)
                    editingItem.value = null
                },
            )
        }

    }
}

@Composable
fun ColumnScope.TodayContent(
    uiState: DashboardUiState,
    editingItem: MutableState<BellSchedule?>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onNavigateToCreate: () -> Unit,
    hasProfile: Boolean,
) {
    val context = LocalContext.current
    val appLanguage = LocalAppLanguage.current
    fun trEn(tr: String, en: String): String = if (appLanguage == AppLanguage.EN) en else tr
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondary = onSurface.copy(alpha = 0.7f)
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val themePair = ThemePalette.getPalette(WidgetStore.getThemeColorName(context))
    val dashboardAccent = themePair.first

    val motionEnabled = WidgetStore.isDashboardMotionEnabled(context)
    val motionStrength = WidgetStore.getDashboardMotionStrength(context) / 100f
    val countdownTextSize = WidgetStore.getDashboardCountdownTextSize(context)
    val scrollPx = (listState.firstVisibleItemIndex * 240 + listState.firstVisibleItemScrollOffset).toFloat()
    val collapse = (scrollPx / 460f).coerceIn(0f, 1f)
    val headerScale = if (motionEnabled) (1f - collapse * (0.44f * motionStrength)).coerceIn(0.74f, 1f) else 1f
    val headerAlpha = if (motionEnabled) (1f - collapse * (0.42f * motionStrength)).coerceIn(0.62f, 1f) else 1f

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(headerScale)
            .alpha(headerAlpha),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val localizedStatus = localizeStatus(uiState.currentStatusText, appLanguage)
            if (uiState.isEndOfDay && localizedStatus.contains(" - ")) {
                val parts = localizedStatus.split(" - ", limit = 2)
                Text(
                    text = parts[0],
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "- ${parts[1]}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = secondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                )
            } else {
                Text(
                    text = localizedStatus,
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            val isDynamicColor = WidgetStore.isDynamicColorEnabled(context)
            val countdownColor = if (isDynamicColor && uiState.secondsRemaining > 0) {
                Color(TimeUtils.getCountdownColor(uiState.secondsRemaining))
            } else {
                dashboardAccent
            }

            if (uiState.secondsRemaining > 0) {
                Text(
                    text = TimeUtils.formatCountdown(uiState.secondsRemaining),
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = countdownTextSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = countdownColor,
                    fontFamily = FontFamily.Monospace,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                uiState.nextBell?.let {
                    Text(
                        text = "${trEn("Bitiş", "Ends")}: ${TimeUtils.minutesToTime(it.endTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondary,
                    )
                }
                // Progress bar: only when we are IN an active event (activeItemId != null)
                if (uiState.activeItemId != null) {
                    uiState.nextBell?.let { activeEvent ->
                        val totalDurationSec = (activeEvent.endTime - activeEvent.startTime) * 60f
                        val rawProgress = if (totalDurationSec > 0f) {
                            1f - (uiState.secondsRemaining / totalDurationSec)
                        } else 0f
                        val animatedProgress by animateFloatAsState(
                            targetValue = rawProgress.coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 800),
                            label = "lesson_progress",
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = animatedProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = countdownColor,
                            trackColor = countdownColor.copy(alpha = 0.18f),
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TodaySummaryCard(
        uiState = uiState,
        trEn = ::trEn,
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (uiState.schedule.isEmpty()) {
        val showCreateCta = !hasProfile || !uiState.hasAnyScheduleForProfile
        val emptyTitle = when {
            !hasProfile -> trEn("Henüz bir profil oluşturulmamış", "No profile created yet")
            uiState.hasAnyScheduleForProfile -> trEn("Bugün ders yok", "No lessons today")
            else -> trEn("Bu profil için henüz ders programı yok", "No schedule for this profile yet")
        }
        val emptyBody = when {
            !hasProfile -> trEn(
                "Profil ve program oluşturmak için aşağıdaki düğmeyi kullan.",
                "Use the button below to create a profile and schedule.",
            )
            uiState.hasAnyScheduleForProfile -> trEn(
                "Seçili profilde bugün için tanımlı ders bulunmuyor.",
                "There are no lessons defined for today in the selected profile.",
            )
            else -> trEn(
                "Bu profile ders programı eklemek için aşağıdaki düğmeyi kullan.",
                "Use the button below to add a schedule to this profile.",
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_animation))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(220.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = emptyTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = secondary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = emptyBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                if (showCreateCta) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToCreate,
                        modifier = Modifier.premiumTouchEffect(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = dashboardAccent.copy(alpha = if (isLightTheme) 0.18f else 0.24f),
                            contentColor = onSurface,
                        ),
                    ) {
                        Text(
                            text = if (hasProfile) {
                                trEn("Ders programı ekle", "Add schedule")
                            } else {
                                trEn("Profil ve program oluştur", "Create profile and schedule")
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            itemsIndexed(uiState.schedule) { index, item ->
                val isActive = uiState.activeItemId == item.id
                val isLast = index == uiState.schedule.size - 1
                val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                val viewportStart = listState.layoutInfo.viewportStartOffset
                val viewportEnd = listState.layoutInfo.viewportEndOffset
                val viewportCenter = (viewportStart + viewportEnd) / 2f
                val itemCenter = itemInfo?.let { it.offset + (it.size / 2f) } ?: viewportCenter
                val viewportSize = (viewportEnd - viewportStart).toFloat().let { if (it <= 0f) 1f else it }
                val normalized = ((itemCenter - viewportCenter) / viewportSize).coerceIn(-0.5f, 0.5f)
                val distance = kotlin.math.abs(normalized)
                val dynamicScale = if (motionEnabled) {
                    (1f - distance * (0.24f * motionStrength)).coerceIn(0.9f, 1f)
                } else {
                    1f
                }
                val dynamicAlpha = if (motionEnabled) {
                    (1f - distance * (0.34f * motionStrength)).coerceIn(0.76f, 1f)
                } else {
                    1f
                }
                ScheduleItemRow(
                    item = item,
                    isActive = isActive,
                    isLast = isLast,
                    onClick = { editingItem.value = item },
                    isLightTheme = isLightTheme,
                    accentColor = if (!item.isBreak) {
                        val lessonColors = listOf(
                            dashboardAccent,
                            Color(0xFF6366F1), // indigo
                            Color(0xFF0EA5E9), // sky
                            Color(0xFF10B981), // emerald
                            Color(0xFFF59E0B), // amber
                            Color(0xFFEC4899), // pink
                            Color(0xFF8B5CF6), // violet
                            Color(0xFF14B8A6), // teal
                        )
                        val lessonIndex = uiState.schedule.take(index + 1).count { !it.isBreak } - 1
                        lessonColors[lessonIndex.coerceIn(0, lessonColors.size - 1)]
                    } else null,
                    modifier = Modifier
                        .scale(dynamicScale)
                        .alpha(dynamicAlpha),
                )
            }
        }
    }
}

@Composable
private fun TodaySummaryCard(
    uiState: DashboardUiState,
    trEn: (String, String) -> String,
) {
    val summary = uiState.summary
    if (summary.totalLessons <= 0 && summary.totalBreaks <= 0 && summary.dayWindow.isBlank()) return

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryPill(
                modifier = Modifier.weight(1f),
                text = "${summary.completedLessons}/${summary.totalLessons} ${trEn("ders", "lessons")}",
            )
            SummaryPill(
                modifier = Modifier.weight(1f),
                text = "${summary.remainingLessons} ${trEn("kalan", "left")}",
            )
            SummaryPill(
                modifier = Modifier.weight(1f),
                text = "${summary.totalBreaks} ${trEn("teneffüs", "breaks")}",
            )
            if (summary.dayWindow.isNotBlank()) {
                SummaryPill(
                    modifier = Modifier.weight(1.15f),
                    text = summary.dayWindow,
                )
            }
        }
    }
}

@Composable
private fun SummaryPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                shape = MaterialTheme.shapes.medium,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

private fun localizeStatus(raw: String, lang: AppLanguage): String {
    if (lang == AppLanguage.TR) return raw
    return raw
        .replace("Bugün ders yok", "No lessons today")
        .replace("Bu profil için henüz ders programı yok", "No schedule for this profile yet")
        .replace("Profil Oluşturuluyor...", "Creating profile...")
        .replace("Sıradaki:", "Next:")
        .replace("Gün Bitti", "Day Finished")
        .replace("Profil Oluşturuluyor...", "Creating profile...")
        .replace("Şu an aktif ders yok", "No active lesson right now")
}

@Composable
fun ScheduleItemRow(
    item: BellSchedule,
    isActive: Boolean,
    isLast: Boolean = false,
    onClick: () -> Unit,
    isLightTheme: Boolean = false,
    accentColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appLanguage = LocalAppLanguage.current
    fun trEn(tr: String, en: String): String = if (appLanguage == AppLanguage.EN) en else tr
    val themePair = ThemePalette.getPalette(WidgetStore.getThemeColorName(context))
    val dashboardAccent = themePair.first
    val dashboardAccentAlt = themePair.second

    val backgroundColor = when {
        isActive && item.isBreak -> dashboardAccentAlt.copy(alpha = if (isLightTheme) 0.24f else 0.30f)
        isActive -> dashboardAccent.copy(alpha = if (isLightTheme) 0.18f else 0.24f)
        item.isBreak -> dashboardAccentAlt.copy(alpha = if (isLightTheme) 0.12f else 0.16f)
        else -> if (isLightTheme) Color.White.copy(alpha = 0.64f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    }

    val textColor = if (isLightTheme) Color(0xFF0F172A) else Color.White
    val subTextColor = if (isLightTheme) Color(0xFF334155) else Color.White.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, MaterialTheme.shapes.small)
            .premiumClickable { onClick() },
    ) {
        // Colored left accent strip
        if (accentColor != null && !item.isBreak) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .align(Alignment.CenterStart)
                    .background(color = accentColor.copy(alpha = if (isActive) 0.9f else 0.5f)),
            )
        }
        Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (accentColor != null && !item.isBreak) 20.dp else 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = if (isLast) "${item.name} (${trEn("Son Ders", "Last Lesson")})" else item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                color = textColor,
            )
            Text(
                text = if (item.isBreak) trEn("Teneffüs", "Break") else trEn("Ders", "Lesson"),
                style = MaterialTheme.typography.labelSmall,
                color = subTextColor,
            )
        }
        Text(
            text = "${TimeUtils.minutesToTime(item.startTime)} - ${TimeUtils.minutesToTime(item.endTime)} (${item.endTime - item.startTime} ${trEn("dk", "min")})",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
    }
}

