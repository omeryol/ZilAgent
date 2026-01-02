package com.zilagent.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.MorphingBackground
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.util.TimeUtils

@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToExamMode: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val colorPaletteName = remember { com.zilagent.app.widget.WidgetStore.getThemeColorName(context) }
    val colorPalette = when (colorPaletteName) {
        "Okyanus" -> Pair(Color(0xFF4FACFE), Color(0xFF00F2FE))
        "Orman" -> Pair(Color(0xFF43E97B), Color(0xFF38F9D7))
        "Gece" -> Pair(Color(0xFF243B55), Color(0xFF141E30))
        "Ateş" -> Pair(Color(0xFFF093FB), Color(0xFFF5576C))
        "Güneş" -> Pair(Color(0xFFF6D365), Color(0xFFFDA085))
        else -> Pair(Color(0xFFE0C3FC), Color(0xFF8EC5FC)) // Lavanta
    }

    val editingItem = remember { mutableStateOf<BellSchedule?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        MorphingBackground(colorPalette = colorPalette)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Glass Header (Branding & Profile)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ZilAgent",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Profil: ${uiState.currentProfile?.name ?: "Varsayılan"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = onNavigateToProfiles) {
                            GradientIcon(icon = Icons.Default.Groups, gradient = IconGradients.Purple, size = 40.dp, iconSize = 20.dp)
                        }
                        IconButton(onClick = onNavigateToExamMode) {
                            GradientIcon(icon = Icons.Default.Timer, gradient = IconGradients.Blue, size = 40.dp, iconSize = 20.dp)
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            GradientIcon(icon = Icons.Default.Settings, gradient = IconGradients.Lava, size = 40.dp, iconSize = 20.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Status Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.currentStatusText,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = TimeUtils.formatCountdown(uiState.secondsRemaining),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    uiState.nextBell?.let {
                        Text(
                            text = "Bitiş: ${TimeUtils.minutesToTime(it.endTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Schedule List or Empty State
            if (uiState.schedule.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.zilagent.app.R.raw.empty_animation))
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(220.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Henüz bir program eklenmemiş",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(uiState.schedule) { index, item ->
                        val isActive = uiState.activeItemId == item.id
                        val isLast = index == uiState.schedule.size - 1
                        ScheduleItemRow(
                            item = item,
                            isActive = isActive,
                            isLast = isLast,
                            onClick = { editingItem.value = item }
                        )
                    }
                }
            }
        }

        // 4. Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToCreate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape),
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Brush.linearGradient(IconGradients.Pink)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        // Edit Dialog
        if (editingItem.value != null) {
            com.zilagent.app.ui.dashboard.EditScheduleDialog(
                item = editingItem.value!!,
                onDismiss = { editingItem.value = null },
                onConfirm = { newStart, newDuration, notifyStart, notifyEnd ->
                    viewModel.updateItem(editingItem.value!!, newStart, newDuration, notifyStart, notifyEnd)
                    editingItem.value = null
                }
            )
        }
    }
}

@Composable
fun ScheduleItemRow(
    item: BellSchedule,
    isActive: Boolean,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isActive -> Color(0xFF6200EE).copy(alpha = 0.4f)
        item.isBreak -> Color(0xFF4CAF50).copy(alpha = 0.3f)
        else -> Color.Black.copy(alpha = 0.15f)
    }
    
    val borderColor = when {
        isActive -> Color(0xFF6200EE).copy(alpha = 0.9f)
        item.isBreak -> Color(0xFF4CAF50).copy(alpha = 0.7f)
        else -> Color.White.copy(alpha = 0.25f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, MaterialTheme.shapes.small)
            .border(2.dp, borderColor, MaterialTheme.shapes.small)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (isLast) "${item.name} (Son Ders)" else item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (item.isBreak) "Teneffüs" else "Ders",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        Text(
            text = "${TimeUtils.minutesToTime(item.startTime)} - ${TimeUtils.minutesToTime(item.endTime)} (${item.endTime - item.startTime} dk)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = Color.White
        )
    }
}
