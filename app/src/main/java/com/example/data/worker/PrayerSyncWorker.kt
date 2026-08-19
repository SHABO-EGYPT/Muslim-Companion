package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.repository.CompanionRepository
import com.example.data.repository.QuranRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PrayerSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CompanionRepository,
    private val quranRepository: QuranRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "PrayerSyncWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            quranRepository.refreshSurahs()
            repository.refreshPrayerTimes()

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "PrayerSyncWorker execution failed, requesting retry", e)
            Result.retry()
        }
    }
}

