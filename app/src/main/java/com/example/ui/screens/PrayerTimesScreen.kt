package com.example.ui.screens

import android.Manifest
import android.location.Geocoder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.*
import com.example.navigation.Routes
import com.example.ui.Translator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PrayerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import java.util.Locale
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.chrono.HijrahDate

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerTimesScreen(viewModel: PrayerViewModel, navController: NavHostController) {
    val times by viewModel.prayerTimes.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val nextPrayerInfo by viewModel.nextPrayerInfo.collectAsState()
    val checkablePrayers by viewModel.checkablePrayers.collectAsState()
    val prayerLoadError by viewModel.prayerLoadError.collectAsState()

    val context = LocalContext.current
    
    LaunchedEffect(prayerLoadError) {
        prayerLoadError?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    )

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                geocoder.getFromLocation(location.latitude, location.longitude, 1, @Suppress("RedundantSamConstructor") object : Geocoder.GeocodeListener {
                                    override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                        val name = if (addresses.isNotEmpty()) {
                                            val address = addresses[0]
                                            val city = address.locality ?: address.subAdminArea
                                            val country = address.countryName
                                            if (city != null && country != null) "$city, $country" else city ?: country ?: "Current Location"
                                        } else "Coordinates: %.2f, %.2f".format(location.latitude, location.longitude)
                                        viewModel.updateLocation(location.latitude, location.longitude, name)
                                    }
                                    override fun onError(errorMessage: String?) {
                                        viewModel.updateLocation(location.latitude, location.longitude, "Current Location")
                                    }
                                })
                            } else {
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                val name = if (addresses != null && addresses.isNotEmpty()) {
                                    val address = addresses[0]
                                    val city = address.locality ?: address.subAdminArea
                                    val country = address.countryName
                                    if (city != null && country != null) "$city, $country" else city ?: country ?: "Current Location"
                                } else "Coordinates: %.2f, %.2f".format(location.latitude, location.longitude)
                                viewModel.updateLocation(location.latitude, location.longitude, name)
                            }
                        } catch (_: Exception) {
                            viewModel.updateLocation(location.latitude, location.longitude, "Current Location")
                        }
                    }
                }
            } catch (_: SecurityException) {}
        }
    }

    Column(modifier = Modifier.fillMaxSize().testTag("prayer_times_screen")) {
        AppHeader(
            title = Translator.translate("prayer_times", settings.language),
            subtitle = locationName,
            rightContent = {
                IconButton(onClick = { locationPermissionState.launchMultiplePermissionRequest() }, modifier = Modifier.testTag("prayer_location_pin")) {
                    Icon(imageVector = Lucide.MapPin, contentDescription = Translator.translate("location", settings.language), tint = if (locationPermissionState.allPermissionsGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                }
            }
        )

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().testTag("prayer_times_header_card"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = MaterialTheme.shapes.medium) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val today = LocalDate.now()
                        val hijri = HijrahDate.from(today)
                        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US)
                        val hijriFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)
                        
                        Text(text = "${today.format(formatter)} · ${hijri.format(hijriFormatter)} AH", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = nextPrayerInfo.second, style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold), color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        val translatedPrayerName = Translator.translate(nextPrayerInfo.first.name.lowercase(), settings.language)
                        Text(text = "${Translator.translate("until_adhan", settings.language)} $translatedPrayerName", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            itemsIndexed(times) { index, item ->
                val isNext = item.name == nextPrayerInfo.third
                val progress by viewModel.userProgress.collectAsState()
                val isCompleted = progress.completedPrayersToday.split(",").filter { it.isNotBlank() }.contains(item.name)
                val isCheckable = checkablePrayers.contains(item.name)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clickable(enabled = isCheckable) { viewModel.togglePrayerCompletion(item.name) }
                        .testTag("prayer_row_${item.name.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = if (isNext) MintTeal else Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    border = if (isNext) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp).alpha(if (isCheckable) 1f else 0.5f), 
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(if (isNext) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (item.iconName) { "sunrise" -> Lucide.Sunrise; "sunset" -> Lucide.Sunset; "moon" -> Lucide.CloudMoon; else -> Lucide.Sun }
                                Icon(imageVector = icon, contentDescription = item.name, tint = if (isNext) DarkTealText else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = Translator.translate(item.name.lowercase(), settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (isNext) DarkTealText else MaterialTheme.colorScheme.onSurface)
                                Text(text = item.arabicName, style = MaterialTheme.typography.bodySmall.copy(fontFamily = ArabicSerifFamily), color = if (isNext) DarkTealText.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val time12 = try {
                                val parts = item.timeString.split(":")
                                val h = parts[0].toInt()
                                val m = parts[1]
                                val suffix = if (h >= 12) "PM" else "AM"
                                val h12 = if (h % 12 == 0) 12 else h % 12
                                "%02d:%s %s".format(h12, m, suffix)
                            } catch (_: Exception) { item.timeString }

                            Text(text = time12, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = if (isNext) DarkTealText else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(12.dp))
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = { viewModel.togglePrayerCompletion(item.name) },
                                enabled = isCheckable,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (isNext) DarkTealText else MaterialTheme.colorScheme.primary, 
                                    uncheckedColor = if (isNext) DarkTealText.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    disabledCheckedColor = if (isNext) DarkTealText.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    disabledUncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("prayer_checkbox_${item.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(title = Translator.translate("qibla_compass", settings.language))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.QIBLA) }.testTag("qibla_compass_card"),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = WarmPeach)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Lucide.Compass, contentDescription = Translator.translate("qibla_direction", settings.language), tint = DarkWarmPeachText)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = Translator.translate("qibla_direction", settings.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DarkWarmPeachText)
                                Text(text = Translator.translate("qibla_subtitle", settings.language), style = MaterialTheme.typography.bodySmall, color = DarkWarmPeachText.copy(alpha = 0.8f))
                            }
                        }
                        Icon(imageVector = Lucide.ChevronRight, contentDescription = null, tint = DarkWarmPeachText)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(title = Translator.translate("calculation_method", settings.language))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {}.testTag("calculation_method_settings_row"),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        val methodKey = when(settings.calculationMethod) {
                            "Egyptian General Authority" -> "egyptian_authority"
                            "University of Islamic Sciences, Karachi" -> "karachi_university"
                            "Islamic Society of North America (ISNA)" -> "isna"
                            "Muslim World League" -> "muslim_world_league"
                            else -> settings.calculationMethod
                        }
                        Text(text = Translator.translate(methodKey, settings.language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Icon(imageVector = Lucide.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
