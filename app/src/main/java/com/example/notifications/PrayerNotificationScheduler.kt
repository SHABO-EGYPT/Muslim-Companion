package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.domain.model.PrayerTime
import java.util.Calendar

class PrayerNotificationScheduler(private val context: Context) {

    fun scheduleNotifications(prayerTimes: List<PrayerTime>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (prayer in prayerTimes) {
            val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                putExtra("prayer_name", prayer.name)
                putExtra("prayer_arabic", prayer.arabicName)
            }

            val requestCode = when(prayer.name.lowercase()) {
                "fajr" -> 1
                "dhuhr", "zuhr" -> 2
                "asr" -> 3
                "maghrib" -> 4
                "isha" -> 5
                else -> prayer.name.hashCode()
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Parse time "HH:mm" safely
            val localTime = com.example.utils.TimeUtils.parsePrayerTime(prayer.timeString) ?: continue

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, localTime.hour)
                set(Calendar.MINUTE, localTime.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // Subtract 10 minutes
                add(Calendar.MINUTE, -10)
            }

                // If the time has already passed today, schedule for tomorrow
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                Log.d("PrayerNotification", "Scheduling ${prayer.name} for ${calendar.time}")

                try {
                    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        alarmManager.canScheduleExactAlarms()
                    } else {
                        true
                    }

                    if (canScheduleExact) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        }
                    } else {
                        // Fallback to non-exact alarm to prevent crashes and ensure notifications still trigger
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("PrayerNotification", "Missing exact alarm permission, falling back to non-exact", e)
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        }
                    } catch (fallbackException: Exception) {
                        Log.e("PrayerNotification", "Failed to schedule fallback non-exact alarm", fallbackException)
                    }
            }
        }
    }
}
