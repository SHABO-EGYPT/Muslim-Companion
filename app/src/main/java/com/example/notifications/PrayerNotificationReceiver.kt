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
import kotlinx.coroutines.runBlocking
import android.media.AudioAttributes
import android.net.Uri
import android.provider.Settings
import com.example.R

class PrayerNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
        val arabicName = intent.getStringExtra("prayer_arabic") ?: ""
        
        Log.d("PrayerNotification", "Received alarm for $prayerName")

        val db = CompanionDatabase.getDatabase(context)

        val settings = runBlocking {
            db.companionDao().getSettingsDirect()
        } ?: AppSettingEntity()

        if (!settings.prayerNotifications) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val soundType = settings.notificationSoundType
        val channelId = "prayer_channel_$soundType"
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
                        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.full_adhan}")
                        setSound(soundUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    }
                    "First Adhan" -> {
                        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.first_adhan}")
                        setSound(soundUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    }
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle("Upcoming Prayer")
            .setContentText("$prayerName ($arabicName) is in 10 minutes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        when (soundType) {
            "Silent" -> {
                builder.setSound(null)
                builder.setVibrate(longArrayOf(0, 500, 200, 500))
            }
            "Full Adhan" -> {
                val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.full_adhan}")
                builder.setSound(soundUri)
            }
            "First Adhan" -> {
                val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.first_adhan}")
                builder.setSound(soundUri)
            }
        }

        notificationManager.notify(prayerName.hashCode(), builder.build())
    }
}
