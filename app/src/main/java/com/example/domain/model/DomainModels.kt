package com.example.domain.model

data class Surah(
    val number: Int,
    val name: String,
    val meaning: String,
    val ayahsCount: Int,
    val arabicName: String,
    val isMakki: Boolean
)

data class Ayah(
    val number: Int,
    val arabicText: String,
    val translation: String,
    val audioUrl: String
)

data class AzkarCategory(
    val id: String,
    val title: String,
    val arabicTitle: String,
    val totalCount: Int,
    val doneCount: Int,
    val iconName: String,
    val colorHex: Long
)

data class DhikrItem(
    val id: Int,
    val arabicText: String,
    val englishTranslation: String,
    val translit: String,
    val repeatTarget: Int
)

data class PrayerTime(
    val name: String,
    val arabicName: String,
    val timeString: String,
    val iconName: String
)

data class AchievementBadge(
    val title: String,
    val description: String,
    val earned: Boolean,
    val iconName: String
)

data class NotificationItem(
    val id: Int,
    val title: String,
    val body: String,
    val relativeTime: String,
    val iconName: String,
    val isUnread: Boolean
)

data class Reciter(
    val id: String,
    val name: String
)

enum class WeatherCondition {
    SUNNY,
    CLEAR_NIGHT,
    CLOUDY,
    RAINY,
    SNOWY,
    HOT
}

data class WeatherState(
    val tempC: Int = 26,
    val tempF: Int = 79,
    val condition: WeatherCondition = WeatherCondition.SUNNY,
    val conditionText: String = "Clear",
    val isLoading: Boolean = false
)

data class NameOfAllah(
    val id: Int,
    val name: String,
    val transliteration: String,
    val meaning: String,
    val explanation: String,
    val meaningEn: String,
    val evidence: String
)

data class QuranicDua(
    val id: Int,
    val text: String,
    val surah: String,
    val ayah: Int,
    val transliteration: String
)


