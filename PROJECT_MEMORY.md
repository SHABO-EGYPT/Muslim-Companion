# Muslim Companion (رفيق المسلم) — Project Memory & Architecture Blueprint
**Date:** September 24, 2026  
**Project:** Muslim Companion (`com.companion.muslim.app`)  
**Current Release Version:** v1.7.4 (`versionCode = 11`)  
**Dedication:** 100% Free & Ad-Free Sadaqah Jariyah (صدقة جارية)

---

## 1. Executive Summary & Philosophy

**Muslim Companion (رفيق المسلم)** is an Android Islamic companion built strictly adhering to **Modern Android Development (MAD)** standards and Clean Architecture. 

### Core Tenets:
1. **100% Free & Ad-Free:** Zero advertisements, zero subscriptions, zero tracking analytics, zero monetized paywalls.
2. **Offline-First Resilience:** 6,236 Ayahs of authentic Uthmani Arabic scripture pre-bundled locally in Room SQLite, offline solar geometry calculation for prayer times, and local storage for bookmarks, progress, and Azkar counters.
3. **High Aesthetic Standards:** Material Design 3 guidelines, dynamic RTL/LTR flipping, tailored Dark/Light mode color schemes, typography scaling, and tactile haptic feedback.
4. **Authenticity & Integrity:** Hadith and Azkar references sourced directly from Hisn Al-Muslim (Sahih al-Bukhari, Sahih Muslim, Sunan Abi Dawud, At-Tirmidhi). Recitations sourced from open-access non-profit networks ([QuranicAudio.com](https://quranicaudio.com/) / Quran.com).

---

## 2. Technical Stack & Core Architecture

| Category | Technologies / Libraries |
| :--- | :--- |
| **Language & Concurrency** | **Kotlin 2.0+**, Coroutines, StateFlow, SharedFlow, Channels |
| **UI & Styling** | **100% Jetpack Compose**, Material Design 3, dynamic Compose `fontScale` typography |
| **Architecture Pattern** | **Clean Architecture** with Unidirectional Data Flow (UDF) (Data $\to$ Domain $\to$ Presentation) |
| **Dependency Injection** | **Dagger Hilt** (`@HiltAndroidApp`, `@HiltViewModel`, `@AndroidEntryPoint`) |
| **Local Persistence** | **Room SQLite (v30)** with automated step migrations (`MIGRATION_18_19` through `MIGRATION_29_30`) |
| **Audio Engine** | **AndroidX Media3 (ExoPlayer)** + foreground `MediaSessionService` (`QuranAudioService`) |
| **Target & Min SDK** | **Target SDK: 37** (Android 15+), **Compile SDK: 37**, **Min SDK: 26** (Android 8.0+) |
| **Network & Remote** | **Retrofit 2** + **Moshi** (`KotlinJsonAdapterFactory`), shared `OkHttpClient` |
| **Background Processing**| **WorkManager** + `SCHEDULE_EXACT_ALARM` with `goAsync()` BroadcastReceivers |
| **Widgets** | **AppWidgetProvider** with remote views and cold-start audio restoration |
| **Build Optimization** | **R8 / ProGuard** code shrinking and resource optimization |

### High-Level Architecture Flow:
```
[Presentation Layer]
   │  ├── Jetpack Compose Screens (HomeScreen, SurahReader, AzkarList, etc.)
   │  └── ViewModels (PrayerViewModel, QuranViewModel, AzkarViewModel, etc.)
   ▼
[Domain Layer]
   │  ├── Business Models & Calculations (OfflinePrayerCalculator, CalculationMethodMapper)
   │  └── Repository Interfaces & Domain Rules
   ▼
[Data Layer]
   │  ├── Local: Room SQLite (CompanionDatabase v30, CompanionDao, Entities)
   │  ├── Remote: Retrofit Services (PrayerApi, WeatherApi, QuranApi)
   │  ├── Audio & Sensors: ExoPlayer MediaSessionService & Qibla SensorManager
   │  └── Location: FusedLocationProviderClient + Geocoder Fallback
```

---

## 3. Project Folder Structure

```
muslim-companion/
├── APK/                                     # Built release and debug APK artifacts
├── assets/                                  # Graphic branding, banner, and design assets
├── gradle/                                  # Gradle wrapper and version catalog (libs.versions.toml)
├── screenshots/                             # Play Store and documentation screenshots
├── Plans/                                   # Architectural specs and feature implementation plans
│   ├── Multiple Azan Voices Implementation Plan
│   ├── Quran Navigation Enhancements Juz Hizb Rub and Sajdah Indicators
│   ├── implementation_plan_onboarding_location_custom_dhikr_chains.md
│   └── implementation_plan_Codebase Audit, Security Hardening & Clean Code Refactoring Plan
├── app/
│   ├── build.gradle.kts                     # App module build configuration (SDK 37, ProGuard, signing)
│   ├── proguard-rules.pro                   # R8/ProGuard obfuscation & keep rules
│   └── src/main/
│       ├── AndroidManifest.xml              # Manifest declaring services, receivers, widgets & permissions
│       ├── assets/                          # Bundled JSON files (surahs, prayer methods, azkar, names of Allah)
│       ├── res/                             # Android resources (drawables, layouts, values, values-ar, xml)
│       └── java/com/example/
│           ├── MainActivity.kt              # Single Activity host with Edge-to-Edge & NavHost
│           ├── MuslimCompanionApp.kt        # Hilt Application entry point
│           ├── audio/                       # Background audio service & player lifecycle
│           │   └── QuranAudioService.kt     # MediaSessionService handling gapless background recitation
│           ├── data/
│           │   ├── local/                   # Room DB (CompanionDatabase.kt, CompanionDao.kt, Entities.kt)
│           │   ├── quran/                   # Quran audio manager, offline downloader & parser
│           │   ├── remote/                  # Retrofit API clients (PrayerApi, QuranApi, WeatherApi, GeminiApi)
│           │   ├── repository/              # Repository implementations (Companion, Quran, Azkar, Weather)
│           │   └── worker/                  # WorkManager workers for prayer and sync
│           ├── di/                          # Dagger Hilt modules (AppModule.kt)
│           ├── domain/                      # Domain logic, prayer calculators & mappers
│           ├── navigation/                  # AppNavigation.kt, navigation routes & bottom nav bar
│           ├── notifications/               # Prayer & Azkar notification schedulers and receivers
│           ├── ui/
│           │   ├── Translation.kt           # Centralized bilingual string dictionary (Arabic/English)
│           │   ├── components/              # Reusable UI widgets, cards, banners, and dialogs
│           │   ├── screens/                 # Compose screens (18 dedicated feature screens)
│           │   │   ├── HomeScreen.kt        # Hero dashboard with prayer countdown, daily verse, quick links
│           │   │   ├── PrayerTimesScreen.kt # Prayer schedule, monthly calendar, and calculation settings
│           │   │   ├── QuranListScreen.kt   # Surah & Juz tabs, search, and reading progress
│           │   │   ├── SurahReaderScreen.kt # Full Uthmani reading with recitations, tafsir, and Sajdah
│           │   │   ├── AzkarListScreen.kt   # Hisn Al-Muslim categorized supplications
│           │   │   ├── AzkarReadingFlowScreen.kt # Interactive horizontal pager flow with vibration
│           │   │   ├── DigitalTasbihScreen.kt    # Tactile Tasbih counter and presets
│           │   │   ├── CustomDhikrChainsScreen.kt# User-created custom dhikr routines
│           │   │   ├── CreateCustomChainScreen.kt# Chain builder with catalog picker
│           │   │   ├── ReciteCustomChainScreen.kt# Guided chain recitation flow
│           │   │   ├── QiblaCompassScreen.kt# Sensor-driven Kaaba compass
│           │   │   ├── NamesOfAllahScreen.kt# 99 Names of Allah with calligraphy & meanings
│           │   │   ├── QuranicDuasScreen.kt # Authentic supplications from the Holy Quran
│           │   │   ├── NotificationsScreen.kt# Notifications history log
│           │   │   ├── QuranSettingsScreen.kt# Reciter selector and offline audio downloads
│           │   │   ├── SettingsScreen.kt    # Prayer calculation, Adhan voice selection, sound modes
│           │   │   ├── ProfileScreen.kt     # Reading streaks, stats, avatar, rating link
│           │   │   └── OnboardingScreen.kt  # Welcome flow with language toggle & GPS location
│           │   ├── theme/                   # Color palettes, Typography, Theme.kt
│           │   └── util/                    # UI helpers, font loaders, formatting
│           └── utils/                       # Common utilities and extensions
```

---

## 4. Key Feature Matrix

| Feature | Capabilities & Characteristics |
| :--- | :--- |
| **Holy Quran Reader** | Offline 6,236 Ayahs, Uthmani script, Surah & Juz tabs, Hizb/Rub banners, 15 Sujud al-Tilawah badges with Dua bottom sheet, End-of-Surah next button, bookmarking & Khatmah tracking. |
| **Gapless Quran Audio** | AndroidX `Media3` + `MediaSessionService` (`QuranAudioService`), background lock-screen controls, multi-reciter support (Alafasy, Abdul Basit, Al-Husary, Al-Minshawi), offline downloads. |
| **Prayer Times & Adhan** | 100% offline pure Kotlin astronomical solar calculations (14 calculation authorities), background alarms via `SCHEDULE_EXACT_ALARM`, 4 authentic Adhan voices with audio preview. |
| **Qibla Compass** | Sensor-fused real-time compass with pre-allocated sensor buffers for zero garbage collection and zero battery drain. |
| **Hisn Al-Muslim & Duas** | Comprehensive authentic Azkar (Morning, Evening, After-Prayer, Sleep, Wakeup, Distress, Protection against Envy/Evil Eye), horizontal pager flow with auto-advance and haptic feedback. |
| **Custom Dhikr Chains** | Custom routine builder allowing personalized phrases, target counts (e.g. 33/100), reordering, and full-screen guided Tasbih flow. |
| **Home Screen Widgets** | **Hero Prayer & Weather Hub** (countdown + 5-prayer timeline + weather) and **Quran Audio Player Widget** (with cold-start audio restoration). |
| **Bilingual & Scalable** | Instant dynamic Arabic (RTL) / English (LTR) language switching and real-time app-wide typography font scaling (Small/Medium/Large). |

---

## 5. Major Milestone & Update History

### v1.7.4 (Google Play Store Production Release)
- **Google Play Store Release Artifacts:**
  - Production Android App Bundle (`APK/Muslim-Companion-1.7.4.aab`) with Target SDK 37, signed using release keystore and optimized with R8.
  - Standalone production release APK (`APK/Muslim-Companion-1.7.4.apk`).
- **Comprehensive Quality Audit & Zero-Lint Milestone:**
  - Passed Android Lint with 0 errors (`lintDebug`, `lintVitalRelease`).
  - Extracted splash screen compatibility styling to `values-v31/themes.xml`.
  - Declared `VIBRATE` permission in manifest for custom Dhikr sequences.
  - Conformed Room SQLite migration supertype parameter signatures.
  - Verified 100% unit test suite passing (`testDebugUnitTest`).

### v1.7.3
- **Accurate Live Weather Synchronization & Overhaul:**
  - **Dynamic Location Awareness:** Integrated `LocationRepository` into `HomeViewModel` and `WeatherRepository`, replacing the hardcoded Makkah coordinates with the user's actual GPS or saved location.
  - **Open-Meteo Integration Upgrades:** Added `timezone=auto` and modern parameters (`current=temperature_2m,weather_code,is_day`) to `WeatherApi`.
  - **Accurate Temperature Rounding:** Upgraded temperature calculations with `Math.round()` to eliminate degree discrepancies with standard weather apps.
  - **Home Screen Widget Dynamic Weather:** Replaced static `"☀️ 26°C"` with dynamic cached temperature and condition icons in `PrayerWeatherWidgetProvider`.
  - **Prayer-Weather Synchronization:** Updating locations in `PrayerViewModel` now instantly synchronizes weather coordinates across the app.
- **Concise Reciter Typography:**
  - Shortened reciter display name to "Al-Minshawi" (and "المنشاوي" in Arabic) in `Translation.kt` to match "Al-Husary" and maintain clean item layout in the Quran Audio settings.
- **Multiple Authentic Adhan Voices:** Implemented 4 world-renowned Adhan recitations:
  - Sheikh Mishary Rashid Alafasy
  - Makkah Al-Mukarramah (Sheikh Ali Mulla)
  - Al-Madinah Al-Munawwarah (Al-Haram Al-Nabawi)
  - Egypt (Sheikh Abdul Basit Abdul Samad)
- **Interactive Adhan Audio Preview:** Audition and test Adhan audio files directly in the settings modal.
- **Room Database Migration v30:** Added `azanVoice` column to `app_settings` with backward compatibility.
- **Protection Duas Category:** Added authentic supplications for protection against harm, envy, and evil eye (`hasad_protection_dua.json`).
- **Dark Mode Legibility Enhancements:** High contrast emerald teal gradients, glowing mint active prayer card, and luminescent Quran download icons.
- **Profile Avatar Memory Optimization:** Added `BitmapFactory.Options.inSampleSize` calculation reducing avatar memory usage by >95% to satisfy Google Play pre-launch advisories.
- **Code Audit & Architecture Refactoring (Zero-Error Lint Pass):**
  - Resolved `MissingPermission` by declaring `android.permission.VIBRATE` for custom dhikr chains.
  - Refactored Android 12+ splash screen attributes into `res/values-v31/themes.xml` preventing API compatibility parse failures on API 26-30.
  - Removed redundant `minSdk >= 26` condition checks across receivers, schedulers, and ViewModels.
  - Conformed all Room `Migration.migrate(db)` parameter signatures, eliminating supertype call warnings.
  - Resolved `EmptySuperCall` in ViewModels and added missing widget TargetApi attributes.
  - Migrated hardcoded dependency coordinates (`icons-lucide`) to `libs.versions.toml`.

### v1.7.0 – v1.7.2
- **"Noor Divine" Home Screen Widget Overhaul:** Deep navy (`#1A2634`) & metallic gold (`#F2CA50`) aesthetic with live weather, next prayer countdown, and active timeline chips.
- **Cold-Start Audio Restoration in Widgets:** Widget Play button triggers `QuranAudioService` to automatically load the last read Surah when the audio player instance is inactive.
- **Quran Division Navigation:** Added Juz' (الأجزاء), Hizb (الأحزاب), and Rub' (أرباع) indicators with decorative banners and dynamic header updates.
- **Sujud al-Tilawah Modal:** Added gold badges (`۩ سجدة تلاوة`) across 15 Sajdah verses with authentic supplications and clipboard copy.
- **End-of-Surah Navigation:** Card at the end of each Surah linking directly to the next Surah, plus completion celebration card at Surah 114.
- **Top App Bar Search Bar:** Integrated expandable search action into the Quran top app bar.
- **Custom Dhikr Chains ("سلاسل الأذكار المخصصة"):** Room schema v29 adding `custom_dhikr_chains` table, ViewModel, builder UI, and recitation flow.

### v1.4.0 – v1.6.0
- **Background Audio Service:** Migrated Quran recitation to AndroidX `MediaSessionService` (`QuranAudioService`).
- **Prayer Notification ANR Fix:** Eliminated `runBlocking` on the main thread in `PrayerNotificationReceiver` using `goAsync()` with coroutine launch.
- **Notification Collisions:** Resolved request code and notification ID collisions for daily prayers.
- **Calculation Method Mapper Consolidation:** Unified data-layer and domain-layer calculation mappers into `CalculationMethodMapper`.

---

## 6. Remaining Goals & Roadmap

### 🎯 High Priority (Pre-Launch & Polish)
1. **Google Play Store Submission:**
   - Execute publishing steps outlined in [PLAY_STORE_PUBLISHING_GUIDE.md](file:///d:/AI/Projects%20With%20AI/Islamic%20App/Muslim%20Companion%20V1/muslim-companion/PLAY_STORE_PUBLISHING_GUIDE.md).
   - Ensure release bundle `app-release.aab` aligns with Google Play Target SDK 37 (Android 15) and 14-day closed testing prerequisites.
2. **Offline Audio Download Reliability:**
   - Implement resume capability and network re-connect retry policies for full-surah bulk downloads.
3. **Automated Notification Tests:**
   - Add automated instrumentation test suites verifying `SCHEDULE_EXACT_ALARM` triggers across Android 12, 13, 14, and 15 permission boundaries (`USE_EXACT_ALARM` vs `SCHEDULE_EXACT_ALARM`).

### 🌟 Future Enhancements (Post-Launch / v2.0)
1. **Interactive Quran Tafsir & Word-by-Word Translation:**
   - Introduce offline tafsir packs (Tafsir Ibn Kathir, Tafsir Al-Saadi, Tafsir Al-Muyassar).
   - Word-by-word Arabic root analysis and vocabulary highlights.
2. **Wear OS Companion App:**
   - Compact standalone Wear OS tile for next prayer countdown and Qibla directional arrow.
3. **Advanced Khatmah Planner:**
   - Customizable Quran completion schedules (e.g., 30-day Ramadan plan, weekly Juz targets) with gentle progress reminder notifications.
4. **Cloud Backup / Multi-Device Sync (Optional & Privacy-Preserving):**
   - End-to-end encrypted backup of user preferences, custom chains, and reading progress using private Google Drive AppData folder without tracking.
