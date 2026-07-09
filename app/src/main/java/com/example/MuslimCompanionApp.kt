package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.CompanionDatabase
import com.example.data.quran.QuranAssetLoader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MuslimCompanionApp : Application() {

    @Inject
    lateinit var quranAssetLoader: QuranAssetLoader

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        seedQuranDatabaseIfNeeded()
    }

    /**
     * On first install (or DB wipe), reads quran_uthmani.json from assets
     * and populates the quran_ayahs Room table (6236 rows).
     * This runs once in the background; all subsequent launches skip it instantly.
     */
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
