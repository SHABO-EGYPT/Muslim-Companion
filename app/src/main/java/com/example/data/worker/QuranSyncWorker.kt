package com.example.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.CompanionDatabase
import com.example.data.repository.QuranRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@HiltWorker
class QuranSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: CompanionDatabase,
    private val quranRepository: QuranRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val dao = database.companionDao()

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
                val connection = URL(urlString).openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                connection.inputStream.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                connection.disconnect()
            }
            file.toURI().toString()
        } catch (e: Exception) {
            e.printStackTrace(); null
        }
    }
}
