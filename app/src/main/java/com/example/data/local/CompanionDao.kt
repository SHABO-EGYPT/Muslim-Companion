package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanionDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgressFlow(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressDirect(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM bookmarks")
    fun getBookmarksFlow(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surahNumber")
    suspend fun deleteBookmark(surahNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber LIMIT 1)")
    suspend fun isBookmarked(surahNumber: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber LIMIT 1)")
    fun isBookmarkedFlow(surahNumber: Int): Flow<Boolean>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingEntity)

    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber AND reciter = :reciter ORDER BY numberInSurah ASC")
    fun getCachedAyahsFlow(surahNumber: Int, reciter: String): Flow<List<CachedAyahEntity>>

    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber AND reciter = :reciter ORDER BY numberInSurah ASC")
    suspend fun getCachedAyahs(surahNumber: Int, reciter: String): List<CachedAyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedAyahs(ayahs: List<CachedAyahEntity>)

    @Query("SELECT * FROM cached_surahs ORDER BY number ASC")
    fun getCachedSurahsFlow(): Flow<List<CachedSurahEntity>>

    @Query("SELECT * FROM cached_surahs ORDER BY number ASC")
    suspend fun getCachedSurahsDirect(): List<CachedSurahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedSurahs(surahs: List<CachedSurahEntity>)

    // ── quran_ayahs (offline bundled text) ──────────────────────────────────

    @Query("SELECT * FROM quran_ayahs WHERE sura = :sura ORDER BY ayah ASC")
    suspend fun getAyahsForSura(sura: Int): List<QuranAyahEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuranAyahs(ayahs: List<QuranAyahEntity>)

    @Query("SELECT COUNT(*) FROM quran_ayahs")
    suspend fun getQuranAyahCount(): Int

    @Query("SELECT * FROM cached_prayer_times")
    fun getCachedPrayerTimesFlow(): Flow<List<CachedPrayerTimeEntity>>

    @Query("SELECT * FROM cached_prayer_times")
    suspend fun getCachedPrayerTimesDirect(): List<CachedPrayerTimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedPrayerTimes(times: List<CachedPrayerTimeEntity>)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()
}
