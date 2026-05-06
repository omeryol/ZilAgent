package com.zilagent.app.ui.exam

import android.app.TimePickerDialog
import android.content.res.Configuration
import android.text.format.DateFormat
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.LocalAppLanguage
import com.zilagent.app.ui.components.MorphingBackground
import com.zilagent.app.util.TimeUtils
import java.time.LocalTime
import java.util.Calendar

@Composable
fun ExamModeScreen(
    onClose: () -> Unit,
    viewModel: ExamViewModel = viewModel(factory = ExamViewModel.Factory),
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isEn = LocalAppLanguage.current == AppLanguage.EN
    fun t(tr: String, en: String): String = if (isEn) en else tr

    val endTimeHourInput by viewModel.endTimeHourInput.collectAsState()
    val endTimeMinuteInput by viewModel.endTimeMinuteInput.collectAsState()
    val butterflySessions by viewModel.butterflySessions.collectAsState()
    val examDurationMinutes by viewModel.examDurationMinutes.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val activeGroups by viewModel.activeGroups.collectAsState()

    var clockScale by remember { mutableStateOf(1.0f) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    BackHandler { onClose() }

    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(isLandscape) {
        val activity = context as? android.app.Activity
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        if (window != null && controller != null) {
            if (isLandscape) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            if (window != null && controller != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val totalSeconds = examDurationMinutes * 60L
    val remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
    val progress = if (totalSeconds > 0) elapsedSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val remainingText = TimeUtils.formatCountdown(remainingSeconds)
    val selectedHour = endTimeHourInput.toIntOrNull()?.coerceIn(0, 23) ?: 0
    val selectedMinute = endTimeMinuteInput.toIntOrNull()?.coerceIn(0, 59) ?: 0
    val use24Hour = DateFormat.is24HourFormat(context)
    val selectedEndTimeText = remember(selectedHour, selectedMinute, use24Hour) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
        }
        DateFormat.getTimeFormat(context).format(calendar.time)
    }
    fun displayTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return DateFormat.getTimeFormat(context).format(calendar.time)
    }

    val breakSuggestions = if (isEn) {
        listOf(
            "Take a deep breath and relax your shoulders.",
            "Rest your eyes by focusing on a distant point for 20 seconds.",
            "Take a sip of water and refresh your mind.",
            "Make sure you sit upright and correct your posture.",
            "Focus only on your breath for a short moment.",
        )
    } else {
        listOf(
            "Derin bir nefes al ve omuzlarını serbest bırak.",
            "Gözlerini 20 saniye boyunca uzağa odaklayarak dinlendir.",
            "Suyundan bir yudum al, zihnini tazele.",
            "Dik oturduğundan emin ol, duruşunu düzelt.",
            "Kısa bir süre için sadece nefesine odaklan.",
        )
    }
    val currentBreakSuggestion = remember(remainingSeconds / 300) { breakSuggestions.random() }

    val readingDuration = (examDurationMinutes * 0.1f * 60).toLong().coerceAtMost(300L)
    val closingDuration = (examDurationMinutes * 0.1f * 60).toLong().coerceAtMost(600L)

    val phaseSetup = t("Sınav Hazırlığı", "Exam Setup")
    val phaseReading = t("Okuma Süresi", "Reading Time")
    val phaseClosing = t("Son Uyarılar", "Final Warnings")
    val phaseOngoing = t("Sınav Devam Ediyor", "Exam In Progress")
    val isSetupState = !isRunning && elapsedSeconds == 0L

    val currentPhase = when {
        isSetupState -> phaseSetup
        elapsedSeconds < readingDuration -> phaseReading
        elapsedSeconds > (totalSeconds - closingDuration) -> phaseClosing
        else -> phaseOngoing
    }

    val palette = when (currentPhase) {
        phaseSetup -> Color(0xFF121212) to Color(0xFF1E1E1E)
        phaseReading -> Color(0xFF1E88E5) to Color(0xFF1565C0)
        phaseClosing -> Color(0xFFF4511E) to Color(0xFFBF360C)
        else -> Color(0xFF2E7D32) to Color(0xFF1B5E20)
    }

    val progressColor = when (currentPhase) {
        phaseReading -> Color(0xFFBBDEFB)
        phaseClosing -> Color(0xFFFFCCBC)
        else -> Color(0xFFC8E6C9)
    }
    val setupScrollState = rememberScrollState()
    val currentSetupTime by produceState(initialValue = LocalTime.now(), isSetupState) {
        if (!isSetupState) return@produceState
        while (true) {
            value = LocalTime.now()
            delay(30_000)
        }
    }
    val activeGroupCountdowns = remember(activeGroups, butterflySessions, elapsedSeconds) {
        viewModel.getActiveGroupCountdowns(elapsedSeconds)
    }
    val pendingStartCountdowns = remember(activeGroups, butterflySessions, elapsedSeconds) {
        viewModel.getPendingGroupStartCountdowns(elapsedSeconds)
    }
    val isUnifiedCountdown = pendingStartCountdowns.isEmpty() && activeGroupCountdowns
        .map { it.remainingSeconds }
        .distinct()
        .size <= 1 && activeGroupCountdowns.isNotEmpty()
    val displayedRemainingText = if (isRunning && isUnifiedCountdown && activeGroupCountdowns.isNotEmpty()) {
        TimeUtils.formatCountdown(activeGroupCountdowns.first().remainingSeconds)
    } else {
        remainingText
    }
    val highlightedDelayedGroup = pendingStartCountdowns.firstOrNull()
    val shouldBlinkDelayedGroup = highlightedDelayedGroup != null && isRunning
    val delayedGroupSeconds = highlightedDelayedGroup?.secondsUntilStart ?: 0L
    val blinkDurationMs = when {
        delayedGroupSeconds <= 30L -> 280
        delayedGroupSeconds <= 120L -> 450
        else -> 700
    }
    val blinkTransition = rememberInfiniteTransition(label = "near_end_blink")
    val blinkAlpha by blinkTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = blinkDurationMs),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "near_end_blink_alpha",
    )
    val delayedBlinkAlpha = if (shouldBlinkDelayedGroup) blinkAlpha else 1f

    Box(modifier = Modifier.fillMaxSize()) {
        MorphingBackground(colorPalette = palette)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )

        if (!isLandscape) {
            IconButton(
                onClick = { onClose() },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = t("Kapat", "Close"), tint = Color.White)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .then(if (isSetupState) Modifier.verticalScroll(setupScrollState) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isSetupState) Arrangement.Top else Arrangement.Center,
            ) {
                Text(
                    text = currentPhase.uppercase(),
                    style = if (isSetupState) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isSetupState) {
                    val quickAccent = Color(0xFF7E57C2)
                    val sessionAccent = Color(0xFFFFCA28)
                    val compactButtonTextSize = 11.sp
                    val panelBg = Color.Black.copy(alpha = 0.18f)
                    val setupObjectMinHeight = 78.dp

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            SectionHeader(
                                title = t("Hızlı Başlat", "Quick Start"),
                                subtitle = t("Dakika ve bitiş saati birlikte güncellenir", "Minutes and end time stay in sync"),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(quickAccent.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                                        .border(1.dp, quickAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                        .heightIn(min = setupObjectMinHeight)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(t("Dakika", "Minutes"), color = quickAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RepeatingIconButton(
                                            onStep = { viewModel.decreaseDuration() },
                                            modifier = Modifier.size(28.dp),
                                        ) {
                                            Icon(Icons.Default.Remove, null, tint = Color.White)
                                        }
                                        Text(
                                            text = t("${examDurationMinutes} dk", "${examDurationMinutes} min"),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                        )
                                        RepeatingIconButton(
                                            onStep = { viewModel.increaseDuration() },
                                            modifier = Modifier.size(28.dp),
                                        ) {
                                            Icon(Icons.Default.Add, null, tint = Color.White)
                                        }
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(quickAccent.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                                        .border(1.dp, quickAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                        .heightIn(min = setupObjectMinHeight)
                                        .clickable {
                                            val initialHour = endTimeHourInput.toIntOrNull()?.coerceIn(0, 23) ?: 8
                                            val initialMinute = endTimeMinuteInput.toIntOrNull()?.coerceIn(0, 59) ?: 0
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, minute -> viewModel.setEndTime(hourOfDay, minute) },
                                                initialHour,
                                                initialMinute,
                                                use24Hour,
                                            ).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(t("Bitiş", "Ends"), color = quickAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = selectedEndTimeText,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.startFromMinutesInput() },
                                modifier = Modifier.fillMaxWidth().height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = quickAccent),
                            ) {
                                Text(t("Şimdi Başlat", "Start Now"), color = Color.White, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.resetAndAlignToNow() },
                                modifier = Modifier.fillMaxWidth().height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)),
                            ) {
                                Text(t("Şimdiye Ayarla", "Align Now"), color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    butterflySessions.forEachIndexed { sessionIndex, session ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CompactEditableField(
                                        value = session.name,
                                        onValueChange = { viewModel.updateButterflySessionName(sessionIndex, it) },
                                        label = t("Oturum", "Session"),
                                        modifier = Modifier.weight(0.45f),
                                        accent = sessionAccent,
                                    )
                                    Column(
                                        modifier = Modifier
                                            .background(sessionAccent.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                                            .border(1.dp, sessionAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                            .heightIn(min = 56.dp)
                                            .weight(1f)
                                            .clickable {
                                                TimePickerDialog(
                                                    context,
                                                    { _, hourOfDay, minute -> viewModel.setButterflySessionEndTime(sessionIndex, hourOfDay, minute) },
                                                    session.endHour,
                                                    session.endMinute,
                                                    use24Hour,
                                                ).show()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(t("Ortak Bitiş", "Shared End"), color = sessionAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(displayTime(session.endHour, session.endMinute), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                session.groups.forEachIndexed { groupIndex, group ->
                                    val status = viewModel.getButterflyStatus(sessionIndex, groupIndex, currentSetupTime)
                                    val effectiveDuration = viewModel.getButterflyEffectiveDuration(sessionIndex, groupIndex) ?: 0
                                    val isGroupActive = ExamViewModel.ActiveGroupKey(sessionIndex, groupIndex) in activeGroups
                                    ButterflyGroupCard(
                                        name = group.name,
                                        statusText = if (isGroupActive) t("Aktif", "Active") else status.label,
                                        statusHighlighted = isGroupActive || status.isActionDue,
                                        startText = displayTime(group.startHour, group.startMinute),
                                        durationMinutes = effectiveDuration,
                                        manualDuration = group.manualDurationMinutes?.toString().orEmpty(),
                                        sessionAccent = sessionAccent,
                                        panelBg = panelBg,
                                        compactButtonTextSize = compactButtonTextSize,
                                        groupLabel = t("Grup", "Group"),
                                        startLabel = t("Başlangıç", "Start"),
                                        durationLabel = t("Süre", "Duration"),
                                        launchButtonText = t("Başlat", "Start"),
                                        showLaunchButton = false,
                                        modifier = Modifier.fillMaxWidth(),
                                        onNameChange = { viewModel.updateButterflyGroupName(sessionIndex, groupIndex, it) },
                                        onManualDurationChange = { viewModel.updateButterflyGroupManualDuration(sessionIndex, groupIndex, it) },
                                        onDecreaseDuration = {
                                            val current = (group.manualDurationMinutes ?: effectiveDuration).coerceAtLeast(1)
                                            val updated = (current - 1).coerceAtLeast(1)
                                            viewModel.updateButterflyGroupManualDuration(sessionIndex, groupIndex, updated.toString())
                                        },
                                        onIncreaseDuration = {
                                            val current = (group.manualDurationMinutes ?: effectiveDuration).coerceAtLeast(1)
                                            val updated = (current + 1).coerceAtMost(1440)
                                            viewModel.updateButterflyGroupManualDuration(sessionIndex, groupIndex, updated.toString())
                                        },
                                        onPickStart = {
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, minute -> viewModel.setButterflyGroupStartTime(sessionIndex, groupIndex, hourOfDay, minute) },
                                                group.startHour,
                                                group.startMinute,
                                                use24Hour,
                                            ).show()
                                        },
                                    )
                                    if (groupIndex != session.groups.lastIndex) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val started = viewModel.startButterflySession(sessionIndex)
                                        if (!started) {
                                            scope.launch { snackbarHostState.showSnackbar(t("Oturum verisini kontrol et", "Check session data")) }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(34.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = sessionAccent),
                                ) {
                                    Text(t("Oturumu Ortak Başlat", "Start Session Together"), color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                        if (sessionIndex != butterflySessions.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                } else {
                    Text(
                        text = currentBreakSuggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }

                if (!isSetupState) {
                    Spacer(modifier = Modifier.height(24.dp))

                    GlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = displayedRemainingText,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = ((if (displayedRemainingText.length <= 5) 72f else 58f) * clockScale).sp,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("${t("GEÇEN", "ELAPSED")}: ${TimeUtils.formatCountdown(elapsedSeconds)}", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)

                            if (pendingStartCountdowns.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(t("Başlamasına Kalan", "Until Group Start"), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    pendingStartCountdowns.forEach { pending ->
                                        val isHighlightedDelayed = pending == highlightedDelayedGroup
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFF7043).copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                                                .border(
                                                    1.dp,
                                                    if (isHighlightedDelayed) Color(0xFFFF7043).copy(alpha = 0.9f) else Color(0xFFFF7043).copy(alpha = 0.45f),
                                                    RoundedCornerShape(8.dp),
                                                )
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                "${pending.sessionName} - ${pending.groupName}",
                                                color = Color(0xFFFFCCBC),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = if (isHighlightedDelayed) Modifier.alpha(delayedBlinkAlpha) else Modifier,
                                            )
                                            Text(
                                                TimeUtils.formatCountdown(pending.secondsUntilStart),
                                                color = Color(0xFFFF7043),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = if (isHighlightedDelayed) Modifier.alpha(delayedBlinkAlpha) else Modifier,
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }

                            if (pendingStartCountdowns.isEmpty() && !isUnifiedCountdown && activeGroupCountdowns.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(t("Grup Sayaçları", "Group Countdowns"), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    activeGroupCountdowns.forEach { countdown ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text("${countdown.sessionName} - ${countdown.groupName}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                            Text(
                                                TimeUtils.formatCountdown(countdown.remainingSeconds),
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(top = 16.dp),
                            ) {
                                RepeatingIconButton(onStep = { clockScale = (clockScale - 0.1f).coerceAtLeast(0.5f) }) {
                                    Icon(Icons.Default.Remove, null, tint = Color.White.copy(alpha = 0.5f))
                                }
                                IconButton(onClick = { clockScale = 1.0f }) {
                                    Icon(Icons.Default.Refresh, null, tint = Color.White.copy(alpha = 0.5f))
                                }
                                RepeatingIconButton(onStep = { clockScale = (clockScale + 0.1f).coerceAtMost(3.0f) }) {
                                    Icon(Icons.Default.Add, null, tint = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { viewModel.toggleRunning() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color(0xFFFF9800) else Color(0xFF4CAF50)),
                        ) {
                            Text(if (isRunning) t("Durdur", "Pause") else t("Devam", "Resume"), fontSize = 20.sp)
                        }
                        Button(onClick = { viewModel.reset() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))) {
                            Text(t("Sıfırla", "Reset"), fontSize = 20.sp)
                        }
                    }
                }
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                val byHeight = maxHeight.value * if (displayedRemainingText.length <= 5) 0.90f else 0.76f
                val widthFactor = if (displayedRemainingText.length <= 5) 2.55f else 2.10f
                val byWidth = (maxWidth.value / displayedRemainingText.length) * widthFactor
                val dynamicClockSize = minOf(byHeight, byWidth).coerceIn(120f, 680f)

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = displayedRemainingText,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = dynamicClockSize.sp,
                            fontFeatureSettings = "tnum",
                            lineHeight = (dynamicClockSize * 0.92f).sp,
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(0.92f).height(14.dp),
                        color = progressColor,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isRunning) t("SINAV DEVAM EDİYOR", "EXAM IN PROGRESS") else t("DURAKLATILDI", "PAUSED"),
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    )
                }
            }

            if (highlightedDelayedGroup != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.26f), RoundedCornerShape(999.dp))
                        .border(1.dp, Color(0xFFFF8A65), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .alpha(delayedBlinkAlpha),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFFF8A65), RoundedCornerShape(999.dp)),
                    )
                    Text(
                        text = t("Geciken", "Delayed") + ": ${highlightedDelayedGroup.groupName} ${TimeUtils.formatCountdown(highlightedDelayedGroup.secondsUntilStart)}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(onClick = { viewModel.toggleRunning() }) {
                    Icon(
                        if (isRunning) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(34.dp),
                    )
                }
                IconButton(onClick = { onClose() }) {
                    Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.75f))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    )
    Text(
        text = subtitle,
        color = Color.White.copy(alpha = 0.62f),
        fontSize = 11.sp,
    )
}

@Composable
private fun CompactEditableField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = label,
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                innerTextField()
            },
        )
    }
}

@Composable
private fun ButterflyGroupCard(
    name: String,
    statusText: String,
    statusHighlighted: Boolean,
    startText: String,
    durationMinutes: Int,
    manualDuration: String,
    groupLabel: String,
    startLabel: String,
    durationLabel: String,
    launchButtonText: String,
    showLaunchButton: Boolean = true,
    sessionAccent: Color,
    panelBg: Color,
    compactButtonTextSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    onNameChange: (String) -> Unit,
    onManualDurationChange: (String) -> Unit,
    onDecreaseDuration: () -> Unit,
    onIncreaseDuration: () -> Unit,
    onPickStart: () -> Unit,
    onStart: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .background(panelBg, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompactEditableField(
                value = name,
                onValueChange = onNameChange,
                label = groupLabel,
                modifier = Modifier.weight(1f),
                accent = sessionAccent,
            )
            StatusBadge(
                text = statusText,
                accent = sessionAccent,
                highlighted = statusHighlighted,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionInfoPill(
                label = startLabel,
                value = startText,
                modifier = Modifier.weight(1f),
                accent = sessionAccent,
                onClick = onPickStart,
            )
            DurationEditorPill(
                label = durationLabel,
                value = manualDuration.ifBlank { durationMinutes.toString() },
                suffix = "dk",
                modifier = Modifier.weight(1f),
                onValueChange = onManualDurationChange,
                onDecrease = onDecreaseDuration,
                onIncrease = onIncreaseDuration,
            )
        }
        if (showLaunchButton && onStart != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = sessionAccent),
            ) {
                Text(launchButtonText, color = Color.White, fontSize = compactButtonTextSize)
            }
        }
    }
}

@Composable
private fun InfoPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
private fun ActionInfoPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(accent.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .border(1.dp, accent.copy(alpha = 0.24f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 60.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun DurationEditorPill(
    label: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .heightIn(min = 60.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompactStepperButton(symbol = "-", onClick = onDecrease)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (value.isEmpty()) {
                                Text("0", color = Color.White.copy(alpha = 0.35f), fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                        Text(
                            suffix,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                },
            )
            CompactStepperButton(symbol = "+", onClick = onIncrease)
        }
    }
}

@Composable
private fun CompactStepperButton(symbol: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(300)
            while (isPressed) {
                onClick()
                delay(55)
            }
        }
    }
    Box(
        modifier = Modifier
            .size(width = 28.dp, height = 24.dp)
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RepeatingIconButton(
    onStep: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(300)
            while (isPressed) {
                onStep()
                delay(55)
            }
        }
    }

    IconButton(onClick = onStep, modifier = modifier, interactionSource = interactionSource) {
        content()
    }
}

@Composable
private fun StatusBadge(text: String, accent: Color, highlighted: Boolean) {
    val bg = if (highlighted) accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f)
    val fg = if (highlighted) accent else Color.White.copy(alpha = 0.74f)
    Text(
        text = text,
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .border(1.dp, fg.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = fg,
        fontSize = 10.sp,
        fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
        maxLines = 1,
    )
}
