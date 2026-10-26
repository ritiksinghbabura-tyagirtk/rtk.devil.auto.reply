package com.rtk.devil.autoreply.integration.whatsapp

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
import java.util.LinkedHashSet
import java.util.concurrent.ConcurrentHashMap

class WhatsAppNotificationListener : NotificationListenerService() {

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val settingsRepository by lazy {
        AppSettingsRepository(applicationContext)
    }

    private val state by lazy {
        getSharedPreferences("rtk_whatsapp_reply_state", MODE_PRIVATE)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in SUPPORTED_PACKAGES) return
        if (sbn.isOngoing) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()?.trim().orEmpty()

        val text = (
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT)
            )?.toString()?.trim().orEmpty()

        if (text.isBlank() || title.isBlank()) return

        // Never store or log incoming message text.
        val chatHash = sha256("${sbn.packageName}|$title")
        val notificationHash = sha256(
            "${sbn.packageName}|${sbn.key}|$title|$text"
        )

        // Prevent duplicate callbacks from processing the same notification concurrently.
        if (!inFlightSignatures.add(notificationHash)) return

        serviceScope.launch {
            try {
                val settings = settingsRepository.settings.first()
                if (!settings.whatsappAutoReplyEnabled) return@launch

                // Keep a bounded history so repeated callbacks do not send another reply.
                val alreadyProcessed = synchronized(state) {
                    val recent = state.getStringSet("recent_signatures", null)
                    notificationHash in (recent ?: emptySet()) ||
                        state.getString("last_signature", null) == notificationHash
                }
                if (alreadyProcessed) return@launch

                val alreadyWelcomed =
                    state.getBoolean("welcomed_$chatHash", false)

                val replyText = when {
                    settings.whatsappWelcomeEnabled && !alreadyWelcomed -> listOf(
                        settings.whatsappWelcomeMessage1.trim(),
                        settings.whatsappWelcomeMessage2.trim()
                    ).filter { it.isNotBlank() }.joinToString("\\n\\n")

                    settings.whatsappEveryMessageEnabled ->
                        settings.whatsappCustomReply.trim()

                    else -> ""
                }

                if (replyText.isBlank()) return@launch

                val sent = withContext(Dispatchers.Main) {
                    sendNotificationReply(notification, replyText)
                }

                if (sent) {
                    synchronized(state) {
                        val recent = LinkedHashSet<String>(
                            state.getStringSet("recent_signatures", null)
                                ?: emptySet()
                        )
                        recent.add(notificationHash)

                        while (recent.size > 100) {
                            val iterator = recent.iterator()
                            if (iterator.hasNext()) {
                                iterator.next()
                                iterator.remove()
                            } else {
                                break
                            }
                        }

                        state.edit()
                            .putString("last_signature", notificationHash)
                            .putStringSet("recent_signatures", recent)
                            .commit()

                        if (settings.whatsappWelcomeEnabled && !alreadyWelcomed) {
                            state.edit()
                                .putBoolean("welcomed_$chatHash", true)
                                .commit()
                        }
                    }
                }
            } catch (e: Exception) {
                // Never log incoming message contents.
                Log.w(
                    TAG,
                    "Notification reply processing failed: ${e.javaClass.simpleName}"
                )
            } finally {
                inFlightSignatures.remove(notificationHash)
            }
        }

    }

    private fun sendNotificationReply(
        notification: Notification,
        replyText: String
    ): Boolean {
        val actions = notification.actions ?: return false

        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            if (remoteInputs.isEmpty()) continue

            val pendingIntent: PendingIntent =
                action.actionIntent ?: continue

            val resultKey = remoteInputs.firstOrNull {
                it.resultKey.isNotBlank()
            }?.resultKey ?: continue

            val fillInIntent = Intent()
            val results = android.os.Bundle().apply {
                putCharSequence(resultKey, replyText)
            }

            RemoteInput.addResultsToIntent(
                remoteInputs,
                fillInIntent,
                results
            )

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
        private val inFlightSignatures =
            ConcurrentHashMap.newKeySet<String>()

        private const val TAG = "RTKWhatsAppReply"

        val SUPPORTED_PACKAGES = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b"
        )
    }
}
