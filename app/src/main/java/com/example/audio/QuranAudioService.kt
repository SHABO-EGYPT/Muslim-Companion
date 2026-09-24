package com.example.audio

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.data.local.CompanionDatabase
import com.example.data.quran.QuranAudioManager
import com.example.notifications.QuranPlayerWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranAudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.audio.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.audio.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.audio.ACTION_PREVIOUS"
    }

    override fun onCreate() {
        super.onCreate()
        
        val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
            .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(androidx.media3.common.C.USAGE_MEDIA)
            .build()

        val httpDataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(25_000)
            .setReadTimeoutMs(30_000)
            .setUserAgent("MuslimCompanion/1.7.4 (Android; ExoPlayer)")

        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this)
            .setDataSourceFactory(httpDataSourceFactory)

        val playerInstance = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setWakeMode(androidx.media3.common.C.WAKE_MODE_NETWORK)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        player = playerInstance

        // Create a PendingIntent to open the MainActivity when notification is clicked
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, playerInstance)
            .setSessionActivity(pendingIntent)
            .build()

        playerInstance.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updatePlaybackState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("QuranAudioService", "ExoPlayer error: ${error.errorCodeName} - ${error.message}", error)
            }
        })
    }

    private fun updatePlaybackState() {
        val p = player ?: return
        val currentMediaItem = p.currentMediaItem
        val metadata = currentMediaItem?.mediaMetadata

        QuranPlaybackState.isPlaying = p.isPlaying
        QuranPlaybackState.currentSurahName = metadata?.title?.toString() ?: "Quran Recitation"
        QuranPlaybackState.currentReciterName = metadata?.artist?.toString() ?: "Muslim Companion"
        QuranPlaybackState.currentAyahNumber = p.currentMediaItemIndex + 1

        QuranPlayerWidgetProvider.updateAllWidgets(this)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            val p = player ?: return@let
            when (action) {
                ACTION_PLAY_PAUSE -> {
                    if (p.isPlaying) {
                        p.pause()
                    } else {
                        if (p.mediaItemCount == 0) {
                            // Cold start from widget or external intent
                            startColdPlayback(p)
                        } else {
                            if (p.playbackState == Player.STATE_IDLE) {
                                p.prepare()
                            }
                            p.play()
                        }
                    }
                }
                ACTION_NEXT -> {
                    if (p.hasNextMediaItem()) {
                        p.seekToNextMediaItem()
                    }
                }
                ACTION_PREVIOUS -> {
                    if (p.hasPreviousMediaItem()) {
                        p.seekToPreviousMediaItem()
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startColdPlayback(p: ExoPlayer) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val db = CompanionDatabase.buildDatabase(applicationContext)
                val progress = db.companionDao().getUserProgressDirect()
                val settings = db.companionDao().getSettingsDirect()

                val surahNumber = progress?.lastReadSurahNumber?.takeIf { it in 1..114 } ?: 1
                val surahName = progress?.lastReadSurahName?.ifBlank { "Al-Fatiha" } ?: "Al-Fatiha"
                val reciterId = settings?.quranReciter?.ifBlank { "ar.alafasy" } ?: "ar.alafasy"
                val reciterName = settings?.reciter?.ifBlank { "Mishary Al-Afasy" } ?: "Mishary Al-Afasy"

                val audioManager = QuranAudioManager(applicationContext)
                val uri = audioManager.getPlaybackUri(reciterId, surahNumber)

                val metadata = MediaMetadata.Builder()
                    .setTitle(surahName)
                    .setArtist(reciterName)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaMetadata(metadata)
                    .build()

                withContext(Dispatchers.Main) {
                    p.setMediaItem(mediaItem)
                    p.prepare()
                    p.play()
                }
            } catch (e: Exception) {
                android.util.Log.e("QuranAudioService", "Failed cold start playback", e)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }
}
