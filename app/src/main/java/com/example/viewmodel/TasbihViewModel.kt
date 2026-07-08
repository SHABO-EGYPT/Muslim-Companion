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

// 5. Tasbih ViewModel
@HiltViewModel
class TasbihViewModel @Inject constructor(private val repository: CompanionRepository) : ViewModel() {
    val settings: StateFlow<AppSettingEntity> = repository.getSettingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingEntity()
        )

    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    init {
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            _count.value = progress.tasbihCount
        }
    }

    private val _target = MutableStateFlow(33)
    val target = _target.asStateFlow()

    private val _currentDhikr = MutableStateFlow("SubhanAllah")
    val currentDhikr = _currentDhikr.asStateFlow()

    fun increment() {
        _count.value++
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            repository.saveUserProgress(progress.copy(tasbihCount = _count.value))
        }
        if (_count.value >= _target.value) {
            // Logic for target reached (e.g. vibration)
        }
    }

    fun reset() {
        _count.value = 0
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            repository.saveUserProgress(progress.copy(tasbihCount = _count.value))
        }
    }

    fun setTarget(newTarget: Int) {
        _target.value = newTarget
        reset()
    }

    fun setDhikr(dhikr: String) {
        _currentDhikr.value = dhikr
        reset()
    }
}
