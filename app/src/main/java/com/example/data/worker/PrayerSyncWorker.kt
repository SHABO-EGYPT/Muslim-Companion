package com.example.data.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.CompanionDatabase
import com.example.data.repository.CompanionRepository
import com.example.data.repository.RealAzkarRepository
import com.example.data.repository.RealQuranRepository

class PrayerSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = CompanionDatabase.getDatabase(applicationContext)

            val dao = db.companionDao()
            val quranRepository = RealQuranRepository(dao)
            val azkarRepository = RealAzkarRepository(applicationContext)
            val repository = CompanionRepository(dao, quranRepository, azkarRepository)

            quranRepository.refreshSurahs()
            repository.refreshPrayerTimes()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
