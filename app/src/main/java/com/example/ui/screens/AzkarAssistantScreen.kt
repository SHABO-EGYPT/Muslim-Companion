package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AzkarViewModel

@Composable
fun AzkarAssistantScreen(viewModel: AzkarViewModel, navController: NavHostController) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val loading by viewModel.assistantLoading.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var queryText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize().testTag("azkar_assistant_screen")) {
        AppHeader(title = Translator.translate("ask_azkar_assistant", settings.language), subtitle = Translator.translate("find_specific_supplications", settings.language), onBack = { navController.popBackStack() })

        if (chatHistory.isEmpty() && !loading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(40.dp), contentAlignment = Alignment.Center) {
                Text(text = Translator.translate("ask_assistant_placeholder", settings.language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("assistant_chat_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(chatHistory) { message ->
                    if (message.isUser) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Box(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                                    .padding(16.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(text = message.text, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Box(
                                modifier = Modifier
                                    .background(color = if (message.isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                                    .padding(16.dp)
                                    .widthIn(max = 300.dp)
                            ) {
                                Text(text = message.text, color = if (message.isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            if (message.azkar.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                message.azkar.forEach { dhikr ->
                                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                                            Text(
                                                text = dhikr.arabicText,
                                                style = TextStyle(fontFamily = ArabicSerifFamily, fontSize = 24.sp, lineHeight = 40.sp, textDirection = TextDirection.Rtl, textAlign = TextAlign.Center),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                            )
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(imageVector = Lucide.RotateCcw, contentDescription = Translator.translate("repeat", settings.language), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(text = "${dhikr.repeatTarget}x", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                            if (dhikr.englishTranslation.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(text = dhikr.englishTranslation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (loading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Box(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                                    .padding(16.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }

        // Input Area
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                modifier = Modifier.fillMaxWidth().testTag("assistant_query_input"),
                placeholder = { Text(Translator.translate("ask_azkar_desc", settings.language)) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant),
                trailingIcon = {
                    IconButton(
                        onClick = { 
                            if (queryText.isNotBlank()) { 
                                viewModel.searchAzkar(queryText)
                                queryText = ""
                                focusManager.clearFocus() 
                            } 
                        },
                        modifier = Modifier.testTag("assistant_submit_btn")
                    ) {
                        Icon(imageVector = Lucide.Send, contentDescription = Translator.translate("send", settings.language), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}
