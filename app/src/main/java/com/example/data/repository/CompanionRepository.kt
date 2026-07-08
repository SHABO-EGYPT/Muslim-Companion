package com.example.data.repository

import com.example.data.local.AppSettingEntity
import com.example.data.local.CachedPrayerTimeEntity
import com.example.data.local.CompanionDao
import com.example.data.local.NotificationEntity
import com.example.data.local.UserProgressEntity
import com.example.data.mapper.MethodMapper
import com.example.data.remote.PrayerApi
import com.example.domain.model.AchievementBadge
import com.example.domain.model.NotificationItem
import com.example.domain.model.PrayerTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class CompanionRepository(
    private val dao: CompanionDao,
    val quranRepository: QuranRepository,
    val azkarRepository: AzkarRepository
) {
    open fun getPrayerTimesFlow(): Flow<List<PrayerTime>> = dao.getCachedPrayerTimesFlow().map { entities ->
        if (entities.isEmpty()) defaultPrayerTimes()
        else entities.map { it.toDomain() }
    }

    open suspend fun refreshPrayerTimesByLocation(latitude: Double, longitude: Double) {
        val settings = dao.getSettingsDirect() ?: AppSettingEntity()
        val methodId = MethodMapper.getMethodId(settings.calculationMethod)
        val formatter = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
        val response = PrayerApi.instance.getTimings(formatter.format(java.util.Date()), latitude, longitude, methodId)
        savePrayerTimes(response.data.timings)
    }

    open suspend fun refreshPrayerTimes(city: String = "Cairo", country: String = "Egypt") {
        val settings = dao.getSettingsDirect() ?: AppSettingEntity()
        val methodId = MethodMapper.getMethodId(settings.calculationMethod)
        val response = PrayerApi.instance.getTimingsByCity(city, country, methodId)
        savePrayerTimes(response.data.timings)
    }

    private suspend fun savePrayerTimes(timings: Map<String, String>) {
        val arabicNames = mapOf("Fajr" to "الفجر", "Dhuhr" to "الظهر", "Asr" to "العصر", "Maghrib" to "المغرب", "Isha" to "العشاء")
        val icons = mapOf("Fajr" to "sunrise", "Dhuhr" to "sun", "Asr" to "sun", "Maghrib" to "sunset", "Isha" to "moon")
        val entities = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").mapNotNull { name ->
            timings[name]?.let { time ->
                CachedPrayerTimeEntity(name, arabicNames[name] ?: "", time, icons[name] ?: "sun")
            }
        }
        if (entities.isNotEmpty()) dao.insertCachedPrayerTimes(entities)
    }

    open fun getSettingsFlow(): Flow<AppSettingEntity> = dao.getSettingsFlow().map { it ?: AppSettingEntity() }
    open suspend fun getSettingsDirect(): AppSettingEntity? = dao.getSettingsDirect()
    open suspend fun saveSettings(settings: AppSettingEntity) = dao.saveSettings(settings)

    open fun getUserProgressFlow(): Flow<UserProgressEntity> = dao.getUserProgressFlow().map { it ?: UserProgressEntity() }
    open suspend fun getUserProgressDirect(): UserProgressEntity? = dao.getUserProgressDirect()
    open suspend fun saveUserProgress(progress: UserProgressEntity) = dao.saveUserProgress(progress)

    open fun getNotificationsFlow(): Flow<List<NotificationEntity>> = dao.getNotificationsFlow()
    open suspend fun insertNotification(notification: NotificationEntity) = dao.insertNotification(notification)
    open suspend fun markNotificationAsRead(id: Int) = dao.markNotificationAsRead(id)

    open fun getBadges(progress: UserProgressEntity): List<AchievementBadge> = listOf(
        AchievementBadge("7-day streak", "Completed 7 days in a row", earned = progress.streak >= 7, "award"),
        AchievementBadge("First Juz", "Read 1st Juz of the Quran", earned = progress.surahsReadCount >= 37, "award"),
        AchievementBadge("Night Owl", "Completed 5 Isha prayers on time", earned = progress.ishaOnTimeCount >= 5, "award"),
        AchievementBadge("Early Riser", "Completed 5 Fajr prayers on time", earned = progress.fajrOnTimeCount >= 5, "award")
    )

    private fun defaultPrayerTimes() = listOf(
        PrayerTime("Fajr", "الفجر", "04:12", "sunrise"),
        PrayerTime("Dhuhr", "الظهر", "12:31", "sun"),
        PrayerTime("Asr", "العصر", "16:04", "sun"),
        PrayerTime("Maghrib", "المغرب", "19:22", "sunset"),
        PrayerTime("Isha", "العشاء", "20:52", "moon")
    )

    private fun CachedPrayerTimeEntity.toDomain() = PrayerTime(name, arabicName, timeString, iconName)
}
