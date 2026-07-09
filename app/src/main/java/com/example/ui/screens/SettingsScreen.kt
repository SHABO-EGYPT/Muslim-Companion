package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.navigation.Routes
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSoundTypeDialog by remember { mutableStateOf(false) }
    var showReciterDialog by remember { mutableStateOf(false) }
    var showCalculationDialog by remember { mutableStateOf(false) }
    var showTextSizeDialog by remember { mutableStateOf(false) }

    if (showSoundTypeDialog) {
        AlertDialog(
            onDismissRequest = { showSoundTypeDialog = false },
            title = { Text(text = Translator.translate("sound_type", settings.language), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    listOf("Silent", "Subtle", "Full Adhan", "First Adhan").forEach { type ->
                        val translationKey = when(type) { 
                            "Silent" -> "silent"
                            "Subtle" -> "default_sound"
                            "Full Adhan" -> "full_adhan"
                            "First Adhan" -> "first_adhan"
                            else -> "silent" 
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.updateNotificationSoundType(type); showSoundTypeDialog = false }.padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = settings.notificationSoundType == type, onClick = { viewModel.updateNotificationSoundType(type); showSoundTypeDialog = false })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate(translationKey, settings.language), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showSoundTypeDialog = false }) { Text(Translator.translate("cancel", settings.language)) } }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = Translator.translate("language", settings.language), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.updateLanguage("English"); showLanguageDialog = false }.padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = settings.language == "English", onClick = { viewModel.updateLanguage("English"); showLanguageDialog = false })
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("English", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.updateLanguage("Arabic"); showLanguageDialog = false }.padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = settings.language == "Arabic", onClick = { viewModel.updateLanguage("Arabic"); showLanguageDialog = false })
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("العربية (Arabic)", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showLanguageDialog = false }) { Text(Translator.translate("cancel", settings.language)) } }
        )
    }

    if (showReciterDialog) {
        AlertDialog(
            onDismissRequest = { showReciterDialog = false },
            title = { Text(text = Translator.translate("reciter", settings.language), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    listOf("ar.alafasy", "ar.abdulbasitmurattal", "ar.husary", "ar.minshawimujawwad").forEach { reciterId ->
                        val reciterKey = when(reciterId) {
                            "ar.alafasy" -> "mishary_alafasy"
                            "ar.abdulbasitmurattal" -> "abdul_basit"
                            "ar.husary" -> "al_husary"
                            "ar.minshawimujawwad" -> "minshawi"
                            else -> reciterId
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.updateReciter(reciterId); showReciterDialog = false }.padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = settings.quranReciter == reciterId, onClick = { viewModel.updateReciter(reciterId); showReciterDialog = false })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate(reciterKey, settings.language), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showReciterDialog = false }) { Text(Translator.translate("cancel", settings.language)) } }
        )
    }

    if (showCalculationDialog) {
        AlertDialog(
            onDismissRequest = { showCalculationDialog = false },
            title = { Text(text = Translator.translate("prayer_calculation", settings.language), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    listOf("Egyptian General Authority", "University of Islamic Sciences, Karachi", "Islamic Society of North America (ISNA)", "Muslim World League").forEach { method ->
                        val methodKey = when(method) {
                            "Egyptian General Authority" -> "egyptian_authority"
                            "University of Islamic Sciences, Karachi" -> "karachi_university"
                            "Islamic Society of North America (ISNA)" -> "isna"
                            "Muslim World League" -> "muslim_world_league"
                            else -> method
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.updateCalculationMethod(method); showCalculationDialog = false }.padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = settings.calculationMethod == method, onClick = { viewModel.updateCalculationMethod(method); showCalculationDialog = false })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate(methodKey, settings.language), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCalculationDialog = false }) { Text(Translator.translate("cancel", settings.language)) } }
        )
    }

    if (showTextSizeDialog) {
        AlertDialog(
            onDismissRequest = { showTextSizeDialog = false },
            title = { Text(text = Translator.translate("text_size", settings.language), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    listOf("Small", "Medium", "Large").forEach { size ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.updateTextSize(size); showTextSizeDialog = false }.padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = settings.textSize == size, onClick = { viewModel.updateTextSize(size); showTextSizeDialog = false })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate(size.lowercase(), settings.language), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showTextSizeDialog = false }) { Text(Translator.translate("cancel", settings.language)) } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().testTag("settings_screen").padding(bottom = 0.dp)) {
        AppHeader(title = Translator.translate("settings", settings.language), onBack = { navController.popBackStack() })

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = Translator.translate("prayer_notifications", settings.language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Switch(checked = settings.prayerNotifications, onCheckedChange = { viewModel.togglePrayerNotifications() }, modifier = Modifier.testTag("notifications_toggle"))
                }

                if (settings.prayerNotifications) {
                    val soundTypeKey = when(settings.notificationSoundType) { 
                        "Silent" -> "silent"
                        "Subtle" -> "default_sound"
                        "Full Adhan" -> "full_adhan"
                        "First Adhan" -> "first_adhan"
                        else -> "silent"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showSoundTypeDialog = true }.padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = Translator.translate("sound_type", settings.language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = Translator.translate(soundTypeKey, settings.language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Lucide.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = Translator.translate("dark_theme", settings.language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Switch(checked = settings.darkTheme, onCheckedChange = { viewModel.toggleDarkTheme() }, modifier = Modifier.testTag("dark_theme_toggle"))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().clickable { showLanguageDialog = true }.padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = Translator.translate("language", settings.language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (settings.language == "Arabic") "العربية" else "English", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Lucide.ChevronRight, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().clickable { showReciterDialog = true }.padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = Translator.translate("reciter", settings.language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val reciterKey = when(settings.quranReciter) {
                            "ar.alafasy" -> "mishary_alafasy"
                            "ar.abdulbasitmurattal" -> "abdul_basit"
                            "ar.husary" -> "al_husary"
                            "ar.minshawimujawwad" -> "minshawi"
                            else -> settings.quranReciter
                        }
                        Text(text = Translator.translate(reciterKey, settings.language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Lucide.ChevronRight, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().clickable { showCalculationDialog = true }.padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = Translator.translate("prayer_calculation", settings.language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val methodKey = when(settings.calculationMethod) {
                            "Egyptian General Authority" -> "egyptian_authority"
                            "University of Islamic Sciences, Karachi" -> "karachi_university"
                            "Islamic Society of North America (ISNA)" -> "isna"
                            "Muslim World League" -> "muslim_world_league"
                            else -> settings.calculationMethod
                        }
                        val methodShort = Translator.translate(methodKey, settings.language)
                        val displayMethod = if (methodShort.length > 20) methodShort.take(17) + "..." else methodShort
                        Text(text = displayMethod, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Lucide.ChevronRight, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().clickable { showTextSizeDialog = true }.padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = Translator.translate("text_size", settings.language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = Translator.translate(settings.textSize.lowercase(), settings.language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Lucide.ChevronRight, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.QURAN_SETTINGS) }.padding(vertical = 16.dp).testTag("quran_settings_menu_item"),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = Translator.translate("quran_reader_settings", settings.language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = Translator.translate("customize", settings.language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Lucide.ChevronRight, contentDescription = "Navigate", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }
        }
    }
}
