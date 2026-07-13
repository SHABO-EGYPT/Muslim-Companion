package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.domain.model.DhikrItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// 5. Tasbih ViewModel
@HiltViewModel
class TasbihViewModel @Inject constructor(
    private val repository: CompanionRepository,
    private val azkarRepository: AzkarRepository
) : ViewModel() {
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

    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            azkarRepository.getTasbihPhrasesFlow().collect { list ->
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
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(1000) // Debounce DB write by 1 second to avoid spamming Room on rapid taps
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            repository.saveUserProgress(progress.copy(tasbihCount = _count.value))
        }
    }

    fun reset() {
        _count.value = 0
        saveJob?.cancel()
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
            saveJob?.cancel()
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
