package com.example.viewmodel

import com.example.data.repository.CompanionRepository
import com.example.domain.model.PrayerTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerCountdownManager @Inject constructor(
    repository: CompanionRepository
) {
    private val _currentTime = MutableStateFlow(LocalTime.now())
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val nextPrayerInfo: StateFlow<Triple<PrayerTime, String, String>> =
        calculateNextPrayerInfo(repository.getPrayerTimesFlow(), _currentTime)
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = Triple(PrayerTime("Asr", "العصر", "16:04", "sun"), "00:00:00", "Asr")
            )

    val checkablePrayers: StateFlow<Set<String>> =
        calculateCheckablePrayers(repository.getPrayerTimesFlow(), _currentTime)
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet()
            )

    init {
        scope.launch {
            while (true) {
                _currentTime.value = LocalTime.now()
                delay(1000)
            }
        }
    }
}

fun calculateNextPrayerInfo(
    prayerTimesFlow: Flow<List<PrayerTime>>,
    currentTimeFlow: Flow<LocalTime>
): Flow<Triple<PrayerTime, String, String>> {
    return combine(prayerTimesFlow, currentTimeFlow) { times, now ->
        if (times.isEmpty()) {
            val placeholder = PrayerTime("Asr", "العصر", "16:04", "sun")
            Triple(placeholder, "00:00:00", "Asr")
        } else {
            val formattedTimes = times.mapNotNull { prayer ->
                com.example.utils.TimeUtils.parsePrayerTime(prayer.timeString)?.let { prayer to it }
            }
            
            val next = formattedTimes.find { it.second.isAfter(now) }
                ?: (formattedTimes.first().first to formattedTimes.first().second.plusHours(24))

            val duration = Duration.between(now, next.second)
            val seconds = duration.seconds
            val countdown = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
            
            Triple(next.first, countdown, next.first.name)
        }
    }
}

fun calculateCheckablePrayers(
    prayerTimesFlow: Flow<List<PrayerTime>>,
    currentTimeFlow: Flow<LocalTime>
): Flow<Set<String>> {
    return combine(prayerTimesFlow, currentTimeFlow) { times, now ->
        if (times.isEmpty()) emptySet<String>()
        else {
            val formattedTimes = times.mapNotNull { prayer ->
                com.example.utils.TimeUtils.parsePrayerTime(prayer.timeString)?.let { prayer to it }
            }
            val nextIndex = formattedTimes.indexOfFirst { it.second.isAfter(now) }
            if (nextIndex == -1) {
                times.map { it.name }.toSet()
            } else {
                times.take(nextIndex).map { it.name }.toSet()
            }
        }
    }
}
