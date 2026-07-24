package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.repository.CompanionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: CompanionRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling prayer notifications")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prayerTimes = repository.getPrayerTimesFlow().first()
                    if (prayerTimes.isNotEmpty()) {
                        PrayerNotificationScheduler(context).scheduleNotifications(prayerTimes)
                        AzkarNotificationScheduler(context).scheduleAzkarNotifications(prayerTimes)
                        Log.d("BootReceiver", "Successfully rescheduled prayer and azkar notifications")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule prayer notifications", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
