package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.navigation.Routes
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, navController: NavHostController) {
    val progress by viewModel.userProgress.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val badges by viewModel.badges.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember(progress.username) { mutableStateOf(progress.username) }
    var editLocation by remember(progress.location) { mutableStateOf(progress.location) }

    val earnedBadgesCount = badges.count { it.earned }
    val stats = listOf(
        Triple(Translator.translate("streak", settings.language), "${progress.streak} ${Translator.translate("days", settings.language)}", Lucide.Flame),
        Triple(Translator.translate("prayer_score", settings.language), "${progress.prayerScore}", Lucide.Star),
        Triple(Translator.translate("your_badges", settings.language), "$earnedBadgesCount ${Translator.translate("completed", settings.language)}", Lucide.Award)
    )

    val menuItems = listOf(
        Triple(Translator.translate("reading_history", settings.language), Lucide.BookOpen, ""),
        Triple(Translator.translate("bookmarked", settings.language), Lucide.Bookmark, Routes.QURAN),
        Triple(Translator.translate("notifications", settings.language), Lucide.Bell, Routes.NOTIFICATIONS),
        Triple(Translator.translate("settings", settings.language), Lucide.Settings, Routes.SETTINGS)
    )

    Column(modifier = Modifier.fillMaxSize().testTag("profile_screen")) {
        AppHeader(title = Translator.translate("profile", settings.language))

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(68.dp).background(MintTeal, CircleShape), contentAlignment = Alignment.Center) {
                        Text(text = progress.username.take(1).uppercase(), style = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = DarkTealText)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = progress.username, style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                        Text(text = progress.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).testTag("profile_edit_button")
                    ) {
                        Icon(imageVector = Lucide.SquarePen, contentDescription = Translator.translate("edit_profile", settings.language), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    stats.forEach { (label, value, icon) ->
                        Card(modifier = Modifier.weight(1f).testTag("profile_stat_${label.lowercase().replace(" ", "_")}"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = MaterialTheme.shapes.small) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = MaterialTheme.colorScheme.onBackground)
                                Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(title = Translator.translate("achievements", settings.language))
                LazyRow(modifier = Modifier.fillMaxWidth().testTag("badges_row"), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(badges) { index, item ->
                        val translatedBadgeTitle = when(item.title) {
                            "7-day streak" -> Translator.translate("streak_7_days", settings.language)
                            "First Juz" -> Translator.translate("first_juz", settings.language)
                            "Night Owl" -> Translator.translate("night_owl", settings.language)
                            "Early Riser" -> Translator.translate("early_riser", settings.language)
                            else -> item.title
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp).testTag("badge_item_${item.title.lowercase().replace(" ", "_")}")) {
                            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(if (item.earned) WarmPeach else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Lucide.Award, contentDescription = item.title, tint = if (item.earned) DarkWarmPeachText else MaterialTheme.colorScheme.outline)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = translatedBadgeTitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionHeader(title = Translator.translate("menu", settings.language))
                Column(modifier = Modifier.fillMaxWidth().testTag("profile_menu_container")) {
                    menuItems.forEach { (label, icon, route) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { if (route.isNotEmpty()) navController.navigate(route) }.padding(vertical = 14.dp).testTag("profile_menu_row_${label.lowercase().replace(" ", "_")}"),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(19.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Icon(imageVector = Lucide.ChevronRight, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    TextField(value = editLocation, onValueChange = { editLocation = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateProfile(editName, editLocation); showEditDialog = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }
}
