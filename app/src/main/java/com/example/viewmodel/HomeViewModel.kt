package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.location.LocationRepository
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.data.repository.WeatherRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// 1. Home ViewModel
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CompanionRepository,
    private val azkarRepository: AzkarRepository,
    private val weatherRepository: WeatherRepository,
    countdownManager: PrayerCountdownManager,
    private val locationRepository: LocationRepository? = null
) : ViewModel() {

    val weatherState: StateFlow<WeatherState> = weatherRepository.weatherState

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

    init {
        refreshWeather()

        // Also update weather whenever the user's saved location changes
        viewModelScope.launch {
            userProgress
                .map { it.location }
                .distinctUntilChanged()
                .drop(1)
                .collect { locationStr ->
                    if (locationStr.isNotBlank()) {
                        refreshWeatherFromLocationString(locationStr)
                    }
                }
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            val loc = locationRepository?.getCurrentLocation()
            if (loc != null) {
                weatherRepository.fetchWeather(loc.latitude, loc.longitude)
                return@launch
            }

            val savedLocation = repository.getUserProgressDirect()?.location?.trim().orEmpty()
            if (savedLocation.isNotBlank()) {
                refreshWeatherFromLocationString(savedLocation)
            } else {
                // Default to Cairo coordinates
                weatherRepository.fetchWeather(30.0444, 31.2357)
            }
        }
    }

    private suspend fun refreshWeatherFromLocationString(locationStr: String) {
        val coords = locationRepository?.getCoordinatesForLocation(locationStr)
        if (coords != null) {
            weatherRepository.fetchWeather(coords.first, coords.second)
        } else {
            weatherRepository.fetchWeather(30.0444, 31.2357)
        }
    }
}
