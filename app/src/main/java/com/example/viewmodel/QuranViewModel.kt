package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.data.repository.QuranRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.json.JSONObject
import org.json.JSONArray
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Content
import com.example.data.remote.Part
import com.example.data.remote.GenerationConfig
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import kotlinx.coroutines.delay

// 2. Quran ViewModel
@HiltViewModel
class QuranViewModel @Inject constructor(private val repository: CompanionRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow("all")
    val activeFilter = _activeFilter.asStateFlow()

    private val _surahsLoadState = MutableStateFlow<SurahsLoadState>(SurahsLoadState.Loading)
    val surahsLoadState = _surahsLoadState.asStateFlow()

    val filteredSurahs = combine(repository.quranRepository.getSurahsFlow(), _searchQuery, _activeFilter) { surahs, query, filter ->
        surahs.filter { surah ->
            val matchesQuery = surah.name.contains(query, ignoreCase = true) || 
                               surah.arabicName.contains(query) ||
                               surah.number.toString() == query
            val matchesFilter = when (filter) {
                "makki" -> surah.isMakki
                "madani" -> !surah.isMakki
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchSurahs()
    }

    fun fetchSurahs() {
        viewModelScope.launch {
            _surahsLoadState.value = SurahsLoadState.Loading
            try {
                repository.quranRepository.refreshSurahs()
                _surahsLoadState.value = SurahsLoadState.Success
            } catch (e: Exception) {
                _surahsLoadState.value = SurahsLoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: String) {
        _activeFilter.value = filter
    }
}

sealed class SurahsLoadState {
    object Loading : SurahsLoadState()
    object Success : SurahsLoadState()
    data class Error(val message: String) : SurahsLoadState()
}
