package com.zilagent.app.ui.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.zilagent.app.data.entity.Profile
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfilesViewModel = viewModel(factory = ProfilesViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kayıtlı Profiller") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
                    )
                )
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.profiles) { profile ->
                        ProfileItem(
                            profile = profile,
                            onSelect = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.selectProfile(profile) 
                            },
                            onDelete = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteProfile(profile) 
                            }
                        )
                    }
                    
                    if (uiState.profiles.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Henüz kayıtlı profil yok.", color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItem(
    profile: Profile,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (profile.isActive) {
                    GradientIcon(
                        icon = Icons.Default.CheckCircle,
                        gradient = IconGradients.Green,
                        size = 32.dp,
                        iconSize = 18.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Spacer(modifier = Modifier.width(44.dp))
                }
                
                Column {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (profile.isActive) {
                        Text(
                            text = "Şu an kullanımda",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (!profile.isActive) {
                Box(modifier = Modifier.clickable { onDelete() }) {
                    GradientIcon(
                        icon = Icons.Default.Delete,
                        gradient = IconGradients.Lava,
                        size = 36.dp,
                        iconSize = 20.dp
                    )
                }
            }
        }
    }
}
