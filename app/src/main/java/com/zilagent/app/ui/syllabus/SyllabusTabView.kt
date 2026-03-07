package com.zilagent.app.ui.syllabus

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.data.entity.SyllabusEntry
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.LocalAppLanguage
import com.zilagent.app.ui.components.premiumClickable
import com.zilagent.app.ui.components.premiumTouchEffect

@Composable
fun SyllabusTabView(
    onNavigateToClasses: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    viewModel: SyllabusViewModel = viewModel(factory = SyllabusViewModel.Factory)
) {
    val appLanguage = LocalAppLanguage.current
    fun trEn(tr: String, en: String): String = if (appLanguage == AppLanguage.EN) en else tr
    val uiState by viewModel.uiState.collectAsState()
    val syllabusEntries by viewModel.getSyllabusForDay(uiState.selectedDay).collectAsState(initial = emptyList())
    val notes by viewModel.getNotesForDay(uiState.selectedDay).collectAsState(initial = emptyList())
    
    val days = if (appLanguage == AppLanguage.EN) listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    var copyTargetDay by remember(uiState.selectedDay) { mutableStateOf(uiState.selectedDay) }
    
    // Dialog State: Pair(LessonOrder, CurrentNote)
    var noteDialogState by remember { mutableStateOf<Pair<Int, String>?>(null) }

    if (noteDialogState != null) {
        val (order, note) = noteDialogState!!
        var currentNote by remember { mutableStateOf(note) }
        
        AlertDialog(
            onDismissRequest = { noteDialogState = null },
            title = { Text(trEn("Ders Notu", "Lesson Note")) },
            text = {
                OutlinedTextField(
                    value = currentNote,
                    onValueChange = { currentNote = it },
                    label = { Text(trEn("Notunuz", "Your note")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveNote(order, currentNote)
                    noteDialogState = null
                }) {
                    Text(trEn("Kaydet", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { noteDialogState = null }) {
                    Text(trEn("İptal", "Cancel"))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Management Buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToClasses,
                modifier = Modifier.weight(1f).premiumTouchEffect(),
                contentPadding = PaddingValues(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                com.zilagent.app.ui.components.GradientIcon(Icons.Default.Class, com.zilagent.app.ui.components.IconGradients.Blue, size = 30.dp, iconSize = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text(trEn("Sınıflar", "Classes"), color = Color.White, fontSize = 12.sp)
            }
            Button(
                onClick = onNavigateToSubjects,
                modifier = Modifier.weight(1f).premiumTouchEffect(),
                contentPadding = PaddingValues(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                com.zilagent.app.ui.components.GradientIcon(Icons.Default.MenuBook, com.zilagent.app.ui.components.IconGradients.Purple, size = 30.dp, iconSize = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text(trEn("Dersler", "Subjects"), color = Color.White, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Compact Day Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEachIndexed { index, day ->
                val dayNum = index + 1
                val isSelected = uiState.selectedDay == dayNum
                
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f)
                val contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(containerColor)
                        .premiumClickable { viewModel.onDaySelected(dayNum) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var copyMenuExpanded by remember { mutableStateOf(false) }
            OutlinedButton(
                onClick = { copyMenuExpanded = true },
                modifier = Modifier.weight(1f).premiumTouchEffect(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("${trEn("Hedef gün", "Target day")}: ${days[copyTargetDay - 1]}", maxLines = 1)
            }
            DropdownMenu(
                expanded = copyMenuExpanded,
                onDismissRequest = { copyMenuExpanded = false }
            ) {
                (1..7).forEach { day ->
                    DropdownMenuItem(
                        text = { Text(days[day - 1]) },
                        onClick = {
                            copyTargetDay = day
                            copyMenuExpanded = false
                        }
                    )
                }
            }
            Button(
                onClick = { viewModel.copyDayPlan(uiState.selectedDay, copyTargetDay) },
                enabled = copyTargetDay != uiState.selectedDay,
                modifier = Modifier.premiumTouchEffect(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f))
            ) {
                Text(trEn("Günü Kopyala", "Copy Day"), color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lessons List
        val lessons = uiState.bellSchedules.filter { !it.isBreak }
        if (lessons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(trEn("Bu gün için bir zil programı tanımlanmamış.", "No schedule defined for this day."), color = Color.White.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                itemsIndexed(lessons) { index, bell ->
                    val lessonOrder = index + 1
                    val entry = syllabusEntries.find { it.lessonOrder == lessonOrder }
                    val currentNote = notes.find { it.lessonOrder == lessonOrder }?.note
                    
                    SyllabusRow(
                        lessonName = bell.name,
                        lessonOrder = lessonOrder,
                        entry = entry,
                        classes = uiState.classes,
                        subjects = uiState.subjects,
                        note = currentNote,
                        onSave = { classId, subId -> 
                            viewModel.saveSyllabusEntry(lessonOrder, classId, subId)
                        },
                        onNoteClick = {
                            noteDialogState = Pair(lessonOrder, currentNote ?: "")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SyllabusRow(
    lessonName: String,
    lessonOrder: Int,
    entry: SyllabusEntry?,
    classes: List<com.zilagent.app.data.entity.SchoolClass>,
    subjects: List<com.zilagent.app.data.entity.SchoolSubject>,
    note: String?,
    onSave: (Long?, Long?) -> Unit,
    onNoteClick: () -> Unit
) {
    val appLanguage = LocalAppLanguage.current
    fun trEn(tr: String, en: String): String = if (appLanguage == AppLanguage.EN) en else tr
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {}, // Empty click to prevent ripple blocking but allow long click
                onLongClick = onNoteClick
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(lessonName, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                if (!note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.EditNote, 
                        contentDescription = "Not Var", 
                        tint = MaterialTheme.colorScheme.primary, // Or a visible color
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Class Selector
                SyllabusDropDown(
                    label = trEn("Sınıf", "Class"),
                    items = classes,
                    selectedId = entry?.classId,
                    onSelected = { onSave(it, entry?.subjectId) },
                    modifier = Modifier.weight(1f),
                    getName = { it.name }
                )
                
                // Subject Selector
                SyllabusDropDown(
                    label = trEn("Ders", "Subject"),
                    items = subjects,
                    selectedId = entry?.subjectId,
                    onSelected = { onSave(entry?.classId, it) },
                    modifier = Modifier.weight(1f),
                    getName = { it.name }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SyllabusDropDown(
    label: String,
    items: List<T>,
    selectedId: Long?,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    getName: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedItem = items.find { 
        when (it) {
            is com.zilagent.app.data.entity.SchoolClass -> it.id == selectedId
            is com.zilagent.app.data.entity.SchoolSubject -> it.id == selectedId
            else -> false
        }
    }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White.copy(alpha = 0.1f)),
            border = CardDefaults.outlinedCardBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.3f)))
        ) {
            Row(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedItem?.let { getName(it) } ?: label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedItem != null) Color.White else Color.White.copy(alpha = 0.5f),
                    maxLines = 1
                )
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text(if (LocalAppLanguage.current == AppLanguage.EN) "Not selected" else "Seçilmedi") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            items.forEach { item ->
                val id = when (item) {
                    is com.zilagent.app.data.entity.SchoolClass -> item.id
                    is com.zilagent.app.data.entity.SchoolSubject -> item.id
                    else -> 0L
                }
                DropdownMenuItem(
                    text = { Text(getName(item)) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}


