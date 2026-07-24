package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.composables.icons.lucide.*
import com.example.data.local.CompanionDatabase

import com.example.data.repository.CompanionRepository
import com.example.data.worker.PrayerSyncWorker
import com.example.data.worker.QuranSyncWorker
import com.example.navigation.AppNavHost
import com.example.navigation.BottomNavItem
import com.example.navigation.Routes
import com.example.ui.Translator
import com.example.ui.theme.*

import com.example.notifications.RequestNotificationPermission
import com.example.notifications.PrayerNotificationScheduler
import com.example.viewmodel.PrayerViewModel
import java.util.concurrent.TimeUnit

import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PrayerAndQuranSync", ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<PrayerSyncWorker>(12, TimeUnit.HOURS)
                .setConstraints(syncConstraints).build()
        )

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "QuranAudioSync", ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<QuranSyncWorker>(24, TimeUnit.HOURS)
                .setConstraints(syncConstraints).build()
        )

        setContent {
            val settingsState = mainViewModel.settings.collectAsStateWithLifecycle()
            val isDarkTheme = settingsState.value.darkTheme

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val backgroundImage = if (isDarkTheme) R.drawable.app_bg_dark else R.drawable.app_bg_light
                    Image(
                        painter = painterResource(id = backgroundImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent,
                        contentColor = if (isDarkTheme) OnBackgroundDark else OnBackgroundLight
                    ) {
                        CompanionApp(mainViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionApp(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val activityContext = androidx.compose.ui.platform.LocalContext.current
    val context = activityContext.applicationContext

    RequestNotificationPermission(activityContext)

    val prayerViewModel: PrayerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val prayerTimes by prayerViewModel.prayerTimes.collectAsStateWithLifecycle()
    LaunchedEffect(prayerTimes) {
        if (prayerTimes.isNotEmpty()) {
            PrayerNotificationScheduler(context).scheduleNotifications(prayerTimes)
            com.example.notifications.AzkarNotificationScheduler(context).scheduleAzkarNotifications(prayerTimes)
        }
    }

    val settings by mainViewModel.settings.collectAsStateWithLifecycle()
    val isArabic = settings.language == "Arabic"
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME

    val bottomNavItems = listOf(
        BottomNavItem(Routes.HOME, Translator.translate("home", settings.language), Lucide.House, Lucide.House),
        BottomNavItem(Routes.QURAN, Translator.translate("quran", settings.language), Lucide.BookOpen, Lucide.BookOpen),
        BottomNavItem(Routes.AZKAR, Translator.translate("azkar", settings.language), Lucide.Heart, Lucide.Heart),
        BottomNavItem(Routes.PRAYER, Translator.translate("prayer", settings.language), Lucide.Clock, Lucide.Clock),
        BottomNavItem(Routes.PROFILE, Translator.translate("profile", settings.language), Lucide.User, Lucide.User)
    )

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (bottomNavItems.any { it.route == currentRoute }) {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_nav_bar"),
                        containerColor = PrimaryTeal,
                        tonalElevation = 8.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(Routes.HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        modifier = Modifier.testTag("nav_icon_${item.route}")
                                    )
                                },
                                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryTeal,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                    indicatorColor = MintTeal
                                )
                            )
                        }
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                AppNavHost(
                    navController = navController,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}

