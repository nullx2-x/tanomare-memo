package app.nullx2.tanomare.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.nullx2.tanomare.data.TaskEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/** 新規追加の初期期限：今日の18時。過ぎていれば明日の18時。 */
private fun defaultDeadline(): LocalDateTime {
    val today18 = LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0))
    return if (today18.isAfter(LocalDateTime.now())) today18 else today18.plusDays(1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    initial: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, requester: String, deadlineMillis: Long, note: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var requester by remember { mutableStateOf(initial?.requester ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var deadline by remember {
        mutableStateOf(initial?.deadlineMillis?.toLocalDateTime() ?: defaultDeadline())
    }
    var titleError by remember { mutableStateOf(false) }
    var requesterError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "頼まれごとを追加" else "頼まれごとを編集") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("件名（必須）") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("件名を入力してください") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = requester,
                    onValueChange = { requester = it; requesterError = false },
                    label = { Text("頼んだ人（必須）") },
                    isError = requesterError,
                    supportingText = if (requesterError) {
                        { Text("頼んだ人を入力してください") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Text("日付：${deadline.year}/${deadline.monthValue}/${deadline.dayOfMonth}")
                    }
                    OutlinedButton(onClick = { showTimePicker = true }) {
                        Text("時刻：%02d:%02d".format(deadline.hour, deadline.minute))
                    }
                }
                Text(
                    text = "期限：${formatDeadline(deadline.toMillis())}",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("メモ（任意）") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                titleError = title.isBlank()
                requesterError = requester.isBlank()
                if (!titleError && !requesterError) {
                    onSave(title, requester, deadline.toMillis(), note)
                }
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("削除") }
                }
                TextButton(onClick = onDismiss) { Text("キャンセル") }
            }
        }
    )

    if (showDatePicker) {
        // Material3 DatePicker は UTC 基準のミリ秒で日付を扱う
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = deadline.toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { picked ->
                        val pickedDate = java.time.Instant.ofEpochMilli(picked)
                            .atZone(ZoneOffset.UTC).toLocalDate()
                        deadline = LocalDateTime.of(pickedDate, deadline.toLocalTime())
                    }
                    showDatePicker = false
                }) { Text("決定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("キャンセル") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = deadline.hour,
            initialMinute = deadline.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("時刻を選択") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    deadline = deadline.withHour(timePickerState.hour)
                        .withMinute(timePickerState.minute)
                    showTimePicker = false
                }) { Text("決定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("キャンセル") }
            }
        )
    }
}
