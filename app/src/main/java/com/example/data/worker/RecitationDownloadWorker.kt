package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.quran.QuranAudioManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class RecitationDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val audioManager: QuranAudioManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val reciterId = inputData.getString("reciter_id") ?: return@withContext Result.failure()

        Log.i("RecitationDownload", "Starting full recitation download for: $reciterId")

        try {
            var downloadedCount = 0
            for (sura in 1..114) {
                if (isStopped) {
                    Log.i("RecitationDownload", "Download cancelled by user.")
                    return@withContext Result.success()
                }

                val localFile = audioManager.localFile(reciterId, sura)
                if (localFile.exists()) {
                    downloadedCount++
                } else {
                    val file = audioManager.downloadSura(reciterId, sura)
                    if (file != null && file.exists()) {
                        downloadedCount++
                    }
                }

                // Publish progress
                val progressPercent = (downloadedCount * 100) / 114
                setProgress(workDataOf(
                    "progress" to progressPercent,
                    "downloaded" to downloadedCount,
                    "reciter_id" to reciterId,
                    "status" to "downloading"
                ))
            }

            // Finished successfully!
            setProgress(workDataOf(
                "progress" to 100,
                "downloaded" to 114,
                "reciter_id" to reciterId,
                "status" to "completed"
            ))
            Result.success()
        } catch (e: Exception) {
            Log.e("RecitationDownload", "Failed to download recitation", e)
            setProgress(workDataOf(
                "status" to "failed",
                "error" to (e.message ?: "Unknown error")
            ))
            Result.failure()
        }
    }
}
