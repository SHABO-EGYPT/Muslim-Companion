package com.example.fake

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Minimal fake DAO for use by FakeCompanionRepository.
 * Only provides what the CompanionRepository constructor needs.
 */
class FakeDao : CompanionDao {
    private val _progress = MutableStateFlow<UserProgressEntity?>(UserProgressEntity())
    private val _settings = MutableStateFlow<AppSettingEntity?>(AppSettingEntity())
    private val _prayerTimes = MutableStateFlow<List<CachedPrayerTimeEntity>>(emptyList())
    private val _surahs = MutableStateFlow<List<CachedSurahEntity>>(emptyList())
    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    private val _quranAyahs = MutableStateFlow<List<QuranAyahEntity>>(emptyList())

    override fun getUserProgressFlow(): Flow<UserProgressEntity?> = _progress
    override suspend fun getUserProgressDirect(): UserProgressEntity? = _progress.value
    override suspend fun saveUserProgress(progress: UserProgressEntity) { _progress.value = progress }

    override fun getSettingsFlow(): Flow<AppSettingEntity?> = _settings
    override suspend fun getSettingsDirect(): AppSettingEntity? = _settings.value
    override suspend fun saveSettings(settings: AppSettingEntity) { _settings.value = settings }

    override fun getBookmarksFlow(): Flow<List<BookmarkEntity>> = MutableStateFlow(emptyList())
    override suspend fun insertBookmark(bookmark: BookmarkEntity) {}
    override suspend fun deleteBookmark(surahNumber: Int) {}
    override suspend fun isBookmarked(surahNumber: Int): Boolean = false
    override fun isBookmarkedFlow(surahNumber: Int): Flow<Boolean> = MutableStateFlow(false)

    override fun getCachedAyahsFlow(surahNumber: Int, reciter: String): Flow<List<CachedAyahEntity>> = MutableStateFlow(emptyList())
    override suspend fun getCachedAyahs(surahNumber: Int, reciter: String): List<CachedAyahEntity> = emptyList()
    override suspend fun insertCachedAyahs(ayahs: List<CachedAyahEntity>) {}
    override suspend fun deleteCachedAyahsForReciter(reciter: String) {}

    override fun getCachedSurahsFlow(): Flow<List<CachedSurahEntity>> = _surahs
    override suspend fun getCachedSurahsDirect(): List<CachedSurahEntity> = _surahs.value
    override suspend fun insertCachedSurahs(surahs: List<CachedSurahEntity>) { _surahs.value = surahs }

    override fun getCachedPrayerTimesFlow(): Flow<List<CachedPrayerTimeEntity>> = _prayerTimes
    override suspend fun getCachedPrayerTimesDirect(): List<CachedPrayerTimeEntity> = _prayerTimes.value
    override suspend fun insertCachedPrayerTimes(times: List<CachedPrayerTimeEntity>) { _prayerTimes.value = times }

    override fun getNotificationsFlow(): Flow<List<NotificationEntity>> = _notifications
    override suspend fun insertNotification(notification: NotificationEntity) {
        _notifications.value = _notifications.value + notification
    }
    override suspend fun markNotificationAsRead(id: Int) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    override suspend fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map {
            it.copy(isRead = true)
        }
    }

    override suspend fun deleteNotification(id: Int) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    override suspend fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    override suspend fun getAyahsForSura(sura: Int): List<QuranAyahEntity> {
        return _quranAyahs.value.filter { it.sura == sura }
    }

    override suspend fun insertQuranAyahs(ayahs: List<QuranAyahEntity>) {
        _quranAyahs.value = _quranAyahs.value + ayahs
    }

    override suspend fun getQuranAyahCount(): Int {
        return _quranAyahs.value.size
    }

    private val _customChains = MutableStateFlow<List<CustomDhikrChainEntity>>(emptyList())

    override fun getAllCustomChainsFlow(): Flow<List<CustomDhikrChainEntity>> = _customChains

    override suspend fun getCustomChainById(id: Int): CustomDhikrChainEntity? {
        return _customChains.value.find { it.id == id }
    }

    override suspend fun insertCustomChain(chain: CustomDhikrChainEntity): Long {
        val current = _customChains.value.toMutableList()
        val assignedId = if (chain.id == 0) current.size + 1 else chain.id
        val updated = chain.copy(id = assignedId)
        val idx = current.indexOfFirst { it.id == assignedId }
        if (idx >= 0) current[idx] = updated else current.add(updated)
        _customChains.value = current
        return assignedId.toLong()
    }

    override suspend fun updateCustomChain(chain: CustomDhikrChainEntity) {
        val current = _customChains.value.toMutableList()
        val idx = current.indexOfFirst { it.id == chain.id }
        if (idx >= 0) {
            current[idx] = chain
            _customChains.value = current
        }
    }

    override suspend fun deleteCustomChain(chain: CustomDhikrChainEntity) {
        _customChains.value = _customChains.value.filterNot { it.id == chain.id }
    }

    override suspend fun deleteCustomChainById(id: Int) {
        _customChains.value = _customChains.value.filterNot { it.id == id }
    }
}
