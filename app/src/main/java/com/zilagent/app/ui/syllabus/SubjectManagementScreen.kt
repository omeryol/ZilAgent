package com.zilagent.app.ui.syllabus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.data.entity.SchoolSubject
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.ui.components.ZilAgentBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyllabusViewModel = viewModel(factory = SyllabusViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<SchoolSubject?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ders İsimleri") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        GradientIcon(Icons.Default.ArrowBack, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                GradientIcon(Icons.Default.Add, IconGradients.Blue, size = 56.dp, iconSize = 24.dp)
            }
        }
    ) { padding ->
        ZilAgentBackground(modifier = Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.subjects) { subject ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(subject.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                if (subject.isSystem) {
                                    Text("Sistem", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                                }
                            }
                            Row {
                                IconButton(onClick = { editingSubject = subject }) {
                                    GradientIcon(Icons.Default.Edit, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                                }
                                if (!subject.isSystem) {
                                    IconButton(onClick = { viewModel.deleteSubject(subject) }) {
                                        GradientIcon(icon = Icons.Default.Delete, gradient = IconGradients.Lava, size = 32.dp, iconSize = 18.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingSubject != null) {
        var name by remember(showAddDialog, editingSubject) { mutableStateOf(editingSubject?.name ?: "") }
        
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                editingSubject = null
            },
            title = { Text(if (editingSubject != null) "Dersi Düzenle" else "Yeni Ders Ekle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ders Adı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotEmpty()) {
                            if (editingSubject != null) {
                                viewModel.updateSubject(editingSubject!!.copy(name = name))
                                editingSubject = null
                            } else {
                                viewModel.addSubject(name)
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text(if (editingSubject != null) "Güncelle" else "Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    editingSubject = null
                }) {
                    Text("İptal")
                }
            }
        )
    }
}
