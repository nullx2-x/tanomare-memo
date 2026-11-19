package app.nullx2.tanomare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val requester: String,
    val deadlineMillis: Long,
    val note: String = "",
    val isDone: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val doneAtMillis: Long? = null
)
