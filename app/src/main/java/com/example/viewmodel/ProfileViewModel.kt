package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.CompanionRepository
import com.example.domain.model.AchievementBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: CompanionRepository) : ViewModel() {
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

    val badges: StateFlow<List<AchievementBadge>> = userProgress
        .map { repository.getBadges(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateProfile(name: String, location: String) {
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            repository.saveUserProgress(progress.copy(
                username = name.trim().take(50),
                location = location.trim().take(100)
            ))
        }
    }

    fun completeOnboarding(name: String, location: String) {
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            repository.saveUserProgress(
                progress.copy(
                    username = name.trim().take(50),
                    location = location.trim().take(100),
                    onboardingCompleted = true
                )
            )
        }
    }
}
