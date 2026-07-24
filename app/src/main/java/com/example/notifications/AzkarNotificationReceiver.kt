package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.AppSettingEntity
import com.example.data.local.NotificationEntity
import com.example.data.repository.CompanionRepository
import com.example.ui.Translator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AzkarNotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: CompanionRepository

    override fun onReceive(context: Context, intent: Intent) {
        val azkarType = intent.getStringExtra("azkar_type") ?: "morning"
        val prayerName = intent.getStringExtra("prayer_name") ?: ""
        val prayerArabic = intent.getStringExtra("prayer_arabic") ?: ""

        Log.d("AzkarNotification", "Received Azkar alarm for type: $azkarType")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(9_000) {
                    val settings = repository.getSettingsDirect() ?: AppSettingEntity()

                    // Check feature flag based on Azkar type
                    val isEnabled = when (azkarType) {
                        "morning" -> settings.morningAzkarNotification
                        "evening" -> settings.eveningAzkarNotification
                        "after_prayer" -> settings.afterPrayerAzkarNotification
                        else -> true
                    }

                    if (!isEnabled) {
                        Log.d("AzkarNotification", "Azkar notification disabled for $azkarType")
                        return@withTimeout
                    }

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channelId = "azkar_notification_channel"
                    val channelName = "Azkar Reminders (أذكار)"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channelId,
                            channelName,
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = "Reminders for Morning, Evening, and After Prayer Azkar"
                        }
                        notificationManager.createNotificationChannel(channel)
                    }

                    val lang = settings.language
                    val isArabic = lang == "Arabic"

                    val (title, body, categoryId) = when (azkarType) {
                        "morning" -> Triple(
                            if (isArabic) "أذكار الصباح 🌅" else "Morning Azkar 🌅",
                            if (isArabic) "حان الآن موعد أذكار الصباح - ابدأ يومك بذكر الله وتلاوة الأذكار المباركة." else "Time for Morning Azkar - Start your day with the remembrance of Allah.",
                            "morning"
                        )
                        "evening" -> Triple(
                            if (isArabic) "أذكار المساء 🌆" else "Evening Azkar 🌆",
                            if (isArabic) "حان الآن موعد أذكار المساء - حصّن نفسك بذكر الله والصلاة على النبي." else "Time for Evening Azkar - Fortify yourself with daily supplications.",
                            "evening"
                        )
                        "after_prayer" -> {
                            val translatedPrayer = Translator.translate(prayerName.lowercase(), lang)
                            Triple(
                                if (isArabic) "أذكار بعد الصلاة 🕌" else "After Prayer Azkar 🕌",
                                if (isArabic) "لا تنسَ تلاوة أذكار بعد صلاة $translatedPrayer ($prayerArabic) والتسبيح." else "Don't forget the supplications and tasbih after $translatedPrayer prayer.",
                                "after_prayer"
                            )
                        }
                        else -> Triple("Remembrance", "Time for daily Azkar", "morning")
                    }

                    // Content Intent to launch Azkar flow in app
                    val contentIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("navigate_to", "azkar_flow")
                        putExtra("azkar_category_id", categoryId)
                    }

                    val contentPendingIntent = PendingIntent.getActivity(
                        context,
                        azkarType.hashCode(),
                        contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val builder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(contentPendingIntent)
                        .setAutoCancel(true)

                    val notificationId = 5000 + azkarType.hashCode() % 1000
                    notificationManager.notify(notificationId, builder.build())

                    // Insert to database history
                    val timeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    repository.insertNotification(
                        NotificationEntity(
                            title = title,
                            description = body,
                            time = timeString,
                            iconId = if (azkarType == "morning") 2 else if (azkarType == "evening") 3 else 1,
                            isRead = false,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("AzkarNotification", "Error handling Azkar notification", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
