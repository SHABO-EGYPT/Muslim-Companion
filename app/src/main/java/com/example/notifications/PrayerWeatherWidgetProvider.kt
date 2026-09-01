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
import com.example.data.local.CompanionDatabase
import com.example.domain.model.PrayerTime
import com.example.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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

            // 1. Settings & Locales
            val settings = try {
                runBlocking(Dispatchers.IO) {
                    try {
                        CompanionDatabase.buildDatabase(context).companionDao().getSettingsDirect()
                    } catch (e: Exception) {
                        null
                    }
                }
            } catch (e: Exception) {
                null
            }
            val isArabic = settings?.language != "English"
            val appLocale = if (isArabic) Locale.forLanguageTag("ar-u-nu-latn") else Locale.US
            val today = LocalDate.now()

            // Hijri Date
            val hijriDateStr = try {
                val hijrahDate = java.time.chrono.HijrahDate.from(today)
                val suffix = if (isArabic) " هـ" else " AH"
                hijrahDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", appLocale)) + suffix
            } catch (e: Exception) {
                if (isArabic) "١٤٤٦ هـ" else "1446 AH"
            }
            views.setTextViewText(R.id.widget_date_hijri, hijriDateStr)

            // Gregorian Date
            val gregorianDateStr = try {
                val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", appLocale)
                today.format(formatter)
            } catch (e: Exception) {
                today.toString()
            }
            views.setTextViewText(R.id.widget_date_gregorian, gregorianDateStr)

            // 2. Weather Status
            views.setTextViewText(R.id.widget_weather, "☀️ 26°C")

            // 3. Prayer Times List (Load cached if available, fallback to defaults)
            val defaultPrayers = listOf(
                PrayerTime("Fajr", "الفجر", "04:12", "sunrise"),
                PrayerTime("Dhuhr", "الظهر", "12:31", "sun"),
                PrayerTime("Asr", "العصر", "16:04", "sun"),
                PrayerTime("Maghrib", "المغرب", "19:22", "sunset"),
                PrayerTime("Isha", "العشاء", "20:52", "moon")
            )

            val prayers = try {
                val cached = runBlocking(Dispatchers.IO) {
                    try {
                        CompanionDatabase.buildDatabase(context).companionDao().getCachedPrayerTimesDirect()
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
                if (cached.isNotEmpty()) {
                    // Standardize to 5 canonical prayers
                    val nameMap = cached.associateBy { it.name.lowercase() }
                    val canonicalNames = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
                    val mapped = canonicalNames.mapNotNull { key ->
                        nameMap[key]?.let { PrayerTime(it.name, it.arabicName, it.timeString, it.iconName) }
                    }
                    if (mapped.size == 5) mapped else cached.map { PrayerTime(it.name, it.arabicName, it.timeString, it.iconName) }
                } else {
                    defaultPrayers
                }
            } catch (e: Exception) {
                defaultPrayers
            }

            val now = LocalTime.now()
            var nextIndex = 0
            var nextPrayerTime: LocalTime = TimeUtils.parsePrayerTime(prayers[0].timeString) ?: LocalTime.of(4, 12)
            var found = false

            for (i in prayers.indices) {
                val parsed = TimeUtils.parsePrayerTime(prayers[i].timeString) ?: continue
                if (parsed.isAfter(now)) {
                    nextIndex = i
                    nextPrayerTime = parsed
                    found = true
                    break
                }
            }

            if (!found) {
                nextIndex = 0
                nextPrayerTime = TimeUtils.parsePrayerTime(prayers[0].timeString) ?: LocalTime.of(4, 12)
            }

            val nextPrayer = prayers[nextIndex]

            // Calculate countdown
            val duration = if (found) {
                Duration.between(now, nextPrayerTime)
            } else {
                Duration.between(now, LocalTime.MAX).plus(Duration.between(LocalTime.MIN, nextPrayerTime))
            }

            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            val countdownStr = if (isArabic) {
                String.format(Locale.US, "%02d:%02d متبقي", hours, minutes)
            } else {
                String.format(Locale.US, "%02d:%02d left", hours, minutes)
            }

            views.setTextViewText(R.id.widget_next_prayer_label, if (isArabic) "الصلاة القادمة" else "NEXT PRAYER")
            views.setTextViewText(R.id.widget_next_prayer_name, if (isArabic) nextPrayer.arabicName else nextPrayer.name)
            views.setTextViewText(R.id.widget_countdown, countdownStr)

            // 4. Populate the 5 Prayer Chips with Active/Inactive Highlight
            fun format12Hour(timeStr: String): String {
                return try {
                    val parsed = TimeUtils.parsePrayerTime(timeStr) ?: return timeStr
                    val hour = parsed.hour
                    val minute = parsed.minute
                    val h12 = if (hour % 12 == 0) 12 else hour % 12
                    String.format(Locale.US, "%02d:%02d", h12, minute)
                } catch (e: Exception) {
                    timeStr
                }
            }

            val chipContainerIds = listOf(
                R.id.widget_chip_1_container,
                R.id.widget_chip_2_container,
                R.id.widget_chip_3_container,
                R.id.widget_chip_4_container,
                R.id.widget_chip_5_container
            )
            val chipNameIds = listOf(
                R.id.widget_chip_1_name,
                R.id.widget_chip_2_name,
                R.id.widget_chip_3_name,
                R.id.widget_chip_4_name,
                R.id.widget_chip_5_name
            )
            val chipTimeIds = listOf(
                R.id.widget_chip_1_time,
                R.id.widget_chip_2_time,
                R.id.widget_chip_3_time,
                R.id.widget_chip_4_time,
                R.id.widget_chip_5_time
            )

            for (i in 0 until 5) {
                if (i < prayers.size) {
                    val p = prayers[i]
                    val isNext = (i == nextIndex)
                    val pName = if (isArabic) p.arabicName else p.name
                    val pTime = format12Hour(p.timeString)

                    views.setTextViewText(chipNameIds[i], pName)
                    views.setTextViewText(chipTimeIds[i], pTime)

                    if (isNext) {
                        views.setInt(chipContainerIds[i], "setBackgroundResource", R.drawable.widget_chip_active)
                        views.setTextColor(chipNameIds[i], android.graphics.Color.parseColor("#F2CA50"))
                        views.setTextColor(chipTimeIds[i], android.graphics.Color.parseColor("#FFFFFF"))
                    } else {
                        views.setInt(chipContainerIds[i], "setBackgroundResource", R.drawable.widget_chip_inactive)
                        views.setTextColor(chipNameIds[i], android.graphics.Color.parseColor("#D0C5AF"))
                        views.setTextColor(chipTimeIds[i], android.graphics.Color.parseColor("#E2E2E2"))
                    }
                }
            }

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

