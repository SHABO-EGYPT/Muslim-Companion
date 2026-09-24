package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class CurrentWeatherInfo(
    @Json(name = "temperature") val temperature: Double = 25.0,
    @Json(name = "temperature_2m") val temperature2m: Double? = null,
    @Json(name = "windspeed") val windspeed: Double = 0.0,
    @Json(name = "weathercode") val weathercode: Int = 0,
    @Json(name = "weather_code") val weatherCodeNew: Int? = null,
    @Json(name = "is_day") val isDay: Int = 1
) {
    val effectiveTemperature: Double
        get() = temperature2m ?: temperature

    val effectiveWeatherCode: Int
        get() = weatherCodeNew ?: weathercode
}

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeatherInfo? = null,
    @Json(name = "current") val current: CurrentWeatherInfo? = null
) {
    val activeWeather: CurrentWeatherInfo?
        get() = current ?: currentWeather
}

interface WeatherApi {
    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("current") current: String = "temperature_2m,weather_code,is_day",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}

