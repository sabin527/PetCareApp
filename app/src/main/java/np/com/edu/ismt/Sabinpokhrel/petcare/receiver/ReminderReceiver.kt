package np.com.edu.ismt.Sabinpokhrel.petcare.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import np.com.edu.ismt.Sabinpokhrel.petcare.PetCareApp
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.dashboard.DashboardActivity

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Pet care task"
        val petName = intent.getStringExtra(EXTRA_PET_NAME) ?: ""
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: ""

        val openIntent = Intent(context, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = androidx.core.app.TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(openIntent)
            getPendingIntent(
                taskId.toInt(),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, PetCareApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$category reminder for $petName")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_PET_NAME = "extra_pet_name"
        const val EXTRA_CATEGORY = "extra_category"
    }
}
