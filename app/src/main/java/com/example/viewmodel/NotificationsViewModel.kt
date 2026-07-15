package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.data.repository.CompanionRepository
import com.example.domain.model.NotificationItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// 9. Notifications ViewModel
@HiltViewModel
class NotificationsViewModel @Inject constructor(private val repository: CompanionRepository) : ViewModel() {
    val settings = repository.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.local.AppSettingEntity())

    val notifications = repository.getNotificationsFlow()
        .map { entities ->
            entities.map { entity ->
                NotificationItem(
                    id = entity.id,
                    title = entity.title,
                    body = entity.description,
                    relativeTime = entity.time,
                    iconName = getIconName(entity.iconId),
                    isUnread = !entity.isRead
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }
    
    private fun getIconName(iconId: Int): String {
        return when (iconId) {
            1 -> "bell"
            2 -> "sun"
            3 -> "moon"
            else -> "bell"
        }
    }
}
