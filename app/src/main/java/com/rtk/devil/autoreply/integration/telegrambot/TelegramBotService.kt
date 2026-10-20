package com.rtk.devil.autoreply.integration.telegrambot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.rtk.devil.autoreply.MainActivity
import com.rtk.devil.autoreply.R
import com.rtk.devil.autoreply.data.local.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TelegramBotService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var tokenStore: BotTokenStore
    private lateinit var settingsRepository: AppSettingsRepository
    private lateinit var manager: NotificationManager
    private var pollingJobActive = false

    override fun onCreate() {
        super.onCreate()
        tokenStore = BotTokenStore(this)
        settingsRepository = AppSettingsRepository(this)
        manager = getSystemService(NotificationManager::class.java)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                saveStatus("STOPPED", "Stopped by user")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val token = tokenStore.readToken()
        if (token.isNullOrBlank()) {
            saveStatus("AUTH ERROR", "Add and verify a bot token first")
            stopSelf()
            return START_NOT_STICKY
        }

        if (!pollingJobActive) {
            pollingJobActive = true
            saveStatus("STARTING", "Connecting to Telegram")
            startForeground(NOTIFICATION_ID, notification("Connecting to Telegram"))
            scope.launch {
                pollLoop(token)
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun pollLoop(token: String) {
        val api = TelegramBotApi()
        val prefs = getSharedPreferences(STATUS_PREFS, MODE_PRIVATE)
        var offset = prefs.getLong(KEY_OFFSET, 0L)
        var retrySeconds = 2L

        try {
            // Verify saved token before polling.
            val identity = api.verifyToken(token)
            saveStatus(
                "RUNNING",
                if (identity.username.isNotBlank()) {
                    "Connected as @${identity.username}"
                } else {
                    "Connected to Telegram"
                }
            )
            updateNotification("Bot connected")

            while (currentCoroutineContext().isActive) {
                try {
                    val updates = api.getUpdates(token, offset, 20)
                    retrySeconds = 2L

                    for (update in updates) {
                        if (update.updateId >= 0) {
                            offset = maxOf(offset, update.updateId + 1)
                            // Persist offset before handling so the same update
                            // is not repeatedly processed after a restart.
                            prefs.edit().putLong(KEY_OFFSET, offset).apply()
                        }

                        if (update.senderIsBot) continue

                        val settings = settingsRepository.settings.first()
                        if (!settings.autoReplyEnabled) continue

                        val reply = when {
                            settings.welcomeEnabled &&
                                !hasWelcomed(update.chatId) -> {
                                markWelcomed(update.chatId)
                                settings.welcomeMessage
                            }
                            settings.everyMessageEnabled -> settings.welcomeMessage
                            else -> null
                        }

                        if (!reply.isNullOrBlank()) {
                            api.sendMessage(token, update.chatId, reply)
                            saveStatus("RUNNING", "Last reply sent successfully")
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    val reason = error.message
                        ?.replace(Regex("[\\r\\n]+"), " ")
                        ?.take(140)
                        ?: "Temporary connection error"
                    saveStatus("RECONNECTING", reason)
                    updateNotification("Connection interrupted; retrying")
                    delay(retrySeconds * 1000L)
                    retrySeconds = (retrySeconds * 2L).coerceAtMost(60L)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            saveStatus("ERROR", error.message ?: "Could not connect to Telegram")
            updateNotification("Telegram connection error")
        } finally {
            pollingJobActive = false
        }
    }

    private fun hasWelcomed(chatId: Long): Boolean =
        getSharedPreferences(STATUS_PREFS, MODE_PRIVATE)
            .getBoolean("welcomed_$chatId", false)

    private fun markWelcomed(chatId: Long) {
        getSharedPreferences(STATUS_PREFS, MODE_PRIVATE)
            .edit().putBoolean("welcomed_$chatId", true).apply()
    }

    private fun saveStatus(status: String, detail: String) {
        getSharedPreferences(STATUS_PREFS, MODE_PRIVATE).edit()
            .putString(KEY_STATUS, status)
            .putString(KEY_DETAIL, detail)
            .putLong(KEY_STATUS_TIME, System.currentTimeMillis())
            .apply()
        if (::manager.isInitialized) updateNotification("$status: $detail")
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "RTK Devil Bot",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Status of the Telegram bot polling service"
                }
            )
        }
    }

    private fun notification(text: String): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            10,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            11,
            Intent(this, TelegramBotService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("RTK Devil Auto Reply")
            .setContentText(text.take(180))
            .setContentIntent(openApp)
            .setOngoing(true)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    private fun updateNotification(text: String) {
        if (::manager.isInitialized) {
            manager.notify(NOTIFICATION_ID, notification(text))
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.rtk.devil.autoreply.bot.START"
        const val ACTION_STOP = "com.rtk.devil.autoreply.bot.STOP"
        const val STATUS_PREFS = "rtk_bot_runtime_status"
        const val KEY_STATUS = "status"
        const val KEY_DETAIL = "detail"
        const val KEY_STATUS_TIME = "status_time"
        private const val KEY_OFFSET = "update_offset"
        private const val CHANNEL_ID = "rtk_devil_bot_service"
        private const val NOTIFICATION_ID = 2907

        fun start(context: android.content.Context) {
            val intent = Intent(context, TelegramBotService::class.java)
                .setAction(ACTION_START)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: android.content.Context) {
            context.startService(
                Intent(context, TelegramBotService::class.java)
                    .setAction(ACTION_STOP)
            )
        }
    }
}
