package com.example.data.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.CompanionDatabase
import com.example.data.repository.RealQuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class QuranSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = CompanionDatabase.getDatabase(applicationContext)

            val dao = db.companionDao()
            val quranRepository = RealQuranRepository(dao)

            val progress = dao.getUserProgressDirect() ?: return Result.success()
            val settings = dao.getSettingsDirect() ?: return Result.success()

            for (i in 1..3) {
                val nextSurah = progress.lastReadSurahNumber + i
                if (nextSurah <= 114) {
                    val ayahs = quranRepository.getSurahWithAudio(nextSurah, settings.quranReciter)
                    val updatedAyahs = ayahs.mapNotNull { ayah ->
                        if (ayah.audioUrl.startsWith("http")) {
                            val cachedFilePath = cacheAudioFile(applicationContext, ayah.audioUrl, "${ayah.id}.mp3")
                            cachedFilePath?.let { ayah.copy(audioUrl = it) }
                        } else null
                    }
                    if (updatedAyahs.isNotEmpty()) {
                        dao.insertCachedAyahs(updatedAyahs)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun cacheAudioFile(context: Context, urlString: String, fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "quran_audio").also { it.mkdirs() }
            val file = File(cacheDir, fileName)
            if (!file.exists()) {
                URL(urlString).openStream().use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
            file.toURI().toString()
        } catch (e: Exception) {
            e.printStackTrace(); null
        }
    }
}
