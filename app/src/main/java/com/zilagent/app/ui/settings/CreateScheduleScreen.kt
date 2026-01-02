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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.ui.components.GlassCard

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun CreateScheduleScreen(
    onSaveComplete: () -> Unit,
    viewModel: CreateScheduleViewModel = viewModel(factory = CreateScheduleViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) {
            onSaveComplete()
            viewModel.resetSaveComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0C3FC),
                        Color(0xFF8EC5FC)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Zil Programı Oluştur",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Active Profile Header
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                 Column(modifier = Modifier.padding(16.dp)) {
                     Text("Aktif Profil", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                     Text(uiState.profileName, style = MaterialTheme.typography.headlineSmall)
                 }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // General Settings Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Genel Program", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
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

                    val context = androidx.compose.ui.platform.LocalContext.current
                    
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
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                    
                    Text("Öğle Arası Ayarları", style = MaterialTheme.typography.titleMedium)
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
                        androidx.compose.material3.Checkbox(
                            checked = uiState.countdownColorEnabled,
                            onCheckedChange = { viewModel.onCountdownColorEnabledChange(it) }
                        )
                        Text(
                            text = "Geri Sayım Renklensin (Yeşil -> Kırmızı)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } // End General settings
            
            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::generateAndSave,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.5f),
                    contentColor = Color.Black
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                } else {
                    Text("Oluştur ve Kaydet")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
