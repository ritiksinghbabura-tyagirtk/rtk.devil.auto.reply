package com.rtk.devil.autoreply.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rtk.devil.autoreply.integration.telegrambot.BotTokenStore
import com.rtk.devil.autoreply.integration.telegrambot.TelegramBotApi
import com.rtk.devil.autoreply.integration.telegrambot.TelegramBotIdentity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BotConnectionState(
    val tokenConfigured: Boolean = false,
    val checking: Boolean = false,
    val connected: Boolean = false,
    val botUsername: String = "",
    val botFirstName: String = "",
    val status: String = "Bot token not configured"
)

class BotConnectionViewModel(application: Application) : AndroidViewModel(application) {

    private val store = BotTokenStore(application)
    private val api = TelegramBotApi()

    private val _state = MutableStateFlow(
        BotConnectionState(tokenConfigured = store.hasToken())
    )
    val state: StateFlow<BotConnectionState> = _state.asStateFlow()

    fun verifyAndSave(token: String) {
        val clean = token.trim()
        if (clean.isBlank()) {
            _state.value = _state.value.copy(
                checking = false,
                connected = false,
                status = "Enter a Bot Token first."
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                checking = true,
                connected = false,
                status = "Verifying token with Telegram..."
            )
            try {
                val identity = withContext(Dispatchers.IO) {
                    api.verifyToken(clean)
                }
                store.saveToken(clean)
                _state.value = identity.toState()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    tokenConfigured = store.hasToken(),
                    checking = false,
                    connected = false,
                    status = e.message ?: "Verification failed. Check internet and token."
                )
            }
        }
    }

    fun verifySavedToken() {
        val token = store.readToken()
        if (token.isNullOrBlank()) {
            _state.value = _state.value.copy(
                tokenConfigured = false,
                connected = false,
                status = "No saved token. Enter one to connect."
            )
            return
        }
        verifyAndSave(token)
    }

    fun clearToken() {
        store.clearToken()
        _state.value = BotConnectionState(status = "Bot token removed from this device.")
    }

    private fun TelegramBotIdentity.toState() = BotConnectionState(
        tokenConfigured = true,
        checking = false,
        connected = true,
        botUsername = username,
        botFirstName = firstName,
        status = if (username.isNotBlank()) {
            "Connected to Telegram bot @$username"
        } else {
            "Token verified with Telegram."
        }
    )
}
