package com.otl.gps.navigation.map.route.view.fragment.travelTools.qiblacompass

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient

class CompassQibla {

    class Builder(private val activity: AppCompatActivity, private var currentLocation: Location) :
        SensorEventListener {

        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private lateinit var sensorManager: SensorManager
        private lateinit var sensor: Sensor
        private var currentDegree = 0f
        private var currentDegreeNeedle = 0f
        private val model: CompassQiblaViewModel =
            ViewModelProvider(activity).get(CompassQiblaViewModel::class.java)

        @SuppressLint("MissingPermission")
        fun build() {

            try {

                model.getLocationAddress(activity, currentLocation)
                sensorManager =
                    activity.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
                sensorManager.registerListener(
                    this, sensor, SensorManager.SENSOR_DELAY_UI
                )
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }



        fun onDirectionChangeListener(onChange: (qiblaDirection: QiblaDirection) -> Unit) = apply {
            model.direction.observe(activity) { onChange(it) }
        }

        fun onGetLocationAddress(onGetLocation: (address: Address) -> Unit) = apply {
            model.locationAddress.observe(activity) { onGetLocation(it) }
        }

        override fun onSensorChanged(event: SensorEvent?) {
            val degree = event?.values?.get(0) ?: 0f
            val destinationLoc = Location("service Provider").apply {
                latitude = 21.422487
                longitude = 39.826206
            }

            var bearTo: Float = currentLocation.bearingTo(destinationLoc)
            if (bearTo < 0) bearTo += 360
            var direction: Float = bearTo - degree
            if (direction < 0) direction += 360

            val isFacingQibla = direction in 359.0..360.0 || direction in 0.0..1.0

            currentDegreeNeedle = direction
            currentDegree = -degree
            val qiblaDirection = QiblaDirection(-degree, direction, isFacingQibla)
            model.updateCompassDirection(qiblaDirection)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }


    }
}