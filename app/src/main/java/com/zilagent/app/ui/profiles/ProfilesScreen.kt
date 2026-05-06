package com.zilagent.app.ui.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.data.entity.Profile
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.ui.components.LocalAppLanguage
import com.zilagent.app.ui.components.ZilAgentBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: ProfilesViewModel = viewModel(factory = ProfilesViewModel.Factory),
) {
    val appLanguage = LocalAppLanguage.current
    fun trEn(tr: String, en: String): String = if (appLanguage == AppLanguage.EN) en else tr
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trEn("Kayıtlı Profiller", "Saved Profiles")) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        GradientIcon(Icons.Default.ArrowBack, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        ZilAgentBackground(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = trEn("Yeni Profil Ekle", "Add New Profile"),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Button(
                                    onClick = { onNavigateToEdit(-1L) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(trEn("Ekle", "Add"))
                                }
                            }
                        }
                    }
                    items(uiState.profiles) { profile ->
                        ProfileItem(
                            profile = profile,
                            activeLabel = trEn("Şu an aktif", "Currently active"),
                            onSelect = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.selectProfile(profile)
                            },
                            onEdit = { onNavigateToEdit(profile.id) },
                            onDuplicate = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.duplicateProfile(profile)
                            },
                            onDelete = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteProfile(profile)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItem(
    profile: Profile,
    activeLabel: String,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (profile.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                if (profile.isActive) {
                    Text(
                        text = activeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    GradientIcon(Icons.Default.Edit, IconGradients.Blue, size = 32.dp, iconSize = 18.dp)
                }
                IconButton(onClick = onDuplicate) {
                    GradientIcon(Icons.Default.ContentCopy, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                }
                IconButton(onClick = onDelete) {
                    GradientIcon(Icons.Default.Delete, IconGradients.Lava, size = 32.dp, iconSize = 18.dp)
                }
            }
        }
    }
}
