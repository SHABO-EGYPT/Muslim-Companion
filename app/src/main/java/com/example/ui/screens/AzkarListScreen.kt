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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AzkarViewModel

@Composable
fun AzkarListScreen(viewModel: AzkarViewModel, navController: NavHostController) {
    val categories by viewModel.azkarCategories.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

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
                        val tasbihChevron = if (androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl) Lucide.ChevronLeft else Lucide.ChevronRight
                        Icon(imageVector = tasbihChevron, contentDescription = "Open", tint = DarkTealText)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.QURANIC_DUAS) }.testTag("promoted_quranic_duas_card"),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(PrimaryTeal.copy(alpha = 0.15f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Lucide.BookOpen, contentDescription = "Quranic Duas", tint = PrimaryTeal)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = Translator.translate("quranic_duas", settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryTeal)
                                Text(text = Translator.translate("quranic_duas_desc", settings.language), style = MaterialTheme.typography.bodySmall, color = PrimaryTeal.copy(alpha = 0.8f))
                            }
                        }
                        val duasChevron = if (androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl) Lucide.ChevronLeft else Lucide.ChevronRight
                        Icon(imageVector = duasChevron, contentDescription = "Open", tint = PrimaryTeal)
                    }
                }
            }

            items(categories) { cat ->
                val isDark = settings.darkTheme || androidx.compose.foundation.isSystemInDarkTheme()
                val (boxBg, iconTint) = when (cat.iconName) {
                    "sunrise" -> {
                        if (isDark) Color(0xFFE65100).copy(alpha = 0.2f) to Color(0xFFFFB74D)
                        else Color(0xFFFFE0B2) to Color(0xFFE65100)
                    }
                    "sunset" -> {
                        if (isDark) Color(0xFF004D40).copy(alpha = 0.2f) to Color(0xFF4DB6AC)
                        else Color(0xFFE0F2F1) to Color(0xFF004D40)
                    }
                    "moon" -> {
                        if (isDark) Color(0xFF311B92).copy(alpha = 0.2f) to Color(0xFF9575CD)
                        else Color(0xFFEDE7F6) to Color(0xFF311B92)
                    }
                    "star" -> {
                        if (isDark) Color(0xFF01579B).copy(alpha = 0.2f) to Color(0xFF4FC3F7)
                        else Color(0xFFE1F5FE) to Color(0xFF01579B)
                    }
                    else -> {
                        if (isDark) Color(0xFF1B5E20).copy(alpha = 0.2f) to Color(0xFF81C784)
                        else Color(0xFFE8F5E9) to Color(0xFF1B5E20)
                    }
                }
                val icon = when (cat.iconName) {
                    "sunrise" -> Lucide.Sunrise
                    "sunset" -> Lucide.Sunset
                    "moon" -> Lucide.Moon
                    "star" -> Lucide.Star
                    else -> Lucide.Sparkles
                }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectCategory(cat); navController.navigate(Routes.AZKAR_FLOW) }.testTag("azkar_category_card_${cat.id}"),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(boxBg, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(imageVector = icon, contentDescription = cat.title, tint = iconTint)
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
