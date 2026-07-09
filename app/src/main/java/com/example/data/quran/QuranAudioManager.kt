package com.example.data.quran

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * Manages offline Quran audio: downloads per-sura MP3s from quranicaudio.com
 * and plays them via ExoPlayer.
 *
 * Audio strategy: **gapless, offline** — one MP3 per sura per reciter,
 * downloaded to internal storage. Audio files are served locally after download.
 *
 * URL pattern (gapless):
 *   https://download.quranicaudio.com/quran/{reciterPath}/{sura:03d}.mp3
 *
 * Local storage:
 *   {filesDir}/audio/{reciterPath}/{sura:03d}.mp3
 */
class QuranAudioManager(private val context: Context) {

    companion object {
        private const val TAG = "QuranAudio"

        /**
         * Maps the app's internal reciter IDs to quranicaudio.com path segments.
         * These match the Quran Android built-in reciters (all gapless).
         */
        val RECITER_PATHS: Map<String, String> = mapOf(
            "ar.alafasy"           to "mishaari_raashid_al_3afaasee",
            "ar.abdulbasitmurattal" to "abdul_baset/murattal",
            "ar.husary"            to "mahmood_khaleel_al-husaree",
            "ar.minshawimujawwad"  to "muhammad_siddeeq_al-minshaawee"
        )

        private const val BASE_CDN = "https://download.quranicaudio.com/quran"
    }

    // ── File resolution ──────────────────────────────────────────────────────

    /** Returns the local [File] for the given sura MP3 (may not exist yet). */
    fun localFile(reciterId: String, suraNumber: Int): File {
        val path = RECITER_PATHS[reciterId] ?: reciterId
        val dir = File(context.filesDir, "audio/$path")
        return File(dir, "%03d.mp3".format(suraNumber))
    }

    /** Returns true if the sura file is already downloaded locally. */
    fun isDownloaded(reciterId: String, suraNumber: Int): Boolean =
        localFile(reciterId, suraNumber).exists()

    // ── Remote URL ───────────────────────────────────────────────────────────

    /** Returns the CDN URL for the given sura. */
    fun remoteUrl(reciterId: String, suraNumber: Int): String {
        val path = RECITER_PATHS[reciterId] ?: reciterId
        return "$BASE_CDN/$path/%03d.mp3".format(suraNumber)
    }

    // ── Download ─────────────────────────────────────────────────────────────

    /**
     * Downloads the sura MP3 to local storage if not already present.
     * Must be called from a coroutine (uses [Dispatchers.IO] internally).
     * @return The local [File] on success, null on failure.
     */
    suspend fun downloadSura(reciterId: String, suraNumber: Int): File? =
        withContext(Dispatchers.IO) {
            val file = localFile(reciterId, suraNumber)
            if (file.exists()) return@withContext file

            file.parentFile?.mkdirs()
            val url = remoteUrl(reciterId, suraNumber)
            Log.d(TAG, "Downloading: $url → ${file.absolutePath}")
            try {
                val connection = URL(url).openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                connection.inputStream.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                connection.disconnect()
                Log.d(TAG, "Downloaded sura $suraNumber (${file.length() / 1024} KB)")
                file
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for sura $suraNumber", e)
                file.delete() // clean up partial file
                null
            }
        }

    // ── Playback URI ─────────────────────────────────────────────────────────

    /**
     * Returns a URI string for ExoPlayer to play.
     * Prefers local file; falls back to streaming URL if not downloaded.
     */
    fun getPlaybackUri(reciterId: String, suraNumber: Int): String {
        val file = localFile(reciterId, suraNumber)
        return if (file.exists()) file.toURI().toString()
        else remoteUrl(reciterId, suraNumber)
    }
}
