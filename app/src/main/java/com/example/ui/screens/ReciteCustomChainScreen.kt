package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.theme.ArabicSerifFamily
import com.example.viewmodel.CustomDhikrViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciteCustomChainScreen(
    viewModel: CustomDhikrViewModel,
    chainId: Int? = null,
    navController: NavController
) {
    LaunchedEffect(chainId) {
        if (chainId != null && chainId > 0) {
            viewModel.startRecitation(chainId)
        }
    }

    val activeChain by viewModel.activeChain.collectAsStateWithLifecycle()
    val stepIndex by viewModel.currentStepIndex.collectAsStateWithLifecycle()
    val count by viewModel.currentStepCount.collectAsStateWithLifecycle()
    val isCompleted by viewModel.isChainCompleted.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val triggerVibrate = {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    val chain = activeChain

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = chain?.title ?: "تسبيح السلسلة",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetRecitation() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (chain == null || chain.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد سلسلة نشطة للبدء", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val items = chain.items
            val currentItem = items.getOrNull(stepIndex) ?: items.first()
            val stepTarget = currentItem.targetCount
            val progress = (count.toFloat() / stepTarget.toFloat()).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isCompleted) {
                            triggerVibrate()
                            viewModel.increment()
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Progress Bar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الذكر ${stepIndex + 1} من ${items.size}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "${((stepIndex.toFloat() / items.size.toFloat()) * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { ((stepIndex.toFloat() + progress) / items.size.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // Main Content Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentItem.arabicText,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = ArabicSerifFamily,
                                    textDirection = TextDirection.Rtl,
                                    lineHeight = 42.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Big Circular Tap Area & Counter
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                )
                            )
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(210.dp),
                            strokeWidth = 10.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "من $stepTarget",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Bottom instruction
                    Text(
                        text = if (!isCompleted) "المس أي مكان على الشاشة للتسبيح" else "تم الانتهاء بحمد الله!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Completion Dialog
                if (isCompleted) {
                    AlertDialog(
                        onDismissRequest = { navController.popBackStack() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = {
                            Text(
                                text = "مبارك! تقبل الله منك",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        },
                        text = {
                            Text(
                                text = "لقد أتممت سلسلة \"${chain.title}\" بنجاح.\nتم تسجيل إتمامك للسلسلة.",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("العودة للقائمة")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.resetRecitation() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إعادة السلسلة")
                            }
                        }
                    )
                }
            }
        }
    }
}
