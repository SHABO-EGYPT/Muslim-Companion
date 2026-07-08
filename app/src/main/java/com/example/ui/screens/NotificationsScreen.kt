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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel, navController: NavHostController) {
    val list by viewModel.notifications.collectAsState()

    Column(modifier = Modifier.fillMaxSize().testTag("notifications_screen").padding(bottom = 80.dp)) {
        AppHeader(title = "Notifications", onBack = { navController.popBackStack() })

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("notifications_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ) {
            items(list) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag("notification_item_${item.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(if (item.isUnread) MintTeal else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (item.iconName) { "flame" -> Lucide.Flame; "star" -> Lucide.Star; else -> Lucide.Bell }
                            Icon(imageVector = icon, contentDescription = null, tint = if (item.isUnread) DarkTealText else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = item.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (item.isUnread) FontWeight.Bold else FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = item.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(text = item.relativeTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }
        }
    }
}
