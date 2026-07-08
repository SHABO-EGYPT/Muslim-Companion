package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.CompanionRepository
import com.example.domain.model.PrayerTime
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

// 6. Prayer ViewModel
@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val repository: CompanionRepository,
    countdownManager: PrayerCountdownManager
) : ViewModel() {

    val prayerTimes: StateFlow<List<PrayerTime>> = repository.getPrayerTimesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userProgress: StateFlow<UserProgressEntity> = repository.getUserProgressFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProgressEntity()
        )

    val settings: StateFlow<AppSettingEntity> = repository.getSettingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingEntity()
        )

    val nextPrayerInfo = countdownManager.nextPrayerInfo

    val checkablePrayers = countdownManager.checkablePrayers

    private val _locationName = MutableStateFlow("Determining Location...")
    val locationName = _locationName.asStateFlow()

    private val _prayerLoadError = MutableStateFlow<String?>(null)
    val prayerLoadError = _prayerLoadError.asStateFlow()

    fun updateLocation(latitude: Double, longitude: Double, name: String) {
        _locationName.value = name
        viewModelScope.launch {
            try {
                repository.refreshPrayerTimesByLocation(latitude, longitude)
                _prayerLoadError.value = null
            } catch (e: Exception) {
                _prayerLoadError.value = e.message ?: "Failed to fetch prayer times. You might be offline."
            }
        }
    }

    fun togglePrayerCompletion(prayerName: String) {
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            val today = LocalDate.now().toString()
            
            // 1. Daily Reset Logic: If lastStreakDate is not today, clear today's progress
            val isNewDay = progress.lastStreakDate != today
            val currentCompletedStr = if (isNewDay) "" else progress.completedPrayersToday
            val completed = currentCompletedStr.split(",").filter { it.isNotBlank() }.toMutableList()
            
            val wasCompleted = completed.contains(prayerName)
            if (wasCompleted) {
                completed.remove(prayerName)
            } else {
                completed.add(prayerName)
            }
            
            val newCompletedStr = completed.joinToString(",")
            val allPrayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            val isAllDone = allPrayers.all { completed.contains(it) }
            
            // 2. Streak Logic: Only increment once per day when all 5 are completed
            var newStreak = progress.streak
            if (isAllDone && !wasCompleted) {
                val yesterday = LocalDate.now().minusDays(1).toString()
                if (progress.lastStreakDate == yesterday) {
                    newStreak++
                } else if (isNewDay || progress.streak == 0) {
                    newStreak = 1
                }
            }

            // 3. Score Reflection: +10 per prayer
            val scoreDelta = if (wasCompleted) -10 else 10
            val newScore = (progress.prayerScore + scoreDelta).coerceAtLeast(0)

            var newFajrCount = progress.fajrOnTimeCount
            var newIshaCount = progress.ishaOnTimeCount
            
            if (!wasCompleted) {
                if (prayerName == "Fajr") newFajrCount++
                if (prayerName == "Isha") newIshaCount++
            } else {
                if (prayerName == "Fajr") newFajrCount = (newFajrCount - 1).coerceAtLeast(0)
                if (prayerName == "Isha") newIshaCount = (newIshaCount - 1).coerceAtLeast(0)
            }

            repository.saveUserProgress(
                progress.copy(
                    completedPrayersToday = newCompletedStr,
                    lastStreakDate = today,
                    streak = newStreak,
                    prayerScore = newScore,
                    fajrOnTimeCount = newFajrCount,
                    ishaOnTimeCount = newIshaCount
                )
            )
        }
    }
}
