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

    fun resetSettings() = viewModelScope.launch {
        repository.resetSettings()
    }
}
