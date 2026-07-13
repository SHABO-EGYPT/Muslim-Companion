package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*

// 1. Home ViewModel
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CompanionRepository,
    private val azkarRepository: AzkarRepository,
    countdownManager: PrayerCountdownManager
) : ViewModel() {
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

    val prayerTimes: StateFlow<List<PrayerTime>> = repository.getPrayerTimesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val nextPrayerInfo = countdownManager.nextPrayerInfo

    val checkablePrayers = countdownManager.checkablePrayers

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val azkarCategories: StateFlow<List<AzkarCategory>> = userProgress
        .flatMapLatest { azkarRepository.getAzkarCategoriesFlow(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
