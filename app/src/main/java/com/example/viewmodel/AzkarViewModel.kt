package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.data.local.UserProgressEntity
import com.example.data.local.AppSettingEntity
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


// 4. Azkar ViewModel
@HiltViewModel
class AzkarViewModel @Inject constructor(
    private val repository: CompanionRepository,
    private val azkarRepository: AzkarRepository
) : ViewModel() {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val azkarCategories: StateFlow<List<AzkarCategory>> = repository.getUserProgressFlow()
        .flatMapLatest { azkarRepository.getAzkarCategoriesFlow(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCategory = MutableStateFlow<AzkarCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _currentAzkarList = MutableStateFlow<List<DhikrItem>>(emptyList())
    val currentAzkarList = _currentAzkarList.asStateFlow()



    private val _flowIndex = MutableStateFlow(0)
    val flowIndex = _flowIndex.asStateFlow()

    fun selectCategory(category: AzkarCategory) {
        _selectedCategory.value = category
        _flowIndex.value = 0
        viewModelScope.launch {
            azkarRepository.getDhikrItemsFlow(category.id).collect {
                _currentAzkarList.value = it
            }
        }
    }

    fun nextStep() {
        _flowIndex.value++
    }

    fun prevStep() {
        if (_flowIndex.value > 0) _flowIndex.value--
    }

    fun setFlowIndex(index: Int) {
        _flowIndex.value = index
    }

    fun updateAzkarProgress(categoryId: String, itemId: Int, count: Int) {
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            val newProgress = when {
                categoryId.contains("الصباح") -> progress.copy(morningDone = count)
                categoryId.contains("المساء") -> progress.copy(eveningDone = count)
                categoryId.contains("النوم") -> progress.copy(sleepDone = count)
                categoryId.contains("الصلاة") -> progress.copy(afterPrayerDone = count)
                else -> progress
            }
            repository.saveUserProgress(newProgress)
        }
    }

    fun completeAzkarFlow(categoryId: String, total: Int) {
        updateAzkarProgress(categoryId, 0, total)
    }



    val userProgress: StateFlow<UserProgressEntity> = repository.getUserProgressFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProgressEntity()
        )

    val settings: StateFlow<AppSettingEntity> = repository.getSettingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingEntity()
        )
}
