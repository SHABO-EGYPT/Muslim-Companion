package com.example.data.repository

import android.util.Log
import com.example.data.local.AppSettingEntity
import com.example.data.local.CachedPrayerTimeEntity
import com.example.data.local.CompanionDao
import com.example.data.local.NotificationEntity
import com.example.data.local.UserProgressEntity
import com.example.data.remote.PrayerApi
import com.example.domain.CalculationMethodMapper
import com.example.domain.model.AchievementBadge
import com.example.domain.model.PrayerTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import java.util.Locale

open class CompanionRepository(
    private val dao: CompanionDao,
    private val quranRepository: QuranRepository,
    private val azkarRepository: AzkarRepository,
    private val prayerApi: PrayerApi
) {
    companion object {
        private const val TAG = "CompanionRepository"
    }

    open fun getPrayerTimesFlow(): Flow<List<PrayerTime>> = dao.getCachedPrayerTimesFlow().map { entities ->
        if (entities.isEmpty()) defaultPrayerTimes()
        else entities.map { it.toDomain() }
    }

    open suspend fun refreshPrayerTimesByLocation(latitude: Double, longitude: Double) {
        val settings = dao.getSettingsDirect() ?: AppSettingEntity()
        try {
            val methodId = CalculationMethodMapper.getMethodId(settings.calculationMethod)
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val response = prayerApi.getTimings(formatter.format(Date()), latitude, longitude, methodId)
            savePrayerTimes(response.data.timings)
        } catch (e: Exception) {
            Log.w(TAG, "Online prayer fetch failed for ($latitude, $longitude). Using offline astronomical calculation fallback.", e)
            val offlineTimes = com.example.data.prayer.OfflinePrayerCalculator.calculate(
                date = LocalDate.now(),
                latitude = latitude,
                longitude = longitude,
                methodName = settings.calculationMethod
            )
            savePrayerTimes(offlineTimes)
        }
    }

    open suspend fun refreshPrayerTimes(city: String = "Cairo", country: String = "Egypt") {
        val settings = dao.getSettingsDirect() ?: AppSettingEntity()
        try {
            val methodId = CalculationMethodMapper.getMethodId(settings.calculationMethod)
            val response = prayerApi.getTimingsByCity(city, country, methodId)
            savePrayerTimes(response.data.timings)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to refresh online prayer times for $city, $country. Using offline calculation fallback.", e)
            // Default Cairo coordinates: 30.0444, 31.2357
            val (lat, lng) = if (city.equals("Cairo", ignoreCase = true)) 30.0444 to 31.2357 else 21.3891 to 39.8579
            val offlineTimes = com.example.data.prayer.OfflinePrayerCalculator.calculate(
                date = LocalDate.now(),
                latitude = lat,
                longitude = lng,
                methodName = settings.calculationMethod
            )
            savePrayerTimes(offlineTimes)
        }
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

    open suspend fun getActiveTrackingDate(): String {
        val now = LocalDateTime.now()
        val cachedTimes = dao.getCachedPrayerTimesDirect()
        val fajrTimeStr = cachedTimes.find { it.name == "Fajr" }?.timeString ?: "04:12"
        val fajrTime = com.example.utils.TimeUtils.parsePrayerTime(fajrTimeStr) ?: LocalTime.of(4, 12)
        
        // Reset time is 5 minutes before Fajr
        val resetTimeToday = LocalDateTime.of(now.toLocalDate(), fajrTime).minusMinutes(5)
        
        val trackingDate = if (now.isBefore(resetTimeToday)) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        return trackingDate.toString()
    }

    private suspend fun sanitizeProgress(progress: UserProgressEntity): UserProgressEntity {
        val activeDate = getActiveTrackingDate()
        val parts = progress.completedPrayersToday.split(":")
        val dateStr = parts.getOrNull(0) ?: ""
        val prayersStr = parts.getOrNull(1) ?: ""
        
        var updated = if (dateStr != activeDate) {
            progress.copy(completedPrayersToday = "")
        } else {
            progress.copy(completedPrayersToday = prayersStr)
        }

        if (updated.lastAzkarDate != activeDate) {
            updated = updated.copy(
                lastAzkarDate = activeDate,
                morningDone = 0,
                eveningDone = 0,
                sleepDone = 0,
                afterPrayerDone = 0,
                wakeupDone = 0,
                customAzkarProgress = ""
            )
        }
        return updated
    }

    open fun getUserProgressFlow(): Flow<UserProgressEntity> = dao.getUserProgressFlow().map { 
        val entity = it ?: UserProgressEntity()
        sanitizeProgress(entity)
    }
    open suspend fun getUserProgressDirect(): UserProgressEntity? = dao.getUserProgressDirect()?.let { 
        sanitizeProgress(it)
    }
    open suspend fun saveUserProgress(progress: UserProgressEntity) {
        val activeDate = getActiveTrackingDate()
        val newCompleted = if (progress.completedPrayersToday.contains(":")) {
            progress.completedPrayersToday
        } else {
            "$activeDate:${progress.completedPrayersToday}"
        }
        val azkarDate = if (progress.lastAzkarDate.isEmpty()) activeDate else progress.lastAzkarDate
        dao.saveUserProgress(progress.copy(
            completedPrayersToday = newCompleted,
            lastAzkarDate = azkarDate
        ))
    }

    open fun getNotificationsFlow(): Flow<List<NotificationEntity>> = dao.getNotificationsFlow()
    open suspend fun insertNotification(notification: NotificationEntity) = dao.insertNotification(notification)
    open suspend fun markNotificationAsRead(id: Int) = dao.markNotificationAsRead(id)
    open suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
    open suspend fun deleteNotification(id: Int) = dao.deleteNotification(id)
    open suspend fun clearAllNotifications() = dao.clearAllNotifications()

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

