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
    val autoReplyEnabled: Boolean = false,
    val welcomeEnabled: Boolean = true,
    val everyMessageEnabled: Boolean = true,
    val welcomeMessage: String =
        "👋 Hello! Thanks for messaging me. I will get back to you soon."
)

class AppSettingsRepository(context: Context) {

    private val store = context.applicationContext.rtkDataStore

    val settings: Flow<AppSettings> = store.data.map { preferences ->
        AppSettings(
            autoReplyEnabled =
                preferences[Keys.AUTO_REPLY] ?: false,
            welcomeEnabled =
                preferences[Keys.WELCOME_ENABLED] ?: true,
            everyMessageEnabled =
                preferences[Keys.EVERY_MESSAGE_ENABLED] ?: true,
            welcomeMessage =
                preferences[Keys.WELCOME_MESSAGE]
                    ?: AppSettings().welcomeMessage
        )
    }

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

    suspend fun resetSettings() {
        store.edit { it.clear() }
    }

    private object Keys {
        val AUTO_REPLY = booleanPreferencesKey("auto_reply_enabled")
        val WELCOME_ENABLED = booleanPreferencesKey("welcome_enabled")
        val EVERY_MESSAGE_ENABLED =
            booleanPreferencesKey("every_message_enabled")
        val WELCOME_MESSAGE = stringPreferencesKey("welcome_message")
    }
}
