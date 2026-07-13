package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log
import androidx.room.Room
import com.example.data.local.AppSettingEntity
import com.example.data.local.CompanionDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import android.media.AudioAttributes
import android.net.Uri
import android.provider.Settings
import com.example.R

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.data.repository.CompanionRepository

@AndroidEntryPoint
class PrayerNotificationReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: CompanionRepository
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
        val arabicName = intent.getStringExtra("prayer_arabic") ?: ""
        
        Log.d("PrayerNotification", "Received alarm for $prayerName")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(9_000) {
                    val settings = repository.getSettingsDirect() ?: AppSettingEntity()

                    if (!settings.prayerNotifications) return@withTimeout

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    val soundType = settings.notificationSoundType
                    val channelId = "prayer_channel_${soundType.lowercase().replace(" ", "_")}"
                    val channelName = "Prayer Notifications ($soundType)"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val importance = if (soundType == "Silent") {
                            NotificationManager.IMPORTANCE_DEFAULT
                        } else {
                            NotificationManager.IMPORTANCE_HIGH
                        }
                        
                        val channel = NotificationChannel(channelId, channelName, importance).apply {
                            description = "Notifications for upcoming prayer times"
                            
                            when (soundType) {
                                "Silent" -> {
                                    setSound(null, null)
                                    enableVibration(true)
                                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                                }
                                "Subtle" -> {
                                    // Default system sound
                                }
                                "Full Adhan" -> {
                                    val soundUri = Uri.parse("android.resource://${context.packageName}/raw/full_adhan")
                                    setSound(soundUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                                }
                                "First Adhan" -> {
                                    val soundUri = Uri.parse("android.resource://${context.packageName}/raw/first_adhan")
                                    setSound(soundUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                                }
                            }
                        }
                        notificationManager.createNotificationChannel(channel)
                    }

                    val lang = settings.language
                    val translatedPrayer = com.example.ui.Translator.translate(prayerName.lowercase(), lang)
                    val title = if (lang == "Arabic") "حان وقت الصلاة" else "Time for Prayer"
                    val contentText = if (lang == "Arabic") {
                        "حان الآن موعد صلاة $translatedPrayer ($arabicName)"
                    } else {
                        "It is time for $translatedPrayer ($arabicName) prayer."
                    }

                    val builder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info) 
                        .setContentTitle(title)
                        .setContentText(contentText)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)

                    when (soundType) {
                        "Silent" -> {
                            builder.setSound(null)
                            builder.setVibrate(longArrayOf(0, 500, 200, 500))
                        }
                        "Full Adhan" -> {
                            val soundUri = Uri.parse("android.resource://${context.packageName}/raw/full_adhan")
                            builder.setSound(soundUri)
                        }
                        "First Adhan" -> {
                            val soundUri = Uri.parse("android.resource://${context.packageName}/raw/first_adhan")
                            builder.setSound(soundUri)
                        }
                    }

                    notificationManager.notify(prayerName.hashCode(), builder.build())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
