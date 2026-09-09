# Multiple Azan Sounds Specification & Design

## Overview
Enable Muslim Companion users to choose their preferred Azan voice for prayer notifications. The options will feature 4 iconic recitations:
1. **Mishary Rashid Alafasy (مشاري راشد العفاسي)** — The existing beloved default Azan.
2. **Makkah (المسجد الحرام - علي أحمد ملا)** — Classic, internationally revered Haram recitation.
3. **Madinah (المسجد النبوي الشريف)** — Serene and distinctive Madinah Haram recitation.
4. **Egypt / Cairo (أذان مصر - الشيخ عبد الباسط عبد الصمد)** — Timeless Egyptian golden-era recitation.

In addition, users can preview (play/pause) each Azan voice directly within the settings dialog before making their choice.

---

## Architecture & Data Flow

### 1. Database & Persistence Layer (`CompanionDatabase` & `AppSettingEntity`)
* **Entity Update**:
  Add `azanVoice: String = "mishary"` to `AppSettingEntity`.
  Supported values:
  - `"mishary"` (default, maintains backwards compatibility with existing users)
  - `"makkah"`
  - `"madinah"`
  - `"abdulbasit"`
* **Room Migration (Version 29 → 30)**:
  ```kotlin
  val MIGRATION_29_30 = object : Migration(29, 30) {
      override fun migrate(database: SupportSQLiteDatabase) {
          database.execSQL("ALTER TABLE app_settings ADD COLUMN azanVoice TEXT NOT NULL DEFAULT 'mishary'")
      }
  }
  ```
  Increment `version = 30` on `CompanionDatabase` and register `MIGRATION_29_30`.

### 2. Audio Assets (`app/src/main/res/raw`)
* `first_adhan.mp3` (retained for Takbeerat-only "First Adhan" style)
* `full_adhan.mp3` (or alias/link for Mishary Alafasy)
* `adhan_makkah.mp3` (high-quality, speech-optimized MP3 ~1.2–1.8 MB)
* `adhan_madinah.mp3` (high-quality, speech-optimized MP3 ~1.2–1.8 MB)
* `adhan_abdulbasit.mp3` (high-quality, speech-optimized MP3 ~1.2–1.8 MB)
* Update `keep.xml` to protect all 5 audio resources during ProGuard / R8 resource shrinking:
  ```xml
  <resources xmlns:tools="http://schemas.android.com/tools"
      tools:keep="@raw/first_adhan,@raw/full_adhan,@raw/adhan_makkah,@raw/adhan_madinah,@raw/adhan_abdulbasit" />
  ```

### 3. ViewModel Layer (`SettingsViewModel`)
* Add helper in `SettingsViewModel`:
  ```kotlin
  fun updateAzanVoice(voice: String) {
      viewModelScope.launch {
          val s = settings.value ?: return@launch
          repository.saveSettings(s.copy(azanVoice = voice))
      }
  }
  ```

### 4. UI Layer (`SettingsScreen.kt`)
* **Structure (Approach A - Separated Settings)**:
  - **Notification Sound Type**:
    - Silent
    - Subtle (system default beep)
    - First Adhan (Takbeerat only)
    - Full Adhan
  - **Azan Voice Row** (displayed right below "Notification Sound Type" whenever `settings.notificationSoundType == "Full Adhan"`):
    - Title: "Azan Voice" / "صوت الأذان"
    - Subtitle / Value: Current selected voice (e.g., "Mishary Alafasy" / "مشاري العفاسي")
    - Clicking opens an `AzanVoiceSelectionDialog`.
  - **`AzanVoiceSelectionDialog`**:
    - Radio button list of the 4 voices.
    - Next to each voice name, an audio preview icon button (Play ▶ / Stop ⏹).
    - Uses an in-memory `MediaPlayer` managed by Compose `DisposableEffect` (stops and releases immediately upon dismissal or navigating away).

### 5. Notification Receiver (`PrayerNotificationReceiver.kt`)
* Determine raw resource from `settings.azanVoice`:
  ```kotlin
  val rawResId = when (settings.azanVoice) {
      "makkah" -> R.raw.adhan_makkah
      "madinah" -> R.raw.adhan_madinah
      "abdulbasit" -> R.raw.adhan_abdulbasit
      else -> R.raw.full_adhan
  }
  ```
* Construct dynamic Notification Channel ID for Android 8.0+:
  ```kotlin
  val channelId = if (soundType == "Full Adhan") {
      "prayer_channel_full_adhan_${settings.azanVoice}"
  } else {
      "prayer_channel_${soundType.lowercase().replace(" ", "_")}"
  }
  ```
* Set channel sound and `NotificationCompat.Builder.setSound(soundUri)` using `rawResId`.

### 6. Localization (`Translation.kt`)
Keys to add in both English and Arabic maps:
* `azan_voice`: "Azan Voice" / "صوت الأذان"
* `azan_mishary`: "Mishary Alafasy" / "مشاري العفاسي"
* `azan_makkah`: "Makkah (Ali Mulla)" / "الحرم المكي (علي ملا)"
* `azan_madinah`: "Madinah" / "الحرم النبوي"
* `azan_abdulbasit`: "Egypt (Abdul Basit)" / "أذان مصر (عبد الباسط عبد الصمد)"
* `preview_sound`: "Preview" / "استماع"

---

## Edge Cases & Failure Modes
1. **Audio Preview Conflict**: If user taps Play on Voice B while Voice A is playing, stop Voice A before starting Voice B.
2. **Screen Dismissal During Audio Preview**: `DisposableEffect` cleans up and calls `mediaPlayer.stop()` and `mediaPlayer.release()`.
3. **Old App Version Migration**: When upgrading from version 29, `MIGRATION_29_30` provides a safe non-null default `"mishary"`.
4. **Offline Capability**: All Azan sounds are packaged locally in `res/raw`, requiring 0 internet connectivity or streaming for prayer notifications to ring reliably.

---

## Testing & Verification Plan
1. **Room Migration Test**: Verify that the database builds and runs migration 29 -> 30 without schema mismatch.
2. **Settings UI Test**: Verify opening Azan Voice dialog, switching selections, and previewing playback.
3. **Notification Verification**: Trigger `PrayerNotificationReceiver` with various voice configurations to verify correct sound channels and resource URIs are passed to Android's `NotificationManager`.
