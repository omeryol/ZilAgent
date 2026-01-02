package com.zilagent.app.ui.exam

import android.os.SystemClock
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.MorphingBackground
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.zilagent.app.util.TimeUtils
import com.airbnb.lottie.compose.*
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import kotlin.random.Random

@Composable
fun ExamModeScreen(
    onClose: () -> Unit,
    viewModel: ExamViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val durationInput by viewModel.durationInput.collectAsState()
    val examDurationMinutes by viewModel.examDurationMinutes.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    // Keep Screen On Logic
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val totalSeconds = examDurationMinutes * 60L
    val remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
    val progress = if (totalSeconds > 0) elapsedSeconds.toFloat() / totalSeconds.toFloat() else 0f
    
    // Break Suggestion Logic (For Mola)
    val breakSuggestions = listOf(
        "Derin bir nefes al ve omuzlarını serbest bırak.",
        "Gözlerini 20 saniye boyunca uzağa odaklayarak dinlendir.",
        "Suyundan bir yudum al, zihnini tazele.",
        "Dik oturduğundan emin ol, duruşunu düzelt.",
        "Kısa bir süre için sadece nefesine odaklan."
    )
    val currentBreakSuggestion = remember(remainingSeconds / 300) { breakSuggestions.random() }

    val readingDuration = (examDurationMinutes * 0.1f * 60).toLong().coerceAtMost(300L)
    val closingDuration = (examDurationMinutes * 0.1f * 60).toLong().coerceAtMost(600L)
    
    val currentPhase = when {
        !isRunning && elapsedSeconds == 0L -> "Sınav Hazırlığı"
        elapsedSeconds < readingDuration -> "Okuma Süresi"
        elapsedSeconds > (totalSeconds - closingDuration) -> "Son Uyarılar"
        else -> "Sınav Devam Ediyor"
    }

    val palette = when {
        currentPhase == "Sınav Hazırlığı" -> Color(0xFF121212) to Color(0xFF1E1E1E)
        currentPhase == "Okuma Süresi" -> Color(0xFF1E88E5) to Color(0xFF1565C0)
        currentPhase == "Son Uyarılar" -> Color(0xFFF4511E) to Color(0xFFBF360C)
        else -> Color(0xFF2E7D32) to Color(0xFF1B5E20)
    }
    
    val progressColor = when (currentPhase) {
        "Okuma Süresi" -> Color(0xFFBBDEFB)
        "Son Uyarılar" -> Color(0xFFFFCCBC)
        else -> Color(0xFFC8E6C9)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MorphingBackground(colorPalette = palette)
        
        if (!isLandscape) {
            // PORTRAIT MODE (Existing improved UI)
            IconButton(
                onClick = { onClose() },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currentPhase.uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!isRunning && elapsedSeconds == 0L) {
                    GlassCard(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Süre Ayarı", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = durationInput,
                                onValueChange = viewModel::onDurationInputChange,
                                label = { Text("Dakika", color = Color.White.copy(alpha = 0.6f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.White),
                                modifier = Modifier.width(140.dp)
                            )
                        }
                    }
                } else {
                    // Mola / Advice text
                    Text(
                        text = currentBreakSuggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                GlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(TimeUtils.formatCountdown(remainingSeconds), style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp, fontFeatureSettings = "tnum"), color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(20.dp))
                        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(12.dp), color = progressColor, trackColor = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("GEÇEN: ${TimeUtils.formatCountdown(elapsedSeconds)}", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!isRunning && elapsedSeconds == 0L) {
                        androidx.compose.material3.Button(onClick = { viewModel.toggleRunning() }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                            Text("Başlat", fontSize = 20.sp)
                        }
                    } else {
                        androidx.compose.material3.Button(onClick = { viewModel.toggleRunning() }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = if (isRunning) Color(0xFFFF9800) else Color(0xFF4CAF50))) {
                            Text(if (isRunning) "Durdur" else "Devam", fontSize = 20.sp)
                        }
                        androidx.compose.material3.Button(onClick = { viewModel.reset() }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))) {
                            Text("Sıfırla", fontSize = 20.sp)
                        }
                    }
                }
            }
        } else {
            // LANDSCAPE MODE (Full Screen Digital Clock)
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp).clickable { viewModel.toggleRunning() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = TimeUtils.formatCountdown(remainingSeconds),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 160.sp, fontFeatureSettings = "tnum"),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(0.8f).height(12.dp),
                    color = progressColor,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                if (!isRunning) {
                     Text("DURAKLATILDI", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                }
            }
            
            // Minimalist Close in Landscape
            IconButton(onClick = { onClose() }, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}
