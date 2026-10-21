package com.rtk.devil.autoreply.integration.notificationreply

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.rtk.devil.autoreply.data.local.AppSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class TelegramNotificationListener : NotificationListenerService() {

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val settingsRepository by lazy {
        AppSettingsRepository(applicationContext)
    }

    private val state by lazy {
        getSharedPreferences("rtk_telegram_notification_reply_state", MODE_PRIVATE)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in SUPPORTED_PACKAGES || sbn.isOngoing) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()?.trim().orEmpty()
        val text = (
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT)
            )?.toString()?.trim().orEmpty()

        if (title.isBlank() || text.isBlank()) return

        val chatHash = sha256("${sbn.packageName}|$title")
        val signature = sha256("${sbn.packageName}|${sbn.key}|$title|$text")

        serviceScope.launch {
            try {
                val settings = settingsRepository.settings.first()
                if (!settings.telegramNotificationEnabled) return@launch

                if (state.getString("last_signature", null) == signature) {
                    return@launch
                }

                val welcomed = state.getBoolean("welcomed_$chatHash", false)
                val replyText = when {
                    settings.telegramWelcomeEnabled && !welcomed ->
                        listOf(
                            settings.telegramWelcomeMessage1.trim(),
                            settings.telegramWelcomeMessage2.trim()
                        ).filter { it.isNotBlank() }.joinToString("\n\n")

                    settings.telegramEveryMessageEnabled ->
                        settings.telegramCustomReply.trim()

                    else -> ""
                }

                if (replyText.isBlank()) return@launch

                val sent = withContext(Dispatchers.Main) {
                    sendNotificationReply(notification, replyText)
                }

                if (sent) {
                    state.edit().putString("last_signature", signature).apply()
                    if (settings.telegramWelcomeEnabled && !welcomed) {
                        state.edit().putBoolean("welcomed_$chatHash", true).apply()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Reply processing failed: ${e.javaClass.simpleName}")
            }
        }
    }

    private fun sendNotificationReply(
        notification: Notification,
        replyText: String
    ): Boolean {
        for (action in notification.actions ?: return false) {
            val remoteInputs = action.remoteInputs ?: continue
            if (remoteInputs.isEmpty()) continue
            val pendingIntent: PendingIntent = action.actionIntent ?: continue
            val resultKey = remoteInputs.firstOrNull {
                it.resultKey.isNotBlank()
            }?.resultKey ?: continue

            val fillInIntent = Intent()
            val results = android.os.Bundle().apply {
                putCharSequence(resultKey, replyText)
            }
            RemoteInput.addResultsToIntent(remoteInputs, fillInIntent, results)

            try {
                pendingIntent.send(applicationContext, 0, fillInIntent)
                return true
            } catch (e: PendingIntent.CanceledException) {
                Log.w(TAG, "Reply action expired or cancelled")
            } catch (e: Exception) {
                Log.w(TAG, "Reply action failed: ${e.javaClass.simpleName}")
            }
        }
        return false
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "RTKTelegramReply"
        private val SUPPORTED_PACKAGES = setOf(
            "org.telegram.messenger",
            "org.thunderdog.challegram"
        )
    }
}
