# Project ProGuard / R8 rules for Google Play release build

# 1. Domain & Remote Models (Moshi JSON serialization)
-keep class com.example.domain.model.** { *; }
-keep class com.example.data.remote.** { *; }
-keep class com.example.data.local.** { *; }

# 2. Room Database & DAOs
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# 3. Moshi & Retrofit
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# 4. Hilt & Dagger
-keep class * extends androidx.hilt.work.HiltWorker
-keep class com.example.di.** { *; }

# 5. Media3 & ExoPlayer (Background Quran Audio Playback)
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# 6. Lucide Icons & Compose UI
-keep class com.composables.icons.lucide.** { *; }

# 7. Strip debug logs in release
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

