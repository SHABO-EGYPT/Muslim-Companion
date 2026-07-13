package com.example

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.PrayerTime
import com.example.notifications.PrayerNotificationScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotificationSchedulerTest {

    @Test
    fun testExactAlarmScheduling() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)

        val scheduler = PrayerNotificationScheduler(context)
        
        // Let's create a prayer time at 15:30 (3:30 PM)
        val prayerTimes = listOf(
            PrayerTime("Asr", "العصر", "15:30", "asr_icon")
        )

        // Run the scheduler
        scheduler.scheduleNotifications(prayerTimes)

        // Fetch scheduled alarms
        val alarms = shadowAlarmManager.scheduledAlarms
        assertEquals(1, alarms.size)

        val alarm = alarms[0]
        assertNotNull(alarm)

        // Check that the alarm is set to exactly 15:30
        val calendar = Calendar.getInstance().apply {
            timeInMillis = alarm.triggerAtTime
        }
        
        assertEquals(15, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
    }
}
