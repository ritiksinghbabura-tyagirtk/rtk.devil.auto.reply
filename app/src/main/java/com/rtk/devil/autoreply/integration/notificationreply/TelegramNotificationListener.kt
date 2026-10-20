package com.rtk.devil.autoreply.integration.notificationreply

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Bundle
import android.util.Log
import com.rtk.devil.autoreply.data.local.AppSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest

/**
 * Telegram notification reply adapter.
 *
 * Uses Android's notification RemoteInput action only.
 * It does not log message contents or store Telegram credentials.
 */
class TelegramNotificationListener : NotificationListenerService() {

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val processingMutex = Mutex()

    private lateinit var settingsRepository: AppSettingsRepository

    private val replyState by lazy {
        getSharedPreferences("rtk_reply_state", MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        settingsRepository = AppSettingsRepository(applicationContext)
        Log.i(TAG, "Notification reply engine created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        if (sbn.packageName !in SUPPORTED_TELEGRAM_PACKAGES) return
        if ((sbn.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0) return

        serviceScope.launch {
            processingMutex.withLock {
                try {
                    processNotification(sbn)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not process Telegram notification", e)
                }
            }
        }
    }

    private suspend fun processNotification(
        sbn: StatusBarNotification
    ) {
        val settings = settingsRepository.settings.first()

        if (!settings.autoReplyEnabled) return
        if (!settings.everyMessageEnabled && !settings.welcomeEnabled) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Notification text can be absent or generic depending on Telegram settings.
        val messageText = (
            extras.getCharSequence(Notification.EXTRA_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                    ?.lastOrNull()
            )?.toString()?.trim().orEmpty()

        if (messageText.isBlank()) return

        // Don't reply to the same notification content repeatedly.
        // Only hashes are stored, never the message text itself.
        val notificationSignature = sha256(
            sbn.key + "|" + sha256(messageText)
        )
        val lastSignature = replyState.getString(
            "last_reply_signature",
            null
        )
        if (notificationSignature == lastSignature) return

        val title = (
            extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
                ?: extras.getCharSequence(Notification.EXTRA_TITLE)
            )?.toString()?.trim().orEmpty()

        // When Every Message is OFF, use Welcome Message once per contact title.
        // If Telegram doesn't provide a title, fall back to notification key.
        if (!settings.everyMessageEnabled && settings.welcomeEnabled) {
            val contactIdentity = if (title.isNotBlank()) title else sbn.key
            val contactKey = "welcomed_" + sha256(contactIdentity)

            if (replyState.getBoolean(contactKey, false)) return

            if (sendReply(notification, settings.welcomeMessage)) {
                replyState.edit()
                    .putBoolean(contactKey, true)
                    .putString("last_reply_signature", notificationSignature)
                    .apply()
                Log.i(TAG, "Welcome reply sent through notification action")
            }
            return
        }

        if (!settings.everyMessageEnabled) return

        if (sendReply(notification, settings.welcomeMessage)) {
            replyState.edit()
                .putString("last_reply_signature", notificationSignature)
                .apply()
            Log.i(TAG, "Automatic reply sent through notification action")
        }
    }

    private fun sendReply(
        notification: Notification,
        replyText: String
    ): Boolean {
        if (replyText.isBlank()) return false

        val actions = notification.actions ?: return false

        for (action in actions) {
            val pendingIntent = action.actionIntent ?: continue
            val remoteInputs = action.remoteInputs ?: continue
            if (remoteInputs.isEmpty()) continue

            // Prefer a free-form text input if the notification provides one.
            val usableInputs = remoteInputs.filter {
                it.allowFreeFormInput
            }.ifEmpty {
                remoteInputs.toList()
            }

            if (usableInputs.isEmpty()) continue

            val fillInIntent = Intent()
            val results = Bundle()

            for (remoteInput in usableInputs) {
                results.putCharSequence(remoteInput.resultKey, replyText)
            }

            RemoteInput.addResultsToIntent(
                usableInputs.toTypedArray(),
                fillInIntent,
                results
            )

            return try {
                pendingIntent.send(this, 0, fillInIntent)
                true
            } catch (e: Exception) {
                Log.w(TAG, "Notification reply action failed", e)
                false
            }
        }

        // Telegram may omit Reply action for some notifications.
        return false
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val TAG = "RTKDevilNotification"

        private val SUPPORTED_TELEGRAM_PACKAGES = setOf(
            "org.telegram.messenger",
            "org.thunderdog.challegram"
        )
    }
}
