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
import com.example.audio.QuranAudioService
import com.example.audio.QuranPlaybackState

class QuranPlayerWidgetProvider : AppWidgetProvider() {

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
        // Refresh all widgets if requested
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, QuranPlayerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.quran_player_widget)

            // 1. Set current playback metadata
            val surahText = QuranPlaybackState.currentSurahName
            val ayahNumber = QuranPlaybackState.currentAyahNumber
            val subtitleText = if (ayahNumber > 0) {
                "${QuranPlaybackState.currentReciterName} - Ayah $ayahNumber"
            } else {
                QuranPlaybackState.currentReciterName
            }

            views.setTextViewText(R.id.widget_title, surahText)
            views.setTextViewText(R.id.widget_subtitle, subtitleText)

            // 2. Play/Pause button icon state
            val playIcon = if (QuranPlaybackState.isPlaying) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }
            views.setImageViewResource(R.id.widget_btn_play, playIcon)

            // 3. Play/Pause pending intent (Service call)
            val playIntent = Intent(context, QuranAudioService::class.java).apply {
                action = QuranAudioService.ACTION_PLAY_PAUSE
            }
            val playPendingIntent = PendingIntent.getService(
                context,
                100,
                playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play, playPendingIntent)

            // 4. Previous pending intent (Service call)
            val prevIntent = Intent(context, QuranAudioService::class.java).apply {
                action = QuranAudioService.ACTION_PREVIOUS
            }
            val prevPendingIntent = PendingIntent.getService(
                context,
                101,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPendingIntent)

            // 5. Next pending intent (Service call)
            val nextIntent = Intent(context, QuranAudioService::class.java).apply {
                action = QuranAudioService.ACTION_NEXT
            }
            val nextPendingIntent = PendingIntent.getService(
                context,
                102,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

            // 6. Launch App when clicking on background
            val appIntent = Intent(context, MainActivity::class.java)
            val appPendingIntent = PendingIntent.getActivity(
                context,
                103,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, appPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_subtitle, appPendingIntent)

            // Update on system manager
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, QuranPlayerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, QuranPlayerWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
