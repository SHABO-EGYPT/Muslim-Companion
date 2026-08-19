package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class CurrentWeatherInfo(
    @Json(name = "temperature") val temperature: Double = 25.0,
    @Json(name = "windspeed") val windspeed: Double = 0.0,
    @Json(name = "weathercode") val weathercode: Int = 0,
    @Json(name = "is_day") val isDay: Int = 1
)

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeatherInfo? = null
)

interface WeatherApi {
    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): OpenMeteoResponse
}

