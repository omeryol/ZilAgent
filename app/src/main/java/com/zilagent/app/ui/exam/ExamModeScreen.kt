package com.zilagent.app.ui.exam

import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.LocalAppLanguage
import com.zilagent.app.ui.components.MorphingBackground
import com.zilagent.app.util.TimeUtils

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

    val durationInput by viewModel.durationInput.collectAsState()
    val examDurationMinutes by viewModel.examDurationMinutes.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    var clockScale by remember { mutableStateOf(1.0f) }
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

    val currentPhase = when {
        !isRunning && elapsedSeconds == 0L -> phaseSetup
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

    Box(modifier = Modifier.fillMaxSize()) {
        MorphingBackground(colorPalette = palette)

        if (!isLandscape) {
            IconButton(
                onClick = { onClose() },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = t("Kapat", "Close"), tint = Color.White)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = currentPhase.uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!isRunning && elapsedSeconds == 0L) {
                    GlassCard(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(t("Süre Ayarı", "Duration"), color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = durationInput,
                                onValueChange = viewModel::onDurationInputChange,
                                label = { Text(t("Dakika", "Minutes"), color = Color.White.copy(alpha = 0.6f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.White),
                                modifier = Modifier.width(140.dp),
                            )
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

                Spacer(modifier = Modifier.height(24.dp))

                GlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = TimeUtils.formatCountdown(remainingSeconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = (72 * clockScale).sp,
                                fontFeatureSettings = "tnum",
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("${t("GEÇEN", "ELAPSED")}: ${TimeUtils.formatCountdown(elapsedSeconds)}", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            IconButton(onClick = { clockScale = (clockScale - 0.1f).coerceAtLeast(0.5f) }) {
                                Icon(Icons.Default.Remove, null, tint = Color.White.copy(alpha = 0.5f))
                            }
                            IconButton(onClick = { clockScale = 1.0f }) {
                                Icon(Icons.Default.Refresh, null, tint = Color.White.copy(alpha = 0.5f))
                            }
                            IconButton(onClick = { clockScale = (clockScale + 0.1f).coerceAtMost(3.0f) }) {
                                Icon(Icons.Default.Add, null, tint = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!isRunning && elapsedSeconds == 0L) {
                        Button(onClick = { viewModel.toggleRunning() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                            Text(t("Başlat", "Start"), fontSize = 20.sp)
                        }
                    } else {
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
            val remainingText = TimeUtils.formatCountdown(remainingSeconds)
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                val byHeight = maxHeight.value * if (remainingText.length <= 5) 0.90f else 0.76f
                val byWidth = (maxWidth.value / remainingText.length) * 2.55f
                val dynamicClockSize = minOf(byHeight, byWidth).coerceIn(120f, 680f)

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = remainingText,
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
