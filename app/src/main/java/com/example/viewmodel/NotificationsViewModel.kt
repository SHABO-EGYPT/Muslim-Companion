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

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun togglePrayerNotifications() {
        viewModelScope.launch {
            val s = settings.value
            repository.saveSettings(s.copy(prayerNotifications = !s.prayerNotifications))
        }
    }

    fun toggleMorningAzkar() {
        viewModelScope.launch {
            val s = settings.value
            repository.saveSettings(s.copy(morningAzkarNotification = !s.morningAzkarNotification))
        }
    }

    fun toggleEveningAzkar() {
        viewModelScope.launch {
            val s = settings.value
            repository.saveSettings(s.copy(eveningAzkarNotification = !s.eveningAzkarNotification))
        }
    }

    fun toggleAfterPrayerAzkar() {
        viewModelScope.launch {
            val s = settings.value
            repository.saveSettings(s.copy(afterPrayerAzkarNotification = !s.afterPrayerAzkarNotification))
        }
    }

    fun sendAzkarTestNotification(context: android.content.Context, type: String) {
        viewModelScope.launch {
            val s = settings.value
            val isArabic = s.language == "Arabic"

            val (title, body, categoryId) = when (type) {
                "morning" -> Triple(
                    if (isArabic) "أذكار الصباح 🌅" else "Morning Azkar 🌅",
                    if (isArabic) "حان الآن موعد أذكار الصباح - ابدأ يومك بذكر الله." else "Time for Morning Azkar - Start your day with remembrance of Allah.",
                    "morning"
                )
                "evening" -> Triple(
                    if (isArabic) "أذكار المساء 🌆" else "Evening Azkar 🌆",
                    if (isArabic) "حان الآن موعد أذكار المساء - حصّن نفسك بذكر الله." else "Time for Evening Azkar - Fortify yourself with remembrance.",
                    "evening"
                )
                else -> Triple(
                    if (isArabic) "أذكار بعد الصلاة 🕌" else "After Prayer Azkar 🕌",
                    if (isArabic) "لا تنسَ أذكار بعد الصلاة والتسبيح المستحب." else "Don't forget the supplications and tasbih after prayer.",
                    "after_prayer"
                )
            }

            val timeString = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())

            repository.insertNotification(
                com.example.data.local.NotificationEntity(
                    title = title,
                    description = body,
                    time = timeString,
                    iconId = if (type == "morning") 2 else if (type == "evening") 3 else 1,
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
            )

            try {
                val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val channelId = "azkar_notification_channel"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val channel = android.app.NotificationChannel(
                        channelId,
                        "Azkar Reminders",
                        android.app.NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                val contentIntent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "azkar_flow")
                    putExtra("azkar_category_id", categoryId)
                }

                val contentPendingIntent = android.app.PendingIntent.getActivity(
                    context,
                    type.hashCode(),
                    contentIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )

                val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(contentPendingIntent)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify((5000..9999).random(), builder.build())
            } catch (e: Exception) {
                android.util.Log.e("NotificationsViewModel", "Failed to trigger Azkar test notification", e)
            }
        }
    }

    fun sendTestNotification(context: android.content.Context) {
        viewModelScope.launch {
            val s = settings.value
            val lang = s.language
            val isArabic = lang == "Arabic"

            val title = if (isArabic) "إشعار تجريبي 🔔" else "Test Notification 🔔"
            val body = if (isArabic)
                "الإشعارات تعمل بنجاح! ستتلقى تنبيهات مواقيت الصلاة في مواعيدها المحسوبة."
            else
                "Notifications are working perfectly! You will receive prayer alerts at their scheduled times."

            val timeString = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())

            repository.insertNotification(
                com.example.data.local.NotificationEntity(
                    title = title,
                    description = body,
                    time = timeString,
                    iconId = 1,
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
            )

            try {
                val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val channelId = "prayer_test_channel"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val channel = android.app.NotificationChannel(
                        channelId,
                        "Test Notifications",
                        android.app.NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify((1000..9999).random(), builder.build())
            } catch (e: Exception) {
                android.util.Log.e("NotificationsViewModel", "Failed to trigger system notification", e)
            }
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
