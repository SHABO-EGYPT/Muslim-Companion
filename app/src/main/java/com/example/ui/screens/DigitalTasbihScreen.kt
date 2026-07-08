package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TasbihViewModel

@Composable
fun DigitalTasbihScreen(viewModel: TasbihViewModel, navController: NavHostController) {
    val count by viewModel.count.collectAsState()
    val target by viewModel.target.collectAsState()
    val dhikr by viewModel.currentDhikr.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val phrases by viewModel.phrases.collectAsState()
    val activeIndex by viewModel.activeIndex.collectAsState()

    val progressFraction = (if (target > 0) count.toFloat() / target.toFloat() else 0f).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxSize().testTag("tasbih_screen")) {
        AppHeader(title = Translator.translate("digital_tasbih", settings.language), onBack = { navController.popBackStack() })

        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            
            // Phrase Selector List
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("tasbih_phrases_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                itemsIndexed(phrases) { index, item ->
                    val isSelected = index == activeIndex
                    val containerColor = if (isSelected) MintTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    val contentColor = if (isSelected) DarkTealText else MaterialTheme.colorScheme.onSurfaceVariant
                    val borderStroke = if (isSelected) BorderStroke(1.5.dp, DarkTealText) else null

                    Card(
                        modifier = Modifier
                            .clickable { viewModel.selectPhrase(index) }
                            .testTag("tasbih_phrase_item_$index"),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        shape = RoundedCornerShape(100.dp),
                        border = borderStroke
                    ) {
                        Text(
                            text = item.arabicText,
                            fontFamily = ArabicSerifFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Text(text = dhikr, style = MaterialTheme.typography.displayLarge.copy(fontFamily = ArabicSerifFamily, fontSize = 34.sp), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier.size(220.dp).clip(CircleShape).background(MintTeal).clickable { viewModel.increment() }.border(8.dp, MaterialTheme.colorScheme.background, CircleShape).testTag("tasbih_tap_target"),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(progress = { progressFraction }, modifier = Modifier.fillMaxSize().padding(4.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 8.dp, trackColor = Color.Transparent)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$count", style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold), color = DarkTealText)
                    Text(text = "${Translator.translate("of", settings.language)} $target", style = MaterialTheme.typography.bodyMedium, color = DarkTealText.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.reset() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.testTag("tasbih_reset_button")
                ) { Text(Translator.translate("reset", settings.language), color = MaterialTheme.colorScheme.onSurfaceVariant) }

                Button(
                    onClick = {
                        viewModel.reset()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.testTag("tasbih_done_button")
                ) {
                    Icon(imageVector = Lucide.Check, contentDescription = "Done"); Spacer(modifier = Modifier.width(4.dp)); Text(Translator.translate("mark_done", settings.language), color = Color.White)
                }
            }
        }
    }
}
