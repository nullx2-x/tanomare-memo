# Tanomare Memo ProGuard rules
# WorkManager instantiates workers by class name via reflection.
-keep class app.nullx2.tanomare.notify.DeadlineWorker { *; }
# Room entities / database
-keep class app.nullx2.tanomare.data.** { *; }
