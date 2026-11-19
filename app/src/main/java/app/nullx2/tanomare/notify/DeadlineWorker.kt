package app.nullx2.tanomare.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.nullx2.tanomare.data.AppDatabase

/**
 * 期限時刻に WorkManager から起動され、タスクがまだ未完了なら通知を出す。
 * WorkManager のジョブは端末再起動後も復元されるため、AlarmManager の
 * 正確なアラーム権限（SCHEDULE_EXACT_ALARM）なしで期限通知が成立する。
 */
class DeadlineWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        if (taskId < 0) return Result.failure()

        // スケジュール後に完了・削除された場合は通知しない
        val task = AppDatabase.get(applicationContext).taskDao().getById(taskId)
            ?: return Result.success()
        if (task.isDone) return Result.success()

        Notifications.showDeadlineNotification(
            applicationContext,
            task.id,
            task.title,
            task.requester
        )
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
    }
}
