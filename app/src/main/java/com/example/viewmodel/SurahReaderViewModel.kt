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
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
    private val audioManager: QuranAudioManager,
    @ApplicationContext context: Context
) : ViewModel() {
    private val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                android.util.Log.d("QuranAudio", "Playback state changed: $state")
                if (state == Player.STATE_ENDED) {
                    playNextAyah()
                }
            }
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                android.util.Log.d("QuranAudio", "Is playing changed: $isPlayingNow")
                _isPlaying.value = isPlayingNow
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("QuranAudio", "ExoPlayer Error!", error)
            }
        })
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

    val bookmarks: StateFlow<List<com.example.data.local.BookmarkEntity>> = repository.quranRepository.getBookmarksFlow()
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
            repository.quranRepository.toggleBookmark(surah.number, surah.name)
        }
    }

    fun playAyah(ayah: Ayah) {
        _currentPlayingAyah.value = ayah.number
        viewModelScope.launch {
            try {
                val uri = if (ayah.audioUrl.isNotBlank()) ayah.audioUrl else {
                    val reciterId = quranSettings.value.quranReciter
                    val surahNumber = _currentSurah.value?.number ?: return@launch
                    // Fallback to local files or single sura download
                    audioManager.getPlaybackUri(reciterId, surahNumber)
                }
                if (uri.isNotBlank()) {
                    player.setMediaItem(MediaItem.fromUri(uri))
                    player.prepare()
                    player.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            val ayahsList = _ayahs.value
            if (_currentPlayingAyah.value == null && ayahsList.isNotEmpty()) {
                playAyah(ayahsList.first())
            } else {
                player.play()
            }
        }
    }

    private fun playNextAyah() {
        val ayahsList = _ayahs.value
        val currentNumber = _currentPlayingAyah.value ?: return
        val currentIdx = ayahsList.indexOfFirst { it.number == currentNumber }
        if (currentIdx != -1 && currentIdx + 1 < ayahsList.size) {
            playAyah(ayahsList[currentIdx + 1])
        }
    }

    fun setSurah(surah: Surah, startAyah: Int = 1) {
        _currentSurah.value = surah
        _currentPlayingAyah.value = startAyah
        loadSurah(surah.number)
    }

    fun loadSurah(surahNumber: Int) {
        viewModelScope.launch {
            _readerLoadState.value = ReaderLoadState.Loading
            try {
                // Ensure surahs are refreshed if missing
                val currentSurahs = repository.quranRepository.getSurahsDirect()
                if (currentSurahs.isEmpty()) {
                    repository.quranRepository.refreshSurahs()
                }
                
                val surahs = repository.quranRepository.getSurahsDirect()
                val surah = surahs.find { it.number == surahNumber }
                if (surah != null) {
                    _currentSurah.value = surah
                    
                    // Fetch settings to know the reciter
                    val currentReciterId = quranSettings.value.quranReciter
                    
                    // Fetch ayahs
                    repository.quranRepository.refreshAyahs(surahNumber, currentReciterId)
                    val ayahsList = repository.quranRepository.getAyahsForSurahFlow(surahNumber, currentReciterId).first()
                    _ayahs.value = ayahsList
                    
                    _readerLoadState.value = ReaderLoadState.Success
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
        player.release()
    }
}
