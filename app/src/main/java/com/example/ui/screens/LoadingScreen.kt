package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.R
import com.example.data.local.UserProgressEntity
import com.example.navigation.Routes
import com.example.ui.theme.ArabicSerifFamily
import com.example.ui.theme.LibreCaslonFontFamily
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    navController: NavHostController,
    userProgress: UserProgressEntity?
) {
    val celestialGold = Color(0xFFE9C349)

    // Entrance Fade-In Animation
    val animatableAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatableAlpha.animateTo(1f, animationSpec = tween(1200, easing = LinearOutSlowInEasing))
    }

    // Auto Navigation after loading sequence completes
    LaunchedEffect(Unit) {
        delay(2500)
        val targetRoute = if (userProgress?.onboardingCompleted == true) Routes.HOME else Routes.ONBOARDING
        navController.navigate(targetRoute) {
            popUpTo(Routes.LOADING) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Full Screen Dark Background (Dark.png)
        Image(
            painter = painterResource(id = R.drawable.loading_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Logo and Text Content Overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .alpha(animatableAlpha.value)
        ) {
            // Transparent Logo without background (LogoWithoutBackground.png)
            Image(
                painter = painterResource(id = R.drawable.loading_logo),
                contentDescription = "Muslim Companion Logo",
                modifier = Modifier
                    .size(240.dp)
                    .testTag("loading_logo"),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Text Branding with Libre Caslon Text Font
            Text(
                text = "Muslim Companion",
                fontSize = 32.sp,
                fontFamily = LibreCaslonFontFamily,
                fontWeight = FontWeight.Bold,
                color = celestialGold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "رفيق المسلم",
                fontSize = 36.sp,
                fontFamily = ArabicSerifFamily,
                fontWeight = FontWeight.Bold,
                color = celestialGold,
                textAlign = TextAlign.Center
            )
        }
    }
}
