package com.example.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.data.repository.QuranRepository
import com.example.ui.screens.*
import com.example.viewmodel.*
import com.example.ui.QiblaCompassScreen

@Composable
fun AppNavHost(navController: NavHostController, repository: CompanionRepository) {
    // Scoped to the Activity's ViewModelStoreOwner so it remains shared across HomeScreen and Azkar screens.
    val azkarViewModel: AzkarViewModel = hiltViewModel()

    val userProgress by repository.getUserProgressFlow().collectAsState(initial = null)
    val startDestination = if (userProgress?.onboardingCompleted == true) Routes.HOME else Routes.ONBOARDING

    if (userProgress == null) {
        // Show loading or empty screen until we know if onboarding is needed
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.ONBOARDING) {
            val onboardingViewModel: ProfileViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = onboardingViewModel,
                navController = navController
            )
        }
        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                azkarViewModel = azkarViewModel,
                navController = navController
            )
        }

        // Nested graph for Quran reading flow. This allows shared scoping of SurahReaderViewModel,
        // ensuring ExoPlayer and associated audio assets are properly released when leaving the flow.
        navigation(
            startDestination = Routes.QURAN,
            route = "quran_flow_graph"
        ) {
            composable(Routes.QURAN) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("quran_flow_graph")
                }
                val sharedReaderViewModel: SurahReaderViewModel = hiltViewModel(parentEntry)
                val quranViewModel: QuranViewModel = hiltViewModel()
                
                QuranListScreen(
                    viewModel = quranViewModel,
                    readerViewModel = sharedReaderViewModel,
                    navController = navController
                )
            }
            composable(Routes.SURAH_READER) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("quran_flow_graph")
                }
                val sharedReaderViewModel: SurahReaderViewModel = hiltViewModel(parentEntry)
                SurahReaderScreen(
                    viewModel = sharedReaderViewModel,
                    navController = navController
                )
            }
            composable(Routes.QURAN_SETTINGS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("quran_flow_graph")
                }
                val sharedReaderViewModel: SurahReaderViewModel = hiltViewModel(parentEntry)
                QuranSettingsScreen(
                    viewModel = sharedReaderViewModel,
                    navController = navController
                )
            }
        }

        composable(Routes.AZKAR) {
            AzkarListScreen(
                viewModel = azkarViewModel,
                navController = navController
            )
        }
        composable(Routes.AZKAR_FLOW) {
            AzkarReadingFlowScreen(
                viewModel = azkarViewModel,
                navController = navController
            )
        }
        composable(Routes.TASBIH) {
            val tasbihViewModel: TasbihViewModel = hiltViewModel()
            DigitalTasbihScreen(
                viewModel = tasbihViewModel,
                navController = navController
            )
        }
        composable(Routes.PRAYER) {
            val prayerViewModel: PrayerViewModel = hiltViewModel()
            PrayerTimesScreen(
                viewModel = prayerViewModel,
                navController = navController
            )
        }
        composable(Routes.PROFILE) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                navController = navController
            )
        }
        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                navController = navController
            )
        }
        composable(Routes.NOTIFICATIONS) {
            val notificationsViewModel: NotificationsViewModel = hiltViewModel()
            NotificationsScreen(
                viewModel = notificationsViewModel,
                navController = navController
            )
        }
        composable(Routes.QIBLA) {
            val qiblaViewModel: QiblaViewModel = hiltViewModel()
            QiblaCompassScreen(
                viewModel = qiblaViewModel,
                navController = navController
            )
        }
    }
}
