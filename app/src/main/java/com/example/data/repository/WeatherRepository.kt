package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.remote.WeatherApi
import com.example.domain.model.WeatherCondition
import com.example.domain.model.WeatherState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherApi: WeatherApi
) {
    companion object {
        private const val TAG = "WeatherRepository"
        private const val PREFS_NAME = "weather_prefs"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _weatherState = MutableStateFlow(getInitialWeatherState())
    val weatherState: StateFlow<WeatherState> = _weatherState

    suspend fun fetchWeather(latitude: Double = 30.0444, longitude: Double = 31.2357) {
        try {
            val response = weatherApi.getCurrentWeather(latitude, longitude)
            val curr = response.activeWeather
            if (curr != null) {
                val tempC = Math.round(curr.effectiveTemperature).toInt()
                val tempF = Math.round((tempC * 9.0 / 5.0) + 32).toInt()
                val hour = LocalTime.now().hour
                val isNight = curr.isDay == 0 || (hour < 6 || hour >= 19)

                val (condition, text) = parseWeatherCode(curr.effectiveWeatherCode, isNight, tempC)
                val newState = WeatherState(
                    tempC = tempC,
                    tempF = tempF,
                    condition = condition,
                    conditionText = text,
                    isLoading = false
                )
                _weatherState.value = newState

                // Persist cached weather for widgets and instant display
                try {
                    prefs.edit()
                        .putInt("cached_temp_c", tempC)
                        .putInt("cached_temp_f", tempF)
                        .putString("cached_condition", condition.name)
                        .putString("cached_condition_text", text)
                        .putLong("cached_time", System.currentTimeMillis())
                        .apply()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to persist weather prefs", e)
                }
            } else {
                _weatherState.value = getInitialWeatherState()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch weather data for ($latitude, $longitude): ${e.message}")
            _weatherState.value = getInitialWeatherState()
        }
    }

    private fun parseWeatherCode(code: Int, isNight: Boolean, tempC: Int): Pair<WeatherCondition, String> {
        if (tempC >= 38) {
            return WeatherCondition.HOT to "Hot"
        }
        return when (code) {
            0 -> if (isNight) WeatherCondition.CLEAR_NIGHT to "Clear Night" else WeatherCondition.SUNNY to "Sunny"
            1, 2 -> if (isNight) WeatherCondition.CLEAR_NIGHT to "Mainly Clear" else WeatherCondition.SUNNY to "Mainly Sunny"
            3 -> WeatherCondition.CLOUDY to "Overcast"
            45, 48 -> WeatherCondition.CLOUDY to "Foggy"
            51, 53, 55 -> WeatherCondition.RAINY to "Drizzle"
            56, 57 -> WeatherCondition.RAINY to "Freezing Drizzle"
            61, 63, 65 -> WeatherCondition.RAINY to "Rain"
            66, 67 -> WeatherCondition.RAINY to "Freezing Rain"
            71, 73, 75, 77 -> WeatherCondition.SNOWY to "Snowfall"
            80, 81, 82 -> WeatherCondition.RAINY to "Rain Showers"
            85, 86 -> WeatherCondition.SNOWY to "Snow Showers"
            95, 96, 99 -> WeatherCondition.RAINY to "Thunderstorm"
            else -> if (isNight) WeatherCondition.CLEAR_NIGHT to "Clear Night" else WeatherCondition.SUNNY to "Sunny"
        }
    }

    private fun getInitialWeatherState(): WeatherState {
        // Read cached weather if available
        if (prefs.contains("cached_temp_c")) {
            val tempC = prefs.getInt("cached_temp_c", 26)
            val tempF = prefs.getInt("cached_temp_f", (tempC * 9 / 5) + 32)
            val condName = prefs.getString("cached_condition", "SUNNY") ?: "SUNNY"
            val condText = prefs.getString("cached_condition_text", "Sunny") ?: "Sunny"
            val condition = try {
                WeatherCondition.valueOf(condName)
            } catch (_: Exception) {
                WeatherCondition.SUNNY
            }
            return WeatherState(
                tempC = tempC,
                tempF = tempF,
                condition = condition,
                conditionText = condText,
                isLoading = false
            )
        }

        val hour = LocalTime.now().hour
        val isNight = hour < 6 || hour >= 19
        val defaultTempC = if (isNight) 22 else 28
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
