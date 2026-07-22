package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.domain.model.AzkarCategory
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AzkarViewModel

@Composable
fun AzkarReadingFlowScreen(viewModel: AzkarViewModel, navController: NavHostController) {
    val category by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val activeCat = category ?: AzkarCategory("morning", "Morning Azkar", "أذكار الصباح", 18, 12, "sunrise", 0xFFFFDEA0)

    val stepIndex by viewModel.flowIndex.collectAsStateWithLifecycle()
    val dhikrs by viewModel.currentAzkarList.collectAsStateWithLifecycle()

    if (dhikrs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { dhikrs.size })

    // Sync pager swipe gestures back to ViewModel's flowIndex state
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != stepIndex) {
            viewModel.setFlowIndex(pagerState.currentPage)
        }
    }

    // Sync ViewModel's flowIndex changes (e.g. Next/Back buttons) back to the pager
    LaunchedEffect(stepIndex) {
        if (pagerState.currentPage != stepIndex) {
            pagerState.animateScrollToPage(stepIndex)
        }
    }

    var currentDhikrCount by remember(stepIndex) { mutableStateOf(0) }

    val progressFraction = (stepIndex + 1).toFloat() / dhikrs.size.toFloat()

    Column(modifier = Modifier.fillMaxSize().testTag("azkar_flow_screen")) {
        AppHeader(
            title = if (settings.language == "Arabic") activeCat.arabicTitle else activeCat.title,
            subtitle = "${stepIndex + 1} ${Translator.translate("of", settings.language)} ${dhikrs.size}",
            onBack = { navController.popBackStack() }
        )

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(6.dp).clip(CircleShape).testTag("azkar_flow_progress_bar"),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { page ->
            val dhikrItem = dhikrs[page]
            val isCurrentPage = page == stepIndex
            val displayCount = if (isCurrentPage) currentDhikrCount else 0

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .clickable {
                        if (isCurrentPage) {
                            if (currentDhikrCount < dhikrItem.repeatTarget - 1) {
                                currentDhikrCount++
                            } else {
                                if (stepIndex >= dhikrs.size - 1) {
                                    viewModel.completeAzkarFlow(activeCat.id, dhikrs.size)
                                    navController.popBackStack()
                                } else {
                                    viewModel.nextStep()
                                }
                            }
                        }
                    }
                    .testTag("azkar_dhikr_card_$page"),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(28.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dhikrItem.arabicText,
                        style = MaterialTheme.typography.displayLarge.copy(fontFamily = ArabicSerifFamily, fontSize = 28.sp, lineHeight = 44.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    if (settings.language != "Arabic") {
                        Text(
                            text = dhikrItem.englishTranslation,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .background(WarmPeach, RoundedCornerShape(100.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$displayCount / ${dhikrItem.repeatTarget}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkWarmPeachText
                        )
                    }
                }
            }
        }
    }
}
