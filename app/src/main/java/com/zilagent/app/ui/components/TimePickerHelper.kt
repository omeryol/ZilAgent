package com.zilagent.app.ui.components

import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@Composable
fun ShowTimePicker(
    initialHour: Int = 8,
    initialMinute: Int = 30,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, initialHour)
    calendar.set(Calendar.MINUTE, initialMinute)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            val formattedTime = String.format("%02d:%02d", hour, minute)
            onTimeSelected(formattedTime)
        },
        initialHour,
        initialMinute,
        true // 24 hour format
    )

    timePickerDialog.show()
}

// Since TimePickerDialog.show() is a side effect and not a composable that stays on screen,
// it's usually better to wrap it in a function that returns a Dialog or triggered by a Click.
// But for Compose, commonly we use a logic wrapper.
// Let's create a functional helper that simply instantiates it.

fun launchTimePicker(
    context: android.content.Context,
    initialTime: String, // "HH:MM"
    onTimeSelected: (String) -> Unit
) {
    val (hour, minute) = try {
        initialTime.split(":").map { it.toInt() }
    } catch (e: Exception) {
        listOf(8, 30)
    }

    TimePickerDialog(
        context,
        { _, h, m ->
            val formattedTime = String.format("%02d:%02d", h, m)
            onTimeSelected(formattedTime)
        },
        hour,
        minute,
        true
    ).show()
}

fun launchDatePicker(
    context: android.content.Context,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
