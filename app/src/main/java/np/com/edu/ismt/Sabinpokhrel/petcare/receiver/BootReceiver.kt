package np.com.edu.ismt.Sabinpokhrel.petcare.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import np.com.edu.ismt.Sabinpokhrel.petcare.PetCareApp
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ReminderScheduler
import kotlinx.coroutines.launch

/**
 * Re-schedules all pending task reminders after a device reboot, since
 * AlarmManager alarms are cleared on restart.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        val app = appContext as PetCareApp

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            val db = app.database
            val pets = db.petDao().getAllPetsSync().associateBy { it.petId }
            val tasks = db.careTaskDao().getAllUpcomingIncompleteTasks()
            tasks.forEach { task ->
                val petName = pets[task.petId]?.name ?: ""
                ReminderScheduler.schedule(appContext, task, petName)
            }
        }
    }
}
