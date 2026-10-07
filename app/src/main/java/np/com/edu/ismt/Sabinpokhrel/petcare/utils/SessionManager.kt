package np.com.edu.ismt.Sabinpokhrel.petcare.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Handles session persistence so the user stays logged in across app restarts.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun createSession(userId: Long, name: String, email: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getUserName(): String = prefs.getString(KEY_NAME, "") ?: ""

    fun getUserEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    fun updateUserName(name: String) {
        prefs.edit().putString(KEY_NAME, name).apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "petcare_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}
