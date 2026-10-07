package np.com.edu.ismt.Sabinpokhrel.petcare.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.receiver.ReminderReceiver

/**
 * Schedules and cancels local notification reminders for care tasks
 * (feeding, medication, grooming, vet appointments, etc.) using AlarmManager.
 */
object ReminderScheduler {

    fun schedule(context: Context, task: CareTask, petName: String) {
        if (!task.remindersEnabled || task.isCompleted) return
        if (task.scheduledAt <= System.currentTimeMillis()) return

        val prefs = AppPreferences(context)
        if (!prefs.areRemindersEnabled()) return
        if (!prefs.isCategoryReminderEnabled(task.category.name)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TASK_ID, task.taskId)
            putExtra(ReminderReceiver.EXTRA_TITLE, task.title)
            putExtra(ReminderReceiver.EXTRA_PET_NAME, petName)
            putExtra(ReminderReceiver.EXTRA_CATEGORY, task.category.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, task.scheduledAt, pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, task.scheduledAt, pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, task.scheduledAt, pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Exact alarm permission not granted; reminder silently skipped.
        }
    }

    fun cancel(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
