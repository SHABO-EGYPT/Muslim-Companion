# Muslim Companion (Android Application)

Muslim Companion is a feature-rich, high-performance, and beautifully designed Android companion app built using modern Android development best practices. It helps users manage their daily prayers, read the Holy Quran, count dhikr, and keep track of daily remembrance routines (Azkar).

---

## Key Features

### 📖 Quran Surah Reader
- **Offline-First Storage:** Integrated Room database with asset-based seeding to load the full Uthmani Arabic scripture text offline, caching English translations from Quran.com for seamless offline access.
- **Dual Script Rendering:** Parallel rendering of Arabic text (Uthmani script) and English translations.
- **Customizable Layout:** Centered divider format, aligning Arabic text to the right and English translation to the left for natural reading flow.
- **Verse Indicators:** Every verse features an authentic **Rub El Hizb** icon header showing the verse number.
- **Translation Toggle:** Quickly show/hide translations to focus purely on the Arabic scripture.
- **Offline & Streaming Audio:** Gapless verse-by-verse audio playback using AndroidX **Media3 (ExoPlayer)**, supporting multiple reciters and offline downloaded audio paths.
- **Customization Settings:** Custom preferences to scale Arabic text size, change fonts, and keep screen awake.

### 📿 Digital Tasbih Counter
- **Phrase Selector:** Chip selectors to switch between popular remembrance phrases (e.g., *Subhan Allah*, *Alhamdulillah*, *Allahu Akbar*) loaded dynamically.
- **Target Tracking:** Set custom targets per phrase and track counts with interactive tap-to-increment circle controls.
- **Tactile Feedback:** Built-in vibration haptic feedback on increments.

### 🌟 Daily Azkar
- **Material 3 Outlined Rows:** Swapped the horizontal scrolling list for 3 vertical Outlined Cards on the Home screen representing Morning, Evening, and after-prayer categories.
- **Progress Trackers:** Features inline category-themed `LinearProgressIndicator`s to dynamically track dhikr progress.
- **Completion Badges:** Displays solid checkmark icons once categories reach 100% completion.
- **Horizontal Pager Flow:** In the Azkar reading screen, uses a `HorizontalPager` allowing users to swipe forward or backward to navigate through daily remembrance cards.
- **Auto-Advance:** Card automatically scrolls to the next remembrance once the target count is completed.

### 🕋 Prayer Times, Qibla Compass & Notifications
- **Accurate Schedules:** Displays local prayer times (Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha).
- **Azan Notification Scheduler:** Background alarms via `AlarmManager` triggering authentic Adhan playback (`first_adhan.mp3` or `full_adhan.mp3`) at exact prayer times, respecting notification states.
- **Calculation Settings:** Support for major calculation methods (Egyptian General Authority, MWL, ISNA, Karachi).
- **Qibla Alignment:** Real-time Qibla compass pointing directly to the Kaaba using device sensors.
- **Mosque Finder:** Map-linked widget querying nearby mosques using location coordinates and Google Maps (with browser fallback).

---

## Screenshots

Here is a visual showcase of the main menus and features of the **Muslim Companion** app:

| 🏠 Home Screen | 📖 Quran Surah List | 📖 Quran Reader |
|:---:|:---:|:---:|
| ![Home](screenshots/home.png) | ![Quran](screenshots/quran.png) | ![Quran Reader](screenshots/surah_reader.png) |

| 📿 Daily Azkar | 📿 Azkar Flow | 🕋 Prayer Times |
|:---:|:---:|:---:|
| ![Azkar](screenshots/azkar.png) | ![Azkar Flow](screenshots/azkar_flow.png) | ![Prayer Times](screenshots/prayer.png) |

| 🕋 Qibla Compass | 📿 Digital Tasbih | ⚙️ Profile / Settings |
|:---:|:---:|:---:|
| ![Qibla](screenshots/qibla.png) | ![Tasbih](screenshots/tasbih.png) | ![Settings](screenshots/profile.png) |

---

## Technical Stack & Architecture

- **UI Framework:** 100% **Jetpack Compose** with Material Design 3 guidelines for a clean, premium, and responsive user experience.
- **Language:** **Kotlin** utilizing Coroutines and Kotlin Flow for reactive, thread-safe asynchronous operations.
- **Architecture:** Clean Architecture pattern splitting the codebase into:
  - **Data Layer:** Room database caches, Retrofit API endpoints, local asset parsers, and repository implementations.
  - **Domain Layer:** Unified data models, repository abstractions, and core business entities.
  - **Presentation Layer:** State-driven Compose screens, Dialogs, and state-holding view models.
- **Dependency Injection:** Powered by **Hilt** (Dagger) for scalable dependency scoping and testing.
- **Local Caching (Room):** Integrates SQLite database caching (version 22) supporting schema migrations and destructive fallback protection to preserve offline usability.
- **Network Client:** **Retrofit + Moshi** integrating directly with the official Quran.com API endpoints.
- **Audio Player:** **AndroidX Media3 (ExoPlayer)** for low-latency network audio streaming.

---

## Installation & Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/SHABO-EGYPT/Muslim-Companion.git
   ```
2. **Open in Android Studio:**
   - Open Android Studio, select **Open**, and navigate to the cloned project folder.
   - Allow Gradle to sync and download required dependencies.
3. **API Keys:**
   - Create a file named `.env` in the root folder and set your `GEMINI_API_KEY` (if utilizing AI assistant components).
4. **Deploy:**
   - Build and run the app directly on an emulator or connected physical Android device.
   - Ready-to-install debug APKs are also located inside the project's [APK](file:///D:/AI/Projects%20With%20AI/Islamic%20App/Muslim%20Companion%20V1/muslim-companion/APK/) folder.
