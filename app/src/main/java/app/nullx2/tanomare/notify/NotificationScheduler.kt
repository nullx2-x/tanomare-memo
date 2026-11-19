package app.nullx2.tanomare.notify

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.nullx2.tanomare.data.TaskEntity
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private fun workName(taskId: Long) = "deadline_$taskId"

    /** タスクの期限に合わせて通知ジョブを（再）登録する。完了済み・期限切れは解除。 */
    fun schedule(context: Context, task: TaskEntity) {
        val workManager = WorkManager.getInstance(context)
        val delayMillis = task.deadlineMillis - System.currentTimeMillis()
        if (task.isDone || delayMillis <= 0) {
            workManager.cancelUniqueWork(workName(task.id))
            return
        }
        val request = OneTimeWorkRequestBuilder<DeadlineWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(DeadlineWorker.KEY_TASK_ID to task.id))
            .addTag("deadline")
            .build()
        workManager.enqueueUniqueWork(
            workName(task.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }
}
