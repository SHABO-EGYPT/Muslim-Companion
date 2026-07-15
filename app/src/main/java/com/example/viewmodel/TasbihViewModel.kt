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
    private val azkarRepository: AzkarRepository,
    private val countdownManager: PrayerCountdownManager
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

        viewModelScope.launch {
            var lastNextPrayerName: String? = null
            countdownManager.nextPrayerInfo
                .map { it.first.name }
                .distinctUntilChanged()
                .collect { nextPrayerName ->
                    if (lastNextPrayerName != null && lastNextPrayerName != nextPrayerName) {
                        reset()
                    }
                    lastNextPrayerName = nextPrayerName
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
        val currentCount = _count.value
        val currentTarget = _target.value
        val phrasesList = _phrases.value
        val currentIndex = _activeIndex.value

        if (currentCount >= currentTarget) {
            // Already completed the last phrase, do not increment or reset on further taps
            if (currentIndex == phrasesList.size - 1) {
                return
            }
        }

        val newCount = currentCount + 1
        _count.value = newCount

        if (newCount == currentTarget) {
            // Award +1 to prayerScore in user progress upon completing a cycle
            viewModelScope.launch {
                val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
                repository.saveUserProgress(progress.copy(prayerScore = progress.prayerScore + 1))
            }
            
            // Auto-advance to the next phrase if it exists
            if (currentIndex < phrasesList.size - 1) {
                viewModelScope.launch {
                    delay(300) // Brief delay for user to see completion feedback before advance
                    selectPhrase(currentIndex + 1)
                }
            }
        }

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
