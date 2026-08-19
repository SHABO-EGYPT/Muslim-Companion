package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class PrayerTimingsResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: PrayerTimingsData
)

@JsonClass(generateAdapter = true)
data class PrayerTimingsData(
    @Json(name = "timings") val timings: Map<String, String>
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
}

