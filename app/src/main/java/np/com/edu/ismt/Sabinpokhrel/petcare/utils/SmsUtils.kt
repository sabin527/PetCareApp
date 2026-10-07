package np.com.edu.ismt.Sabinpokhrel.petcare.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

/**
 * Sends an SMS straight from the app (no messaging app is opened).
 *
 * The system reports back once the message has left the phone, and only then
 * is the caller told it was SENT. Any failure comes back with a readable reason.
 */
object SmsUtils {

    private const val ACTION_PREFIX = "np.com.edu.ismt.Sabinpokhrel.petcare.SMS_SENT."
    private const val CONFIRM_TIMEOUT_MS = 30_000L
    private var sendCounter = 0

    enum class Result {
        /** The system confirmed the message was sent. */
        SENT,
        /** The message was handed to the phone but no confirmation arrived in time. */
        NOT_CONFIRMED,
        /** The phone refused because a permission is missing (e.g. "getGroupIdLevel1"). */
        PERMISSION_DENIED,
        /** Sending failed; the detail text says why. */
        FAILED
    }

    /**
     * @param onResult called on the main thread with the outcome and, when
     *                 something went wrong, a human-readable explanation.
     */
    fun sendSms(
        context: Context,
        phoneNumber: String,
        message: String,
        onResult: (Result, String?) -> Unit
    ) {
        val appContext = context.applicationContext
        val smsManager = getSmsManager(appContext)
        if (smsManager == null) {
            onResult(Result.FAILED, "This device can't send SMS messages.")
            return
        }

        // A unique action per send keeps results from different sends apart.
        val action = ACTION_PREFIX + (sendCounter++)
        val handler = Handler(Looper.getMainLooper())
        var receiver: BroadcastReceiver? = null
        var finished = false

        fun finish(result: Result, detail: String?) {
            if (finished) return
            finished = true
            handler.removeCallbacksAndMessages(null)
            receiver?.let { runCatching { appContext.unregisterReceiver(it) } }
            onResult(result, detail)
        }

        try {
            val parts = smsManager.divideMessage(message)
            var remaining = parts.size
            var firstError: String? = null

            // Long messages are split into parts; wait until every part reports back.
            val sentReceiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context, intent: Intent) {
                    if (resultCode != Activity.RESULT_OK && firstError == null) {
                        firstError = describeError(resultCode)
                    }
                    remaining--
                    if (remaining <= 0) {
                        val error = firstError
                        if (error == null) finish(Result.SENT, null) else finish(Result.FAILED, error)
                    }
                }
            }
            receiver = sentReceiver
            ContextCompat.registerReceiver(
                appContext,
                sentReceiver,
                IntentFilter(action),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

            val sentIntent = PendingIntent.getBroadcast(
                appContext,
                0,
                Intent(action).setPackage(appContext.packageName),
                PendingIntent.FLAG_IMMUTABLE
            )

            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent>(List(parts.size) { sentIntent })
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null)
            }

            // Sending can take a while on a weak signal, so allow plenty of time.
            handler.postDelayed({
                finish(
                    Result.NOT_CONFIRMED,
                    "The phone accepted the message but didn't confirm it was sent within " +
                            "${CONFIRM_TIMEOUT_MS / 1000} seconds. Check your messaging app's Sent " +
                            "folder before trying again."
                )
            }, CONFIRM_TIMEOUT_MS)
        } catch (e: Exception) {
            if (e is SecurityException || e.message?.contains("getGroupIdLevel1") == true) {
                finish(Result.PERMISSION_DENIED, e.message)
            } else if (e is IllegalArgumentException) {
                finish(Result.FAILED, "That phone number or message isn't valid.")
            } else {
                finish(Result.FAILED, e.message ?: "Unknown error while sending.")
            }
        }
    }

    /**
     * Uses the SIM that is set as the default for SMS, which matters on dual-SIM
     * phones, and the newer system-service SmsManager on Android 12+.
     */
    @Suppress("DEPRECATION")
    private fun getSmsManager(context: Context): SmsManager? {
        val subId = runCatching { SubscriptionManager.getDefaultSmsSubscriptionId() }.getOrDefault(-1)
        val hasSub = subId >= 0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val base = context.getSystemService(SmsManager::class.java)
            if (hasSub) base?.createForSubscriptionId(subId) else base
        } else {
            if (hasSub) SmsManager.getSmsManagerForSubscriptionId(subId) else SmsManager.getDefault()
        }
    }

    /** Turns the system's send-error codes into something a user can act on. */
    private fun describeError(code: Int): String = when (code) {
        1 -> "The phone couldn't send the message. Check your SIM, balance and signal."
        2 -> "Airplane mode is on or the phone's radio is off."
        3 -> "The message or phone number is invalid."
        4 -> "No mobile network service. Check your signal and SIM."
        5 -> "Too many messages were sent recently. Try again later."
        7, 8 -> "Messages to this kind of number aren't allowed."
        else -> "The phone couldn't send the message (error $code)."
    }
}