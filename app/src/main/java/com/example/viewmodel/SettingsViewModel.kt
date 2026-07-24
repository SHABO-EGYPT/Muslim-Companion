package com.example.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettingEntity
import com.example.data.repository.CompanionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// 8. Settings ViewModel
@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: CompanionRepository) : ViewModel() {
    val settings: StateFlow<AppSettingEntity> = repository.getSettingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingEntity()
        )

    fun togglePrayerNotifications() {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(prayerNotifications = !s.prayerNotifications))
        }
    }

    fun toggleMorningAzkarNotification() {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(morningAzkarNotification = !s.morningAzkarNotification))
        }
    }

    fun toggleEveningAzkarNotification() {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(eveningAzkarNotification = !s.eveningAzkarNotification))
        }
    }

    fun toggleAfterPrayerAzkarNotification() {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(afterPrayerAzkarNotification = !s.afterPrayerAzkarNotification))
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(darkTheme = !s.darkTheme))
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(language = lang))
        }
    }

    fun updateReciter(reciter: String) {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(quranReciter = reciter))
        }
    }

    fun updateTextSize(size: String) {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(textSize = size))
        }
    }

    fun updateNotificationSoundType(type: String) {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(notificationSoundType = type))
        }
    }

    fun updateCalculationMethod(method: String) {
        viewModelScope.launch {
            val s = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(s.copy(calculationMethod = method))
        }
    }
}
