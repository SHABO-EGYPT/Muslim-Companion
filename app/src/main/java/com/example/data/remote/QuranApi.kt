package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class ChaptersResponse(
    @Json(name = "chapters") val chapters: List<Chapter>
)

@JsonClass(generateAdapter = true)
data class Chapter(
    @Json(name = "id") val id: Int,
    @Json(name = "name_simple") val nameSimple: String? = null,
    @Json(name = "name_arabic") val nameArabic: String? = null,
    @Json(name = "verses_count") val versesCount: Int? = null,
    @Json(name = "revelation_place") val revelationPlace: String? = null,
    @Json(name = "translated_name") val translatedName: TranslatedName? = null
)

@JsonClass(generateAdapter = true)
data class TranslatedName(
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class VersesResponse(
    @Json(name = "verses") val verses: List<Verse>
)

@JsonClass(generateAdapter = true)
data class Verse(
    @Json(name = "id") val id: Int,
    @Json(name = "verse_number") val verseNumber: Int? = null,
    @Json(name = "verse_key") val verseKey: String? = null,
    @Json(name = "text_uthmani") val textUthmani: String,
    @Json(name = "translations") val translations: List<Translation>? = null
)

@JsonClass(generateAdapter = true)
data class Translation(
    @Json(name = "id") val id: Int,
    @Json(name = "resource_id") val resourceId: Int,
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class AudioResponse(
    @Json(name = "audio_files") val audioFiles: List<AudioFile>
)

@JsonClass(generateAdapter = true)
data class AudioFile(
    @Json(name = "verse_key") val verseKey: String? = null,
    @Json(name = "verse_number") val verseNumber: Int? = null,
    @Json(name = "url") val url: String
)

interface QuranApi {
    @GET("chapters")
    suspend fun getAllChapters(@Query("per_page") perPage: Int = 114): ChaptersResponse

    @GET("verses/by_chapter/{chapter_number}")
    suspend fun getChapterVerses(
        @Path("chapter_number") chapterNumber: Int,
        @Query("language") language: String = "en",
        @Query("translations") translations: Int = 85,
        @Query("fields") fields: String = "text_uthmani",
        @Query("per_page") perPage: Int = 300
    ): VersesResponse

    @GET("recitations/{reciter_id}/by_chapter/{chapter_id}")
    suspend fun getChapterAudio(
        @Path("reciter_id") reciterId: Int,
        @Path("chapter_id") chapterId: Int,
        @Query("per_page") perPage: Int = 300
    ): AudioResponse
}

