package com.example.fake

import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedAyahEntity
import com.example.data.repository.QuranRepository
import com.example.domain.model.Ayah
import com.example.domain.model.Surah
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake QuranRepository for unit testing.
 * No network calls — fully in-memory and controllable.
 */
class FakeQuranRepository : QuranRepository {

    private val _surahs = MutableStateFlow<List<Surah>>(emptyList())
    private val _bookmarks = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    private val _ayahs = mutableMapOf<Pair<Int, String>, List<Ayah>>()
    private val _cachedAyahs = mutableMapOf<Pair<Int, String>, List<CachedAyahEntity>>()

    fun setSurahs(surahs: List<Surah>) {
        _surahs.value = surahs
    }

    fun setAyahs(surahNumber: Int, reciter: String, ayahs: List<Ayah>) {
        _ayahs[surahNumber to reciter] = ayahs
    }

    fun setCachedAyahs(surahNumber: Int, reciter: String, ayahs: List<CachedAyahEntity>) {
        _cachedAyahs[surahNumber to reciter] = ayahs
    }

    fun addBookmark(surahNumber: Int, surahName: String) {
        val current = _bookmarks.value.toMutableList()
        current.add(BookmarkEntity(surahNumber, surahName))
        _bookmarks.value = current
    }

    override fun getSurahsFlow(): Flow<List<Surah>> = _surahs

    override suspend fun getSurahsDirect(): List<Surah> = _surahs.value

    override suspend fun refreshSurahs() {
        // No-op in fake
    }

    override fun getAyahsForSurahFlow(surahNumber: Int, reciter: String): Flow<List<Ayah>> {
        return MutableStateFlow(_ayahs[surahNumber to reciter] ?: emptyList())
    }

    override suspend fun refreshAyahs(surahNumber: Int, reciter: String) {
        // No-op in fake
    }

    override fun getBookmarksFlow(): Flow<List<BookmarkEntity>> = _bookmarks

    override suspend fun toggleBookmark(surahNumber: Int, surahName: String) {
        val current = _bookmarks.value.toMutableList()
        val existing = current.find { it.surahNumber == surahNumber }
        if (existing != null) {
            current.remove(existing)
        } else {
            current.add(BookmarkEntity(surahNumber, surahName))
        }
        _bookmarks.value = current
    }

    override fun isBookmarkedFlow(surahNumber: Int): Flow<Boolean> {
        return MutableStateFlow(_bookmarks.value.any { it.surahNumber == surahNumber })
    }

    override suspend fun getSurahWithAudio(surahNumber: Int, reciter: String): List<CachedAyahEntity> {
        return _cachedAyahs[surahNumber to reciter] ?: emptyList()
    }
}
