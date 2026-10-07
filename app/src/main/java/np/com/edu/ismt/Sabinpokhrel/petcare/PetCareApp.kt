package np.com.edu.ismt.Sabinpokhrel.petcare

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.AppDatabase

class PetCareApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    companion object {
        const val REMINDER_CHANNEL_ID = "petcare_reminders"
        lateinit var instance: PetCareApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        np.com.edu.ismt.Sabinpokhrel.petcare.utils.AppPreferences.applySavedTheme(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Pet Care Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for feeding, medication, grooming and other pet care tasks"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
