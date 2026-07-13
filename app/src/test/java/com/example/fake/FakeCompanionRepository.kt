package com.example.fake

import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.data.repository.QuranRepository
import com.example.domain.model.PrayerTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake CompanionRepository for unit testing ViewModels.
 * Fully in-memory, no database or network dependencies.
 */
class FakeCompanionRepository(
    val quranRepository: FakeQuranRepository = FakeQuranRepository(),
    val azkarRepository: FakeAzkarRepository = FakeAzkarRepository(),
    val dao: FakeDao = FakeDao()
) : CompanionRepository(
    dao = dao,
    quranRepository = quranRepository,
    azkarRepository = azkarRepository
) {
    private val _userProgress = MutableStateFlow(UserProgressEntity())
    private val _settings = MutableStateFlow(AppSettingEntity())
    private val _prayerTimes = MutableStateFlow(
        listOf(
            PrayerTime("Fajr", "الفجر", "04:12", "sunrise"),
            PrayerTime("Dhuhr", "الظهر", "12:31", "sun"),
            PrayerTime("Asr", "العصر", "16:04", "sun"),
            PrayerTime("Maghrib", "المغرب", "19:22", "sunset"),
            PrayerTime("Isha", "العشاء", "20:52", "moon")
        )
    )

    private var _directProgress: UserProgressEntity? = UserProgressEntity()
    private var _directSettings: AppSettingEntity? = AppSettingEntity()

    fun setProgress(progress: UserProgressEntity) {
        _userProgress.value = progress
        _directProgress = progress
    }

    fun setSettings(settings: AppSettingEntity) {
        _settings.value = settings
        _directSettings = settings
    }

    fun setPrayerTimes(times: List<PrayerTime>) {
        _prayerTimes.value = times
    }

    override fun getUserProgressFlow(): Flow<UserProgressEntity> = _userProgress

    override suspend fun getUserProgressDirect(): UserProgressEntity? = _directProgress

    override suspend fun saveUserProgress(progress: UserProgressEntity) {
        _userProgress.value = progress
        _directProgress = progress
    }

    override fun getSettingsFlow(): Flow<AppSettingEntity> = _settings

    override suspend fun getSettingsDirect(): AppSettingEntity? = _directSettings

    override suspend fun saveSettings(settings: AppSettingEntity) {
        _settings.value = settings
        _directSettings = settings
    }

    override fun getPrayerTimesFlow(): Flow<List<PrayerTime>> = _prayerTimes

    override suspend fun refreshPrayerTimes(city: String, country: String) {
        // No-op in fake
    }

    override suspend fun refreshPrayerTimesByLocation(latitude: Double, longitude: Double) {
        // No-op in fake
    }
}
