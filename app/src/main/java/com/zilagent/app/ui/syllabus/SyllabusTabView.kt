package com.zilagent.app.ui.syllabus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zilagent.app.data.entity.SchoolClass
import com.zilagent.app.data.entity.SchoolSubject
import com.zilagent.app.data.entity.SyllabusEntry
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.LocalAppLanguage
import com.zilagent.app.ui.components.premiumClickable
import com.zilagent.app.ui.components.premiumTouchEffect
import com.zilagent.app.util.TimeUtils
import com.zilagent.app.widget.WidgetStore
import kotlinx.coroutines.delay
import java.time.LocalDate

private data class CellEditorState(
    val dayOfWeek: Int,
    val dayLabel: String,
    val lesson: WeeklyLessonPlan,
)

@Composable
fun SyllabusTabView(
    viewModel: SyllabusViewModel = viewModel(factory = SyllabusViewModel.Factory),
) {
    val appLanguage = LocalAppLanguage.current
    val context = LocalContext.current
    fun trEn(tr: String, en: String): String = if (appLanguage == AppLanguage.EN) en else tr
    val uiState by viewModel.uiState.collectAsState()
    val shortDayLabels = remember(appLanguage) {
        if (appLanguage == AppLanguage.EN) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        } else {
            listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
        }
    }
    val longDayLabels = remember(appLanguage) {
        if (appLanguage == AppLanguage.EN) {
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        } else {
            listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")
        }
    }

    var copyDialogOpen by remember { mutableStateOf(false) }
    var editorState by remember { mutableStateOf<CellEditorState?>(null) }
    var hideEmptyDays by rememberSaveable { mutableStateOf(WidgetStore.isWeeklyHideEmptyDays(context)) }
    var sectionTab by rememberSaveable { mutableIntStateOf(0) }
    var managerTab by rememberSaveable { mutableIntStateOf(0) }
    var showClassDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<SchoolClass?>(null) }
    var showSubjectDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<SchoolSubject?>(null) }
    val refreshTick by produceState(initialValue = 0) {
        while (true) {
            delay(30_000)
            value += 1
        }
    }

    if (copyDialogOpen) {
        WeeklyCopyDialog(
            dayLabels = longDayLabels,
            onDismiss = { copyDialogOpen = false },
            onCopy = { fromDay, toDay ->
                viewModel.copyDayPlan(fromDay, toDay)
                copyDialogOpen = false
            },
            trEn = ::trEn,
        )
    }

    if (editorState != null) {
        WeeklyLessonEditorDialog(
            state = editorState!!,
            classes = uiState.classes,
            subjects = uiState.subjects,
            onDismiss = { editorState = null },
            onSave = { dayOfWeek, lessonOrder, classId, subjectId, note ->
                viewModel.saveSyllabusEntry(dayOfWeek, lessonOrder, classId, subjectId)
                viewModel.saveNote(dayOfWeek, lessonOrder, note)
                editorState = null
            },
            trEn = ::trEn,
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SectionToolbar(
            selectedSection = sectionTab,
            onSelectBoard = { sectionTab = 0 },
            onSelectManagement = { sectionTab = 1 },
            trEn = ::trEn,
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (sectionTab == 1) {
            WeeklyToolbar(
                selectedManagerTab = managerTab,
                onSelectClasses = { managerTab = 0 },
                onSelectSubjects = { managerTab = 1 },
                trEn = ::trEn,
            )

            Spacer(modifier = Modifier.height(10.dp))

            InlineManagerPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                managerTab = managerTab,
                classes = uiState.classes,
                subjects = uiState.subjects,
                trEn = ::trEn,
                onAddClass = {
                    editingClass = null
                    showClassDialog = true
                },
                onEditClass = {
                    editingClass = it
                    showClassDialog = true
                },
                onDeleteClass = viewModel::deleteClass,
                onAddSubject = {
                    editingSubject = null
                    showSubjectDialog = true
                },
                onEditSubject = {
                    editingSubject = it
                    showSubjectDialog = true
                },
                onDeleteSubject = viewModel::deleteSubject,
            )
        }

        if (sectionTab == 0 && uiState.currentProfileId == -1L) {
            GlassCard(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = trEn("Aktif profil seçilmeden haftalık tablo gösterilemiyor.", "Weekly board needs an active profile."),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
            return
        }

        val hasEmptyDays = uiState.weekPlans.any { it.lessons.isEmpty() }
        if (sectionTab == 0 && hasEmptyDays) {
            Spacer(modifier = Modifier.height(10.dp))
            FilterChip(
                selected = hideEmptyDays,
                onClick = {
                    hideEmptyDays = !hideEmptyDays
                    WidgetStore.setWeeklyHideEmptyDays(context, hideEmptyDays)
                },
                label = { Text(trEn("Boş günleri gizle", "Hide empty days")) },
                modifier = Modifier.align(Alignment.Start),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }

        if (sectionTab == 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { copyDialogOpen = true },
                modifier = Modifier.fillMaxWidth().premiumTouchEffect(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentPadding = PaddingValues(vertical = 10.dp),
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(trEn("Günü Kopyala", "Copy Day"), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        val weekPlans = uiState.weekPlans.let { plans ->
            val filteredPlans = if (hideEmptyDays) {
                plans.filter { it.lessons.isNotEmpty() }
            } else {
                plans
            }
            if (filteredPlans.isEmpty()) plans else filteredPlans
        }
        val maxLessonCount = (weekPlans.maxOfOrNull { it.lessons.size } ?: 0).coerceAtLeast(1)
        val today = remember(refreshTick) { LocalDate.now().dayOfWeek.value }
        val currentMinutes = remember(refreshTick) { TimeUtils.getCurrentMinutes() }
        val boardScrollState = rememberScrollState()

        if (sectionTab == 0) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
            ) {
            val columnSpacing = 6.dp
            val rowSpacing = 4.dp
            val headerHeight = if (maxLessonCount >= 8) 32.dp else 36.dp
            val rowHeight = when {
                maxLessonCount >= 9 -> 48.dp
                maxLessonCount >= 8 -> 50.dp
                maxLessonCount >= 6 -> 56.dp
                else -> 60.dp
            }
            val compact = rowHeight <= 52.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(boardScrollState),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(columnSpacing),
                ) {
                    weekPlans.forEach { dayPlan ->
                        DayHeaderCell(
                            label = shortDayLabels[dayPlan.dayOfWeek - 1],
                            isToday = dayPlan.dayOfWeek == today,
                            isEmpty = dayPlan.lessons.isEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(headerHeight),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                repeat(maxLessonCount) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(columnSpacing),
                    ) {
                        weekPlans.forEach { dayPlan ->
                            val lesson = dayPlan.lessons.getOrNull(rowIndex)
                            WeeklyBoardCell(
                                lesson = lesson,
                                className = resolveClassName(lesson?.entry, uiState.classes),
                                subjectName = resolveSubjectName(lesson?.entry, uiState.subjects),
                                classColorHex = resolveClassColorHex(lesson?.entry, uiState.classes),
                                isCurrentDay = dayPlan.dayOfWeek == today,
                                isActive = lesson != null &&
                                    dayPlan.dayOfWeek == today &&
                                    currentMinutes >= lesson.bell.startTime &&
                                    currentMinutes < lesson.bell.endTime,
                                isElapsed = lesson != null &&
                                    dayPlan.dayOfWeek == today &&
                                    currentMinutes >= lesson.bell.endTime,
                                compact = compact,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(rowHeight),
                                onClick = {
                                    if (lesson != null) {
                                        editorState = CellEditorState(
                                            dayOfWeek = dayPlan.dayOfWeek,
                                            dayLabel = longDayLabels[dayPlan.dayOfWeek - 1],
                                            lesson = lesson,
                                        )
                                    }
                                },
                                trEn = ::trEn,
                            )
                        }
                    }
                    if (rowIndex < maxLessonCount - 1) {
                        Spacer(modifier = Modifier.height(rowSpacing))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    if (showClassDialog || editingClass != null) {
        ClassEditorDialog(
            editingClass = editingClass,
            onDismiss = {
                showClassDialog = false
                editingClass = null
            },
            onSave = { name, colorHex ->
                if (editingClass != null) {
                    viewModel.updateClass(editingClass!!.copy(name = name, colorHex = colorHex))
                } else {
                    viewModel.addClass(name, colorHex)
                }
                showClassDialog = false
                editingClass = null
            },
            trEn = ::trEn,
        )
    }

    if (showSubjectDialog || editingSubject != null) {
        SubjectEditorDialog(
            editingSubject = editingSubject,
            onDismiss = {
                showSubjectDialog = false
                editingSubject = null
            },
            onSave = { name ->
                if (editingSubject != null) {
                    viewModel.updateSubject(editingSubject!!.copy(name = name))
                } else {
                    viewModel.addSubject(name)
                }
                showSubjectDialog = false
                editingSubject = null
            },
            trEn = ::trEn,
        )
    }
}

@Composable
private fun SectionToolbar(
    selectedSection: Int,
    onSelectBoard: () -> Unit,
    onSelectManagement: () -> Unit,
    trEn: (String, String) -> String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompactActionButton(
            label = trEn("Ders Programı", "Schedule"),
            icon = Icons.Default.MenuBook,
            selected = selectedSection == 0,
            onClick = onSelectBoard,
            modifier = Modifier.weight(1f),
        )
        CompactActionButton(
            label = trEn("Sınıflar / Dersler", "Classes / Subjects"),
            icon = Icons.Default.Class,
            selected = selectedSection == 1,
            onClick = onSelectManagement,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WeeklyToolbar(
    selectedManagerTab: Int,
    onSelectClasses: () -> Unit,
    onSelectSubjects: () -> Unit,
    trEn: (String, String) -> String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompactActionButton(
            label = trEn("Sınıflar", "Classes"),
            icon = Icons.Default.Class,
            selected = selectedManagerTab == 0,
            onClick = onSelectClasses,
            modifier = Modifier.weight(1f),
        )
        CompactActionButton(
            label = trEn("Dersler", "Subjects"),
            icon = Icons.Default.MenuBook,
            selected = selectedManagerTab == 1,
            onClick = onSelectSubjects,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CompactActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Button(
        onClick = onClick,
        modifier = modifier.premiumTouchEffect(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
            },
        ),
        contentPadding = PaddingValues(vertical = 10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = onSurface, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InlineManagerPanel(
    modifier: Modifier = Modifier,
    managerTab: Int,
    classes: List<SchoolClass>,
    subjects: List<SchoolSubject>,
    trEn: (String, String) -> String,
    onAddClass: () -> Unit,
    onEditClass: (SchoolClass) -> Unit,
    onDeleteClass: (SchoolClass) -> Unit,
    onAddSubject: () -> Unit,
    onEditSubject: (SchoolSubject) -> Unit,
    onDeleteSubject: (SchoolSubject) -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (managerTab == 0) trEn("Sınıf Listesi", "Class List") else trEn("Ders Listesi", "Subject List"),
                        color = onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = if (managerTab == 0) {
                            trEn("Haftalık tabloda kullanacağın sınıfları burada yönet.", "Manage the classes used in the weekly board here.")
                        } else {
                            trEn("Ders isimlerini ekle, düzenle ve sade tut.", "Add, edit, and keep subject names tidy here.")
                        },
                        color = onSurface.copy(alpha = 0.64f),
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
                Button(
                    onClick = { if (managerTab == 0) onAddClass() else onAddSubject() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.premiumTouchEffect(),
                ) {
                    Text(
                        text = if (managerTab == 0) trEn("Sınıf ekle", "Add class") else trEn("Ders ekle", "Add subject"),
                        color = onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (managerTab == 0) {
                if (classes.isEmpty()) {
                    Text(
                        text = trEn("Henüz sınıf eklenmemiş.", "No classes added yet."),
                        color = onSurface.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 4.dp),
                    ) {
                        items(classes, key = { it.id }) { schoolClass ->
                            CompactManagerRow(
                                leading = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(Color(android.graphics.Color.parseColor(schoolClass.colorHex))),
                                    )
                                },
                                title = schoolClass.name,
                                subtitle = schoolClass.colorHex,
                                onEdit = { onEditClass(schoolClass) },
                                onDelete = { onDeleteClass(schoolClass) },
                            )
                        }
                    }
                }
            } else {
                if (subjects.isEmpty()) {
                    Text(
                        text = trEn("Henüz ders eklenmemiş.", "No subjects added yet."),
                        color = onSurface.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 4.dp),
                    ) {
                        items(subjects, key = { it.id }) { subject ->
                            CompactManagerRow(
                                leading = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)),
                                    )
                                },
                                title = subject.name,
                                subtitle = if (subject.isSystem) trEn("Sistem", "System") else trEn("Özel", "Custom"),
                                onEdit = { onEditSubject(subject) },
                                onDelete = if (subject.isSystem) null else ({ onDeleteSubject(subject) }),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactManagerRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        leading()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                color = onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = onSurface.copy(alpha = 0.58f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "Düzenle",
            color = onSurface.copy(alpha = 0.78f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.premiumClickable { onEdit() },
        )
        if (onDelete != null) {
            Text(
                text = "Sil",
                color = Color(0xFFE35D6A),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.premiumClickable { onDelete() },
            )
        }
    }
}

@Composable
private fun ClassEditorDialog(
    editingClass: SchoolClass?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    trEn: (String, String) -> String,
) {
    var name by remember(editingClass) { mutableStateOf(editingClass?.name ?: "") }
    var selectedColor by remember(editingClass) { mutableStateOf(editingClass?.colorHex ?: "#3F51B5") }
    val colors = listOf("#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingClass != null) trEn("Sınıfı düzenle", "Edit class") else trEn("Yeni sınıf", "New class")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(trEn("Sınıf adı", "Class name")) },
                    modifier = Modifier.fillMaxWidth(),
                )
                InlineColorPicker(colors = colors, selectedColor = selectedColor, onColorSelected = { selectedColor = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim(), selectedColor) }) {
                Text(trEn("Kaydet", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(trEn("İptal", "Cancel")) }
        },
    )
}

@Composable
private fun SubjectEditorDialog(
    editingSubject: SchoolSubject?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    trEn: (String, String) -> String,
) {
    var name by remember(editingSubject) { mutableStateOf(editingSubject?.name ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingSubject != null) trEn("Dersi düzenle", "Edit subject") else trEn("Yeni ders", "New subject")) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(trEn("Ders adı", "Subject name")) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) {
                Text(trEn("Kaydet", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(trEn("İptal", "Cancel")) }
        },
    )
}

@Composable
private fun InlineColorPicker(
    colors: List<String>,
    selectedColor: String,
    onColorSelected: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        colors.take(8).forEach { colorHex ->
            val isSelected = selectedColor == colorHex
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(99.dp),
                    )
                    .premiumClickable { onColorSelected(colorHex) },
            )
        }
    }
}

@Composable
private fun DayHeaderCell(
    label: String,
    isToday: Boolean,
    isEmpty: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val bg = when {
        isToday -> colorScheme.primaryContainer.copy(alpha = 0.96f)
        isEmpty -> colorScheme.surfaceVariant.copy(alpha = 0.42f)
        else -> colorScheme.secondaryContainer.copy(alpha = 0.78f)
    }
    val borderColor = when {
        isToday -> colorScheme.primary.copy(alpha = 0.9f)
        isEmpty -> colorScheme.outlineVariant.copy(alpha = 0.72f)
        else -> colorScheme.secondary.copy(alpha = 0.38f)
    }
    val textColor = when {
        isToday -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSurface
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun WeeklyBoardCell(
    lesson: WeeklyLessonPlan?,
    className: String,
    subjectName: String,
    classColorHex: String?,
    isCurrentDay: Boolean,
    isActive: Boolean,
    isElapsed: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    trEn: (String, String) -> String,
) {
    val colorScheme = MaterialTheme.colorScheme
    val onSurface = colorScheme.onSurface
    val topLineSize = if (compact) 8.sp else 9.sp
    val titleSize = if (compact) 10.sp else 12.sp
    val subtitleSize = if (compact) 8.sp else 9.sp

    if (lesson == null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.18f))
                .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.34f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "—",
                color = onSurface.copy(alpha = 0.48f),
                fontSize = 14.sp,
            )
        }
        return
    }

    if (lesson.isLunchBreak) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(colorScheme.tertiaryContainer.copy(alpha = 0.82f))
                .border(1.dp, colorScheme.tertiary.copy(alpha = 0.42f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = onSurface.copy(alpha = 0.78f),
                    modifier = Modifier.size(if (compact) 12.dp else 16.dp)
                )
                if (!compact) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = trEn("Öğle Arası", "Lunch"),
                    color = onSurface.copy(alpha = 0.8f),
                    fontSize = if (compact) 8.sp else 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val hasAssignment = className.isNotBlank() || subjectName.isNotBlank()
    val accentColor = when {
        !hasAssignment -> Color(0xFFFFB300)
        else -> parseColorOrNull(classColorHex) ?: MaterialTheme.colorScheme.primary
    }
    val bg = when {
        isActive -> accentColor.copy(alpha = 0.38f)
        isElapsed -> colorScheme.surfaceVariant.copy(alpha = 0.52f)
        !hasAssignment -> accentColor.copy(alpha = 0.24f)
        isCurrentDay -> accentColor.copy(alpha = 0.22f)
        else -> accentColor.copy(alpha = 0.16f)
    }
    val borderColor = when {
        isActive -> accentColor.copy(alpha = 0.92f)
        !hasAssignment -> accentColor.copy(alpha = 0.72f)
        isCurrentDay -> accentColor.copy(alpha = 0.38f)
        else -> colorScheme.outlineVariant.copy(alpha = 0.16f)
    }
    val containerAlpha = if (isElapsed) 0.48f else 1f
    val primaryTextColor = if (isElapsed) onSurface.copy(alpha = 0.72f) else onSurface
    val secondaryTextColor = if (isElapsed) onSurface.copy(alpha = 0.58f) else onSurface.copy(alpha = 0.88f)

    Column(
        modifier = modifier
            .alpha(containerAlpha)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp),
            )
            .premiumClickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${lesson.lessonOrder}.",
                color = primaryTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = topLineSize,
                maxLines = 1,
            )
            Text(
                text = className.ifBlank { "—" },
                color = secondaryTextColor,
                fontSize = topLineSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = minutesToTime(lesson.bell.startTime),
                    color = secondaryTextColor,
                    fontSize = topLineSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                if (!lesson.note?.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(if (compact) 7.dp else 8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(accentColor),
                    )
                }
            }
        }

        if (hasAssignment) {
            val primaryLabel = subjectName.ifBlank { trEn("Ders yok", "Subject not assigned") }
            Text(
                text = primaryLabel,
                color = primaryTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = titleSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subjectName.isBlank() && className.isNotBlank()) {
                Text(
                    text = trEn("Yalnızca sınıf atanmış", "Only class assigned"),
                    color = secondaryTextColor,
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Text(
                text = trEn("Boş", "Empty"),
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = titleSize,
                maxLines = 1,
            )
            if (!compact) {
                Text(
                    text = trEn("Atama yok", "Not assigned"),
                    color = secondaryTextColor,
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ProgramHealthCard(
    health: ProgramHealth,
    trEn: (String, String) -> String,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = trEn("Program SaÄŸlÄ±ÄŸÄ±", "Program Health"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HealthPill(
                    modifier = Modifier.weight(1f),
                    value = health.missingAssignmentCount.toString(),
                    label = trEn("Eksik Atama", "Missing Assignments"),
                )
                HealthPill(
                    modifier = Modifier.weight(1f),
                    value = health.notedLessonCount.toString(),
                    label = trEn("Notlu Ders", "Lessons With Notes"),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HealthPill(
                    modifier = Modifier.weight(1f),
                    value = health.emptyDayCount.toString(),
                    label = trEn("BoÅŸ GÃ¼n", "Empty Days"),
                )
                HealthPill(
                    modifier = Modifier.weight(1f),
                    value = health.overlapCount.toString(),
                    label = trEn("Saat Ã‡akÄ±ÅŸmasÄ±", "Time Conflicts"),
                )
            }
        }
    }
}

@Composable
private fun HealthPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProgramHealthCardClean(
    health: ProgramHealth,
    trEn: (String, String) -> String,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = trEn("Program Sağlığı", "Program Health"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HealthPill(
                    modifier = Modifier.weight(1f),
                    value = health.missingAssignmentCount.toString(),
                    label = trEn("Eksik Atama", "Missing Assignments"),
                )
                HealthPill(
                    modifier = Modifier.weight(1f),
                    value = health.notedLessonCount.toString(),
                    label = trEn("Notlu Ders", "Lessons With Notes"),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HealthPill(
                    modifier = Modifier.weight(1f),
                    value = health.emptyDayCount.toString(),
                    label = trEn("Boş Gün", "Empty Days"),
                )
                HealthPill(
                    modifier = Modifier.weight(1f),
                    value = health.overlapCount.toString(),
                    label = trEn("Saat Çakışması", "Time Conflicts"),
                )
            }
        }
    }
}

@Composable
private fun WeeklyLessonEditorDialog(
    state: CellEditorState,
    classes: List<com.zilagent.app.data.entity.SchoolClass>,
    subjects: List<com.zilagent.app.data.entity.SchoolSubject>,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Long?, Long?, String) -> Unit,
    trEn: (String, String) -> String,
) {
    var selectedClassId by remember(state.dayOfWeek, state.lesson.lessonOrder) { mutableStateOf(state.lesson.entry?.classId) }
    var selectedSubjectId by remember(state.dayOfWeek, state.lesson.lessonOrder) { mutableStateOf(state.lesson.entry?.subjectId) }
    var noteText by remember(state.dayOfWeek, state.lesson.lessonOrder) { mutableStateOf(state.lesson.note?.note.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("${state.dayLabel} • ${state.lesson.bell.name}")
                Text(
                    "${minutesToTime(state.lesson.bell.startTime)} - ${minutesToTime(state.lesson.bell.endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
        },
        text = {
            Column {
                SyllabusDropDown(
                    label = trEn("Sınıf", "Class"),
                    items = classes,
                    selectedId = selectedClassId,
                    onSelected = { selectedClassId = it },
                    modifier = Modifier.fillMaxWidth(),
                    getName = { it.name },
                )
                Spacer(modifier = Modifier.height(10.dp))
                SyllabusDropDown(
                    label = trEn("Ders", "Subject"),
                    items = subjects,
                    selectedId = selectedSubjectId,
                    onSelected = { selectedSubjectId = it },
                    modifier = Modifier.fillMaxWidth(),
                    getName = { it.name },
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(trEn("Not", "Note")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        state.dayOfWeek,
                        state.lesson.lessonOrder,
                        selectedClassId,
                        selectedSubjectId,
                        noteText,
                    )
                },
            ) {
                Text(trEn("Kaydet", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(trEn("İptal", "Cancel"))
            }
        },
    )
}

@Composable
private fun WeeklyCopyDialog(
    dayLabels: List<String>,
    onDismiss: () -> Unit,
    onCopy: (Int, Int) -> Unit,
    trEn: (String, String) -> String,
) {
    var fromDay by remember { mutableIntStateOf(1) }
    var toDay by remember { mutableIntStateOf(2) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(trEn("Gün Kopyala", "Copy Day")) },
        text = {
            Column {
                DayDropDownField(
                    label = trEn("Kaynak", "Source"),
                    selectedDay = fromDay,
                    dayLabels = dayLabels,
                    onSelected = { fromDay = it },
                )
                Spacer(modifier = Modifier.height(10.dp))
                DayDropDownField(
                    label = trEn("Hedef", "Target"),
                    selectedDay = toDay,
                    dayLabels = dayLabels,
                    onSelected = { toDay = it },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onCopy(fromDay, toDay) }, enabled = fromDay != toDay) {
                Text(trEn("Kopyala", "Copy"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(trEn("İptal", "Cancel"))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDropDownField(
    label: String,
    selectedDay: Int,
    dayLabels: List<String>,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val onSurface = MaterialTheme.colorScheme.onSurface
    OutlinedCard(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)),
        border = CardDefaults.outlinedCardBorder(enabled = true),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = onSurface.copy(alpha = 0.6f))
                Text(dayLabels[selectedDay - 1], style = MaterialTheme.typography.bodyLarge, color = onSurface, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = onSurface)
        }
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        dayLabels.forEachIndexed { index, dayLabel ->
            DropdownMenuItem(
                text = { Text(dayLabel) },
                onClick = {
                    onSelected(index + 1)
                    expanded = false
                },
            )
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
    getName: (T) -> String,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedItem = items.find {
        when (it) {
            is com.zilagent.app.data.entity.SchoolClass -> it.id == selectedId
            is com.zilagent.app.data.entity.SchoolSubject -> it.id == selectedId
            else -> false
        }
    }
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)),
            border = CardDefaults.outlinedCardBorder(enabled = true),
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedItem?.let { getName(it) } ?: label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedItem != null) onSurface else onSurface.copy(alpha = 0.55f),
                        maxLines = 1,
                    )
                }
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = onSurface, modifier = Modifier.size(18.dp))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(if (LocalAppLanguage.current == AppLanguage.EN) "Not selected" else "Seçilmedi") },
                onClick = {
                    onSelected(null)
                    expanded = false
                },
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
                    },
                )
            }
        }
    }
}

private fun resolveClassName(
    entry: SyllabusEntry?,
    classes: List<com.zilagent.app.data.entity.SchoolClass>,
): String {
    val classId = entry?.classId ?: return ""
    return classes.firstOrNull { it.id == classId }?.name.orEmpty()
}

private fun resolveClassColorHex(
    entry: SyllabusEntry?,
    classes: List<com.zilagent.app.data.entity.SchoolClass>,
): String? {
    val classId = entry?.classId ?: return null
    return classes.firstOrNull { it.id == classId }?.colorHex
}

private fun resolveSubjectName(
    entry: SyllabusEntry?,
    subjects: List<com.zilagent.app.data.entity.SchoolSubject>,
): String {
    val subjectId = entry?.subjectId ?: return ""
    return subjects.firstOrNull { it.id == subjectId }?.name.orEmpty()
}

private fun minutesToTime(minutes: Int): String {
    val hour = minutes / 60
    val minute = minutes % 60
    return "%02d:%02d".format(hour, minute)
}

private fun parseColorOrNull(colorHex: String?): Color? {
    if (colorHex.isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: IllegalArgumentException) {
        null
    }
}

