package com.example.data.repository

import android.util.Log
import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedAyahEntity
import com.example.data.local.CompanionDao
import com.example.data.quran.QuranAssetLoader
import com.example.data.quran.QuranAudioManager
import com.example.data.quran.SurahMetadata
import com.example.data.remote.QuranApi
import com.example.domain.model.Ayah
import com.example.domain.model.Surah
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

// ─────────────────────────────────────────────────────────────────────────────
// Interface (unchanged — ViewModels depend only on this)
// ─────────────────────────────────────────────────────────────────────────────

interface QuranRepository {
    fun getSurahsFlow(): Flow<List<Surah>>
    suspend fun getSurahsDirect(): List<Surah>
    suspend fun refreshSurahs()
    fun getAyahsForSurahFlow(surahNumber: Int, reciter: String): Flow<List<Ayah>>
    suspend fun refreshAyahs(surahNumber: Int, reciter: String)
    suspend fun getSurahWithAudio(surahNumber: Int, reciter: String): List<CachedAyahEntity>
    fun getBookmarksFlow(): Flow<List<BookmarkEntity>>
    suspend fun toggleBookmark(surahNumber: Int, surahName: String)
    fun isBookmarkedFlow(surahNumber: Int): Flow<Boolean>
}

// ─────────────────────────────────────────────────────────────────────────────
// Offline implementation (ACTIVE)
// • Surahs    → hardcoded via SurahMetadata (no network)
// • Arabic    → Room DB seeded from bundled asset on first launch
// • Translation → fetched online from quran.com (per user request)
// • Audio     → downloaded from quranicaudio.com to internal storage
// ─────────────────────────────────────────────────────────────────────────────

class OfflineQuranRepository(
    private val dao: CompanionDao,
    private val assetLoader: QuranAssetLoader,
    private val audioManager: QuranAudioManager
) : QuranRepository {

    // ── Surahs ───────────────────────────────────────────────────────────────

    override fun getSurahsFlow(): Flow<List<Surah>> = flow {
        emit(SurahMetadata.ALL.map { it.toDomain() })
    }

    override suspend fun getSurahsDirect(): List<Surah> =
        SurahMetadata.ALL.map { it.toDomain() }

    /** Seeds the DB from assets if needed; surah list itself is hardcoded. */
    override suspend fun refreshSurahs() {
        if (!assetLoader.isSeeded()) {
            Log.i("QuranRepo", "Seeding quran_ayahs from bundled asset…")
            assetLoader.seedDatabase()
            Log.i("QuranRepo", "Seeding complete (6236 ayahs).")
        }
    }

    // ── Ayahs ────────────────────────────────────────────────────────────────

    override fun getAyahsForSurahFlow(surahNumber: Int, reciter: String): Flow<List<Ayah>> =
        flow {
            val entities = dao.getAyahsForSura(surahNumber)
            // Normalise legacy numeric reciter IDs (e.g. "7") → "ar.alafasy"
            val cacheKey = if (reciter.all { it.isDigit() }) "ar.alafasy" else reciter
            val cachedTranslations = dao.getCachedAyahs(surahNumber, cacheKey)
                .associateBy { it.numberInSurah }

            emit(entities.map { entity ->
                val cached = cachedTranslations[entity.ayah]
                Ayah(
                    number = entity.ayah,
                    arabicText = entity.arabicText,
                    translation = cached?.translation ?: "",
                    audioUrl = cached?.audioUrl ?: ""
                )
            })
        }

    /**
     * 1. Seeds Arabic text from asset if needed.
     * 2. Fetches translation and verse-by-verse audio URLs online and caches them.
     * 3. Triggers background download of sura MP3 for offline audio.
     */
    override suspend fun refreshAyahs(surahNumber: Int, reciter: String) {
        // Step 1: ensure Arabic text is seeded (blocks until done)
        if (!assetLoader.isSeeded()) {
            Log.i("QuranRepo", "Seeding Arabic text before loading ayahs…")
            assetLoader.seedDatabase()
        }

        // Step 2: fetch translation & audio if not already cached.
        val isLegacyReciterId = reciter.all { it.isDigit() }
        val existing = if (isLegacyReciterId) emptyList()
                       else dao.getCachedAyahs(surahNumber, reciter)

        if (existing.isEmpty() || existing.any { it.audioUrl.isBlank() }) {
            try {
                val versesResponse = QuranApi.instance.getChapterVerses(surahNumber)
                val arabicMap = dao.getAyahsForSura(surahNumber).associateBy { it.ayah }

                // Fetch verse audio URLs
                val numericReciterId = when (reciter) {
                    "ar.alafasy" -> 7
                    "ar.abdulbasitmurattal" -> 1
                    "ar.husary" -> 5
                    "ar.minshawimujawwad" -> 12
                    else -> if (reciter.all { it.isDigit() }) reciter.toInt() else 7
                }
                val audioResponse = try {
                    QuranApi.instance.getChapterAudio(numericReciterId, surahNumber)
                } catch (e: Exception) {
                    null
                }
                val audioByKey = audioResponse?.audio_files?.mapNotNull { it.verse_key?.let { k -> k to it } }?.toMap() ?: emptyMap()
                val audioByNum = audioResponse?.audio_files?.mapNotNull { it.verse_number?.let { n -> n to it } }?.toMap() ?: emptyMap()

                val entities = versesResponse.verses.mapIndexed { index, verse ->
                    val vNum = verse.verse_number ?: (index + 1)
                    val arabicText = arabicMap[vNum]?.arabicText ?: verse.text_uthmani

                    val rawTranslation = verse.translations?.firstOrNull()?.text ?: ""
                    val cleanTranslation = if (rawTranslation.isNotBlank()) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            android.text.Html.fromHtml(rawTranslation, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                        } else {
                            @Suppress("DEPRECATION")
                            android.text.Html.fromHtml(rawTranslation).toString().trim()
                        }
                    } else ""

                    val vKey = "${surahNumber}:${vNum}"
                    val rawUrl = audioByKey[vKey]?.url ?: audioByNum[vNum]?.url ?: ""
                    val audioUrl = when {
                        rawUrl.isBlank() -> ""
                        rawUrl.startsWith("//") -> "https:$rawUrl"
                        !rawUrl.startsWith("http") -> "https://verses.quran.foundation/${rawUrl.trimStart('/')}"
                        else -> rawUrl
                    }

                    // Always store with the new-format reciter ID
                    val cacheKey = if (isLegacyReciterId) "ar.alafasy" else reciter
                    CachedAyahEntity(
                        id = "${surahNumber}_${vNum}_${cacheKey}",
                        surahNumber = surahNumber,
                        numberInSurah = vNum,
                        reciter = cacheKey,
                        arabicText = arabicText,
                        translation = cleanTranslation,
                        audioUrl = audioUrl
                    )
                }
                dao.insertCachedAyahs(entities)
            } catch (e: Exception) {
                Log.w("QuranRepo", "Translation/Audio fetch failed (offline?): ${e.message}")
            }
        }

        // Step 3: trigger background audio download
        val audioReciter = if (isLegacyReciterId) "ar.alafasy" else reciter
        audioManager.downloadSura(audioReciter, surahNumber)
    }

    override suspend fun getSurahWithAudio(surahNumber: Int, reciter: String): List<CachedAyahEntity> {
        refreshAyahs(surahNumber, reciter)
        return dao.getCachedAyahs(surahNumber, reciter)
    }

    // ── Bookmarks ────────────────────────────────────────────────────────────

    override fun getBookmarksFlow(): Flow<List<BookmarkEntity>> = dao.getBookmarksFlow()

    override suspend fun toggleBookmark(surahNumber: Int, surahName: String) {
        if (dao.isBookmarked(surahNumber)) dao.deleteBookmark(surahNumber)
        else dao.insertBookmark(BookmarkEntity(surahNumber, surahName))
    }

    override fun isBookmarkedFlow(surahNumber: Int): Flow<Boolean> =
        dao.isBookmarkedFlow(surahNumber)
}

// ─────────────────────────────────────────────────────────────────────────────
// Legacy online implementation (kept for easy rollback; not wired in DI)
// ─────────────────────────────────────────────────────────────────────────────

class RealQuranRepository(private val dao: CompanionDao) : QuranRepository {
    override fun getSurahsFlow(): Flow<List<Surah>> = dao.getCachedSurahsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getSurahsDirect(): List<Surah> =
        dao.getCachedSurahsDirect().map { it.toDomain() }

    override suspend fun refreshSurahs() {
        val cachedCount = dao.getCachedSurahsDirect().size
        if (cachedCount >= 114) return
        try {
            val response = QuranApi.instance.getAllChapters(114)
            if (response.chapters.isNotEmpty()) {
                dao.insertCachedSurahs(response.chapters.map { it.toEntity() })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun getAyahsForSurahFlow(surahNumber: Int, reciter: String): Flow<List<Ayah>> =
        dao.getCachedAyahsFlow(surahNumber, reciter).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshAyahs(surahNumber: Int, reciter: String) {
        getSurahWithAudio(surahNumber, reciter)
    }

    override suspend fun getSurahWithAudio(surahNumber: Int, reciter: String): List<CachedAyahEntity> {
        val cached = dao.getCachedAyahs(surahNumber, reciter)
        if (cached.isNotEmpty()) return cached
        try {
            val versesResponse = QuranApi.instance.getChapterVerses(surahNumber)
            val audioResponse = QuranApi.instance.getChapterAudio(reciter.toInt(), surahNumber)
            val versesList = versesResponse.verses
            val audioByKey = audioResponse.audio_files.mapNotNull { it.verse_key?.let { k -> k to it } }.toMap()
            val audioByNum = audioResponse.audio_files.mapNotNull { it.verse_number?.let { n -> n to it } }.toMap()
            val entities = versesList.mapIndexed { index, verse ->
                val vNum = verse.verse_number ?: (index + 1)
                val vKey = verse.verse_key ?: "${surahNumber}:${vNum}"
                var rawUrl = audioByKey[vKey]?.url ?: audioByNum[vNum]?.url ?: ""
                val audioUrl = when {
                    rawUrl.isBlank() -> ""
                    rawUrl.startsWith("//") -> "https:$rawUrl"
                    !rawUrl.startsWith("http") -> "https://verses.quran.foundation/${rawUrl.trimStart('/')}"
                    else -> rawUrl
                }
                val cleanArabic = if (vNum == 1 && surahNumber != 1 && surahNumber != 9 && verse.text_uthmani.contains("بِسْمِ")) {
                    val words = verse.text_uthmani.split(" ")
                    if (words.size > 4 && words[0].contains("بِسْمِ")) words.drop(4).joinToString(" ").trim()
                    else verse.text_uthmani
                } else verse.text_uthmani
                CachedAyahEntity(
                    id = "${surahNumber}_${vNum}_${reciter}", surahNumber = surahNumber,
                    numberInSurah = vNum, reciter = reciter, arabicText = cleanArabic,
                    translation = verse.translations?.firstOrNull()?.text ?: "", audioUrl = audioUrl
                )
            }
            dao.insertCachedAyahs(entities)
            return entities
        } catch (e: Exception) { e.printStackTrace(); throw e }
    }

    override fun getBookmarksFlow(): Flow<List<BookmarkEntity>> = dao.getBookmarksFlow()
    override suspend fun toggleBookmark(surahNumber: Int, surahName: String) {
        if (dao.isBookmarked(surahNumber)) dao.deleteBookmark(surahNumber)
        else dao.insertBookmark(BookmarkEntity(surahNumber, surahName))
    }
    override fun isBookmarkedFlow(surahNumber: Int): Flow<Boolean> = dao.isBookmarkedFlow(surahNumber)
}

// ─────────────────────────────────────────────────────────────────────────────
// Extension mappers (private)
// ─────────────────────────────────────────────────────────────────────────────

private fun com.example.data.remote.Chapter.toEntity() = com.example.data.local.CachedSurahEntity(
    number = id, name = name_simple ?: "Unknown", meaning = translated_name?.name ?: "Unknown",
    ayahsCount = verses_count ?: 0, arabicName = name_arabic ?: "",
    isMakki = revelation_place?.equals("makkah", ignoreCase = true) ?: true
)

private fun com.example.data.local.CachedSurahEntity.toDomain() = Surah(
    number = number, name = name, meaning = meaning, ayahsCount = ayahsCount,
    arabicName = arabicName, isMakki = isMakki
)

private fun CachedAyahEntity.toDomain() = Ayah(
    number = numberInSurah, arabicText = arabicText, translation = translation, audioUrl = audioUrl
)
