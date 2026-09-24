# Muslim Companion (رفيق المسلم) — Architectural Mind Map & System Topology
**Date:** September 24, 2026  
**Application:** Muslim Companion (`com.companion.muslim.app`)  
**Architecture:** Modern Android Development (MAD) Clean Architecture (Presentation, Domain, Data) with Unidirectional Data Flow (UDF) & Dagger-Hilt DI  

---

## 1. High-Level Architectural Mind Map

The following Mermaid mind map visualizes the structural organization and functional breakdown of the codebase:

```mermaid
mindmap
  root((Muslim Companion<br/>رفيق المسلم))
    Presentation Layer
      Navigation & Shell
        MainActivity.kt
        AppNavigation.kt
        NavigationItem.kt
        Translation.kt
        Theme and Components
      UI Screens 18 Screens
        HomeScreen Dashboard
        PrayerTimesScreen
        QuranListScreen
        SurahReaderScreen
        AzkarListScreen
        AzkarReadingFlowScreen
        DigitalTasbihScreen
        CustomDhikrChainsScreen
        CreateCustomChainScreen
        ReciteCustomChainScreen
        QiblaCompassScreen
        NamesOfAllahScreen
        QuranicDuasScreen
        NotificationsScreen
        ProfileScreen
        SettingsScreen
        OnboardingScreen
        LoadingScreen
      ViewModels StateFlow UDF
        HomeViewModel
        PrayerViewModel
        PrayerCountdown
        QuranViewModel
        SurahReaderViewModel
        AzkarViewModel
        CustomDhikrViewModel
        TasbihViewModel
        QiblaViewModel
        ProfileViewModel
        SettingsViewModel
        NotificationsViewModel
    Domain Layer
      Pure Business Logic
        OfflinePrayerCalculator
        StreakCalculator
        CalculationMethodMapper
      Domain Entities
        Prayer Times Models
        Quran Surah and Ayah
        Azkar Categories and Items
        Custom Dhikr Chain Models
        User Settings Models
    Data Layer
      Local Storage Room v30
        CompanionDatabase
        CompanionDao
        UserProgressEntity
        AppSettingEntity
        BookmarkEntity
        CachedAyahEntity
        CachedSurahEntity
        CachedPrayerTimeEntity
        QuranAyahEntity 6236 Verses
        NotificationEntity
        CustomDhikrChainEntity
      Repository Pattern
        CompanionRepository
        QuranRepository
        AzkarRepository
        CustomDhikrRepository
        WeatherRepository
        NamesOfAllahRepository
        QuranicDuasRepository
      Remote API Retrofit Moshi
        PrayerApi Aladhan
        QuranApi Quran.com
        WeatherApi OpenMeteo
        GeminiApi Google AI
      Pre-bundled Assets
        surahs.json
        quran_uthmani.json
        azkar categories json
        names_of_allah.json
        quranic_duas.json
    OS Services & Integration
      Foreground Audio Media3
        QuranAudioService
        ExoPlayer and MediaSession
        Lock Screen and System Media Style
      Alarms and Background
        PrayerNotificationScheduler
        PrayerNotificationReceiver
        AzkarNotificationScheduler
        AzkarNotificationReceiver
        BootReceiver
      App Widgets
        PrayerWeatherWidgetProvider
        QuranPlayerWidgetProvider
        Cold Start Playback Handler
      Hardware and Sensors
        SensorManager Accelerometer Magnetometer
        FusedLocationProvider GPS
```

---

## 2. File Dependency & Module Interaction Graph

This diagram illustrates concrete file dependencies and how classes interact across the Clean Architecture boundaries:

```mermaid
flowchart TB
    subgraph UI_Screens["Presentation: Compose Screens"]
        HomeScr["HomeScreen.kt"]
        ReaderScr["SurahReaderScreen.kt"]
        PrayerScr["PrayerTimesScreen.kt"]
        AzkarFlowScr["AzkarReadingFlowScreen.kt"]
        CustomChainScr["CustomDhikrChainsScreen.kt"]
        TasbihScr["DigitalTasbihScreen.kt"]
        QiblaScr["QiblaCompassScreen.kt"]
        SettingsScr["SettingsScreen.kt"]
    end

    subgraph UI_VM["Presentation: ViewModels & StateFlow"]
        HomeVM["HomeViewModel.kt"]
        ReaderVM["SurahReaderViewModel.kt"]
        PrayerVM["PrayerViewModel.kt"]
        Countdown["PrayerCountdown.kt"]
        AzkarVM["AzkarViewModel.kt"]
        CustomVM["CustomDhikrViewModel.kt"]
        TasbihVM["TasbihViewModel.kt"]
        QiblaVM["QiblaViewModel.kt"]
        SettingsVM["SettingsViewModel.kt"]
    end

    subgraph DI["Dependency Injection (Hilt)"]
        AppModule["AppModule.kt"]
    end

    subgraph Domain["Domain Layer"]
        Streak["StreakCalculator.kt"]
        MethodMap["CalculationMethodMapper.kt"]
        SolarCalc["OfflinePrayerCalculator.kt"]
    end

    subgraph Repos["Data: Repositories"]
        CompRepo["CompanionRepository.kt"]
        QuranRepo["QuranRepository.kt"]
        AzkarRepo["AzkarRepository.kt"]
        CustomRepo["CustomDhikrRepository.kt"]
        WeatherRepo["WeatherRepository.kt"]
    end

    subgraph Local_DB["Data: Room SQLite v30 & Assets"]
        RoomDB["CompanionDatabase.kt"]
        RoomDAO["CompanionDao.kt"]
        Entities["Entities.kt"]
        RawAudio["res/raw Audio (Adhan & Chimes)"]
        JSONAssets["assets/ (Quran, Azkar, Duas)"]
    end

    subgraph Remote_Net["Data: Remote Network"]
        RetrofitClient["Shared OkHttpClient & Moshi"]
        PrayerApi["PrayerApi.kt"]
        QuranApi["QuranApi.kt"]
        WeatherApi["WeatherApi.kt"]
    end

    subgraph OS_Integration["OS Services, Widgets & Receivers"]
        AudioSvc["QuranAudioService.kt (Media3)"]
        PrayerReceiver["PrayerNotificationReceiver.kt"]
        PrayerScheduler["PrayerNotificationScheduler.kt"]
        AzkarReceiver["AzkarNotificationReceiver.kt"]
        PrayerWidget["PrayerWeatherWidgetProvider.kt"]
        QuranWidget["QuranPlayerWidgetProvider.kt"]
    end

    %% UI to ViewModel bindings
    HomeScr --> HomeVM
    ReaderScr --> ReaderVM
    PrayerScr --> PrayerVM
    PrayerScr --> Countdown
    AzkarFlowScr --> AzkarVM
    CustomChainScr --> CustomVM
    TasbihScr --> TasbihVM
    QiblaScr --> QiblaVM
    SettingsScr --> SettingsVM

    %% ViewModels to Repositories & Domain
    HomeVM --> CompRepo
    HomeVM --> WeatherRepo
    PrayerVM --> CompRepo
    PrayerVM --> MethodMap
    PrayerVM --> Streak
    ReaderVM --> QuranRepo
    AzkarVM --> AzkarRepo
    CustomVM --> CustomRepo
    TasbihVM --> CompRepo
    SettingsVM --> CompRepo

    %% DI Injections
    AppModule -.-> CompRepo
    AppModule -.-> QuranRepo
    AppModule -.-> AzkarRepo
    AppModule -.-> CustomRepo
    AppModule -.-> RoomDB
    AppModule -.-> RetrofitClient

    %% Repository Layer Integrations
    CompRepo --> RoomDAO
    CompRepo --> PrayerApi
    CompRepo --> SolarCalc
    QuranRepo --> RoomDAO
    QuranRepo --> QuranApi
    QuranRepo --> JSONAssets
    AzkarRepo --> JSONAssets
    AzkarRepo --> RoomDAO
    CustomRepo --> RoomDAO
    WeatherRepo --> WeatherApi

    RoomDB --> RoomDAO
    RoomDAO --> Entities

    %% Audio & Widget interactions
    ReaderVM -.-> AudioSvc
    QuranWidget ==>|Direct Broadcast| AudioSvc
    AudioSvc --> RoomDAO
    PrayerScheduler --> PrayerReceiver
    PrayerReceiver --> RoomDB
    PrayerReceiver --> RawAudio
    AzkarReceiver --> RoomDB
    PrayerWidget --> RoomDAO
```

---

## 3. Active Runtime Data Flows

### A. Prayer Time Calculation & Exact Adhan Notification
```mermaid
sequenceDiagram
    autonumber
    participant UI as PrayerTimesScreen
    participant VM as PrayerViewModel
    participant Repo as CompanionRepository
    participant Calc as OfflinePrayerCalculator
    participant DB as CompanionDatabase (Room v30)
    participant Alarm as AlarmManager (SCHEDULE_EXACT_ALARM)
    participant Receiver as PrayerNotificationReceiver (goAsync)
    participant Audio as MediaPlayer (Adhan Audio)

    UI->>VM: loadPrayerTimes()
    VM->>Repo: getPrayerTimes(lat, lng, method)
    alt Offline Mode Active
        Repo->>Calc: computeSolarTimes(lat, lng, date, method)
        Calc-->>Repo: Calculated prayer times list
    else Online Cached
        Repo->>DB: getCachedPrayerTimes()
    end
    Repo-->>VM: StateFlow<PrayerTimesState>
    VM-->>UI: Display timetable, next prayer countdown & active prayer chip
    VM->>Alarm: scheduleDailyAlarms(prayers)
    
    Note over Alarm,Receiver: Alarm triggers at exact prayer moment
    Alarm->>Receiver: onReceive(PrayerIntent)
    Receiver->>DB: Query azanVoice & soundSettings (9s timeout protection)
    DB-->>Receiver: Sound settings (e.g. adhan_makkah / adhan_madinah)
    Receiver->>Audio: Play selected Adhan raw audio
    Receiver->>UI: Post high-priority notification with app icon & action
```

---

### B. Holy Quran Reader & Gapless Audio Playback
```mermaid
sequenceDiagram
    autonumber
    participant Reader as SurahReaderScreen
    participant VM as SurahReaderViewModel
    participant Svc as QuranAudioService (Media3)
    participant Repo as QuranRepository
    participant DB as Room SQLite (v30)
    participant Widget as QuranPlayerWidgetProvider

    Reader->>VM: loadSurah(surahNumber)
    VM->>Repo: getSurahVerses(surahNumber)
    Repo->>DB: Query quran_ayahs (6,236 pre-bundled verses)
    DB-->>Repo: List<QuranAyahEntity>
    Repo-->>VM: SurahContent (Ayahs, Sajdahs, Hizb/Rub division markers)
    VM-->>Reader: Render LazyColumn with Uthmani script & Sajdah badges

    Reader->>VM: playRecitation(surahNumber, reciter)
    VM->>Svc: sendIntent(ACTION_PLAY)
    alt Local File Present
        Svc->>Svc: Play from local storage
    else Stream Recitation
        Svc->>Svc: Stream from QuranicAudio CDN
    end
    Svc-->>Widget: Synchronize widget playback state
    Svc-->>Reader: Emit playback progress (Current Ayah highlighting)
```

---

### C. Custom Dhikr Chains Creation & Guided Recitation Flow
```mermaid
sequenceDiagram
    autonumber
    participant Builder as CreateCustomChainScreen
    participant Reciter as ReciteCustomChainScreen
    participant VM as CustomDhikrViewModel
    participant Repo as CustomDhikrRepository
    participant DB as CompanionDatabase (Room v30)

    Builder->>VM: saveCustomChain(title, selectedItems, targetCounts)
    VM->>Repo: insertChain(CustomDhikrChainEntity)
    Repo->>DB: INSERT INTO custom_dhikr_chains (JSON serialization)
    DB-->>Repo: Saved chain ID
    
    Reciter->>VM: startRecitation(chainId)
    VM->>Repo: getChainById(chainId)
    Repo->>DB: SELECT * FROM custom_dhikr_chains WHERE id = chainId
    DB-->>VM: CustomDhikrChainEntity
    VM-->>Reciter: Show active phrase, target count & circular progress
    
    loop User Taps Tasbih Button
        Reciter->>VM: incrementDhikr()
        VM->>VM: Tactile haptic click vibration
        alt Target reached for current phrase
            VM->>VM: Long haptic vibration & auto-advance to next phrase
        end
    end
    
    Reciter->>VM: onChainFinished()
    VM->>Repo: incrementTimesCompleted(chainId)
    Repo->>DB: UPDATE custom_dhikr_chains SET timesCompleted = timesCompleted + 1
```

---

## 4. Key Architectural Patterns Employed

1. **Unidirectional Data Flow (UDF):** Every UI screen observes an immutable `StateFlow` from its dedicated `ViewModel`, emitting user interactions as events.
2. **Offline-First Resilience:** 100% of core worship capabilities (full Quran Uthmani scripture, Hadith supplications, solar geometry calculations, and Asma Ul-Husna) function completely offline without internet access.
3. **Decoupled Background Services:** Background Quran recitation is handled via AndroidX `MediaSessionService` with lock-screen MediaStyle notifications; exact prayer alarms use `goAsync()` BroadcastReceivers with timeout safeguards.
4. **Resilient Home Screen Widgets:** Standalone widget providers with cold-start audio restoration and live prayer countdown chips.
