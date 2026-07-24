package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.domain.model.PrayerTime
import com.example.utils.TimeUtils
import java.util.Calendar

class AzkarNotificationScheduler(private val context: Context) {

    fun scheduleAzkarNotifications(prayerTimes: List<PrayerTime>) {
        scheduleMorningAzkar()
        scheduleEveningAzkar()
        if (prayerTimes.isNotEmpty()) {
            scheduleAfterPrayerAzkar(prayerTimes)
        }
    }

    fun scheduleMorningAzkar(hour: Int = 7, minute: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AzkarNotificationReceiver::class.java).apply {
            putExtra("azkar_type", "morning")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d("AzkarNotification", "Scheduling Morning Azkar for ${calendar.time}")
        setAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    fun scheduleEveningAzkar(hour: Int = 17, minute: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AzkarNotificationReceiver::class.java).apply {
            putExtra("azkar_type", "evening")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d("AzkarNotification", "Scheduling Evening Azkar for ${calendar.time}")
        setAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    fun scheduleAfterPrayerAzkar(prayerTimes: List<PrayerTime>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (prayer in prayerTimes) {
            val intent = Intent(context, AzkarNotificationReceiver::class.java).apply {
                putExtra("azkar_type", "after_prayer")
                putExtra("prayer_name", prayer.name)
                putExtra("prayer_arabic", prayer.arabicName)
            }

            val requestCode = 2000 + when (prayer.name.lowercase()) {
                "fajr" -> 1
                "dhuhr", "zuhr" -> 2
                "asr" -> 3
                "maghrib" -> 4
                "isha" -> 5
                else -> prayer.name.hashCode() % 100
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val localTime = TimeUtils.parsePrayerTime(prayer.timeString) ?: continue

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, localTime.hour)
                set(Calendar.MINUTE, localTime.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, 10) // 10 minutes after prayer time
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            Log.d("AzkarNotification", "Scheduling After ${prayer.name} Azkar for ${calendar.time}")
            setAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun setAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canScheduleExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            }
        } catch (e: Exception) {
            Log.e("AzkarNotification", "Failed to schedule alarm", e)
        }
    }
}
