package com.example.data.repository

import com.example.data.remote.WeatherApi
import com.example.domain.model.WeatherCondition
import com.example.domain.model.WeatherState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor() {
    private val _weatherState = MutableStateFlow(getDefaultWeatherState())
    val weatherState: StateFlow<WeatherState> = _weatherState

    suspend fun fetchWeather(latitude: Double = 21.422487, longitude: Double = 39.826206) {
        try {
            val response = WeatherApi.instance.getCurrentWeather(latitude, longitude)
            val curr = response.currentWeather
            if (curr != null) {
                val tempC = curr.temperature.toInt()
                val tempF = (tempC * 9 / 5) + 32
                val hour = LocalTime.now().hour
                val isNight = curr.isDay == 0 || (hour < 6 || hour >= 19)

                val (condition, text) = parseWeatherCode(curr.weathercode, isNight, tempC)
                _weatherState.value = WeatherState(
                    tempC = tempC,
                    tempF = tempF,
                    condition = condition,
                    conditionText = text,
                    isLoading = false
                )
            } else {
                _weatherState.value = getDefaultWeatherState()
            }
        } catch (e: Exception) {
            _weatherState.value = getDefaultWeatherState()
        }
    }

    private fun parseWeatherCode(code: Int, isNight: Boolean, tempC: Int): Pair<WeatherCondition, String> {
        if (tempC >= 38) {
            return WeatherCondition.HOT to "Hot"
        }
        return when (code) {
            0, 1 -> if (isNight) WeatherCondition.CLEAR_NIGHT to "Clear Night" else WeatherCondition.SUNNY to "Sunny"
            2, 3, 45, 48 -> WeatherCondition.CLOUDY to "Cloudy"
            51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99 -> WeatherCondition.RAINY to "Rainy"
            71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOWY to "Snowy"
            else -> if (isNight) WeatherCondition.CLEAR_NIGHT to "Clear Night" else WeatherCondition.SUNNY to "Sunny"
        }
    }

    private fun getDefaultWeatherState(): WeatherState {
        val hour = LocalTime.now().hour
        val isNight = hour < 6 || hour >= 19
        val defaultTempC = if (isNight) 22 else 30
        val defaultTempF = (defaultTempC * 9 / 5) + 32
        val condition = if (isNight) WeatherCondition.CLEAR_NIGHT else WeatherCondition.SUNNY
        val text = if (isNight) "Clear Night" else "Sunny"
        return WeatherState(
            tempC = defaultTempC,
            tempF = defaultTempF,
            condition = condition,
            conditionText = text,
            isLoading = false
        )
    }
}
