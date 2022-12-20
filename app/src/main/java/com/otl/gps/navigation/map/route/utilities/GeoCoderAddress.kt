package com.abl.gpstracker.navigation.maps.routefinder.app.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class GeoCoderAddress(var context: Context) {
    var TAG = "GeoCoderAddress"
    fun getCompleteAddress(
        Lat: Double,
        Long: Double,
        tvAddress: TextView,
        result: (address: String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {

            var strAdd = ""
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses: List<Address>? = geocoder.getFromLocation(Lat, Long, 1)
                if (addresses != null) {

                    val returnedAddress: Address = addresses[0]
//                val cityName = returnedAddress.locality
//                val countryName = returnedAddress.countryName
//                Log.w(TAG, cityName.toString())
//                Log.w(TAG, countryName.toString())
                    val strReturnedAddress = StringBuilder("")

                    for (i in 0..returnedAddress.maxAddressLineIndex) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                    }

                    CoroutineScope(Dispatchers.Main).launch {

                        strAdd = strReturnedAddress.toString()
                        tvAddress.text = strAdd
                        result(strAdd)
                    }
                    Log.w(TAG, strReturnedAddress.toString())
                } else {
                    Log.w(TAG, "No Address returned!")
                    CoroutineScope(Dispatchers.Main).launch {
                        result("")
                    }
                }
            } catch (e: Exception) {

                e.printStackTrace()

                Log.w(TAG, "Canont get Address!")
                CoroutineScope(Dispatchers.Main).launch {

                    tvAddress.text = ""
                    result("")
                }

            }

        }
    }

    fun getCompleteAddress(Lat: Double, Long: Double, result: (address: String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var strAdd = ""
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses: List<Address>? = geocoder.getFromLocation(Lat, Long, 1)
                if (addresses != null) {
                    val returnedAddress: Address = addresses[0]
//                 val cityName = returnedAddress.locality
//                 val countryName = returnedAddress.countryName
//                 Log.w(TAG, cityName.toString())
//                 Log.w(TAG, countryName.toString())
                    val strReturnedAddress = StringBuilder("")
                    for (i in 0..returnedAddress.maxAddressLineIndex) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                    }
                    strAdd = strReturnedAddress.toString()
                    CoroutineScope(Dispatchers.Main).launch {

                        result(strAdd)
                    }
//                Log.w(TAG, strReturnedAddress.toString())
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        result("")
                    }
//                Log.w(TAG, "No Address returned!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    result("")
//              Log.w(TAG, "Canont get Address!")
                }
            }

        }
    }


    fun getCityName(Lat: Double, Long: Double): String? {
        var strAdd = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(Lat, Long, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val cityName = returnedAddress.locality
                val countryName = returnedAddress.countryName
                Log.w(TAG, cityName.toString())
                Log.w(TAG, countryName.toString())
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                Log.w(TAG, strReturnedAddress.toString())
            } else {
                Log.w(TAG, "No Address returned!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w(TAG, "Canont get Address!")
        }
        return strAdd
    }


    fun getLatLng(address: String): LatLng? {
        var p1: LatLng? = null
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocationName(address, 5)
            if (addresses != null) {

                val returnedAddress: Address = addresses[0]
                val latitude = returnedAddress.latitude
                val longitude = returnedAddress.longitude
                Log.w(TAG, latitude.toString())
                Log.w(TAG, longitude.toString())
                p1 = LatLng(latitude, longitude)

            } else {
                Log.w(TAG, "No LatLng returned!")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.w(TAG, "Canont get LatLng!")
        }
        return p1
    }


    fun getAddressFromName(placename: String): ArrayList<Address>? {
        var addresses:ArrayList<Address>? = ArrayList<Address>()
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            addresses?.addAll(geocoder.getFromLocationName(placename, 2))

        } catch (e: Exception) {
            e.printStackTrace()
            return addresses
        }
        return addresses
    }


}