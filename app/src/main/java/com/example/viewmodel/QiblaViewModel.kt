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
    private val repository: CompanionRepository
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

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    // Kaaba coordinates
    private val kaabaLat = 21.422487
    private val kaabaLng = 39.826206

    init {
        viewModelScope.launch {
            // Setup or check things if needed
            // Location handled explicitly by startSensors or UI later
        }
    }

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

    fun updateLocation(lat: Double, lng: Double) {
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
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone()
        }

        val g = gravity
        val m = geomagnetic
        if (g != null && m != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val success = SensorManager.getRotationMatrix(R, I, g, m)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                _azimuth.value = (azimuthDeg + 360f) % 360f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
