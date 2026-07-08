package com.example.data.remote

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class PrayerTimingsResponse(
    val code: Int,
    val status: String,
    val data: PrayerTimingsData
)

@JsonClass(generateAdapter = true)
data class PrayerTimingsData(
    val timings: Map<String, String>
)

interface PrayerApi {
    @GET("timingsByCity")
    suspend fun getTimingsByCity(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int? = null
    ): PrayerTimingsResponse

    @GET("timings")
    suspend fun getTimings(
        @Query("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int? = null
    ): PrayerTimingsResponse

    companion object {
        private const val BASE_URL = "https://api.aladhan.com/v1/"

        private val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val instance: PrayerApi by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(PrayerApi::class.java)
        }
    }
}
