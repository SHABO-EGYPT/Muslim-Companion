package com.example.ui.screens

import android.Manifest
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.R
import com.example.navigation.Routes
import com.example.ui.Translator
import com.example.ui.theme.DarkTealText
import com.example.ui.theme.MintTeal
import com.example.ui.theme.PrimaryTeal
import com.example.viewmodel.ProfileViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: ProfileViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var selectedLanguage by remember(settings.language) { mutableStateOf(settings.language) }
    val isArabic = selectedLanguage == "Arabic"
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var manualCityInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    )

    fun fetchLocation() {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
            return
        }

        isLocating = true
        location = Translator.translate("locating", selectedLanguage)

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            val handleLocation = { loc: android.location.Location ->
                val geocoder = Geocoder(context, Locale.getDefault())
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(loc.latitude, loc.longitude, 1, @Suppress("RedundantSamConstructor") object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                val resolved = if (addresses.isNotEmpty()) {
                                    val address = addresses[0]
                                    val city = address.locality ?: address.subAdminArea ?: address.adminArea
                                    val country = address.countryName
                                    if (city != null && country != null) "$city, $country" else city ?: country ?: "Current Location"
                                } else {
                                    "Coordinates: %.2f, %.2f".format(Locale.US, loc.latitude, loc.longitude)
                                }
                                location = resolved
                                isLocating = false
                            }
                            override fun onError(errorMessage: String?) {
                                location = "Coordinates: %.2f, %.2f".format(Locale.US, loc.latitude, loc.longitude)
                                isLocating = false
                            }
                        })
                    } else {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                val resolved = if (addresses != null && addresses.isNotEmpty()) {
                                    val address = addresses[0]
                                    val city = address.locality ?: address.subAdminArea ?: address.adminArea
                                    val country = address.countryName
                                    if (city != null && country != null) "$city, $country" else city ?: country ?: "Current Location"
                                } else {
                                    "Coordinates: %.2f, %.2f".format(Locale.US, loc.latitude, loc.longitude)
                                }
                                withContext(Dispatchers.Main) {
                                    location = resolved
                                    isLocating = false
                                }
                            } catch (_: Exception) {
                                withContext(Dispatchers.Main) {
                                    location = "Coordinates: %.2f, %.2f".format(Locale.US, loc.latitude, loc.longitude)
                                    isLocating = false
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    location = "Coordinates: %.2f, %.2f".format(Locale.US, loc.latitude, loc.longitude)
                    isLocating = false
                }
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    handleLocation(loc)
                } else {
                    try {
                        val cts = CancellationTokenSource()
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cts.token
                        ).addOnSuccessListener { freshLoc ->
                            if (freshLoc != null) {
                                handleLocation(freshLoc)
                            } else {
                                location = Translator.translate("location_not_available", selectedLanguage)
                                isLocating = false
                            }
                        }.addOnFailureListener {
                            location = Translator.translate("location_not_available", selectedLanguage)
                            isLocating = false
                        }
                    } catch (_: Exception) {
                        location = Translator.translate("location_not_available", selectedLanguage)
                        isLocating = false
                    }
                }
            }.addOnFailureListener {
                location = Translator.translate("location_not_available", selectedLanguage)
                isLocating = false
            }
        } catch (_: SecurityException) {
            location = Translator.translate("location_not_available", selectedLanguage)
            isLocating = false
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted && location.isBlank()) {
            fetchLocation()
        } else if (!locationPermissionState.allPermissionsGranted && location.isBlank()) {
            location = Translator.translate("location_not_available", selectedLanguage)
        }
    }

    if (showManualDialog) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = {
                Text(
                    text = Translator.translate("edit_location_manually", selectedLanguage),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                OutlinedTextField(
                    value = manualCityInput,
                    onValueChange = { manualCityInput = it },
                    label = { Text(Translator.translate("enter_city_name", selectedLanguage)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualCityInput.isNotBlank()) {
                            location = manualCityInput.trim()
                        }
                        showManualDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text(Translator.translate("save", selectedLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text(Translator.translate("cancel", selectedLanguage))
                }
            }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize().testTag("onboarding_screen")) {
            Image(
                painter = painterResource(id = R.drawable.app_bg_light),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Language Selection Segmented Switcher
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isArabicSelected = selectedLanguage == "Arabic"
                        val isEnglishSelected = selectedLanguage == "English"

                        Surface(
                            onClick = {
                                selectedLanguage = "Arabic"
                                viewModel.updateLanguage("Arabic")
                            },
                            shape = RoundedCornerShape(100.dp),
                            color = if (isArabicSelected) PrimaryTeal else Color.Transparent,
                            modifier = Modifier.testTag("onboarding_lang_arabic")
                        ) {
                            Text(
                                text = "العربية",
                                fontWeight = FontWeight.Bold,
                                color = if (isArabicSelected) Color.White else DarkTealText.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                selectedLanguage = "English"
                                viewModel.updateLanguage("English")
                            },
                            shape = RoundedCornerShape(100.dp),
                            color = if (isEnglishSelected) PrimaryTeal else Color.Transparent,
                            modifier = Modifier.testTag("onboarding_lang_english")
                        ) {
                            Text(
                                text = "English",
                                fontWeight = FontWeight.Bold,
                                color = if (isEnglishSelected) Color.White else DarkTealText.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MintTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.User,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = Translator.translate("welcome_to_app", selectedLanguage),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = DarkTealText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = Translator.translate("onboarding_subtitle", selectedLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkTealText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Translator.translate("your_name", selectedLanguage)) },
                    placeholder = { Text(Translator.translate("enter_your_name", selectedLanguage)) },
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = PrimaryTeal.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            manualCityInput = if (location.contains("Coordinates") || location.contains("available") || location.contains("Locating") || location.contains("تحديد")) "" else location
                            showManualDialog = true
                        }
                        .testTag("onboarding_location_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MintTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Lucide.MapPin,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = Translator.translate("your_location", selectedLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryTeal
                                )
                                Text(
                                    text = if (isLocating) Translator.translate("locating", selectedLanguage) else location.ifBlank { Translator.translate("location_not_available", selectedLanguage) },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = DarkTealText
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLocating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryTeal
                                )
                            } else {
                                IconButton(
                                    onClick = { fetchLocation() },
                                    modifier = Modifier.size(36.dp).testTag("onboarding_retry_location_btn")
                                ) {
                                    Icon(
                                        imageVector = Lucide.RotateCw,
                                        contentDescription = Translator.translate("retry_location", selectedLanguage),
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        manualCityInput = if (location.contains("Coordinates") || location.contains("available") || location.contains("Locating") || location.contains("تحديد")) "" else location
                                        showManualDialog = true
                                    },
                                    modifier = Modifier.size(36.dp).testTag("onboarding_edit_location_btn")
                                ) {
                                    Icon(
                                        imageVector = Lucide.Pencil,
                                        contentDescription = Translator.translate("edit_location_manually", selectedLanguage),
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = {
                        val finalName = if (name.isBlank()) (if (isArabic) "المستخدم" else "User") else name.trim()
                        val finalLocation = if (location.isBlank() || location.contains("Locating") || location.contains("تحديد")) {
                            if (isArabic) "مصر (افتراضي)" else "Egypt (Default)"
                        } else {
                            location
                        }
                        viewModel.completeOnboarding(finalName, finalLocation, selectedLanguage)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_get_started_btn"),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text(
                        text = Translator.translate("get_started", selectedLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
