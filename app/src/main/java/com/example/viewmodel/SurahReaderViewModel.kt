package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.CompanionRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.content.ComponentName
import com.example.audio.QuranAudioService
import com.example.data.quran.QuranAudioManager
import kotlinx.coroutines.launch

sealed class ReaderLoadState {
    object Idle : ReaderLoadState()
    object Loading : ReaderLoadState()
    object Success : ReaderLoadState()
    data class Error(val message: String) : ReaderLoadState()
}

// 3. Surah Reader ViewModel
@HiltViewModel
class SurahReaderViewModel @Inject constructor(
    private val repository: CompanionRepository,
    private val quranRepository: com.example.data.repository.QuranRepository,
    private val audioManager: QuranAudioManager,
    @ApplicationContext context: Context
) : ViewModel() {
    private var player: Player? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, QuranAudioService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                player = controller
                controller.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        android.util.Log.d("QuranAudio", "Playback state changed: $state")
                        if (state == Player.STATE_ENDED) {
                            playNextSurah()
                        }
                    }
                    override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                        android.util.Log.d("QuranAudio", "Is playing changed: $isPlayingNow")
                        _isPlaying.value = isPlayingNow
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val index = controller.currentMediaItemIndex
                        val list = _ayahs.value
                        if (index >= 0 && index < list.size) {
                            _currentPlayingAyah.value = list[index].number
                        }
                    }
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        android.util.Log.e("QuranAudio", "MediaController Error!", error)
                    }
                })
                _isPlaying.value = controller.isPlaying
                val index = controller.currentMediaItemIndex
                val list = _ayahs.value
                if (controller.isPlaying && index >= 0 && index < list.size) {
                    _currentPlayingAyah.value = list[index].number
                }
            } catch (e: Exception) {
                android.util.Log.e("QuranAudio", "Failed to build MediaController", e)
            }
        }, { runnable -> android.os.Handler(android.os.Looper.getMainLooper()).post(runnable) })
    }

    private val _currentSurah = MutableStateFlow<Surah?>(null)
    val currentSurah = _currentSurah.asStateFlow()

    private val _ayahs = MutableStateFlow<List<Ayah>>(emptyList())
    val ayahs = _ayahs.asStateFlow()

    private val _readerLoadState = MutableStateFlow<ReaderLoadState>(ReaderLoadState.Idle)
    val readerLoadState = _readerLoadState.asStateFlow()

    private val _currentPlayingAyah = MutableStateFlow<Int?>(null)
    val currentPlayingAyah = _currentPlayingAyah.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    val bookmarks: StateFlow<List<com.example.data.local.BookmarkEntity>> = quranRepository.getBookmarksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProgress: StateFlow<UserProgressEntity> = repository.getUserProgressFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProgressEntity())

    val quranSettings: StateFlow<AppSettingEntity> = repository.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingEntity())

    val recitersList = listOf(
        Reciter("ar.alafasy", "Mishary Al-Afasy"),
        Reciter("ar.abdulbasitmurattal", "Abdul Basit"),
        Reciter("ar.husary", "Al-Husary"),
        Reciter("ar.minshawimujawwad", "Al-Minshawi")
    )

    fun toggleBookmark(surah: Surah) {
        viewModelScope.launch {
            quranRepository.toggleBookmark(surah.number, surah.name)
        }
    }

    fun playAyah(ayah: Ayah) {
        _currentPlayingAyah.value = ayah.number
        val activeIndex = _ayahs.value.indexOfFirst { it.number == ayah.number }.coerceAtLeast(0)
        
        viewModelScope.launch {
            try {
                val reciterName = recitersList.find { it.id == quranSettings.value.quranReciter }?.name ?: "Reciter"
                val surahName = _currentSurah.value?.name ?: "Quran Recitation"
                
                val mediaItems = _ayahs.value.map { a ->
                    val uri = if (a.audioUrl.isNotBlank()) a.audioUrl else {
                        val reciterId = quranSettings.value.quranReciter
                        val surahNumber = _currentSurah.value?.number ?: 1
                        audioManager.getPlaybackUri(reciterId, surahNumber)
                    }
                    val metadata = MediaMetadata.Builder()
                        .setTitle(surahName)
                        .setArtist(reciterName)
                        .build()
                    MediaItem.Builder()
                        .setUri(uri)
                        .setMediaMetadata(metadata)
                        .build()
                }
                
                player?.let { p ->
                    p.setMediaItems(mediaItems)
                    p.seekTo(activeIndex, 0L)
                    p.prepare()
                    p.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun togglePlayPause() {
        val p = player ?: return
        if (p.isPlaying) {
            p.pause()
        } else {
            val ayahsList = _ayahs.value
            if (p.mediaItemCount == 0 && ayahsList.isNotEmpty()) {
                val activeAyahNum = _currentPlayingAyah.value ?: 1
                val activeAyah = ayahsList.find { it.number == activeAyahNum } ?: ayahsList.first()
                playAyah(activeAyah)
            } else {
                p.play()
            }
        }
    }

    private fun playNextSurah() {
        val currentSurahVal = _currentSurah.value ?: return
        val nextSurahNumber = currentSurahVal.number + 1
        if (nextSurahNumber <= 114) {
            viewModelScope.launch {
                try {
                    val surahs = quranRepository.getSurahsDirect()
                    val nextSurah = surahs.find { it.number == nextSurahNumber }
                    if (nextSurah != null) {
                        setSurah(nextSurah, startAyah = 1, playImmediately = true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setSurah(surah: Surah, startAyah: Int = 1, playImmediately: Boolean = false) {
        _currentSurah.value = surah
        _currentPlayingAyah.value = startAyah
        loadSurah(surah.number, playImmediately, startAyah)
    }

    fun loadSurah(surahNumber: Int, playImmediately: Boolean = false, startAyah: Int = 1) {
        viewModelScope.launch {
            _readerLoadState.value = ReaderLoadState.Loading
            try {
                // Ensure surahs are refreshed if missing
                val currentSurahs = quranRepository.getSurahsDirect()
                if (currentSurahs.isEmpty()) {
                    quranRepository.refreshSurahs()
                }
                
                val surahs = quranRepository.getSurahsDirect()
                val surah = surahs.find { it.number == surahNumber }
                if (surah != null) {
                    _currentSurah.value = surah
                    
                    // Fetch settings to know the reciter
                    val currentReciterId = quranSettings.value.quranReciter
                    
                    // Fetch ayahs
                    quranRepository.refreshAyahs(surahNumber, currentReciterId)
                    val ayahsList = quranRepository.getAyahsForSurahFlow(surahNumber, currentReciterId).first()
                    _ayahs.value = ayahsList
                    
                    _readerLoadState.value = ReaderLoadState.Success

                    if (playImmediately && ayahsList.isNotEmpty()) {
                        val activeAyah = ayahsList.find { it.number == startAyah } ?: ayahsList.first()
                        playAyah(activeAyah)
                    }
                } else {
                    _readerLoadState.value = ReaderLoadState.Error("Surah not found")
                }
            } catch (e: Exception) {
                _readerLoadState.value = ReaderLoadState.Error(e.message ?: "Failed to load Surah")
            }
        }
    }

    fun updateQuranTextSize(size: Float) {
        viewModelScope.launch {
            val settings = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(settings.copy(quranTextSize = size))
        }
    }

    fun updateQuranFont(font: String) {
        viewModelScope.launch {
            val settings = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(settings.copy(quranFont = font))
        }
    }

    fun updateQuranKeepScreenOn(keep: Boolean) {
        viewModelScope.launch {
            val settings = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(settings.copy(quranKeepScreenOn = keep))
        }
    }

    fun updateQuranShowTranslation(show: Boolean) {
        viewModelScope.launch {
            val settings = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(settings.copy(quranShowTranslation = show))
        }
    }

    fun updateQuranReciter(reciter: String) {
        viewModelScope.launch {
            val settings = repository.getSettingsDirect() ?: AppSettingEntity()
            repository.saveSettings(settings.copy(quranReciter = reciter))
            // Reload surah with the new reciter if loaded
            _currentSurah.value?.let { surah ->
                loadSurah(surah.number)
            }
        }
    }

    fun startRecitationDownload(reciterId: String, context: Context) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        val data = androidx.work.workDataOf("reciter_id" to reciterId)
        val request = androidx.work.OneTimeWorkRequest.Builder(com.example.data.worker.RecitationDownloadWorker::class.java)
            .setInputData(data)
            .addTag("recitation_download_worker_$reciterId")
            .addTag("recitation_download_worker")
            .build()
        workManager.enqueueUniqueWork(
            "recitation_download_$reciterId",
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelRecitationDownload(reciterId: String, context: Context) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        workManager.cancelUniqueWork("recitation_download_$reciterId")
    }

    fun updateProgress(
        surahNumber: Int,
        surahName: String,
        surahArabicName: String,
        ayahNumber: Int,
        progress: Float
    ) {
        viewModelScope.launch {
            val currentProgress = repository.getUserProgressDirect() ?: UserProgressEntity()
            repository.saveUserProgress(
                currentProgress.copy(
                    lastReadSurahNumber = surahNumber,
                    lastReadSurahName = surahName,
                    lastReadSurahArabicName = surahArabicName,
                    lastReadAyahNumber = ayahNumber,
                    lastReadProgress = progress
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        (player as? MediaController)?.release()
    }
}
