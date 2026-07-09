# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep data classes that might be serialized via Moshi
-keep class com.example.domain.model.** { *; }
-keep class com.example.data.remote.** { *; }

# Strip debug logs in release
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}
