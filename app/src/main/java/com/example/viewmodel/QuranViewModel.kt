package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.QuranRepository
import com.example.domain.model.Surah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// 2. Quran ViewModel
@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow("all")
    val activeFilter = _activeFilter.asStateFlow()

    private val _surahsLoadState = MutableStateFlow<SurahsLoadState>(SurahsLoadState.Loading)
    val surahsLoadState = _surahsLoadState.asStateFlow()

    val filteredSurahs = combine(quranRepository.getSurahsFlow(), _searchQuery, _activeFilter) { surahs, query, filter ->
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
                quranRepository.refreshSurahs()
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
