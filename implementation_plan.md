# Redesign Next Prayer Home Widget

Redesign the **Next Prayer Widget Card** on the Home Screen to improve visual layout, adapt the background to weather conditions, and display the current weather temperature in Celsius and Fahrenheit.

## Proposed Changes

### [Component Name] Next Prayer Card & Home Screen

#### [MODIFY] [HomeScreen.kt](file:///d:/AI/Projects%20With%20AI/Islamic%20App/Muslim%20Companion%20V1/muslim-companion/app/src/main/java/com/example/ui/screens/HomeScreen.kt)
- **1. Remove Arabic Name from 2nd Row**: Remove `nextPrayerInfo.first.arabicName` text (`الظهر`) positioned below the main prayer title (`Dhuhr`), keeping a clean single prayer name title (translated according to language setting).
- **2. Weather-Responsive Dynamic Background**: Replace static gradient (`listOf(PrimaryTeal, Secondary)`) with dynamic weather-responsive background gradients based on current weather condition:
  - **Sunny / Clear Day**: Vibrant emerald teal gradient (`#00796B` → `#004D40`)
  - **Clear Night**: Midnight starlight indigo gradient (`#1B263B` → `#0D1B2A`)
  - **Cloudy / Overcast**: Slate teal-gray gradient (`#37474F` → `#263238`)
  - **Rainy / Stormy**: Deep stormy blue-teal gradient (`#2C3E50` → `#1A252C`)
  - **Snowy / Cold**: Frost teal ice gradient (`#455A64` → `#1C313A`)
  - **Hot Heatwave**: Warm golden amber-teal gradient (`#D84315` → `#00695C`)
- **3. Temperature & Weather Display (°C / °F)**:
  - Update top-right corner of the card to display both Celsius and Fahrenheit temperatures (e.g. `26°C / 79°F`) alongside a contextual weather icon (Sun, Moon, Cloud, Rain, Snowflake, etc.).

---

### [Component Name] Weather State & Data Integration

#### [NEW] [WeatherRepository.kt](file:///d:/AI/Projects%20With%20AI/Islamic%20App/Muslim%20Companion%20V1/muslim-companion/app/src/main/java/com/example/data/repository/WeatherRepository.kt)
- Implement lightweight weather repository leveraging Open-Meteo free API (or fallback location/time-based estimation when offline).
- Expose `StateFlow<WeatherState>` with fields:
  - `tempC`: Int
  - `tempF`: Int
  - `condition`: `WeatherCondition` (SUNNY, CLEAR_NIGHT, CLOUDY, RAINY, SNOWY, HOT)
  - `conditionDescription`: String

#### [MODIFY] [HomeViewModel.kt](file:///d:/AI/Projects%20With%20AI/Islamic%20App/Muslim%20Companion%20V1/muslim-companion/app/src/main/java/com/example/viewmodel/HomeViewModel.kt)
- Inject `WeatherRepository` and expose `weatherState` to `HomeScreen`.

---

## Verification Plan

### Automated Tests
- Run Gradle test suite: `gradlew test` to ensure zero regressions across viewmodels and repositories.

### Manual Verification
- Verify `HomeScreen` renders the updated card cleanly without duplicate Arabic text on line 2.
- Test background gradient transitions across different weather conditions (Sunny, Night, Cloudy, Rainy, Hot).
- Verify temperature displays accurately in both Celsius (°C) and Fahrenheit (°F).
