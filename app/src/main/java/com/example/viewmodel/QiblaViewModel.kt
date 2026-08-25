package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.data.repository.CompanionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class QiblaViewModel @Inject constructor(
    application: Application,
    private val repository: CompanionRepository,
    private val locationRepository: com.example.data.location.LocationRepository
) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth = _azimuth.asStateFlow()

    private val _qiblaBearing = MutableStateFlow(0f)
    val qiblaBearing = _qiblaBearing.asStateFlow()
    
    private val _locationName = MutableStateFlow("Unknown Location")
    val locationName = _locationName.asStateFlow()

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false
    private val rotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    // Kaaba coordinates
    private val kaabaLat = 21.422487
    private val kaabaLng = 39.826206

    fun startSensors() {
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            val location = locationRepository.getCurrentLocation()
            if (location != null) {
                updateLocation(location.latitude, location.longitude, location.locationName)
            }
        }
    }

    fun updateLocation(lat: Double, lng: Double, resolvedName: String? = null) {
        val userLoc = Location("user").apply {
            latitude = lat
            longitude = lng
        }
        val kaabaLoc = Location("kaaba").apply {
            latitude = kaabaLat
            longitude = kaabaLng
        }
        val bearing = userLoc.bearingTo(kaabaLoc)
        _qiblaBearing.value = (bearing + 360f) % 360f

        if (resolvedName != null) {
            _locationName.value = resolvedName
        } else {
            viewModelScope.launch {
                _locationName.value = locationRepository.reverseGeocode(lat, lng)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, 3)
            hasGravity = true
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                _azimuth.value = (azimuthDeg + 360f) % 360f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }
}

