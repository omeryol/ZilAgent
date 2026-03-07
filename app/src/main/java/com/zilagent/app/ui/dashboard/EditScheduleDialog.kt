package com.zilagent.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.ui.components.launchTimePicker
import com.zilagent.app.ui.components.premiumClickable
import com.zilagent.app.ui.components.premiumTouchEffect
import com.zilagent.app.util.TimeUtils

@Composable
fun EditScheduleDialog(
    item: BellSchedule,
    onDismiss: () -> Unit,
    onConfirm: (newStartTime: Int, newDuration: Int, notifyStart: Boolean, notifyEnd: Boolean) -> Unit,
) {
    val context = LocalContext.current
    var startTimeMinutes by remember { mutableStateOf(item.startTime) }
    var durationText by remember { mutableStateOf((item.endTime - item.startTime).toString()) }
    var notifyStart by remember { mutableStateOf(item.notifyAtStart) }
    var notifyEnd by remember { mutableStateOf(item.notifyAtEnd) }

    val parsedDuration = durationText.toIntOrNull() ?: 0
    val safeDuration = parsedDuration.coerceAtLeast(0)
    val endTimeMinutes = startTimeMinutes + safeDuration
    val kindText = if (item.isBreak) "Teneffus" else "Ders"

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        )
                        Text(
                            text = "$kindText duzenlemesi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                    GradientIcon(
                        icon = Icons.Default.Schedule,
                        gradient = IconGradients.Purple,
                        size = 40.dp,
                        iconSize = 18.dp,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Baslangic saati",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = TimeUtils.minutesToTime(startTimeMinutes),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .premiumClickable {
                                launchTimePicker(context, TimeUtils.minutesToTime(startTimeMinutes)) { timeStr ->
                                    val parts = timeStr.split(":")
                                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    startTimeMinutes = h * 60 + m
                                }
                            },
                    )

                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { input ->
                            if (input.all(Char::isDigit)) durationText = input
                        },
                        label = { Text("Sure (dk)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "Bitis saati: ${TimeUtils.minutesToTime(endTimeMinutes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB7F5C8),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
                        .padding(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GradientIcon(Icons.Default.Notifications, IconGradients.Blue, size = 30.dp, iconSize = 15.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Bildirimler",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().premiumClickable { notifyStart = !notifyStart },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = notifyStart, onCheckedChange = { notifyStart = it })
                        Text("Baslangicta bildir", color = Color.White.copy(alpha = 0.92f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().premiumClickable { notifyEnd = !notifyEnd },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = notifyEnd, onCheckedChange = { notifyEnd = it })
                        Text("Bitiste bildir", color = Color.White.copy(alpha = 0.92f))
                    }
                }

                Text(
                    text = "Degisiklik kaydedildiginde sonraki tum satirlar zincirleme guncellenir.",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.premiumTouchEffect()) {
                        Text("Iptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val duration = durationText.toIntOrNull()
                            if (duration != null && duration > 0) {
                                onConfirm(startTimeMinutes, duration, notifyStart, notifyEnd)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B8CFF)),
                        modifier = Modifier.premiumTouchEffect(),
                    ) {
                        GradientIcon(Icons.Default.Alarm, IconGradients.Blue, size = 26.dp, iconSize = 14.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}
