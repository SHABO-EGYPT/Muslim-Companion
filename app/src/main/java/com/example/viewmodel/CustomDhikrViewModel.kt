package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CustomDhikrRepository
import com.example.domain.model.ChainDhikrItem
import com.example.domain.model.CustomDhikrChain
import com.example.domain.model.DhikrItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomDhikrViewModel @Inject constructor(
    private val repository: CustomDhikrRepository,
    private val azkarRepository: AzkarRepository
) : ViewModel() {

    val chains: StateFlow<List<CustomDhikrChain>> = repository.getAllChainsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val catalogTasbihPhrases: StateFlow<List<DhikrItem>> = azkarRepository.getTasbihPhrasesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _activeChain = MutableStateFlow<CustomDhikrChain?>(null)
    val activeChain = _activeChain.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex = _currentStepIndex.asStateFlow()

    private val _currentStepCount = MutableStateFlow(0)
    val currentStepCount = _currentStepCount.asStateFlow()

    private val _isChainCompleted = MutableStateFlow(false)
    val isChainCompleted = _isChainCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultTemplatesIfEmpty()
        }
    }

    fun saveChain(
        id: Int = 0,
        title: String,
        description: String = "",
        items: List<ChainDhikrItem>,
        colorHex: Long = 0xFF0D9488
    ) {
        viewModelScope.launch {
            val total = items.sumOf { it.targetCount }
            val chain = CustomDhikrChain(
                id = id,
                title = title,
                description = description,
                items = items,
                totalCount = total,
                colorHex = colorHex
            )
            repository.saveChain(chain)
        }
    }

    fun deleteChain(id: Int) {
        viewModelScope.launch {
            repository.deleteChain(id)
        }
    }

    fun startRecitation(chainId: Int) {
        viewModelScope.launch {
            val chain = repository.getChainById(chainId) ?: chains.value.find { it.id == chainId }
            _activeChain.value = chain
            _currentStepIndex.value = 0
            _currentStepCount.value = 0
            _isChainCompleted.value = false
        }
    }

    fun increment() {
        val chain = _activeChain.value ?: return
        val currentStep = _currentStepIndex.value
        val items = chain.items
        if (items.isEmpty()) return

        val targetForStep = items[currentStep].targetCount
        val newCount = _currentStepCount.value + 1

        if (newCount >= targetForStep) {
            if (currentStep < items.size - 1) {
                _currentStepIndex.value = currentStep + 1
                _currentStepCount.value = 0
            } else {
                _currentStepCount.value = targetForStep
                _isChainCompleted.value = true
                viewModelScope.launch {
                    repository.recordChainCompletion(chain.id)
                }
            }
        } else {
            _currentStepCount.value = newCount
        }
    }

    fun resetRecitation() {
        _currentStepIndex.value = 0
        _currentStepCount.value = 0
        _isChainCompleted.value = false
    }
}
