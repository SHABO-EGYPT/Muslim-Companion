package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.data.repository.QuranRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.json.JSONObject
import org.json.JSONArray
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Content
import com.example.data.remote.Part
import com.example.data.remote.GenerationConfig
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import kotlinx.coroutines.delay

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
