package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.viewmodel.AzkarViewModel

@Composable
fun AzkarReadingFlowScreen(viewModel: AzkarViewModel, navController: NavHostController) {
    val category by viewModel.selectedCategory.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val activeCat = category ?: AzkarCategory("morning", "Morning Azkar", "أذكار الصباح", 18, 12, "sunrise", 0xFFFFDEA0)

    val stepIndex by viewModel.flowIndex.collectAsState()
    val dhikrs by viewModel.currentAzkarList.collectAsState()

    if (dhikrs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val activeDhikr = dhikrs.getOrElse(stepIndex % dhikrs.size) { dhikrs[0] }
    val progressFraction = (stepIndex + 1).toFloat() / dhikrs.size.toFloat()

    Column(modifier = Modifier.fillMaxSize().testTag("azkar_flow_screen")) {
        AppHeader(title = if (settings.language == "Arabic") activeCat.arabicTitle else activeCat.title, subtitle = "${stepIndex + 1} ${Translator.translate("of", settings.language)} ${dhikrs.size}", onBack = { navController.popBackStack() })

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(6.dp).clip(CircleShape).testTag("azkar_flow_progress_bar"),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp).testTag("azkar_dhikr_card"),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = activeDhikr.arabicText, style = MaterialTheme.typography.displayLarge.copy(fontFamily = ArabicSerifFamily, fontSize = 28.sp, lineHeight = 44.sp), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                if (settings.language != "Arabic") {
                    Text(text = activeDhikr.englishTranslation, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
                if (activeDhikr.repeatTarget > 1) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.background(WarmPeach, RoundedCornerShape(100.dp)).padding(horizontal = 16.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(text = "×${activeDhikr.repeatTarget}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DarkWarmPeachText)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { viewModel.prevStep() },
                modifier = Modifier.weight(1f).height(48.dp).testTag("azkar_flow_back_button"),
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(imageVector = Lucide.ChevronLeft, contentDescription = Translator.translate("back", settings.language)); Spacer(modifier = Modifier.width(4.dp)); Text(Translator.translate("back", settings.language))
            }

            Button(
                onClick = {
                    if (stepIndex >= dhikrs.size - 1) {
                        viewModel.completeAzkarFlow(activeCat.id, dhikrs.size); navController.popBackStack()
                    } else { viewModel.nextStep() }
                },
                modifier = Modifier.weight(2f).height(48.dp).testTag("azkar_flow_next_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(if (stepIndex >= dhikrs.size - 1) Translator.translate("completed", settings.language) else Translator.translate("next", settings.language))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = if (stepIndex >= dhikrs.size - 1) Lucide.Check else Lucide.ChevronRight, contentDescription = null)
            }
        }
    }
}
