# Implementation Plan: Onboarding Polish, Location Fix, Custom Dhikr Chains & Home Screen Widget Overhaul

**Author / Date:** Antigravity Pairing Assistant (2026-08-20 / Updated: 2026-08-21)  
**Target Project:** Muslim Companion Android App (Jetpack Compose + Hilt + Room + Media3 ExoPlayer)  
**Status:** In Progress (Phase 1 & Phase 2 Completed & Verified)  

---

## 1. Executive Summary & Goals

This plan incorporates all 5 core requirements for the Muslim Companion app:
1. **[COMPLETED] Onboarding Language Selection**: Allow first-time users to choose their language (English / العربية) right on the welcome screen when entering their name, with instant dynamic RTL/LTR layout and text translation.
2. **[COMPLETED] Onboarding Location Detection Fix & Resilience**: Fix the broken/stuck location detection on the onboarding screen using multi-tier GPS retrieval (`lastLocation` $\to$ `getCurrentLocation(HIGH_ACCURACY)` $\to$ background Geocoding + manual city selector fallback).
3. **[COMPLETED] Home Screen Player Widget Fix & Cold-Start Audio**: Auto-load last read Surah audio and start foreground playback immediately when user presses "Play" on the home screen widget without app pre-launch.
4. **[COMPLETED] Widget Identification & Localized Naming**: Distinct, localized titles and descriptions for all widgets in the Android Widget Picker in both Arabic & English ("Quran Audio Player" vs "Prayer Times & Weather Hub").
5. **[PENDING] Custom Dhikr Chains ("سلاسل الأذكار المخصصة")**: Enable users to build, save, and recite personalized chains/sequences of Dhikr (selecting phrases, setting target counts like 33/100, and reciting them in an auto-advancing Tasbih flow).

---

## 2. Detailed Technical Breakdown & Architecture

### Part 1: Onboarding Language Selection [COMPLETED & VERIFIED]
- **Segmented Language Switcher**: Added toggle for **العربية** / **English** on `OnboardingScreen.kt`.
- **Dynamic RTL/LTR Flipping**: Screen layout and strings instantly translate and flip layout direction in real time.
- **Preference Persistence**: Saved to `AppSettingEntity.language` in `ProfileViewModel.kt`.

---

### Part 2: Location Detection Fix on Onboarding Screen [COMPLETED & VERIFIED]
- **Multi-tier GPS Resolution**: `lastLocation` $\to$ `getCurrentLocation(HIGH_ACCURACY)` $\to$ background thread `Geocoder` with API 33+ `GeocodeListener` support.
- **Interactive UI Controls**: Added Retry button (`RotateCw`) and manual city edit dialog (`Pencil`) on `OnboardingScreen.kt`.
- **Verified on Emulator**: Successfully resolved location to **"Sadat City, Egypt"**.

---

### Part 3: Home Screen Player Widget Fix (Play Button & Audio Engine) [COMPLETED & VERIFIED]
- **Cold-Start Audio Restoration (`QuranAudioService.kt`)**:
  - Handled `ACTION_PLAY_PAUSE` when `player.mediaItemCount == 0`: queries `CompanionDatabase` for the user's `lastReadSurahNumber` and preferred `quranReciter`, builds `MediaItem`, prepares `ExoPlayer`, and begins immediate playback.
- **Service Scope & Lifecycle**:
  - Implemented `serviceScope` (`SupervisorJob() + Dispatchers.Main`) to manage asynchronous cold-start loading safely without service crashes or background ANRs.
- **Live State Synchronization (`QuranPlayerWidgetProvider.kt`)**:
  - Synchronizes `QuranPlaybackState` and triggers widget updates on Play/Pause, Next, and Previous.

---

### Part 4: Widget Identification & Clear Naming [COMPLETED & VERIFIED]
- **Localized Strings**:
  - In `res/values/strings.xml` (English):
    - `widget_quran_player_name`: *"Quran Audio Player"*
    - `widget_quran_player_desc`: *"Controls Quran audio recitation directly from your home screen."*
    - `widget_prayer_weather_name`: *"Prayer Times & Weather Hub"*
    - `widget_prayer_weather_desc`: *"Hero Dashboard Hub showing Next Prayer countdown, upcoming prayer timeline, and live weather status."*
  - In `res/values-ar/strings.xml` (Arabic):
    - `widget_quran_player_name`: *"مشغل القرآن الكريم"*
    - `widget_quran_player_desc`: *"التحكم في تلاوة القرآن الكريم مباشرة من الشاشة الرئيسية."*
    - `widget_prayer_weather_name`: *"مواقيت الصلاة والطقس"*
    - `widget_prayer_weather_desc`: *"عرض العد التنازلي للصلاة القادمة وجدول مواقيت اليوم والطقس المباشر."*
- **Manifest Labels**:
  - Added `android:label="@string/widget_quran_player_name"` to `QuranPlayerWidgetProvider`.
  - Added `android:label="@string/widget_prayer_weather_name"` to `PrayerWeatherWidgetProvider`.
- **Widget Metadata**:
  - Linked `@string/widget_quran_player_desc` in `quran_player_widget_info.xml`.
  - Linked `@string/widget_prayer_weather_desc` in `prayer_weather_widget_info.xml`.

---

### Part 5: Custom Dhikr Chains ("سلاسل الأذكار المخصصة") [UPCOMING]
- **Database & Schema**:
  - Add `CustomDhikrChainEntity` in `Entities.kt` (ID, name, description, itemsJson, totalTargetCount, timesCompleted, timestamps).
  - Room `Migration_28_29` in `CompanionDatabase.kt`.
  - DAO & Repository CRUD methods.
- **UI Components & Flow**:
  - `CustomChainsScreen.kt`: Saved routines list and management.
  - `CreateCustomChainScreen.kt`: Builder with Azkar catalog search and count steppers.
  - `CustomChainRecitationScreen.kt`: Guided full-screen Tasbih flow with auto-advance and haptic feedback.
  - Entry points in `AzkarListScreen.kt` and `DigitalTasbihScreen.kt`.

---

## 3. Step-by-Step Execution Checklist

### Phase 1: Onboarding Polish & Location (Completed)
- [x] Add distinct localized string resources for onboarding to `Translation.kt`.
- [x] Update `ProfileViewModel.kt` to support language updates and enhanced onboarding completion.
- [x] Add interactive segmented language switcher (Arabic / English) in `OnboardingScreen.kt` with live RTL/LTR layout direction changes.
- [x] Implement multi-tier location detection (`lastLocation` $\to$ `getCurrentLocation` $\to$ background geocoding).
- [x] Add location Refresh/Retry button and Manual City input dialog on `OnboardingScreen.kt`.
- [x] Run unit tests (`./gradlew testDebugUnitTest`) $\to$ PASS.
- [x] Deploy and verify on Android emulator (`Pixel_9(AVD)`) $\to$ PASS.

### Phase 2: Home Screen Widgets Overhaul & Audio Fix (Completed)
- [x] Add distinct localized string resources (`values/strings.xml` and `values-ar/strings.xml`) for all widgets.
- [x] Add `android:label` to widget receivers in `AndroidManifest.xml`.
- [x] Update `quran_player_widget_info.xml` and `prayer_weather_widget_info.xml` descriptions.
- [x] Update `QuranAudioService.kt` to handle cold-start playback (load last read Surah when player is empty).
- [x] Run unit tests (`./gradlew testDebugUnitTest`) $\to$ PASS.
- [x] Deploy and verify on Android emulator (`Pixel_9(AVD)`) $\to$ PASS.

### Phase 3: Custom Dhikr Chains Feature (Pending)
- [ ] Add `CustomDhikrChainEntity` in `Entities.kt`.
- [ ] Implement `Migration_28_29` in `CompanionDatabase.kt` and bump database version to `29`.
- [ ] Add DAO queries in `CompanionDao.kt` and repository functions in `CompanionRepository.kt`.
- [ ] Create `CustomDhikrViewModel.kt`.
- [ ] Create `CustomChainsScreen.kt`, `CreateCustomChainScreen.kt`, and `CustomChainRecitationScreen.kt`.
- [ ] Link entry points from `AzkarListScreen.kt` and `DigitalTasbihScreen.kt`.
- [ ] Register navigation routes in `AppNavigation.kt`.

### Phase 4: Final Verification & Tests (Pending)
- [ ] Run full unit test suite `./gradlew test`.
- [ ] Deploy to emulator and test custom chain builder + home screen widgets.

---

## 4. Summary of Files & Status

| Component | File Path | Status | Description |
|-----------|-----------|--------|-------------|
| **Localization** | `app/src/main/java/com/example/ui/Translation.kt` | ✅ COMPLETED | Added onboarding & location translation keys |
| **UI / Onboarding** | `app/src/main/java/com/example/viewmodel/ProfileViewModel.kt` | ✅ COMPLETED | Added `updateLanguage` and profile completion |
| **UI / Onboarding** | `app/src/main/java/com/example/ui/screens/OnboardingScreen.kt` | ✅ COMPLETED | Added language switcher + multi-tier GPS location + manual edit |
| **Widgets** | `app/src/main/AndroidManifest.xml` | ✅ COMPLETED | Added `android:label` to both widget receivers |
| **Widgets** | `app/src/main/res/values/strings.xml` | ✅ COMPLETED | Added distinct English widget titles & descriptions |
| **Widgets** | `app/src/main/res/values-ar/strings.xml` | ✅ COMPLETED | Added distinct Arabic widget titles & descriptions |
| **Widgets** | `app/src/main/res/xml/quran_player_widget_info.xml` | ✅ COMPLETED | Updated description string reference |
| **Widgets** | `app/src/main/res/xml/prayer_weather_widget_info.xml` | ✅ COMPLETED | Updated description string reference |
| **Audio / Widget** | `app/src/main/java/com/example/audio/QuranAudioService.kt` | ✅ COMPLETED | Implemented cold-start audio restoration |
| **Data / Room** | `app/src/main/java/com/example/data/local/Entities.kt` | ⏳ PENDING | Add `CustomDhikrChainEntity` |
| **Data / Room** | `app/src/main/java/com/example/data/local/CompanionDatabase.kt` | ⏳ PENDING | Bump version to 29, add `MIGRATION_28_29` |
| **Data / Room** | `app/src/main/java/com/example/data/local/CompanionDao.kt` | ⏳ PENDING | Add custom chain queries |
| **Data / Repo** | `app/src/main/java/com/example/data/repository/CompanionRepository.kt` | ⏳ PENDING | Expose custom chain CRUD methods |
| **UI / Custom Chains** | `app/src/main/java/com/example/viewmodel/CustomDhikrViewModel.kt` | ⏳ PENDING | ViewModel for chain creation & recitation |
| **UI / Custom Chains** | `app/src/main/java/com/example/ui/screens/CustomChainsScreen.kt` | ⏳ PENDING | Saved chains list & management |
| **UI / Custom Chains** | `app/src/main/java/com/example/ui/screens/CreateCustomChainScreen.kt` | ⏳ PENDING | Chain builder with Azkar picker |
| **UI / Custom Chains** | `app/src/main/java/com/example/ui/screens/CustomChainRecitationScreen.kt` | ⏳ PENDING | Guided recitation mode with auto-advance |
| **UI / Integration** | `app/src/main/java/com/example/ui/screens/AzkarListScreen.kt` | ⏳ PENDING | Add custom chains entry card |
| **UI / Integration** | `app/src/main/java/com/example/ui/screens/DigitalTasbihScreen.kt` | ⏳ PENDING | Add custom chains shortcut |
| **Navigation** | `app/src/main/java/com/example/navigation/AppNavigation.kt` | ⏳ PENDING | Add custom chains navigation routes |
