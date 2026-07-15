package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.MapPin
import com.example.viewmodel.QiblaViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QiblaCompassScreen(viewModel: QiblaViewModel, navController: NavHostController) {
    val azimuth by viewModel.azimuth.collectAsState()
    val qiblaBearing by viewModel.qiblaBearing.collectAsState()
    
    val context = LocalContext.current
    var locationName by remember { mutableStateOf("Unknown Location") }

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                
                val handleLocation = { location: android.location.Location ->
                    viewModel.updateLocation(location.latitude, location.longitude)
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(location.latitude, location.longitude, 1, @Suppress("RedundantSamConstructor") object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                    if (addresses.isNotEmpty()) {
                                        val address = addresses[0]
                                        val city = address.locality ?: address.subAdminArea
                                        val country = address.countryName
                                        locationName = if (city != null && country != null) "$city, $country"
                                        else city ?: country ?: "Current Location"
                                    }
                                }
                            })
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                val address = addresses[0]
                                val city = address.locality ?: address.subAdminArea
                                val country = address.countryName
                                locationName = if (city != null && country != null) "$city, $country"
                                else city ?: country ?: "Current Location"
                            }
                        }
                    } catch (e: Exception) {
                        locationName = "Current Location"
                    }
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        handleLocation(location)
                    } else {
                        try {
                            fusedLocationClient.getCurrentLocation(
                                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                null
                            ).addOnSuccessListener { freshLocation ->
                                if (freshLocation != null) {
                                    handleLocation(freshLocation)
                                } else {
                                    locationName = "Location Signal Weak"
                                }
                            }.addOnFailureListener {
                                locationName = "Location Signal Weak"
                            }
                        } catch (e: Exception) {
                            locationName = "Location Signal Weak"
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Ignored
            }
        } else {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        viewModel.startSensors()
        onDispose {
            viewModel.stopSensors()
        }
    }

    // Direction to Kaaba relative to the phone's top
    val pointerRotation = qiblaBearing - azimuth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Lucide.ChevronLeft, contentDescription = "Back")
            }
            Text(
                text = "Qibla Compass",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Location Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Lucide.MapPin, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = locationName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Compass UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Compass Ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    radius = size.width / 2,
                    style = Stroke(width = 8.dp.toPx())
                )
                
                // North indicator
                rotate(-azimuth) {
                    drawLine(
                        color = Color.Red,
                        start = Offset(size.width / 2, 20.dp.toPx()),
                        end = Offset(size.width / 2, 0f),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Outer dial for Qibla
            Canvas(modifier = Modifier.fillMaxSize()) {
                rotate(pointerRotation) {
                    val radius = size.width / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Draw a pointer to Kaaba
                    drawLine(
                        color = Color(0xFF00C853), // Green for Kaaba
                        start = center,
                        end = Offset(center.x, center.y - radius + 10.dp.toPx()),
                        strokeWidth = 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    
                    drawCircle(
                        color = Color(0xFF00C853),
                        radius = 12.dp.toPx(),
                        center = center
                    )
                }
            }
            
            // Icon in the middle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.Compass,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bearing Details
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isAligned = Math.abs(pointerRotation % 360) < 5 || Math.abs(pointerRotation % 360) > 355
            
            Text(
                text = if (isAligned) "You are facing the Qibla!" else "Rotate to face the Qibla",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (isAligned) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Kaaba is at ${qiblaBearing.toInt()}° from North",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
