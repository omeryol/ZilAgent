package com.zilagent.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.util.TimeUtils

@Composable
fun EditScheduleDialog(
    item: BellSchedule,
    onDismiss: () -> Unit,
    onConfirm: (newStartTime: Int, newDuration: Int, notifyStart: Boolean, notifyEnd: Boolean) -> Unit
) {
    val context = LocalContext.current
    
    // States
    var startTimeMinutes by remember { mutableStateOf(item.startTime) }
    var durationText by remember { mutableStateOf((item.endTime - item.startTime).toString()) }
    var notifyStart by remember { mutableStateOf(item.notifyAtStart) }
    var notifyEnd by remember { mutableStateOf(item.notifyAtEnd) }
    
    // Derived state for End Time display
    val endTimeMinutes = try {
        startTimeMinutes + (durationText.toIntOrNull() ?: 0)
    } catch (e: Exception) {
        startTimeMinutes
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "${item.name} Düzenle")
        },
        text = {
            Column {
                // 1. Start Time (Clickable)
                Text(
                    text = "Başlangıç Saati",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = TimeUtils.minutesToTime(startTimeMinutes),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .clickable {
                            com.zilagent.app.ui.components.launchTimePicker(
                                context, 
                                TimeUtils.minutesToTime(startTimeMinutes)
                            ) { timeStr ->
                                val parts = timeStr.split(":")
                                val h = parts[0].toInt()
                                val m = parts[1].toInt()
                                startTimeMinutes = h * 60 + m
                            }
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Duration (TextField)
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            durationText = it 
                        }
                    },
                    label = { Text("Süre (Dakika)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 3. Calculated End Time
                Text(
                    text = "Bitiş Saati (Otomatik): ${TimeUtils.minutesToTime(endTimeMinutes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                
                Spacer(modifier = Modifier.height(16.dp))

                // 4. Notification Toggles
                // Start Notification
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(
                        checked = notifyStart,
                        onCheckedChange = { notifyStart = it }
                    )
                    Text("Başlangıçta Bildir", style = MaterialTheme.typography.bodyMedium)
                }

                // End Notification
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(
                        checked = notifyEnd,
                        onCheckedChange = { notifyEnd = it }
                    )
                    Text("Bitişte Bildir", style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Not: Bu değişiklik sonraki tüm programı zincirleme olarak güncelleyecektir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationText.toIntOrNull()
                    if (duration != null && duration > 0) {
                        onConfirm(startTimeMinutes, duration, notifyStart, notifyEnd)
                    }
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
