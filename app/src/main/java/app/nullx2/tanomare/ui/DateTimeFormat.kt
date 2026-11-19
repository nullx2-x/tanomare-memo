package app.nullx2.tanomare.ui

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun LocalDateTime.toMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

private val dayOfWeekJa = arrayOf("月", "火", "水", "木", "金", "土", "日")

/** 例: 2026年11月20日(金) 15:00 */
fun formatDeadline(millis: Long): String {
    val dt = millis.toLocalDateTime()
    val dow = dayOfWeekJa[dt.dayOfWeek.value - 1]
    return "%d年%d月%d日(%s) %02d:%02d".format(
        dt.year, dt.monthValue, dt.dayOfMonth, dow, dt.hour, dt.minute
    )
}

enum class DeadlineState { OVERDUE, TODAY, TOMORROW, FUTURE }

fun deadlineState(millis: Long): DeadlineState {
    val today = LocalDate.now()
    val date = millis.toLocalDateTime().toLocalDate()
    return when {
        millis < System.currentTimeMillis() -> DeadlineState.OVERDUE
        date == today -> DeadlineState.TODAY
        date == today.plusDays(1) -> DeadlineState.TOMORROW
        else -> DeadlineState.FUTURE
    }
}

/** 一覧カードに出す相対ラベル（期限切れ / 今日 / 明日 / あとN日）。 */
fun relativeLabel(millis: Long): String? {
    val today = LocalDate.now()
    val date = millis.toLocalDateTime().toLocalDate()
    return when (deadlineState(millis)) {
        DeadlineState.OVERDUE -> "期限切れ"
        DeadlineState.TODAY -> "今日まで"
        DeadlineState.TOMORROW -> "明日まで"
        DeadlineState.FUTURE -> {
            val days = ChronoUnit.DAYS.between(today, date)
            "あと${days}日"
        }
    }
}
