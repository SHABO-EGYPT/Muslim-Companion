package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.QuranicDuasRepository
import com.example.domain.model.QuranicDua
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class QuranicDuasViewModel @Inject constructor(
    repository: QuranicDuasRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _bookmarkedIds = MutableStateFlow<Set<Int>>(emptySet())
    val bookmarkedIds: StateFlow<Set<Int>> = _bookmarkedIds

    val allDuas: StateFlow<List<QuranicDua>> = repository.getQuranicDuasFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredDuas: StateFlow<List<QuranicDua>> = combine(allDuas, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter { item ->
                item.text.contains(q) ||
                item.surah.contains(q) ||
                item.transliteration.lowercase().contains(q) ||
                "${item.ayah}".contains(q)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(duaId: Int) {
        _bookmarkedIds.value = if (_bookmarkedIds.value.contains(duaId)) {
            _bookmarkedIds.value - duaId
        } else {
            _bookmarkedIds.value + duaId
        }
    }
}
