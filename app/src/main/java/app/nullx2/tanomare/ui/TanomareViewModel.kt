package app.nullx2.tanomare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.nullx2.tanomare.data.AppDatabase
import app.nullx2.tanomare.data.TaskEntity
import app.nullx2.tanomare.notify.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val label: String) {
    ACTIVE("未完了"),
    DONE("完了"),
    ALL("すべて")
}

class TanomareViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.get(application).taskDao()

    val tasks: StateFlow<List<TaskEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _filter = MutableStateFlow(TaskFilter.ACTIVE)
    val filter: StateFlow<TaskFilter> = _filter

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun add(title: String, requester: String, deadlineMillis: Long, note: String) {
        viewModelScope.launch {
            val id = dao.insert(
                TaskEntity(
                    title = title.trim(),
                    requester = requester.trim(),
                    deadlineMillis = deadlineMillis,
                    note = note.trim()
                )
            )
            dao.getById(id)?.let { NotificationScheduler.schedule(getApplication(), it) }
        }
    }

    fun update(task: TaskEntity) {
        viewModelScope.launch {
            dao.update(task)
            NotificationScheduler.schedule(getApplication(), task)
        }
    }

    fun setDone(task: TaskEntity, done: Boolean) {
        update(
            task.copy(
                isDone = done,
                doneAtMillis = if (done) System.currentTimeMillis() else null
            )
        )
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch {
            dao.delete(task)
            NotificationScheduler.cancel(getApplication(), task.id)
        }
    }

    /** 削除の取り消し用：同じ内容で再登録し、通知も張り直す。 */
    fun restore(task: TaskEntity) {
        viewModelScope.launch {
            dao.insert(task)
            NotificationScheduler.schedule(getApplication(), task)
        }
    }
}
