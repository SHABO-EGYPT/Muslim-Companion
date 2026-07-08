package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "",
    val streak: Int = 0,
    val lastStreakDate: String = "",
    val lastReadSurahNumber: Int = 1,
    val lastReadSurahName: String = "Al-Fatiha",
    val lastReadSurahArabicName: String = "الفاتحة",
    val lastReadAyahNumber: Int = 1,
    val lastReadProgress: Float = 0f,
    val tasbihCount: Int = 0,
    val tasbihActivePhraseIndex: Int = 0,
    val morningDone: Int = 0,
    val eveningDone: Int = 0,
    val sleepDone: Int = 0,
    val afterPrayerDone: Int = 0,
    val prayerHistoryCsv: String = "",
    val completedPrayersToday: String = "",
    val location: String = "",
    val surahsReadCount: Int = 0,
    val prayerScore: Int = 0,
    val fajrOnTimeCount: Int = 0,
    val ishaOnTimeCount: Int = 0,
    val onboardingCompleted: Boolean = false
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val surahNumber: Int,
    val surahName: String
)

@Entity(tableName = "cached_ayahs")
data class CachedAyahEntity(
    @PrimaryKey val id: String,
    val surahNumber: Int,
    val numberInSurah: Int,
    val reciter: String,
    val arabicText: String,
    val translation: String,
    val audioUrl: String
)

@Entity(tableName = "cached_surahs")
data class CachedSurahEntity(
    @PrimaryKey val number: Int,
    val name: String,
    val meaning: String,
    val ayahsCount: Int,
    val arabicName: String,
    val isMakki: Boolean
)

@Entity(tableName = "cached_prayer_times")
data class CachedPrayerTimeEntity(
    @PrimaryKey val name: String,
    val arabicName: String,
    val timeString: String,
    val iconName: String
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val id: Int = 1,
    val prayerNotifications: Boolean = true,
    val darkTheme: Boolean = false,
    val language: String = "English",
    val reciter: String = "Mishary Al-Afasy",
    val calculationMethod: String = "Egyptian General Authority",
    val textSize: String = "Medium",
    val quranTextSize: Float = 26f,
    val quranReciter: String = "7",
    val quranFont: String = "Classic Serif",
    val quranKeepScreenOn: Boolean = true,
    val notificationSoundType: String = "Subtle",
    val quranShowTranslation: Boolean = true
)
