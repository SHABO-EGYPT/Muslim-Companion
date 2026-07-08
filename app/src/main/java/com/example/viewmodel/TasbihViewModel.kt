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

    private val _target = MutableStateFlow(33)
    val target = _target.asStateFlow()

    private val _currentDhikr = MutableStateFlow("سُبْحَانَ اللَّهِ")
    val currentDhikr = _currentDhikr.asStateFlow()

    private val _phrases = MutableStateFlow<List<DhikrItem>>(emptyList())
    val phrases = _phrases.asStateFlow()

    private val _activeIndex = MutableStateFlow(0)
    val activeIndex = _activeIndex.asStateFlow()

    init {
        viewModelScope.launch {
            repository.azkarRepository.getTasbihPhrasesFlow().collect { list ->
                _phrases.value = list
                val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
                _activeIndex.value = progress.tasbihActivePhraseIndex
                _count.value = progress.tasbihCount
                updateDhikrAndTargetForActiveIndex()
            }
        }
    }

    private fun updateDhikrAndTargetForActiveIndex() {
        val list = _phrases.value
        val index = _activeIndex.value
        if (list.isNotEmpty() && index in list.indices) {
            val item = list[index]
            _currentDhikr.value = item.arabicText
            _target.value = item.repeatTarget
        } else {
            _currentDhikr.value = "سُبْحَانَ اللَّهِ"
            _target.value = 33
        }
    }

    fun increment() {
        _count.value++
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            repository.saveUserProgress(progress.copy(tasbihCount = _count.value))
        }
    }

    fun reset() {
        _count.value = 0
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            repository.saveUserProgress(progress.copy(tasbihCount = _count.value))
        }
    }

    fun selectPhrase(index: Int) {
        if (index in _phrases.value.indices) {
            _activeIndex.value = index
            _count.value = 0
            updateDhikrAndTargetForActiveIndex()
            viewModelScope.launch {
                val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
                repository.saveUserProgress(progress.copy(
                    tasbihActivePhraseIndex = index,
                    tasbihCount = 0
                ))
            }
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
