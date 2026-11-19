package app.nullx2.tanomare.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.nullx2.tanomare.data.TaskEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TanomareApp(viewModel: TanomareViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var editorTarget by remember { mutableStateOf<TaskEntity?>(null) }
    var editorOpen by remember { mutableStateOf(false) }

    val visibleTasks = remember(tasks, filter) {
        when (filter) {
            TaskFilter.ACTIVE -> tasks.filter { !it.isDone }
            TaskFilter.DONE -> tasks.filter { it.isDone }
            TaskFilter.ALL -> tasks
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("頼まれごとメモ") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editorTarget = null
                editorOpen = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "頼まれごとを追加")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.label) }
                    )
                }
            }

            if (visibleTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (filter) {
                            TaskFilter.ACTIVE -> "未完了の頼まれごとはありません。\n右下の＋から追加できます。"
                            TaskFilter.DONE -> "完了した頼まれごとはまだありません。"
                            TaskFilter.ALL -> "頼まれごとはまだありません。\n右下の＋から追加できます。"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visibleTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggleDone = { done -> viewModel.setDone(task, done) },
                            onClick = {
                                editorTarget = task
                                editorOpen = true
                            },
                            onDelete = {
                                viewModel.delete(task)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "「${task.title}」を削除しました",
                                        actionLabel = "元に戻す"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restore(task)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (editorOpen) {
        TaskEditDialog(
            initial = editorTarget,
            onDismiss = { editorOpen = false },
            onSave = { title, requester, deadlineMillis, note ->
                val target = editorTarget
                if (target == null) {
                    viewModel.add(title, requester, deadlineMillis, note)
                } else {
                    viewModel.update(
                        target.copy(
                            title = title,
                            requester = requester,
                            deadlineMillis = deadlineMillis,
                            note = note
                        )
                    )
                }
                editorOpen = false
            },
            onDelete = editorTarget?.let { target ->
                {
                    viewModel.delete(target)
                    editorOpen = false
                }
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggleDone: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val state = deadlineState(task.deadlineMillis)
    val overdue = !task.isDone && state == DeadlineState.OVERDUE
    val today = !task.isDone && state == DeadlineState.TODAY

    val containerColor = when {
        task.isDone -> MaterialTheme.colorScheme.surfaceVariant
        overdue -> MaterialTheme.colorScheme.errorContainer
        today -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val deadlineColor = when {
        task.isDone -> MaterialTheme.colorScheme.onSurfaceVariant
        overdue -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggleDone(it) }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (today || overdue) FontWeight.Bold else FontWeight.Normal,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "頼んだ人：${task.requester}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "期限：${formatDeadline(task.deadlineMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = deadlineColor,
                    fontWeight = if (overdue || today) FontWeight.Bold else FontWeight.Normal
                )
                if (!task.isDone) {
                    relativeLabel(task.deadlineMillis)?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = deadlineColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (task.note.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = task.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "削除")
            }
            Spacer(Modifier.width(0.dp))
        }
    }
}
