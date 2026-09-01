package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.domain.model.Surah
import com.example.navigation.Routes
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.QuranViewModel
import com.example.viewmodel.SurahReaderViewModel
import com.example.viewmodel.SurahsLoadState

import com.example.data.quran.QuranStructureData

@Composable
fun QuranListScreen(viewModel: QuranViewModel, readerViewModel: SurahReaderViewModel, navController: NavHostController) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val surahs by viewModel.filteredSurahs.collectAsStateWithLifecycle()
    val quranSettings by readerViewModel.quranSettings.collectAsStateWithLifecycle()
    val progress by readerViewModel.userProgress.collectAsStateWithLifecycle()
    val settings by readerViewModel.quranSettings.collectAsStateWithLifecycle() // Using quranSettings as settings
    val loadState by viewModel.surahsLoadState.collectAsStateWithLifecycle()
    var showQuranSettings by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Surahs, 1 = Juz'

    val filteredJuz = remember(query) {
        if (query.isBlank()) {
            QuranStructureData.ALL_JUZ
        } else {
            val q = query.trim()
            QuranStructureData.ALL_JUZ.filter {
                it.nameArabic.contains(q, ignoreCase = true) ||
                it.nameEnglish.contains(q, ignoreCase = true) ||
                it.number.toString() == q ||
                it.startSurahNameArabic.contains(q, ignoreCase = true) ||
                it.startSurahNameEnglish.contains(q, ignoreCase = true) ||
                it.endSurahNameArabic.contains(q, ignoreCase = true) ||
                it.endSurahNameEnglish.contains(q, ignoreCase = true)
            }
        }
    }

    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var isTopVisible by remember { mutableStateOf(true) }
    var lastScrollOffset by remember { mutableStateOf(0) }
    var accumulatedUpScroll by remember { mutableStateOf(0f) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset }
            .collect { totalOffset ->
                val delta = totalOffset - lastScrollOffset

                if (delta > 0) { // Scrolling down
                    if (isTopVisible) isTopVisible = false
                    accumulatedUpScroll = 0f
                } else if (delta < 0) { // Scrolling up
                    accumulatedUpScroll += kotlin.math.abs(delta).toFloat()
                    if (accumulatedUpScroll > with(density) { 100.dp.toPx() } || totalOffset == 0) {
                        isTopVisible = true
                    }
                }

                lastScrollOffset = totalOffset
            }
    }

    Column(
        modifier = Modifier.fillMaxSize().testTag("quran_list_screen")
    ) {
        AppHeader(
            title = Translator.translate("quran", settings.language),
            rightContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { 
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) {
                                viewModel.onSearchQueryChanged("")
                            }
                        },
                        modifier = Modifier.testTag("quran_search_toggle_button")
                    ) {
                        Icon(
                            imageVector = Lucide.Search,
                            contentDescription = Translator.translate("search", settings.language),
                            tint = if (isSearchActive || query.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showQuranSettings = !showQuranSettings },
                        modifier = Modifier.testTag("quran_settings_toggle_button")
                    ) {
                        Icon(
                            imageVector = Lucide.Settings,
                            contentDescription = Translator.translate("settings", settings.language),
                            tint = if (showQuranSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        AnimatedVisibility(
            visible = isSearchActive,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text(Translator.translate("search_surah", settings.language), style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(imageVector = Lucide.Search, contentDescription = Translator.translate("search", settings.language)) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Lucide.X, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).testTag("quran_search_field"),
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
        }

        AnimatedVisibility(
            visible = showQuranSettings,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).testTag("quran_embedded_settings_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = Translator.translate("quran_reader_settings", settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        IconButton(onClick = { showQuranSettings = false }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Lucide.X, contentDescription = Translator.translate("cancel", settings.language), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = Translator.translate("keep_screen_on", settings.language), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = Translator.translate("keep_screen_on_desc", settings.language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = quranSettings.quranKeepScreenOn, onCheckedChange = { readerViewModel.updateQuranKeepScreenOn(it) })
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${Translator.translate("text_size", settings.language)} (${quranSettings.quranTextSize.toInt()} sp)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                        Slider(
                            value = quranSettings.quranTextSize.coerceIn(14f, 32f),
                            onValueChange = { readerViewModel.updateQuranTextSize(it) },
                            valueRange = 14f..32f,
                            colors = SliderDefaults.colors(thumbColor = DarkTealText, activeTrackColor = MintTeal)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Column {
                        Text(text = Translator.translate("quran_font_style", settings.language), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(modifier = Modifier.height(6.dp))
                        val fonts = listOf("Uthmanic Hafs", "Amiri Quran", "Scheherazade New", "Noto Naskh Arabic")
                        val fontFamilies = remember { fonts.associateWith { getQuranFontFamily(it) } }
                        var fontExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .clickable { fontExpanded = true }.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = quranSettings.quranFont, style = MaterialTheme.typography.bodyMedium)
                                Icon(imageVector = if (fontExpanded) Lucide.ChevronUp else Lucide.ChevronDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = fontExpanded,
                                onDismissRequest = { fontExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f).background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            ) {
                                fonts.forEach { fontName ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = fontName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (quranSettings.quranFont == fontName) FontWeight.Bold else FontWeight.Normal))
                                                Text(text = "القرآن", style = TextStyle(fontFamily = fontFamilies[fontName] ?: androidx.compose.ui.text.font.FontFamily.Default, fontSize = 14.sp, textDirection = TextDirection.Rtl))
                                            }
                                        },
                                        onClick = { readerViewModel.updateQuranFont(fontName); fontExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isTopVisible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp).clickable {
                        val lastSurah = surahs.find { it.number == progress.lastReadSurahNumber } 
                            ?: Surah(progress.lastReadSurahNumber, progress.lastReadSurahName, "", 0, "", true)
                        readerViewModel.setSurah(lastSurah, progress.lastReadAyahNumber); navController.navigate(Routes.SURAH_READER)
                    }.drawBehind {
                        val center = Offset(this.size.width * 0.88f, this.size.height * 0.5f)
                        drawCircle(color = Color.White.copy(alpha = 0.08f), radius = this.size.width * 0.35f, center = center)
                        drawCircle(color = Color.White.copy(alpha = 0.04f), radius = this.size.width * 0.5f, center = center)
                        drawCircle(color = Color.White.copy(alpha = 0.2f), radius = 8f, center = Offset(this.size.width * 0.78f, this.size.height * 0.22f))
                        drawCircle(color = Color.White.copy(alpha = 0.15f), radius = 6f, center = Offset(this.size.width * 0.92f, this.size.height * 0.18f))
                    }.testTag("quran_last_read_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(colors = listOf(PrimaryTeal, Secondary))).padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Lucide.BookOpen, contentDescription = Translator.translate("last_read", settings.language), tint = MintTeal, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = Translator.translate("last_read", settings.language).uppercase(), style = MaterialTheme.typography.titleMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MintTeal)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = progress.lastReadSurahArabicName.ifBlank { Translator.translate("fatiha_arabic", settings.language) }, 
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = ArabicSerifFamily), 
                                        color = Color.White
                                    )
                                    Text(text = "${Translator.translate("verse", settings.language)}: ${progress.lastReadAyahNumber}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                }
                            }
                            Button(
                                onClick = {
                                    val lastSurah = surahs.find { it.number == progress.lastReadSurahNumber } 
                                        ?: Surah(progress.lastReadSurahNumber, progress.lastReadSurahName, "", 0, "", true)
                                    readerViewModel.setSurah(lastSurah, progress.lastReadAyahNumber); navController.navigate(Routes.SURAH_READER)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MintTeal, contentColor = DarkTealText),
                                shape = RoundedCornerShape(100.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("resume_button")
                            ) {
                                Text(text = Translator.translate("resume", settings.language), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Tab Switcher: Surahs vs Juz'
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            thickness = 1.dp
                        )
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = Translator.translate("surahs_tab", settings.language),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = Translator.translate("juz_tab", settings.language),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (selectedTab == 0) {
            // ── Surahs Tab ──────────────────────────────────────────────────────────
            when (val state = loadState) {
                is SurahsLoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("surahs_loading_indicator"))
                    }
                }
                is SurahsLoadState.Error -> {
                    Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(text = state.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.testTag("surahs_error_message"))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchSurahs() }, modifier = Modifier.testTag("surahs_retry_button")) { Text(Translator.translate("retry", settings.language)) }
                    }
                }
                is SurahsLoadState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).testTag("surah_list_container"),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        state = listState
                    ) {
                        items(surahs) { surah ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { readerViewModel.setSurah(surah); navController.navigate(Routes.SURAH_READER) }.testTag("surah_row_${surah.number}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isDark = settings.darkTheme
                                    RubElHizbIcon(
                                        number = surah.number, 
                                        color = if (isDark) MintTeal.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        textColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = surah.arabicName, 
                                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = ArabicSerifFamily, fontSize = 24.sp), 
                                            color = MaterialTheme.colorScheme.onSurface, 
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = surah.name, 
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                                            color = MaterialTheme.colorScheme.onSurface, 
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "${surah.meaning} · ${surah.ayahsCount} ${Translator.translate("ayahs", settings.language)}",
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(54.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ── Juz' Tab ────────────────────────────────────────────────────────────
            val isArabic = settings.language == "Arabic"
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("juz_list_container"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                state = listState
            ) {
                items(filteredJuz) { juz ->
                    val isDark = settings.darkTheme
                    val startSurahName = if (isArabic) "سورة ${juz.startSurahNameArabic}" else "Surah ${juz.startSurahNameEnglish}"
                    val endSurahName = if (isArabic) "سورة ${juz.endSurahNameArabic}" else "Surah ${juz.endSurahNameEnglish}"
                    val juzTitle = if (isArabic) "الجزء ${QuranStructureData.toArabicOrdinal(juz.number)}" else "Juz ${juz.number}"
                    val rangeText = if (isArabic) {
                        "$startSurahName (${juz.startAyahNumber}) ← $endSurahName (${juz.endAyahNumber})"
                    } else {
                        "$startSurahName (Ayah ${juz.startAyahNumber}) → $endSurahName (Ayah ${juz.endAyahNumber})"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                val targetSurah = surahs.find { it.number == juz.startSurahNumber }
                                    ?: Surah(
                                        number = juz.startSurahNumber,
                                        name = juz.startSurahNameEnglish,
                                        meaning = "",
                                        ayahsCount = 0,
                                        arabicName = juz.startSurahNameArabic,
                                        isMakki = true
                                    )
                                readerViewModel.setSurah(targetSurah, startAyah = juz.startAyahNumber)
                                navController.navigate(Routes.SURAH_READER)
                            }
                            .testTag("juz_row_${juz.number}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RubElHizbIcon(
                                number = juz.number,
                                color = if (isDark) MintTeal.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                textColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = juzTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = if (isArabic) ArabicSerifFamily else null,
                                        fontSize = if (isArabic) 22.sp else 18.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = rangeText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.width(54.dp))
                        }
                    }
                }
            }
        }
    }
}
