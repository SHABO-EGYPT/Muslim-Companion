package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.navigation.Routes
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AzkarViewModel
import com.example.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(viewModel: HomeViewModel, azkarViewModel: AzkarViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val progress by viewModel.userProgress.collectAsState()
    
    val navigateToTab = { route: String ->
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val settings by viewModel.settings.collectAsState()
    val azkarCats by viewModel.azkarCategories.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val nextPrayerInfo by viewModel.nextPrayerInfo.collectAsState()

    val today = LocalDate.now()
    val appLocale = remember(settings.language) {
        if (settings.language == "Arabic") java.util.Locale.forLanguageTag("ar-u-nu-latn") else java.util.Locale.US
    }
    val gregorianDate = remember(today, appLocale) {
        today.format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM yyyy", appLocale))
    }
    val hijriDateString = remember(today, appLocale) {
        val date = java.time.chrono.HijrahDate.from(today)
        val suffix = if (settings.language == "Arabic") " هـ" else " AH"
        date.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", appLocale)) + suffix
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("home_screen_container"),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            val hour = LocalTime.now().hour
            val isNight = hour < 6 || hour >= 18
            val cardColor = if (isNight) Color(0xFF1A237E) else MintTeal
            val contentColor = if (isNight) Color.White else DarkTealText
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).testTag("greeting_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        val greeting = if (hour < 12) Translator.translate("good_morning", settings.language) 
                                      else if (hour < 18) Translator.translate("good_afternoon", settings.language) 
                                      else Translator.translate("good_evening", settings.language)
                        Text(text = "$greeting,", style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.8f))
                        Text(text = progress.username, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = contentColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Lucide.Clock, contentDescription = null, tint = contentColor.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val timeString = LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a", appLocale))
                            Text(text = timeString, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = contentColor)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$hijriDateString · $gregorianDate", style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
                        if (progress.streak > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Lucide.Flame, contentDescription = "Streak", tint = if (isNight) WarmPeach else DarkTealText, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "${progress.streak} ${Translator.translate("streak", settings.language)}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = contentColor)
                                }
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(64.dp).background(Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(imageVector = if (isNight) Lucide.Moon else Lucide.Sun, contentDescription = "Time of day", tint = if (isNight) Color(0xFFFFD54F) else Color(0xFFFFF176), modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }, modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.1f), CircleShape).testTag("home_notification_button")) {
                            Icon(imageVector = Lucide.Bell, contentDescription = "Notifications", tint = contentColor)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { navigateToTab(Routes.PRAYER) }.testTag("next_prayer_card"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.background(Brush.linearGradient(colors = listOf(PrimaryTeal, Secondary))).padding(24.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text(text = Translator.translate("next_prayer", settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = MintTeal)
                                Spacer(modifier = Modifier.height(4.dp))
                                val prayerNameKey = nextPrayerInfo.first.name.lowercase()
                                Text(text = Translator.translate(prayerNameKey, settings.language), style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp), color = Color.White)
                                Text(text = nextPrayerInfo.first.arabicName, style = MaterialTheme.typography.titleLarge.copy(fontFamily = ArabicSerifFamily), color = Color.White.copy(alpha = 0.85f))
                            }
                            val iconVector = when(nextPrayerInfo.first.iconName) {
                                "sunrise" -> Lucide.Sunrise
                                "sunset" -> Lucide.Sunset
                                "moon" -> Lucide.Moon
                                else -> Lucide.Sun
                            }
                            Box(modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(imageVector = iconVector, contentDescription = nextPrayerInfo.first.name, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start) {
                            Text(text = nextPrayerInfo.second, style = MaterialTheme.typography.displayMedium.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            val translatedUntilAdhan = Translator.translate("until_adhan", settings.language)
                            Text(text = translatedUntilAdhan, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(bottom = 8.dp))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { prayer ->
                                val isActive = prayer == nextPrayerInfo.first.name
                                Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(if (isActive) Color.White else Color.White.copy(alpha = 0.25f)))
                            }
                        }
                    }
                }
            }
        }

        item {
            val completedPrayersCount = progress.completedPrayersToday.split(",").filter { it.isNotBlank() }.size
            val totalAzkarCount = azkarCats.sumOf { it.totalCount }.takeIf { it > 0 } ?: 1
            val doneAzkarCount = azkarCats.sumOf { it.doneCount }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f).testTag("streak_card"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.Center) {
                        Box(modifier = Modifier.size(32.dp).background(WarmPeach, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Lucide.Flame, contentDescription = "Streak", tint = DarkWarmPeachText, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${progress.streak} ${Translator.translate("days", settings.language)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = MaterialTheme.colorScheme.onBackground)
                        Text(text = Translator.translate("streak", settings.language), style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(modifier = Modifier.weight(1f).testTag("azkar_stat_card"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicatorM3(percentage = doneAzkarCount.toFloat() / totalAzkarCount.toFloat(), size = 32, strokeWidth = 3)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${(doneAzkarCount.toFloat() / totalAzkarCount.toFloat() * 100).toInt()}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = MaterialTheme.colorScheme.onBackground)
                        Text(text = Translator.translate("azkar", settings.language), style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(modifier = Modifier.weight(1f).clickable { navigateToTab(Routes.PRAYER) }.testTag("prayer_stat_card"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicatorM3(percentage = completedPrayersCount.toFloat() / 5f, size = 32, strokeWidth = 3, color = MintTeal)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$completedPrayersCount/5", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = MaterialTheme.colorScheme.onBackground)
                        Text(text = "${Translator.translate("prayer", settings.language)} (+${progress.prayerScore})", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(title = Translator.translate("read_quran", settings.language), actionText = Translator.translate("see_all", settings.language), onActionClick = { navigateToTab(Routes.QURAN) })
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { navController.navigate(Routes.SURAH_READER) }.testTag("continue_reading_card"), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).background(MintTeal, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Text(text = "${progress.lastReadSurahNumber}", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp, fontFamily = ArabicSerifFamily), color = DarkTealText)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = progress.lastReadSurahArabicName.ifBlank { Translator.translate("fatiha_arabic", settings.language) }, 
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontFamily = ArabicSerifFamily), 
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val translatedOf = Translator.translate("of", settings.language)
                            val translatedCompleted = Translator.translate("completed", settings.language)
                            Text(text = "${Translator.translate("verse", settings.language)} ${progress.lastReadAyahNumber} $translatedOf 110 · ${(progress.lastReadProgress * 100).toInt()}% $translatedCompleted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(imageVector = Lucide.ChevronRight, contentDescription = Translator.translate("resume", settings.language), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            SectionHeader(title = Translator.translate("daily_azkar", settings.language), actionText = Translator.translate("see_all", settings.language), onActionClick = { navigateToTab(Routes.AZKAR) })
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("azkar_rows_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                azkarCats.take(6).chunked(3).forEach { rowCats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCats.forEach { cat ->
                            val accentColor = when (cat.id) {
                                "morning" -> WarmPeach
                                "evening" -> MintTeal
                                else -> SageGreen
                            }
                            val isDark = settings.darkTheme || androidx.compose.foundation.isSystemInDarkTheme()
                            val iconTint = when (cat.id) {
                                "morning" -> if (isDark) WarmPeach else DarkWarmPeachText
                                "evening" -> if (isDark) MintTeal else DarkTealText
                                else -> if (isDark) SageGreen else DarkGreenText
                            }
                            val icon = when (cat.iconName) {
                                "sunrise" -> Lucide.Sunrise
                                "sunset" -> Lucide.Sunset
                                else -> Lucide.Moon
                            }
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable {
                                        azkarViewModel.selectCategory(cat)
                                        navController.navigate(Routes.AZKAR_FLOW)
                                    }
                                    .testTag("azkar_row_${cat.id}"),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val boxBg = if (isDark) accentColor.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.15f)
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(boxBg, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = cat.title,
                                            tint = iconTint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (settings.language == "Arabic") cat.arabicTitle else cat.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val progressFraction = if (cat.totalCount > 0) cat.doneCount.toFloat() / cat.totalCount.toFloat() else 0f
                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = iconTint,
                                        trackColor = iconTint.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                        repeat(3 - rowCats.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = Translator.translate("tools_features", settings.language))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val isDark = settings.darkTheme || androidx.compose.foundation.isSystemInDarkTheme()
                HomeWidget(title = Translator.translate("digital_tasbih", settings.language), subtitle = Translator.translate("digital_tasbih_subtitle", settings.language), icon = Lucide.Fingerprint, iconBackground = MintTeal, iconTint = if (isDark) MintTeal else DarkTealText, onClick = { navController.navigate(Routes.TASBIH) })
                HomeWidget(title = Translator.translate("qibla_direction", settings.language), subtitle = Translator.translate("qibla_subtitle", settings.language), icon = Lucide.Compass, iconBackground = WarmPeach, iconTint = if (isDark) WarmPeach else DarkWarmPeachText, onClick = { navController.navigate(Routes.QIBLA) })
                HomeWidget(title = Translator.translate("prayer_times", settings.language), subtitle = Translator.translate("full_day_schedule", settings.language), icon = Lucide.Calendar, iconBackground = SageGreen, iconTint = if (isDark) SageGreen else DarkGreenText, onClick = { navigateToTab(Routes.PRAYER) })
                HomeWidget(
                    title = Translator.translate("mosque_finder", settings.language),
                    subtitle = Translator.translate("mosque_subtitle", settings.language),
                    icon = Lucide.MapPin,
                    iconBackground = MintTeal,
                    iconTint = if (isDark) MintTeal else DarkTealText,
                    onClick = {
                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=mosque"))
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=mosque"))
                            try {
                                context.startActivity(webIntent)
                            } catch (ex: Exception) {
                                Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}
