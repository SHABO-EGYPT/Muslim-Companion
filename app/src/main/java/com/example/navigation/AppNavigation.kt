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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.AzkarViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.QuranViewModel
import com.example.viewmodel.SurahReaderViewModel
import com.example.viewmodel.TasbihViewModel
import com.example.viewmodel.PrayerViewModel
import com.example.viewmodel.ProfileViewModel
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.NotificationsViewModel
import com.example.viewmodel.QiblaViewModel
import com.example.viewmodel.NamesOfAllahViewModel
import com.example.viewmodel.QuranicDuasViewModel

import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.QuranListScreen
import com.example.ui.screens.SurahReaderScreen
import com.example.ui.screens.QuranSettingsScreen
import com.example.ui.screens.AzkarListScreen
import com.example.ui.screens.AzkarReadingFlowScreen
import com.example.ui.screens.DigitalTasbihScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.NamesOfAllahScreen
import com.example.ui.screens.QuranicDuasScreen
import com.example.ui.QiblaCompassScreen

@Composable
fun AppNavHost(navController: NavHostController, mainViewModel: MainViewModel) {
    // Scoped to the Activity's ViewModelStoreOwner so it remains shared across HomeScreen and Quran screens.
    val azkarViewModel: AzkarViewModel = hiltViewModel()
    val surahReaderViewModel: SurahReaderViewModel = hiltViewModel()

    val userProgress by mainViewModel.userProgress.collectAsStateWithLifecycle()
    val startDestination = if (userProgress?.onboardingCompleted == true) Routes.HOME else Routes.ONBOARDING

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
                readerViewModel = surahReaderViewModel,
                navController = navController
            )
        }

        // Nested graph for Quran reading flow.
        navigation(
            startDestination = Routes.QURAN,
            route = "quran_flow_graph"
        ) {
            composable(Routes.QURAN) {
                val quranViewModel: QuranViewModel = hiltViewModel()
                
                QuranListScreen(
                    viewModel = quranViewModel,
                    readerViewModel = surahReaderViewModel,
                    navController = navController
                )
            }
            composable(Routes.SURAH_READER) {
                SurahReaderScreen(
                    viewModel = surahReaderViewModel,
                    navController = navController
                )
            }
            composable(Routes.QURAN_SETTINGS) {
                QuranSettingsScreen(
                    viewModel = surahReaderViewModel,
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
        composable(Routes.NAMES_OF_ALLAH) {
            val namesViewModel: com.example.viewmodel.NamesOfAllahViewModel = hiltViewModel()
            com.example.ui.screens.NamesOfAllahScreen(
                viewModel = namesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.QURANIC_DUAS) {
            val quranicDuasViewModel: com.example.viewmodel.QuranicDuasViewModel = hiltViewModel()
            com.example.ui.screens.QuranicDuasScreen(
                viewModel = quranicDuasViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
