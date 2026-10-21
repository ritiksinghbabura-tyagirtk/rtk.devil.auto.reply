package com.rtk.devil.autoreply.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.rtkDataStore by preferencesDataStore(
    name = "rtk_devil_settings"
)

data class AppSettings(
    // Legacy settings retained for existing code compatibility.
    val autoReplyEnabled: Boolean = false,
    val welcomeEnabled: Boolean = true,
    val everyMessageEnabled: Boolean = true,
    val welcomeMessage: String =
        "👋 Hello! Thanks for messaging me. I will get back to you soon.",

    // WhatsApp notification-reply module.
    val whatsappAutoReplyEnabled: Boolean = false,
    val whatsappWelcomeEnabled: Boolean = true,
    val whatsappEveryMessageEnabled: Boolean = true,
    val whatsappWelcomeMessage1: String =
        "👋 Hello! Thanks for messaging RTK DEVIL DEPARTMENT.",
    val whatsappWelcomeMessage2: String =
        "I will get back to you soon. GitHub: https://github.com/ritiksinghbabura-tyagirtk",
    val whatsappCustomReply: String =
        "Thanks for your message! I will reply as soon as possible.",

    // Telegram notification-reply module.
    val telegramNotificationEnabled: Boolean = false,
    val telegramWelcomeEnabled: Boolean = true,
    val telegramEveryMessageEnabled: Boolean = true,
    val telegramWelcomeMessage1: String =
        "👋 Hello! Thanks for messaging RTK DEVIL DEPARTMENT.",
    val telegramWelcomeMessage2: String =
        "I will get back to you soon. GitHub: https://github.com/ritiksinghbabura-tyagirtk",
    val telegramCustomReply: String =
        "Thanks for your message! I will reply as soon as possible.",

    // Telegram Bot API is a separate integration.
    val telegramBotAutoReplyEnabled: Boolean = false
)

class AppSettingsRepository(context: Context) {

    private val store = context.applicationContext.rtkDataStore

    val settings: Flow<AppSettings> = store.data.map { preferences ->
        val legacyAutoReply = preferences[Keys.AUTO_REPLY] ?: false

        AppSettings(
            autoReplyEnabled = legacyAutoReply,
            welcomeEnabled = preferences[Keys.WELCOME_ENABLED] ?: true,
            everyMessageEnabled =
                preferences[Keys.EVERY_MESSAGE_ENABLED] ?: true,
            welcomeMessage =
                preferences[Keys.WELCOME_MESSAGE]
                    ?: AppSettings().welcomeMessage,

            whatsappAutoReplyEnabled =
                preferences[Keys.WHATSAPP_AUTO_REPLY] ?: false,
            whatsappWelcomeEnabled =
                preferences[Keys.WHATSAPP_WELCOME_ENABLED] ?: true,
            whatsappEveryMessageEnabled =
                preferences[Keys.WHATSAPP_EVERY_MESSAGE] ?: true,
            whatsappWelcomeMessage1 =
                preferences[Keys.WHATSAPP_WELCOME_1]
                    ?: AppSettings().whatsappWelcomeMessage1,
            whatsappWelcomeMessage2 =
                preferences[Keys.WHATSAPP_WELCOME_2]
                    ?: AppSettings().whatsappWelcomeMessage2,
            whatsappCustomReply =
                preferences[Keys.WHATSAPP_CUSTOM_REPLY]
                    ?: AppSettings().whatsappCustomReply,

            // Existing installations retain their previous Telegram
            // notification toggle until the new independent toggle is saved.
            telegramNotificationEnabled =
                preferences[Keys.TELEGRAM_NOTIFICATION]
                    ?: legacyAutoReply,
            telegramWelcomeEnabled =
                preferences[Keys.TELEGRAM_WELCOME_ENABLED] ?: true,
            telegramEveryMessageEnabled =
                preferences[Keys.TELEGRAM_EVERY_MESSAGE] ?: true,
            telegramWelcomeMessage1 =
                preferences[Keys.TELEGRAM_WELCOME_1]
                    ?: AppSettings().telegramWelcomeMessage1,
            telegramWelcomeMessage2 =
                preferences[Keys.TELEGRAM_WELCOME_2]
                    ?: AppSettings().telegramWelcomeMessage2,
            telegramCustomReply =
                preferences[Keys.TELEGRAM_CUSTOM_REPLY]
                    ?: AppSettings().telegramCustomReply,

            telegramBotAutoReplyEnabled =
                preferences[Keys.TELEGRAM_BOT_AUTO_REPLY] ?: false
        )
    }

    // Legacy API retained so existing modules continue compiling.
    suspend fun setAutoReplyEnabled(value: Boolean) {
        store.edit { it[Keys.AUTO_REPLY] = value }
    }

    suspend fun setWelcomeEnabled(value: Boolean) {
        store.edit { it[Keys.WELCOME_ENABLED] = value }
    }

    suspend fun setEveryMessageEnabled(value: Boolean) {
        store.edit { it[Keys.EVERY_MESSAGE_ENABLED] = value }
    }

    suspend fun setWelcomeMessage(value: String) {
        store.edit { it[Keys.WELCOME_MESSAGE] = value }
    }

    suspend fun setWhatsappAutoReplyEnabled(value: Boolean) =
        store.edit { it[Keys.WHATSAPP_AUTO_REPLY] = value }

    suspend fun setWhatsappWelcomeEnabled(value: Boolean) =
        store.edit { it[Keys.WHATSAPP_WELCOME_ENABLED] = value }

    suspend fun setWhatsappEveryMessageEnabled(value: Boolean) =
        store.edit { it[Keys.WHATSAPP_EVERY_MESSAGE] = value }

    suspend fun setWhatsappWelcomeMessage1(value: String) =
        store.edit { it[Keys.WHATSAPP_WELCOME_1] = value }

    suspend fun setWhatsappWelcomeMessage2(value: String) =
        store.edit { it[Keys.WHATSAPP_WELCOME_2] = value }

    suspend fun setWhatsappCustomReply(value: String) =
        store.edit { it[Keys.WHATSAPP_CUSTOM_REPLY] = value }

    suspend fun setTelegramNotificationEnabled(value: Boolean) =
        store.edit { it[Keys.TELEGRAM_NOTIFICATION] = value }

    suspend fun setTelegramWelcomeEnabled(value: Boolean) =
        store.edit { it[Keys.TELEGRAM_WELCOME_ENABLED] = value }

    suspend fun setTelegramEveryMessageEnabled(value: Boolean) =
        store.edit { it[Keys.TELEGRAM_EVERY_MESSAGE] = value }

    suspend fun setTelegramWelcomeMessage1(value: String) =
        store.edit { it[Keys.TELEGRAM_WELCOME_1] = value }

    suspend fun setTelegramWelcomeMessage2(value: String) =
        store.edit { it[Keys.TELEGRAM_WELCOME_2] = value }

    suspend fun setTelegramCustomReply(value: String) =
        store.edit { it[Keys.TELEGRAM_CUSTOM_REPLY] = value }

    suspend fun setTelegramBotAutoReplyEnabled(value: Boolean) =
        store.edit { it[Keys.TELEGRAM_BOT_AUTO_REPLY] = value }

    suspend fun resetSettings() {
        store.edit { it.clear() }
    }

    private object Keys {
        // Original keys: do not rename; existing preferences use them.
        val AUTO_REPLY = booleanPreferencesKey("auto_reply_enabled")
        val WELCOME_ENABLED = booleanPreferencesKey("welcome_enabled")
        val EVERY_MESSAGE_ENABLED =
            booleanPreferencesKey("every_message_enabled")
        val WELCOME_MESSAGE = stringPreferencesKey("welcome_message")

        val WHATSAPP_AUTO_REPLY =
            booleanPreferencesKey("whatsapp_auto_reply_enabled")
        val WHATSAPP_WELCOME_ENABLED =
            booleanPreferencesKey("whatsapp_welcome_enabled")
        val WHATSAPP_EVERY_MESSAGE =
            booleanPreferencesKey("whatsapp_every_message_enabled")
        val WHATSAPP_WELCOME_1 =
            stringPreferencesKey("whatsapp_welcome_message_1")
        val WHATSAPP_WELCOME_2 =
            stringPreferencesKey("whatsapp_welcome_message_2")
        val WHATSAPP_CUSTOM_REPLY =
            stringPreferencesKey("whatsapp_custom_reply")

        val TELEGRAM_NOTIFICATION =
            booleanPreferencesKey("telegram_notification_enabled")
        val TELEGRAM_WELCOME_ENABLED =
            booleanPreferencesKey("telegram_welcome_enabled")
        val TELEGRAM_EVERY_MESSAGE =
            booleanPreferencesKey("telegram_every_message_enabled")
        val TELEGRAM_WELCOME_1 =
            stringPreferencesKey("telegram_welcome_message_1")
        val TELEGRAM_WELCOME_2 =
            stringPreferencesKey("telegram_welcome_message_2")
        val TELEGRAM_CUSTOM_REPLY =
            stringPreferencesKey("telegram_custom_reply")

        val TELEGRAM_BOT_AUTO_REPLY =
            booleanPreferencesKey("telegram_bot_auto_reply_enabled")
    }
}
