# Security & Improvement Review — Muslim Companion V1

**Date:** July 9, 2026  
**App Type:** Single-user local Android app (Kotlin / Jetpack Compose)  
**Risk Level:** LOW (no critical vulnerabilities)

---

## Table of Contents
- [Overall Assessment](#overall-assessment)
- [Medium Severity Issues](#medium-severity-issues)
- [Low Severity Issues](#low-severity-issues)
- [Code Quality & Maintainability](#code-quality--maintainability)
- [What's Done Right](#whats-done-right)
- [Top 5 Recommended Actions](#top-5-recommended-actions)

---

## Overall Assessment

No critical or high-severity vulnerabilities were found. The app is well-architected with clean separation of concerns, proper use of Jetpack libraries, and no obvious security anti-patterns. However, several **medium-severity** issues (notably ANR risk, excessive logging, data backup exposure) should be addressed promptly, along with multiple low-severity and code quality improvements.

---

## Medium Severity Issues

### M1. `runBlocking` on Main Thread in BroadcastReceiver — ANR Risk

**Files:** `app/src/main/java/com/example/notifications/PrayerNotificationReceiver.kt:27-31`

```kotlin
val db = CompanionDatabase.getDatabase(context)
val settings = runBlocking {
    db.companionDao().getSettingsDirect()
} ?: AppSettingEntity()
```

`BroadcastReceiver.onReceive()` runs on the main thread. `runBlocking` blocks it until the Room database query completes. If the DB is locked or slow, this causes an **Application Not Responding (ANR)** crash after ~5 seconds.

**Fix:** Use `goAsync()` with a coroutine:

```kotlin
override fun onReceive(context: Context, intent: Intent) {
    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val db = CompanionDatabase.getDatabase(context)
            val settings = db.companionDao().getSettingsDirect() ?: AppSettingEntity()
            if (settings.prayerNotifications) {
                showNotification(context, intent, settings)
            }
        } finally {
            pendingResult.finish()
        }
    }
}
```

---

### M2. `e.printStackTrace()` in Production Code (8+ occurrences)

These leak internal app structure, file paths, and API response details. ProGuard does **not** strip them.

| File | Line |
|------|------|
| `AzkarViewModel.kt` | 151 |
| `AzkarRepository.kt` | 46, 60 |
| `PrayerSyncWorker.kt` | 31 |
| `QuranRepository.kt` | 209, 255 |
| `SurahReaderViewModel.kt` | 106 |
| `QuranSyncWorker.kt` | 47, 63 |

**Fix:** Replace with structured logging or remove entirely:

```kotlin
// Instead of: e.printStackTrace()
if (BuildConfig.DEBUG) Log.e("Tag", "Message", e)
```

---

### M3. Excessive Logging in Release Builds (~29 calls)

| Log Level | Count |
|-----------|-------|
| `Log.d()` | Many debug logs |
| `Log.e()` | Error logs with stack traces |
| `Log.i()` | Status/info messages |
| `Log.w()` | Warning messages |

The default `proguard-android-optimize.txt` does **not** strip `Log.d/v`. These logs persist in release builds.

**Fix:** Use Timber library, which auto-strips in release:
```kotlin
// Install in Application.onCreate()
if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
```

---

### M4. No Gradle Dependency Locking

**File:** `gradle/libs.versions.toml`

No lockfile exists. Transitive dependency versions can shift between builds, breaking reproducibility and potentially introducing vulnerabilities.

**Fix:** Enable dependency locking in `settings.gradle.kts`:
```kotlin
dependencyLocking {
    lockAllConfigurations()
}
```
Then run `./gradlew dependencies --write-locks` to generate the lockfile.

---

### M5. Empty Backup Rules — Full Cloud Backup of Local Database

**Files:**
- `AndroidManifest.xml:13` — `android:allowBackup="true"`
- `res/xml/backup_rules.xml` — commented out / empty
- `res/xml/data_extraction_rules.xml` — commented out / empty

On Android 12+, all app-internal storage (Room DB with prayer history, location, reading progress) is eligible for Google Drive backup.

**Fix Option 1:** Disable backup entirely:
```xml
android:allowBackup="false"
```

**Fix Option 2:** Add explicit exclude rules in `data_extraction_rules.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="database" path="companion-db" />
        <exclude domain="file" path="audio/" />
    </cloud-backup>
    <device-transfer>
        <include domain="database" path="companion-db" />
    </device-transfer>
</data-extraction-rules>
```

---

### M6. API Key as URL Query Parameter

**File:** `app/src/main/java/com/example/data/remote/GeminiApi.kt:62`

```kotlin
@Query("key") apiKey: String
```

The Gemini API requires the key as a query parameter. Query parameters are visible in:
- Server access logs
- Network monitoring tools
- Proxy logs

This is an upstream API constraint and cannot be avoided, but users should be aware their key is exposed in URLs.

---

## Low Severity Issues

### L1. No SSL/TLS Certificate Pinning

| API Client | File |
|------------|------|
| GeminiApiService | `GeminiApi.kt:69` |
| QuranApi | `QuranApi.kt:83` |
| PrayerApi | `PrayerApi.kt:46` (no custom client) |

All use the default Android trust store. No `CertificatePinner` is configured.

**Recommendation (defense-in-depth):**
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("generativelanguage.googleapis.com", "sha256/...")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

---

### L2. Audio Download Has No Timeout

**File:** `app/src/main/java/com/example/data/quran/QuranAudioManager.kt:81`

```kotlin
URL(url).openStream().use { input ->
```

Raw `URL.openStream()` uses the default timeout (infinite on some networks). All other network calls in the app (Retrofit/OkHttp) use 60s timeouts.

**Fix:**
```kotlin
val connection = URL(url).openConnection() as HttpURLConnection
connection.connectTimeout = 15_000
connection.readTimeout = 15_000
connection.getInputStream().use { input ->
    file.outputStream().use { output ->
        input.copyTo(output)
    }
}
```

---

### L3. Weak PendingIntent Request Codes (Hash Collision)

**Files:**
- `PrayerNotificationScheduler.kt:25` — `prayer.name.hashCode()`
- `PrayerNotificationReceiver.kt:95` — `prayer.name.hashCode()`

Using `String.hashCode()` for notification IDs may produce collisions between different prayer names.

**Fix:** Use a deterministic mapping:
```kotlin
enum class PrayerName(val requestCode: Int) {
    Fajr(100), Dhuhr(101), Asr(102), Maghrib(103), Isha(104)
}
```

---

### L4. No Tapjacking Protection

No usage of `filterTouchesWhenObscured="true"` or `FLAG_WINDOW_IS_OBSCURED`.

**Fix:** Add to sensitive UI surfaces (profile edit, onboarding):
```kotlin
// In Activity.onCreate()
window.setFlags(WindowManager.LayoutParams.FLAG_WINDOW_IS_OBSCURED)
```

---

### L5. No Network Security Config

No `res/xml/network_security_config.xml` is defined.

**Fix:** Create `res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```
And reference in `AndroidManifest.xml`:
```xml
android:networkSecurityConfig="@xml/network_security_config"
```

---

### L6. Input Validation: Username & Location Not Sanitized

**Files:**
- `ProfileViewModel.kt:58-63` — name and location stored directly
- `OnboardingScreen.kt:184-185` — passed to `completeOnboarding`

**Fix:**
```kotlin
fun updateProfile(name: String, location: String) {
    val safeName = name.trim().take(50)
    val safeLocation = location.trim().take(100)
    // ...
}
```

---

### L7. Firebase Dependencies Declared but No google-services.json

The version catalog declares Firebase BOM, Firebase AI, and Firebase AppCheck, but no `google-services.json` is present. The `google-services` Gradle plugin is not applied in `app/build.gradle.kts`.

**Fix:** Either remove unused Firebase dependencies or add the proper configuration files.

---

### L8. Empty ProGuard Rules File

**File:** `app/proguard-rules.pro`

Only contains a default template comment. While Moshi and Room use KSP codegen, add explicit keep rules as needed:
```
-keepattributes *Annotation*
-keep class com.example.** { *; }
```

---

### L9. CountdownManager Scope Never Cancelled

**File:** `app/src/main/java/com/example/viewmodel/PrayerCountdown.kt:21,27`

```kotlin
private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
// Uses SharingStarted.Eagerly
```

The timer runs indefinitely even when no UI is observing it. This wastes battery on a ticking coroutine.

**Fix:** Use `SharingStarted.WhileSubscribed()` or cancel the scope when no observers remain.

---

### L10. Notification ID Collisions

**File:** `app/src/main/java/com/example/notifications/PrayerNotificationReceiver.kt:95`

```kotlin
notificationManager.notify(prayerName.hashCode(), builder.build())
```

If two prayer names collide on `hashCode()`, the second notification overwrites the first. Use unique IDs:
```kotlin
notify(prayerName.hashCode() and 0x7FFFFFFF, builder.build())
// Or use a unique counter per prayer name
```

---

## Code Quality & Maintainability

### C1. Dead Code: `RealQuranRepository` (Legacy Implementation)

**File:** `app/src/main/java/com/example/data/repository/QuranRepository.kt:192-264`

The `RealQuranRepository` class is not wired in `AppModule.kt` (`OfflineQuranRepository` is used instead). 72 lines of dead code increase maintenance burden.

---

### C2. Unused Imports in Multiple Files

`AzkarViewModel.kt`, `ProfileViewModel.kt`, `QuranViewModel.kt`, and `PrayerViewModel.kt` all contain copy-pasted imports that are never used, e.g.:
- `Context`, `ExoPlayer`, `MediaItem`, `Player`, `InputStream`
- `JSONObject`, `JSONArray`
- `GeminiApiService`, `GenerateContentRequest`, `Content`, `Part`, `GenerationConfig`

---

### C3/C4. Workers Not Using Hilt Injection

**Files:**
- `PrayerSyncWorker.kt:12-15`
- `QuranSyncWorker.kt`

These workers manually construct `CompanionDatabase`, DAOs, and repositories instead of using `@HiltWorker`. This bypasses DI and creates duplicate singleton instances.

**Fix:**
```kotlin
@HiltWorker
class PrayerSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: CompanionDatabase
) : CoroutineWorker(appContext, workerParams) {
    // ...
}
```

---

### C5. Prayer Time Parsing Is Fragile

Multiple files parse time strings with `split(" ")[0]` then `split(":")`:
- `PrayerNotificationScheduler.kt:31-32`
- `PrayerCountdown.kt:60-61,93-94`

If the Aladhan API returns an unexpected format (e.g. `"04:12 (UTC)"`, `"4:12 am"`), parsing silently fails.

**Fix:** Use robust parsing with regex or `LocalTime.parse()`:
```kotlin
private fun parseTime(timeStr: String): LocalTime? {
    return try {
        val cleaned = timeStr.split(" ").first().trim()
        val parts = cleaned.split(":")
        LocalTime.of(parts[0].toInt(), parts[1].toInt())
    } catch (e: Exception) {
        null
    }
}
```

---

### C6. Destructive Migration Fallback

**File:** `app/src/main/java/com/example/data/local/CompanionDatabase.kt:84`

```kotlin
.fallbackToDestructiveMigration()
```

If a migration path is missing, all user data is silently destroyed. Consider `fallbackToDestructiveMigration(false)` (crashes instead of data loss) or ensure all migrations are covered.

---

### C7. Repeated Font Lookup on Recompose

**File:** `SurahReaderScreen.kt:317` (inside `LazyColumn` item)

```kotlin
fontFamily = getQuranFontFamily(quranSettings.quranFont),
```

This function is called on every recomposition of each ayah item. Cache with `remember`:
```kotlin
val quranFontFamily = remember(quranSettings.quranFont) {
    getQuranFontFamily(quranSettings.quranFont)
}
```

---

## What's Done Right

- **SQL Injection:** None — All Room queries use parameterized `:param` bindings
- **No WebView/XSS:** No `WebView`, `setJavaScriptEnabled()`, HTML properly stripped with `Html.fromHtml()`
- **All HTTPS:** Every external API uses HTTPS (Gemini, Quran.com, Aladhan, Quran audio CDNs)
- **No Hardcoded Secrets:** API key injected via Secrets Gradle Plugin into `BuildConfig`
- **Proper PendingIntent Flags:** `FLAG_IMMUTABLE` used (correct for API 23+)
- **BroadcastReceiver Not Exported:** `PrayerNotificationReceiver` is `exported="false"`
- **HTML Sanitization:** `Html.fromHtml()` strips HTML from quran.com translations before display
- **Type-Safe JSON:** Moshi with KSP codegen (no unsafe reflection-based deserialization)
- **Room Migrations:** Proper migration objects for version upgrades
- **Secure Random:** No use of `java.util.Random` for security-sensitive purposes
- **.env in .gitignore:** Confirmed at `.gitignore:12`
- **Clean Architecture:** Proper separation into domain/data/presentation layers with Hilt DI
- **Offline First:** Quran data bundled as asset, Room as local cache with network fallback
- **Permission Handling:** Proper runtime permission requests (location, notifications)

---

## Top 5 Recommended Actions

| Priority | ID | Issue | Impact |
|----------|----|-------|--------|
| **1** | M1 | `runBlocking` in BroadcastReceiver → ANR | **CRASH** — App stops responding |
| **2** | M2/M3 | Excessive logging & stack traces | **LEAK** — Internal structure exposed |
| **3** | M5 | Empty backup rules → cloud backup | **PRIVACY** — Personal data in cloud |
| **4** | M4 | No dependency locking | **BUILD** — Non-reproducible builds |
| **5** | L2 | Audio download has no timeout | **HANG** — Download can hang forever |
