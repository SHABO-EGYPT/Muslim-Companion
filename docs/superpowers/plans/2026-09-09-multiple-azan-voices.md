# Multiple Azan Voices Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable users to select and preview their favorite Azan recitation (Mishary Alafasy, Makkah Ali Mulla, Madinah Haram, or Egypt Abdul Basit) for prayer notifications.

**Architecture:** Extend `AppSettingEntity` with an `azanVoice` property backed by Room Migration 29 → 30. Map chosen voices to bundled raw MP3 assets in `PrayerNotificationReceiver` with dynamic Android notification channels, and provide a settings selection dialog with an in-app audio preview in Jetpack Compose.

**Tech Stack:** Kotlin, Jetpack Compose, Android NotificationChannel / NotificationManager, Android MediaPlayer, Room DB, Hilt.

## Global Constraints
- Android API level support: minSdk 26, targetSdk 34+.
- Database integrity: Room schema migration from 29 to 30 must preserve all existing user settings without data loss.
- Audio efficiency: Keep audio assets local in `res/raw` protected by `keep.xml`.
- Media playback safety: `MediaPlayer` instances created for previewing audio in Compose must be cleanly stopped and released upon dialog dismiss or screen disposal to avoid memory or audio focus leaks.

---

### Task 1: Download & Bundle Audio Assets

**Files:**
- Create: `app/src/main/res/raw/adhan_makkah.mp3`
- Create: `app/src/main/res/raw/adhan_madinah.mp3`
- Create: `app/src/main/res/raw/adhan_abdulbasit.mp3`
- Modify: `app/src/main/res/raw/keep.xml`

**Interfaces:**
- Produces: Raw Android resources `R.raw.adhan_makkah`, `R.raw.adhan_madinah`, `R.raw.adhan_abdulbasit`, and keeps `R.raw.full_adhan` and `R.raw.first_adhan`.

- [ ] **Step 1: Download the 3 high-quality Azan recordings**
Download:
1. Makkah (Ali Mulla): `Ali_Ibn_Ahmad_Mala_1_-_Al_Haram_Al_Maki_(علي_بن_أحمد_ملا_-_الحرم_المكي).mp3`
2. Madinah (Haram Madani): `Adhan_Al_Haram_Al_Madani_-_Al_Madinah_1_(أذان_الحرم_المدني_-_المدينة_المنورة).mp3`
3. Egypt (Abdul Basit): `Abdulbasit_Abdusamad_1_-_Egypt_(عبد_الباسط_عبد_الصمد_-_مصر).mp3`
Save into `app/src/main/res/raw/` with sanitized, lowercase resource names:
- `app/src/main/res/raw/adhan_makkah.mp3`
- `app/src/main/res/raw/adhan_madinah.mp3`
- `app/src/main/res/raw/adhan_abdulbasit.mp3`

- [ ] **Step 2: Update `keep.xml`**
Update `app/src/main/res/raw/keep.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:keep="@raw/first_adhan,@raw/full_adhan,@raw/adhan_makkah,@raw/adhan_madinah,@raw/adhan_abdulbasit" />
```

- [ ] **Step 3: Verify audio files presence and validity**
Check file sizes and verify they are valid MP3s using python or ffprobe.

- [ ] **Step 4: Commit audio assets**
```bash
git add app/src/main/res/raw/
git commit -m "feat(audio): add Makkah, Madinah, and Abdul Basit Azan audio assets"
```

---

### Task 2: Database Schema & Migration (Version 29 → 30)

**Files:**
- Modify: `app/src/main/java/com/example/data/local/Entities.kt:90-110`
- Modify: `app/src/main/java/com/example/data/local/CompanionDatabase.kt:20-30, 115-130`

**Interfaces:**
- Consumes: `AppSettingEntity`
- Produces: `AppSettingEntity.azanVoice: String = "mishary"`, `MIGRATION_29_30`

- [ ] **Step 1: Update `AppSettingEntity`**
In `app/src/main/java/com/example/data/local/Entities.kt`:
Add `azanVoice: String = "mishary"` to `AppSettingEntity`:
```kotlin
    val notificationSoundType: String = "Full Adhan",
    val azanVoice: String = "mishary",
```

- [ ] **Step 2: Add `MIGRATION_29_30` and bump database version in `CompanionDatabase.kt`**
Update `version = 30`:
```kotlin
        private val MIGRATION_29_30 = object : androidx.room.migration.Migration(29, 30) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE app_settings ADD COLUMN azanVoice TEXT NOT NULL DEFAULT 'mishary'")
            }
        }
```
Add `MIGRATION_29_30` to `addMigrations(...)` in `buildDatabase`.

- [ ] **Step 3: Run unit tests to verify Room compilation**
Run: `./gradlew testDebugUnitTest`
Expected: PASS

- [ ] **Step 4: Commit database migration**
```bash
git add app/src/main/java/com/example/data/local/Entities.kt app/src/main/java/com/example/data/local/CompanionDatabase.kt
git commit -m "feat(db): add azanVoice column to app_settings with migration 29->30"
```

---

### Task 3: Localization & SettingsViewModel

**Files:**
- Modify: `app/src/main/java/com/example/ui/Translation.kt`
- Modify: `app/src/main/java/com/example/viewmodel/SettingsViewModel.kt`
- Modify: `app/src/test/java/com/example/TranslatorTest.kt`

**Interfaces:**
- Consumes: `AppSettingEntity.azanVoice`
- Produces: `SettingsViewModel.updateAzanVoice(voice: String)`, new translation keys in `Translator`

- [ ] **Step 1: Write test for new translation keys**
In `app/src/test/java/com/example/TranslatorTest.kt`, add assertions verifying `azan_voice`, `azan_mishary`, `azan_makkah`, `azan_madinah`, `azan_abdulbasit`, `preview_sound` resolve in both English and Arabic.

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew testDebugUnitTest --tests com.example.TranslatorTest`
Expected: FAIL due to missing keys.

- [ ] **Step 3: Add translations and `updateAzanVoice`**
In `app/src/main/java/com/example/ui/Translation.kt`:
English:
```kotlin
"azan_voice" to "Azan Voice",
"azan_mishary" to "Mishary Alafasy",
"azan_makkah" to "Makkah (Ali Mulla)",
"azan_madinah" to "Madinah",
"azan_abdulbasit" to "Egypt (Abdul Basit)",
"preview_sound" to "Preview Sound",
```
Arabic:
```kotlin
"azan_voice" to "صوت الأذان",
"azan_mishary" to "مشاري العفاسي",
"azan_makkah" to "الحرم المكي (علي ملا)",
"azan_madinah" to "الحرم النبوي",
"azan_abdulbasit" to "أذان مصر (عبد الباسط عبد الصمد)",
"preview_sound" to "استماع للأذان",
```
In `app/src/main/java/com/example/viewmodel/SettingsViewModel.kt`:
```kotlin
fun updateAzanVoice(voice: String) {
    viewModelScope.launch {
        val s = settings.value ?: return@launch
        repository.saveSettings(s.copy(azanVoice = voice))
    }
}
```

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew testDebugUnitTest --tests com.example.TranslatorTest`
Expected: PASS

- [ ] **Step 5: Commit changes**
```bash
git add app/src/main/java/com/example/ui/Translation.kt app/src/main/java/com/example/viewmodel/SettingsViewModel.kt app/src/test/java/com/example/TranslatorTest.kt
git commit -m "feat(settings): add azan voice translations and SettingsViewModel.updateAzanVoice"
```

---

### Task 4: Notification Receiver Azan Audio Routing

**Files:**
- Modify: `app/src/main/java/com/example/notifications/PrayerNotificationReceiver.kt:40-115`

**Interfaces:**
- Consumes: `settings.notificationSoundType`, `settings.azanVoice`
- Produces: Correct channel ID and audio resource URI for incoming notifications.

- [ ] **Step 1: Update channel creation and notification sound routing**
In `app/src/main/java/com/example/notifications/PrayerNotificationReceiver.kt`:
1. Helper for getting Azan sound URI:
```kotlin
val soundResId = when (settings.azanVoice) {
    "makkah" -> com.example.R.raw.adhan_makkah
    "madinah" -> com.example.R.raw.adhan_madinah
    "abdulbasit" -> com.example.R.raw.adhan_abdulbasit
    else -> com.example.R.raw.full_adhan
}
val soundUri = Uri.parse("android.resource://${context.packageName}/$soundResId")
```
2. Channel ID:
```kotlin
val channelId = if (soundType == "Full Adhan") {
    "prayer_channel_full_adhan_${settings.azanVoice}"
} else {
    "prayer_channel_${soundType.lowercase().replace(" ", "_")}"
}
```
3. Set sound on `channel` and `builder` using the resolved `soundUri`.

- [ ] **Step 2: Run unit tests**
Run: `./gradlew testDebugUnitTest`
Expected: PASS

- [ ] **Step 3: Commit notification receiver updates**
```bash
git add app/src/main/java/com/example/notifications/PrayerNotificationReceiver.kt
git commit -m "feat(notifications): support dynamic azan voice channels and sound URIs"
```

---

### Task 5: Settings UI Azan Voice Selection & Audio Preview

**Files:**
- Modify: `app/src/main/java/com/example/ui/screens/SettingsScreen.kt`

**Interfaces:**
- Consumes: `settings.notificationSoundType`, `settings.azanVoice`, `viewModel.updateAzanVoice`
- Produces: `AzanVoiceSelectionDialog` with play/stop audio preview and voice selection.

- [ ] **Step 1: Implement `AzanVoiceSelectionDialog` and Azan Voice Settings Row**
In `SettingsScreen.kt`:
1. Add state: `var showAzanVoiceDialog by remember { mutableStateOf(false) }`
2. Add "Azan Voice" row below "Notification Sound Type" when `settings.prayerNotifications && settings.notificationSoundType == "Full Adhan"`:
   - Displays label `Translator.translate("azan_voice", settings.language)`
   - Displays current voice name in primary color
   - Clicking opens `showAzanVoiceDialog = true`
3. In `AzanVoiceSelectionDialog`:
   - List the 4 voices:
     - `"mishary"` -> `azan_mishary`, `R.raw.full_adhan`
     - `"makkah"` -> `azan_makkah`, `R.raw.adhan_makkah`
     - `"madinah"` -> `azan_madinah`, `R.raw.adhan_madinah`
     - `"abdulbasit"` -> `azan_abdulbasit`, `R.raw.adhan_abdulbasit`
   - Maintain `var currentlyPlayingVoice by remember { mutableStateOf<String?>(null) }`
   - Play/Stop preview button with `Icons.Default.PlayArrow` / `Icons.Default.Stop`
   - Manage `MediaPlayer` with `DisposableEffect` so that on dismiss or change, playback stops and media player is released.
   - Selecting a voice calls `viewModel.updateAzanVoice(voice)` and stops preview.

- [ ] **Step 2: Run unit tests and compilation check**
Run: `./gradlew testDebugUnitTest`
Expected: PASS

- [ ] **Step 3: Commit UI changes**
```bash
git add app/src/main/java/com/example/ui/screens/SettingsScreen.kt
git commit -m "feat(ui): add Azan Voice selector dialog with audio preview in Settings"
```

---

### Task 6: End-to-End Verification & Build Validation

**Files:**
- Test files and build scripts

- [ ] **Step 1: Run complete test suite**
Run: `./gradlew testDebugUnitTest`
Expected: All unit tests pass.

- [ ] **Step 2: Build debug APK**
Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Verify APK contains the new raw audio resources**
Inspect generated APK with `aapt` or zip check to verify `res/raw/adhan_makkah.mp3`, `adhan_madinah.mp3`, `adhan_abdulbasit.mp3` are bundled properly.
