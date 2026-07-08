# Muslim Companion (Android Application)

Muslim Companion is a feature-rich, high-performance, and beautifully designed Android companion app built using modern Android development best practices. It helps users manage their daily prayers, read the Holy Quran, count dhikr, and keep track of daily remembrance routines (Azkar).

---

## Key Features

### 📖 Quran Surah Reader
- **Dual Script Rendering:** Parallel rendering of Arabic text (Uthmani script) and English translations.
- **Customizable Layout:** Built with a clean, centered divider format, aligning Arabic text to the right and English translation to the left for natural reading flow.
- **Verse Indicators:** Every verse features an authentic **Rub El Hizb** icon header showing the verse number.
- **Translation Toggle:** A setting toggle in the reader preferences allows users to turn translations on/off to focus purely on the Arabic scripture.
- **Audio Streaming:** Seamless verse-by-verse audio streaming using Google's **Media3 (ExoPlayer)**, supporting multiple reciters (e.g., Mishary Al-Afasy, Abdul Basit, Al-Husary, Al-Minshawi).
- **Settings Customization:** Custom controls to scale Arabic text size, change fonts (Classic Serif, Modern Sans, Monospace Style), and keep screen awake during reading.

### 📿 Digital Tasbih Counter
- **Phrase Selector:** A horizontal chip selector to switch between popular remembrance phrases (e.g., *Subhan Allah*, *Alhamdulillah*, *Allahu Akbar*, *La ilaha illallah*) loaded dynamically from `tasbih.json`.
- **Target Tracking:** Set custom targets per phrase and track your counts with interactive tap-to-increment circle controls.
- **Tactile Feedback:** Built-in vibration haptic feedback on increments.

### 🌟 Daily Azkar Flow
- **Swipeable Cards:** Uses a `HorizontalPager` allowing users to swipe forward or backward to navigate through daily remembrance cards.
- **Interactive Tapping:** Tapping directly on the Azkar cards increments progress. The card automatically scrolls to the next remembrance once the target count (e.g., 3x or 33x) is completed.

### 🕋 Prayer Times & Qibla Compass
- **Accurate Schedules:** Displays local prayer times (Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha).
- **Calculation Settings:** Support for major Islamic calculation methods (Egyptian General Authority, MWL, ISNA, Karachi).
- **Qibla Alignment:** Real-time Qibla compass pointing directly to the Kaaba using device sensors.
- **Mosque Finder:** Integrates location services to find nearby places of worship.

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
