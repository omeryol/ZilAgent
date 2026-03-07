package com.zilagent.app.ui.syllabus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.data.entity.SchoolClass
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.GradientIcon
import com.zilagent.app.ui.components.IconGradients
import com.zilagent.app.ui.components.LocalAppLanguage
import com.zilagent.app.ui.components.ZilAgentBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyllabusViewModel = viewModel(factory = SyllabusViewModel.Factory),
) {
    val appLanguage = LocalAppLanguage.current
    fun trEn(tr: String, en: String): String = if (appLanguage == AppLanguage.EN) en else tr
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<SchoolClass?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trEn("Sınıflarım", "My Classes")) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        GradientIcon(Icons.Default.ArrowBack, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
            )
        },
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
            ) {
                GradientIcon(Icons.Default.Add, IconGradients.Blue, size = 56.dp, iconSize = 24.dp)
            }
        },
    ) { padding ->
        ZilAgentBackground(modifier = Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.classes) { schoolClass ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(schoolClass.colorHex))),
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(schoolClass.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            }
                            Row {
                                IconButton(onClick = { editingClass = schoolClass }) {
                                    GradientIcon(Icons.Default.Edit, IconGradients.Purple, size = 32.dp, iconSize = 18.dp)
                                }
                                IconButton(onClick = { viewModel.deleteClass(schoolClass) }) {
                                    GradientIcon(Icons.Default.Delete, IconGradients.Lava, size = 32.dp, iconSize = 18.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingClass != null) {
        var name by remember(showAddDialog, editingClass) { mutableStateOf(editingClass?.name ?: "") }
        var selectedColor by remember(showAddDialog, editingClass) { mutableStateOf(editingClass?.colorHex ?: "#3F51B5") }

        val colors = listOf("#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722")

        AlertDialog(
            onDismissRequest = { showAddDialog = false; editingClass = null },
            title = { Text(if (editingClass != null) trEn("Sınıfı Düzenle", "Edit Class") else trEn("Yeni Sınıf Ekle", "Add Class")) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(trEn("Sınıf Adı", "Class Name")) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(trEn("Renk Seçimi", "Color"), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    ColorPicker(colors = colors, selectedColor = selectedColor, onColorSelected = { selectedColor = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotEmpty()) {
                        if (editingClass != null) {
                            viewModel.updateClass(editingClass!!.copy(name = name, colorHex = selectedColor))
                            editingClass = null
                        } else {
                            viewModel.addClass(name, selectedColor)
                            showAddDialog = false
                        }
                    }
                }) {
                    Text(if (editingClass != null) trEn("Güncelle", "Update") else trEn("Ekle", "Add"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; editingClass = null }) {
                    Text(trEn("İptal", "Cancel"))
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPicker(colors: List<String>, selectedColor: String, onColorSelected: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        colors.forEach { colorHex ->
            val isSelected = selectedColor == colorHex
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.Black else Color.Gray, CircleShape)
                    .clickable { onColorSelected(colorHex) },
            )
        }
    }
}
