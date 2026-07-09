package com.example.ui.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SurahReaderViewModel

@Composable
fun QuranSettingsScreen(viewModel: SurahReaderViewModel, navController: NavHostController) {
    val quranSettings by viewModel.quranSettings.collectAsState()
    val settings by viewModel.quranSettings.collectAsState() // settings variable for translation
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(quranSettings.quranKeepScreenOn) {
        if (quranSettings.quranKeepScreenOn) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    Column(modifier = Modifier.fillMaxSize().testTag("quran_settings_screen").padding(bottom = 0.dp).verticalScroll(rememberScrollState())) {
        AppHeader(title = Translator.translate("quran_settings", settings.language), subtitle = Translator.translate("customize_reading_desc", settings.language), onBack = { navController.popBackStack() })

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("quran_preview_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = Translator.translate("reader_preview", settings.language), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        style = TextStyle(fontFamily = getQuranFontFamily(quranSettings.quranFont), fontSize = quranSettings.quranTextSize.sp, lineHeight = 1.8.em, textDirection = TextDirection.Rtl, textAlign = TextAlign.Center),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (quranSettings.quranShowTranslation) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "In the name of God, the Lord of Mercy, the Giver of Mercy!",
                            style = TextStyle(fontSize = (quranSettings.quranTextSize * 0.62f).sp, lineHeight = 1.5.em, textAlign = TextAlign.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { viewModel.updateQuranShowTranslation(!quranSettings.quranShowTranslation) }.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = Translator.translate("show_translation", settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = Translator.translate("show_translation_desc", settings.language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = quranSettings.quranShowTranslation, onCheckedChange = { viewModel.updateQuranShowTranslation(it) }, modifier = Modifier.testTag("show_translation_switch"))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { viewModel.updateQuranKeepScreenOn(!quranSettings.quranKeepScreenOn) }.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = Translator.translate("keep_screen_on", settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = Translator.translate("keep_screen_on_desc", settings.language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = quranSettings.quranKeepScreenOn, onCheckedChange = { viewModel.updateQuranKeepScreenOn(it) }, modifier = Modifier.testTag("keep_screen_on_switch"))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(text = Translator.translate("arabic_text_size", settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        Text(text = Translator.translate("adjust_script_size_desc", settings.language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(text = "${quranSettings.quranTextSize.toInt()} sp", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DarkTealText)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = quranSettings.quranTextSize.coerceIn(18f, 44f),
                    onValueChange = { viewModel.updateQuranTextSize(it) },
                    valueRange = 18f..44f,
                    colors = SliderDefaults.colors(thumbColor = DarkTealText, activeTrackColor = MintTeal, inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.testTag("quran_text_size_slider")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Column {
                Text(text = Translator.translate("quran_font_style", settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = Translator.translate("choose_preferred_font_desc", settings.language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                val fonts = listOf("Uthmanic Hafs", "Amiri Quran", "Scheherazade New", "Noto Naskh Arabic")
                var expanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable { expanded = true }.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = quranSettings.quranFont, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Icon(imageVector = if (expanded) Lucide.ChevronUp else Lucide.ChevronDown, contentDescription = "Dropdown icon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f).background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).testTag("quran_font_dropdown")
                    ) {
                        fonts.forEach { fontName ->
                            DropdownMenuItem(
                                text = {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = fontName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (quranSettings.quranFont == fontName) FontWeight.Bold else FontWeight.Normal), color = if (quranSettings.quranFont == fontName) DarkTealText else MaterialTheme.colorScheme.onSurface)
                                        Text(text = "القرآن", style = TextStyle(fontFamily = getQuranFontFamily(fontName), fontSize = 16.sp, textDirection = TextDirection.Rtl), color = if (quranSettings.quranFont == fontName) DarkTealText else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = { viewModel.updateQuranFont(fontName); expanded = false },
                                modifier = Modifier.testTag("font_option_$fontName")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Column {
                Text(
                    text = Translator.translate("offline_recitation", settings.language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Translator.translate("download_recitation_desc", settings.language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                val currentReciterId = quranSettings.quranReciter
                val reciterKey = when(currentReciterId) {
                    "ar.alafasy" -> "mishary_alafasy"
                    "ar.abdulbasitmurattal" -> "abdul_basit"
                    "ar.husary" -> "al_husary"
                    "ar.minshawimujawwad" -> "minshawi"
                    else -> currentReciterId
                }

                val workManager = remember { androidx.work.WorkManager.getInstance(context) }
                val workInfos by workManager.getWorkInfosForUniqueWorkLiveData("recitation_download_$currentReciterId")
                    .observeAsState(initial = emptyList())
                val workInfo = workInfos.firstOrNull()

                val audioManager = remember { com.example.data.quran.QuranAudioManager(context) }
                var downloadedCount by remember(currentReciterId, workInfo) {
                    mutableStateOf(
                        (1..114).count { audioManager.isDownloaded(currentReciterId, it) }
                    )
                }

                val isDownloading = workInfo != null && workInfo.state == androidx.work.WorkInfo.State.RUNNING
                val progress = if (isDownloading) {
                    workInfo.progress.getInt("progress", 0)
                } else 0

                val statusText = when {
                    isDownloading -> "${Translator.translate("downloading", settings.language)} ($progress%)"
                    downloadedCount == 114 -> Translator.translate("downloaded", settings.language) + " (114/114) ✓"
                    downloadedCount > 0 -> "${Translator.translate("partial_download", settings.language)} ($downloadedCount/114)"
                    else -> Translator.translate("not_downloaded", settings.language)
                }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("offline_download_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Translator.translate(reciterKey, settings.language),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (downloadedCount == 114) MintTeal else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isDownloading) {
                                TextButton(
                                    onClick = { viewModel.cancelRecitationDownload(currentReciterId, context) },
                                    modifier = Modifier.testTag("cancel_download_btn")
                                ) {
                                    Text(
                                        text = Translator.translate("cancel", settings.language),
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (downloadedCount < 114) {
                                Button(
                                    onClick = { viewModel.startRecitationDownload(currentReciterId, context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkTealText),
                                    modifier = Modifier.testTag("start_download_btn")
                                ) {
                                    Text(text = Translator.translate("download", settings.language))
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        // Delete files
                                        (1..114).forEach { sura ->
                                            val file = audioManager.localFile(currentReciterId, sura)
                                            if (file.exists()) file.delete()
                                        }
                                        downloadedCount = 0
                                    },
                                    modifier = Modifier.testTag("delete_download_btn")
                                ) {
                                    Icon(
                                        imageVector = Lucide.Trash2,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        if (isDownloading) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                                color = MintTeal,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> LiveData<T>.observeAsState(initial: T): State<T> {
    val state = remember { mutableStateOf(initial) }
    DisposableEffect(this) {
        val observer = androidx.lifecycle.Observer<T> { value ->
            if (value != null) {
                state.value = value
            }
        }
        observeForever(observer)
        onDispose {
            removeObserver(observer)
        }
    }
    return state
}
