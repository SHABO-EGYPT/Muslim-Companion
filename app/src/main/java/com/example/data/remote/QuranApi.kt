package com.example.data.remote

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class ChaptersResponse(val chapters: List<Chapter>)

@JsonClass(generateAdapter = true)
data class Chapter(
    val id: Int,
    val name_simple: String? = null,
    val name_arabic: String? = null,
    val verses_count: Int? = null,
    val revelation_place: String? = null,
    val translated_name: TranslatedName? = null
)

@JsonClass(generateAdapter = true)
data class TranslatedName(val name: String? = null)

@JsonClass(generateAdapter = true)
data class VersesResponse(val verses: List<Verse>)

@JsonClass(generateAdapter = true)
data class Verse(
    val id: Int,
    val verse_number: Int? = null,
    val verse_key: String? = null,
    val text_uthmani: String,
    val translations: List<Translation>? = null
)

@JsonClass(generateAdapter = true)
data class Translation(
    val id: Int,
    val resource_id: Int,
    val text: String
)

@JsonClass(generateAdapter = true)
data class AudioResponse(val audio_files: List<AudioFile>)

@JsonClass(generateAdapter = true)
data class AudioFile(
    val verse_key: String? = null,
    val verse_number: Int? = null,
    val url: String
)

interface QuranApi {
    @GET("chapters")
    suspend fun getAllChapters(@Query("per_page") perPage: Int = 114): ChaptersResponse

    @GET("verses/by_chapter/{chapter_number}")
    suspend fun getChapterVerses(
        @Path("chapter_number") chapterNumber: Int,
        @Query("language") language: String = "en",
        @Query("translations") translations: Int = 85,
        @Query("fields") fields: String = "text_uthmani"
    ): VersesResponse

    @GET("recitations/{reciter_id}/by_chapter/{chapter_id}")
    suspend fun getChapterAudio(
        @Path("reciter_id") reciterId: Int,
        @Path("chapter_id") chapterId: Int
    ): AudioResponse

    companion object {
        private const val BASE_URL = "https://api.quran.com/api/v4/"

        private val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        private val client = OkHttpClient.Builder()
            .build()

        val instance: QuranApi by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(QuranApi::class.java)
        }
    }
}
