package com.otl.gps.navigation.map.route.view.fragment.travelTools.qiblacompass

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*

class CompassQiblaViewModel : ViewModel() {

    private val _locationAddress = MutableLiveData<Address>()
    val locationAddress get() = _locationAddress

    private val _direction = MutableLiveData<QiblaDirection>()
    val direction get() = _direction

    private val _permission = MutableLiveData<Pair<Boolean, String>>()
    val permission get() = _permission

    fun updateCompassDirection(qiblaDirection: QiblaDirection) {
        viewModelScope.launch {
            _direction.value = qiblaDirection
        }
    }

    fun getLocationAddress(context: Context, location: Location){
        try {
            Geocoder(context, Locale.getDefault()).apply {
                if (Build.VERSION.SDK_INT >= 33) {


                    getFromLocation(location.latitude, location.longitude, 1)?.first() .let { address ->
                                _locationAddress.value = address
                            }
                        }

                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onPermissionUpdate(isGranted: Boolean, message: String? = "") {
        _permission.value = Pair(isGranted, message ?: "")
    }
}