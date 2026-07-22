package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.CompanionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CompanionRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            if (repository.getUserProgressDirect() == null) {
                repository.saveUserProgress(UserProgressEntity())
            }
            if (repository.getSettingsDirect() == null) {
                repository.saveSettings(AppSettingEntity())
            }
        }
    }

    val settings: StateFlow<AppSettingEntity> = repository.getSettingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingEntity()
        )

    val userProgress: StateFlow<UserProgressEntity?> = repository.getUserProgressFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
