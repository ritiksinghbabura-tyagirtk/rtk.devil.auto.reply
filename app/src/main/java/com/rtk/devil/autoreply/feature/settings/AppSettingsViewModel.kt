package com.rtk.devil.autoreply.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rtk.devil.autoreply.data.local.AppSettings
import com.rtk.devil.autoreply.data.local.AppSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSettingsViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = AppSettingsRepository(application)

    val settings = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    // Legacy methods.
    fun setAutoReply(value: Boolean) = viewModelScope.launch {
        repository.setAutoReplyEnabled(value)
    }

    fun setWelcomeEnabled(value: Boolean) = viewModelScope.launch {
        repository.setWelcomeEnabled(value)
    }

    fun setEveryMessageEnabled(value: Boolean) = viewModelScope.launch {
        repository.setEveryMessageEnabled(value)
    }

    fun setWelcomeMessage(value: String) = viewModelScope.launch {
        repository.setWelcomeMessage(value)
    }

    // WhatsApp settings.
    fun setWhatsappAutoReply(value: Boolean) = viewModelScope.launch {
        repository.setWhatsappAutoReplyEnabled(value)
    }

    fun setWhatsappWelcomeEnabled(value: Boolean) = viewModelScope.launch {
        repository.setWhatsappWelcomeEnabled(value)
    }

    fun setWhatsappEveryMessage(value: Boolean) = viewModelScope.launch {
        repository.setWhatsappEveryMessageEnabled(value)
    }

    fun setWhatsappWelcomeMessage1(value: String) = viewModelScope.launch {
        repository.setWhatsappWelcomeMessage1(value)
    }

    fun setWhatsappWelcomeMessage2(value: String) = viewModelScope.launch {
        repository.setWhatsappWelcomeMessage2(value)
    }

    fun setWhatsappCustomReply(value: String) = viewModelScope.launch {
        repository.setWhatsappCustomReply(value)
    }

    // Telegram notification settings.
    fun setTelegramNotificationEnabled(value: Boolean) =
        viewModelScope.launch {
            repository.setTelegramNotificationEnabled(value)
        }

    fun setTelegramWelcomeEnabled(value: Boolean) = viewModelScope.launch {
        repository.setTelegramWelcomeEnabled(value)
    }

    fun setTelegramEveryMessage(value: Boolean) = viewModelScope.launch {
        repository.setTelegramEveryMessageEnabled(value)
    }

    fun setTelegramWelcomeMessage1(value: String) = viewModelScope.launch {
        repository.setTelegramWelcomeMessage1(value)
    }

    fun setTelegramWelcomeMessage2(value: String) = viewModelScope.launch {
        repository.setTelegramWelcomeMessage2(value)
    }

    fun setTelegramCustomReply(value: String) = viewModelScope.launch {
        repository.setTelegramCustomReply(value)
    }

    // Telegram Bot API setting is independent from notification replies.
    fun setTelegramBotAutoReply(value: Boolean) = viewModelScope.launch {
        repository.setTelegramBotAutoReplyEnabled(value)
    }

    fun resetSettings() = viewModelScope.launch {
        repository.resetSettings()
    }
}
