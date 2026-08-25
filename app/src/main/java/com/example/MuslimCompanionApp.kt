package com.example

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.data.quran.QuranAssetLoader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MuslimCompanionApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var quranAssetLoader: QuranAssetLoader

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        seedQuranDatabaseIfNeeded()
    }

    private fun createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager ?: return
            
            // Azkar Channel
            val azkarChannel = android.app.NotificationChannel(
                "azkar_notification_channel",
                "Azkar Reminders (أذكار)",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for morning, evening, and daily Azkar reminders"
            }
            notificationManager.createNotificationChannel(azkarChannel)
        }
    }

    private fun seedQuranDatabaseIfNeeded() {
        appScope.launch {
            try {
                if (!quranAssetLoader.isSeeded()) {
                    Log.i("MuslimCompanion", "First launch: seeding Quran text from assets…")
                    quranAssetLoader.seedDatabase()
                    Log.i("MuslimCompanion", "Quran text seeded successfully.")
                }
            } catch (e: Exception) {
                Log.e("MuslimCompanion", "Failed to seed Quran text", e)
            }
        }
    }
}
