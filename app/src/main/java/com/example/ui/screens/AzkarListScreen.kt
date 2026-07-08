package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.navigation.Routes
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AzkarViewModel

@Composable
fun AzkarListScreen(viewModel: AzkarViewModel, navController: NavHostController) {
    val categories by viewModel.azkarCategories.collectAsState()
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().testTag("azkar_list_screen")
    ) {
        AppHeader(title = Translator.translate("azkar", settings.language), subtitle = Translator.translate("daily_remembrance", settings.language))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("azkar_categories_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.TASBIH) }.testTag("promoted_tasbih_card"),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MintTeal)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Lucide.Fingerprint, contentDescription = "Tasbih", tint = DarkTealText)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = Translator.translate("digital_tasbih", settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DarkTealText)
                                Text(text = Translator.translate("digital_tasbih_subtitle", settings.language), style = MaterialTheme.typography.bodySmall, color = DarkTealText.copy(alpha = 0.8f))
                            }
                        }
                        Icon(imageVector = Lucide.ChevronRight, contentDescription = "Open", tint = DarkTealText)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.AZKAR_ASSISTANT) }.testTag("azkar_assistant_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Lucide.Sparkles, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = Translator.translate("ask_azkar_assistant", settings.language), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = Translator.translate("find_specific_supplications", settings.language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                        Icon(imageVector = Lucide.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                    }
                }
            }

            items(categories) { cat ->
                val colorHex = Color(cat.colorHex)
                val icon = when (cat.iconName) {
                    "sunrise" -> Lucide.Sunrise; "sunset" -> Lucide.Sunset; "moon" -> Lucide.Moon; else -> Lucide.Star
                }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectCategory(cat); navController.navigate(Routes.AZKAR_FLOW) }.testTag("azkar_category_card_${cat.id}"),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(colorHex, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(imageVector = icon, contentDescription = cat.title, tint = if (settings.darkTheme) DarkTealText.copy(alpha = 0.9f) else DarkTealText)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = if (settings.language == "Arabic") cat.arabicTitle else cat.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text(text = cat.arabicTitle, style = MaterialTheme.typography.bodySmall.copy(fontFamily = ArabicSerifFamily), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        CircularProgressIndicatorM3(percentage = cat.doneCount.toFloat() / cat.totalCount.toFloat(), size = 36, strokeWidth = 3, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }


        }
    }
}
