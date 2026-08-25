package com.example.navigation

object Routes {
    const val LOADING = "loading"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val QURAN = "quran"
    const val SURAH_READER = "surah_reader"
    const val AZKAR = "azkar"
    const val AZKAR_FLOW = "azkar_flow"
    const val TASBIH = "tasbih"
    const val PRAYER = "prayer"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val QURAN_SETTINGS = "quran_settings"
    const val NOTIFICATIONS = "notifications"
    const val QIBLA = "qibla"
    const val NAMES_OF_ALLAH = "names_of_allah"
    const val QURANIC_DUAS = "quranic_duas"
    const val CUSTOM_CHAINS = "custom_chains"
    const val CREATE_CUSTOM_CHAIN = "create_custom_chain"
    const val RECITE_CUSTOM_CHAIN = "recite_custom_chain"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
