package com.example.data.repository

import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedAyahEntity
import com.example.data.local.CompanionDao
import com.example.data.remote.QuranApi
import com.example.domain.model.Ayah
import com.example.domain.model.Surah
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

class RealQuranRepository(private val dao: CompanionDao) : QuranRepository {
    override fun getSurahsFlow(): Flow<List<Surah>> = dao.getCachedSurahsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getSurahsDirect(): List<Surah> = dao.getCachedSurahsDirect().map { it.toDomain() }

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

    override fun getAyahsForSurahFlow(surahNumber: Int, reciter: String): Flow<List<Ayah>> {
        return dao.getCachedAyahsFlow(surahNumber, reciter).map { entities ->
            entities.map { it.toDomain() }
        }
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
            // Create maps using both key types just in case
            val audioFilesByKey = audioResponse.audio_files.mapNotNull { it.verse_key?.let { key -> key to it } }.toMap()
            val audioFilesByNumber = audioResponse.audio_files.mapNotNull { it.verse_number?.let { num -> num to it } }.toMap()

            val entities = versesList.mapIndexed { index, verse ->
                val vNum = verse.verse_number ?: (index + 1)
                val vKey = verse.verse_key ?: "${surahNumber}:${vNum}"
                
                var rawAudioUrl = audioFilesByKey[vKey]?.url 
                    ?: audioFilesByNumber[vNum]?.url 
                    ?: ""
                val audioUrl = if (rawAudioUrl.isNotBlank()) {
                    if (rawAudioUrl.startsWith("//")) {
                        "https:$rawAudioUrl"
                    } else if (!rawAudioUrl.startsWith("http")) {
                        "https://verses.quran.foundation/${rawAudioUrl.trimStart('/')}"
                    } else {
                        rawAudioUrl
                    }
                } else {
                    ""
                }
                
                val cleanArabicText = if (vNum == 1 && surahNumber != 1 && surahNumber != 9) {
                    if (verse.text_uthmani.contains("بِسْمِ")) {
                        val words = verse.text_uthmani.split(" ")
                        if (words.size > 4 && words[0].contains("بِسْمِ")) {
                            words.drop(4).joinToString(" ").trim()
                        } else {
                            verse.text_uthmani
                        }
                    } else {
                        verse.text_uthmani
                    }
                } else {
                    verse.text_uthmani
                }

                CachedAyahEntity(
                    id = "${surahNumber}_${vNum}_${reciter}",
                    surahNumber = surahNumber,
                    numberInSurah = vNum,
                    reciter = reciter,
                    arabicText = cleanArabicText,
                    translation = verse.translations?.firstOrNull()?.text ?: "",
                    audioUrl = audioUrl
                )
            }

            dao.insertCachedAyahs(entities)
            return entities
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun getBookmarksFlow(): Flow<List<BookmarkEntity>> = dao.getBookmarksFlow()

    override suspend fun toggleBookmark(surahNumber: Int, surahName: String) {
        if (dao.isBookmarked(surahNumber)) {
            dao.deleteBookmark(surahNumber)
        } else {
            dao.insertBookmark(BookmarkEntity(surahNumber, surahName))
        }
    }

    override fun isBookmarkedFlow(surahNumber: Int): Flow<Boolean> = dao.isBookmarkedFlow(surahNumber)
}

private fun com.example.data.remote.Chapter.toEntity() = com.example.data.local.CachedSurahEntity(
    number = id,
    name = name_simple ?: "Unknown",
    meaning = translated_name?.name ?: "Unknown",
    ayahsCount = verses_count ?: 0,
    arabicName = name_arabic ?: "",
    isMakki = revelation_place?.equals("makkah", ignoreCase = true) ?: true
)

private fun com.example.data.local.CachedSurahEntity.toDomain() = Surah(
    number = number,
    name = name,
    meaning = meaning,
    ayahsCount = ayahsCount,
    arabicName = arabicName,
    isMakki = isMakki
)

private fun CachedAyahEntity.toDomain() = Ayah(
    number = numberInSurah,
    arabicText = arabicText,
    translation = translation,
    audioUrl = audioUrl
)
