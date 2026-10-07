package np.com.edu.ismt.Sabinpokhrel.petcare.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Stores lightweight app-wide preferences: dark mode, whether task reminders
 * are globally enabled, and preferred weight unit (kg/lb).
 */
class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        applyTheme(enabled)
    }

    fun areRemindersEnabled(): Boolean = prefs.getBoolean(KEY_REMINDERS_ENABLED, true)

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()
    }

    /** Per-category reminder toggle, keyed by TaskCategory name (e.g. "FEEDING"). Defaults to on. */
    fun isCategoryReminderEnabled(categoryName: String): Boolean =
        prefs.getBoolean(KEY_CATEGORY_PREFIX + categoryName, true)

    fun setCategoryReminderEnabled(categoryName: String, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CATEGORY_PREFIX + categoryName, enabled).apply()
    }

    /** "kg" or "lb". Defaults to "kg". */
    fun getWeightUnit(): String = prefs.getString(KEY_WEIGHT_UNIT, "kg") ?: "kg"

    fun toggleWeightUnit(): String {
        val next = if (getWeightUnit() == "kg") "lb" else "kg"
        prefs.edit().putString(KEY_WEIGHT_UNIT, next).apply()
        return next
    }

    companion object {
        private const val PREFS_NAME = "petcare_app_prefs"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_CATEGORY_PREFIX = "reminder_category_"
        private const val KEY_WEIGHT_UNIT = "weight_unit"

        /** Call once at app startup (see PetCareApp.onCreate) to restore the saved theme. */
        fun applySavedTheme(context: Context) {
            val enabled = AppPreferences(context).isDarkModeEnabled()
            applyTheme(enabled)
        }

        private fun applyTheme(darkModeEnabled: Boolean) {
            AppCompatDelegate.setDefaultNightMode(
                if (darkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}
