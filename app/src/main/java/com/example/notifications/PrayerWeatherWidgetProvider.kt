package com.example.notifications

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.domain.model.PrayerTime
import com.example.utils.TimeUtils
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PrayerWeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, PrayerWeatherWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, PrayerWeatherWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.prayer_weather_widget)

            // 1. Gregorian Date Format
            val arabicLocale = Locale.forLanguageTag("ar")
            val dateStr = try {
                val formatter = DateTimeFormatter.ofPattern("EEEE ، d MMMM", arabicLocale)
                LocalDate.now().format(formatter)
            } catch (e: Exception) {
                "اليوم"
            }
            views.setTextViewText(R.id.widget_date, dateStr)

            // 2. Weather Status
            views.setTextViewText(R.id.widget_weather, "26°C مشمس")

            // 3. Default Prayer Times list
            val defaultPrayers = listOf(
                PrayerTime("Fajr", "الفجر", "04:12", "sunrise"),
                PrayerTime("Dhuhr", "الظهر", "12:31", "sun"),
                PrayerTime("Asr", "العصر", "16:04", "sun"),
                PrayerTime("Maghrib", "المغرب", "19:22", "sunset"),
                PrayerTime("Isha", "العشاء", "20:52", "moon")
            )

            val now = LocalTime.now()
            var nextPrayer: PrayerTime = defaultPrayers[0]
            var nextPrayerTime: LocalTime = TimeUtils.parsePrayerTime(defaultPrayers[0].timeString) ?: LocalTime.of(4, 12)
            var found = false

            for (prayer in defaultPrayers) {
                val parsed = TimeUtils.parsePrayerTime(prayer.timeString) ?: continue
                if (parsed.isAfter(now)) {
                    nextPrayer = prayer
                    nextPrayerTime = parsed
                    found = true
                    break
                }
            }

            if (!found) {
                nextPrayer = defaultPrayers[0]
                nextPrayerTime = TimeUtils.parsePrayerTime(defaultPrayers[0].timeString) ?: LocalTime.of(4, 12)
            }

            // Calculate countdown
            val duration = if (found) {
                Duration.between(now, nextPrayerTime)
            } else {
                Duration.between(now, LocalTime.MAX).plus(Duration.between(LocalTime.MIN, nextPrayerTime))
            }

            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            val countdownStr = if (hours > 0) {
                "متبقي $hours س و $minutes د"
            } else {
                "متبقي $minutes دقيقة"
            }

            val formattedTime = try {
                nextPrayerTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
            } catch (e: Exception) {
                nextPrayer.timeString
            }

            views.setTextViewText(R.id.widget_next_prayer, "الصلاة القادمة: ${nextPrayer.arabicName} ($formattedTime)")
            views.setTextViewText(R.id.widget_countdown, countdownStr)

            // Timeline Chips (3 upcoming prayers)
            views.setTextViewText(R.id.widget_prayer_1, "${defaultPrayers[1].arabicName} ${defaultPrayers[1].timeString}")
            views.setTextViewText(R.id.widget_prayer_2, "${defaultPrayers[2].arabicName} ${defaultPrayers[2].timeString}")
            views.setTextViewText(R.id.widget_prayer_3, "${defaultPrayers[3].arabicName} ${defaultPrayers[3].timeString}")

            // Click Intent to open MainActivity
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
