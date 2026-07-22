package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.NamesOfAllahRepository
import com.example.domain.model.NameOfAllah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class NamesOfAllahViewModel @Inject constructor(
    repository: NamesOfAllahRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedName = MutableStateFlow<NameOfAllah?>(null)
    val selectedName: StateFlow<NameOfAllah?> = _selectedName

    val allNames: StateFlow<List<NameOfAllah>> = repository.getNamesOfAllahFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredNames: StateFlow<List<NameOfAllah>> = combine(allNames, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter { item ->
                item.name.contains(q) ||
                item.transliteration.lowercase().contains(q) ||
                item.meaning.contains(q) ||
                item.explanation.contains(q) ||
                item.meaningEn.lowercase().contains(q)
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

    fun selectName(name: NameOfAllah?) {
        _selectedName.value = name
    }
}
