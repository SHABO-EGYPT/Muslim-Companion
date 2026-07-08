package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ChevronLeft
import com.example.ui.theme.MintTeal
import com.example.ui.theme.DarkTealText

@Composable
fun SectionHeader(title: String, actionText: String? = null, onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        if (actionText != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onActionClick() }.testTag("section_action_$title")
            )
        }
    }
}

@Composable
fun HomeWidget(
    title: String, subtitle: String, icon: ImageVector,
    iconBackground: Color, iconTint: Color, onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.testTag("home_widget_${title.lowercase().replace(" ", "_")}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isDark = isSystemInDarkTheme()
                val boxBg = if (isDark) iconBackground.copy(alpha = 0.2f) else iconBackground.copy(alpha = 0.15f)
                Box(modifier = Modifier.size(44.dp).background(boxBg, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = MaterialTheme.colorScheme.onSurface)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(imageVector = Lucide.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun AppHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null, rightContent: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).testTag("back_button")) {
                    Icon(imageVector = Lucide.ChevronLeft, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                if (subtitle != null) Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (rightContent != null) Box(modifier = Modifier.padding(start = 8.dp)) { rightContent() }
    }
}

@Composable
fun CircularProgressIndicatorM3(percentage: Float, size: Int = 48, strokeWidth: Int = 4, color: Color = MaterialTheme.colorScheme.primary) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size.dp)) {
        CircularProgressIndicator(progress = { percentage }, modifier = Modifier.fillMaxSize(), color = color, strokeWidth = strokeWidth.dp, trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    }
}

@Composable
fun RubElHizbIcon(
    number: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(modifier = modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val path = Path().apply {
                val outer = w / 2f; val inner = outer * 0.78f
                val cx = w / 2f; val cy = h / 2f
                for (i in 0 until 16) {
                    val angle = i * kotlin.math.PI / 8
                    val r = if (i % 2 == 0) outer else inner
                    val x = cx + r * kotlin.math.cos(angle).toFloat()
                    val y = cy + r * kotlin.math.sin(angle).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path = path, color = color)
        }
        Text(text = "$number", style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = textColor)
    }
}

fun getQuranFontFamily(fontName: String): FontFamily {
    return when (fontName) {
        "Classic Serif" -> FontFamily.Serif
        "Modern Sans" -> FontFamily.SansSerif
        "Monospace Style" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
}
