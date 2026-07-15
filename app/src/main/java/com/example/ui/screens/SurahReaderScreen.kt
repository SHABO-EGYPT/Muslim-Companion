package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.domain.model.Surah
import com.example.navigation.Routes
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ReaderLoadState
import com.example.viewmodel.SurahReaderViewModel

@Composable
fun SurahReaderScreen(viewModel: SurahReaderViewModel, navController: NavHostController) {
    val surah by viewModel.currentSurah.collectAsState()
    val progress by viewModel.userProgress.collectAsState()
    val quranSettings by viewModel.quranSettings.collectAsState()
    val settings by viewModel.quranSettings.collectAsState() // for translator
    
    val activeSurah = surah ?: Surah(
        number = progress.lastReadSurahNumber.takeIf { it > 0 } ?: 1,
        name = progress.lastReadSurahName.ifBlank { "Al-Fatiha" },
        meaning = "",
        ayahsCount = 0,
        arabicName = "",
        isMakki = true
    )
    val bookmarks by viewModel.bookmarks.collectAsState()
    val isBookmarked = bookmarks.any { it.surahNumber == activeSurah.number }

    val formattedEnglishName = remember(activeSurah.name) {
        val base = activeSurah.name.trim()
        if (base.startsWith("Surah")) base else "Surah $base"
    }

    val formattedArabicName = remember(activeSurah.arabicName) {
        val base = activeSurah.arabicName.trim()
        if (base.isEmpty()) "" else {
            if (base.startsWith("سورة")) base else "سورة $base"
        }
    }

    val headerTitle = if (settings.language == "Arabic") formattedArabicName else formattedEnglishName

    val loadState by viewModel.readerLoadState.collectAsState()
    val currentAyahNumber by viewModel.currentPlayingAyah.collectAsState()
    val reciters = viewModel.recitersList


    val isPlaying by viewModel.isPlaying.collectAsState()

    var showAyahMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedAyahForMenu by remember { mutableStateOf<com.example.domain.model.Ayah?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity
    DisposableEffect(quranSettings.quranKeepScreenOn) {
        if (quranSettings.quranKeepScreenOn) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    LaunchedEffect(surah) {
        if (surah == null) {
            val startAyah = progress.lastReadAyahNumber.takeIf { it > 0 } ?: 1
            viewModel.loadSurah(activeSurah.number, startAyah = startAyah)
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("surah_reader_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                title = headerTitle,
                subtitle = "${activeSurah.meaning} · ${activeSurah.ayahsCount} ${Translator.translate("ayahs", settings.language)}",
                onBack = { navController.popBackStack() },
                rightContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.toggleBookmark(activeSurah) }, modifier = Modifier.testTag("bookmark_action_button")) {
                            Icon(imageVector = Lucide.Bookmark, contentDescription = "Bookmark", tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )

            when (val state = loadState) {
                is ReaderLoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
                    }
                }
                is ReaderLoadState.Error -> {
                    Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(text = state.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.testTag("error_message"))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.setSurah(activeSurah) }, modifier = Modifier.testTag("retry_button")) { Text(Translator.translate("retry", settings.language)) }
                    }
                }
                is ReaderLoadState.Success -> {
                    val ayahs by viewModel.ayahs.collectAsState()
                    val listState = rememberLazyListState()
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val topPadding = with(density) { 16.dp.toPx().toInt() }
                    val quranFontFamily = remember(quranSettings.quranFont) { getQuranFontFamily(quranSettings.quranFont) }

                    var hasScrolledToInitial by remember { mutableStateOf(false) }
                    LaunchedEffect(ayahs) {
                        if (ayahs.isNotEmpty() && !hasScrolledToInitial) {
                            val startAyah = currentAyahNumber ?: progress.lastReadAyahNumber
                            val index = ayahs.indexOfFirst { it.number == startAyah }
                            if (index != -1) {
                                listState.scrollToItem(index + 1, -topPadding)
                                hasScrolledToInitial = true
                            }
                        }
                    }

                    LaunchedEffect(currentAyahNumber, isPlaying) {
                        if (isPlaying && currentAyahNumber != null) {
                            val index = ayahs.indexOfFirst { it.number == currentAyahNumber }
                            if (index != -1) {
                                // Scroll to item and use negative offset to move it to the top of the viewport
                                listState.animateScrollToItem(index + 1, -topPadding)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).testTag("ayahs_scroller"),
                        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 300.dp),
                        state = listState,
                        userScrollEnabled = !isPlaying
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).drawBehind {
                                    val center = Offset(this.size.width * 0.15f, this.size.height * 0.85f)
                                    drawCircle(color = Color.White.copy(alpha = 0.05f), radius = this.size.width * 0.3f, center = center)
                                    drawCircle(color = Color.White.copy(alpha = 0.03f), radius = this.size.width * 0.45f, center = center)
                                },
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(colors = listOf(PrimaryTeal, Secondary))).padding(vertical = 24.dp, horizontal = 20.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text(text = formattedEnglishName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp), color = Color.White, textAlign = TextAlign.Center)
                                        Text(text = "${activeSurah.meaning} · ${activeSurah.ayahsCount} ${Translator.translate("ayahs", settings.language)}", style = MaterialTheme.typography.bodyMedium, color = MintTeal, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(modifier = Modifier.width(100.dp), color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(text = formattedArabicName, style = MaterialTheme.typography.displayLarge.copy(fontFamily = ArabicSerifFamily, fontSize = 36.sp), color = Color.White, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                            if (activeSurah.number != 9 && activeSurah.number != 1) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))))
                                    Text(text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = ArabicSerifFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp), textAlign = TextAlign.Center)
                                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), Color.Transparent))))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        itemsIndexed(ayahs) { index, ayah ->
                            val isPlayingThis = currentAyahNumber == ayah.number
                            val opacity = if (isPlaying && !isPlayingThis) 0.4f else 1.0f
                            
                            val cleanedText = if (ayah.number == 1 && activeSurah.number != 1 && activeSurah.number != 9) {
                                if (ayah.arabicText.contains("بِسْمِ")) {
                                    val words = ayah.arabicText.split(" ")
                                    if (words.size > 4 && words[0].contains("بِسْمِ")) {
                                        words.drop(4).joinToString(" ").trim()
                                    } else {
                                        ayah.arabicText
                                    }
                                } else {
                                    ayah.arabicText
                                }
                            } else {
                                ayah.arabicText
                            }

                            val cleanedTranslation = remember(ayah.translation) {
                                if (ayah.translation.isBlank()) "" else {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                        android.text.Html.fromHtml(ayah.translation, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                                    } else {
                                        @Suppress("DEPRECATION")
                                        android.text.Html.fromHtml(ayah.translation).toString().trim()
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .alpha(opacity)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                viewModel.updateProgress(activeSurah.number, activeSurah.name, activeSurah.arabicName, ayah.number, ayah.number.toFloat() / activeSurah.ayahsCount)
                                                viewModel.playAyah(ayah)
                                            },
                                            onLongPress = {
                                                selectedAyahForMenu = ayah
                                                showAyahMenu = true
                                            }
                                        )
                                    },
                                horizontalAlignment = Alignment.Start
                            ) {
                                if (isPlayingThis) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = Translator.translate("playing", settings.language),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Text(
                                    text = buildAnnotatedString {
                                        append(cleanedText)
                                        append("  ")
                                        appendInlineContent("ayah_number")
                                    },
                                    inlineContent = mapOf(
                                        "ayah_number" to InlineTextContent(
                                            Placeholder(
                                                width = 2.em,
                                                height = 2.em,
                                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                            )
                                        ) {
                                            RubElHizbIcon(
                                                number = ayah.number,
                                                modifier = Modifier.fillMaxSize(),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                textColor = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    ),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontFamily = quranFontFamily,
                                        fontSize = quranSettings.quranTextSize.sp,
                                        lineHeight = 1.8.em,
                                        textDirection = TextDirection.Rtl,
                                        textAlign = TextAlign.Center,
                                        color = if (isPlayingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                if (quranSettings.quranShowTranslation && cleanedTranslation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = cleanedTranslation,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = (quranSettings.quranTextSize * 0.62f).sp,
                                            lineHeight = 1.5.em,
                                            textAlign = TextAlign.Left,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                    )
                                }
                                
                                if (index < ayahs.size - 1) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        if (currentAyahNumber != null) {
            FloatingActionButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 24.dp).testTag("floating_play_pause_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(imageVector = if (isPlaying) Lucide.Pause else Lucide.Play, contentDescription = if (isPlaying) "Pause" else "Play", modifier = Modifier.size(24.dp))
            }
        }
    }

    if (showAyahMenu && selectedAyahForMenu != null) {
        DropdownMenu(
            expanded = showAyahMenu,
            onDismissRequest = { showAyahMenu = false },
            offset = androidx.compose.ui.unit.DpOffset(x = menuOffset.x.dp / 3, y = 0.dp) // Rough estimation, better handled by Box
        ) {
            DropdownMenuItem(
                text = { Text(Translator.translate("play_from_here", settings.language)) },
                onClick = {
                    showAyahMenu = false
                    selectedAyahForMenu?.let {
                        viewModel.updateProgress(activeSurah.number, activeSurah.name, activeSurah.arabicName, it.number, it.number.toFloat() / activeSurah.ayahsCount)
                        viewModel.playAyah(it)
                    }
                },
                leadingIcon = { Icon(Lucide.Play, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }
    }


}
