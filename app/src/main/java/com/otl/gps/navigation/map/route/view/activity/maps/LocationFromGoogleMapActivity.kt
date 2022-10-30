package com.otl.gps.navigation.map.route.view.activity.maps

import com.otl.gps.navigation.map.route.R
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import application.RawGpsApp
import com.abl.gpstracker.navigation.maps.routefinder.app.utils.GeoCoderAddress

import com.google.android.gms.ads.AdSize
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mapbox.geojson.Point
import com.otl.gps.navigation.map.route.databinding.ActivityLocationFromGoogleMapBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.DialogUtils


class LocationFromGoogleMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private lateinit var binding: ActivityLocationFromGoogleMapBinding
    lateinit var locationManager: LocationManager
    private var locationByNetwork: Location? = null
    private var TAG = "SelectMapLocationActivity"
    private var isMapLoaded = false
    private var marker: Marker? = null
    private lateinit var networkLocationListener: LocationListener
    private var address: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private lateinit var geoCoderAddress: GeoCoderAddress

    private lateinit var map: GoogleMap
    private var mapStyle: String = "default"

    /////////////////////////////////////////////////////////////////////////////////
    var canShowNativeAd = false
    var canShowInter = false
    var adsReloadTry = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DialogUtils.showLoadingDialog(this)
        binding = ActivityLocationFromGoogleMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        loadMap()
        //loadNativeBanner()
        loadBanner()
        binding.layoutHeader.tvTitle.text = "Select Location"
        checkPermissionBeforeLocation()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geoCoderAddress = GeoCoderAddress(this)
        clickEvent()

    }



    private fun loadMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this@LocationFromGoogleMapActivity)
        getLocationFromPref()

    }

    /**
     * Checking Permissions using Dexter
     */

    private fun checkPermissionBeforeLocation() {
        if (checkGPSStatus()) {
            Dexter.withContext(this@LocationFromGoogleMapActivity)
                .withPermissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            getLocation()
                        }
                        // check for permanent denial of any permission
                        if (!report.areAllPermissionsGranted()) {
                            Toast.makeText(
                                this@LocationFromGoogleMapActivity,
                                "Permissions Are Necessery!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken
                    ) {
                        Toast.makeText(
                            this@LocationFromGoogleMapActivity,
                            "Permission Denied",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        token.continuePermissionRequest()
                        DialogUtils.dismissLoading()

                    }
                })
                .onSameThread()
                .check()
        } else {

            DialogUtils.dismissLoading()

            /////////////////////////////////////////////////////////////////////////////

            Toast.makeText(
                this@LocationFromGoogleMapActivity,
                "Please Make sure GPS/Network is on",
                Toast.LENGTH_SHORT
            ).show()

        }


    }


    //check location service is on or off
    private fun checkGPSStatus(): Boolean {
        var gps_enabled = false
        var network_enabled = false
        if (!::locationManager.isInitialized) {
            locationManager =
                this@LocationFromGoogleMapActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        } catch (ex: java.lang.Exception) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: java.lang.Exception) {
        }


        if (!gps_enabled && !network_enabled) {
            DialogUtils.dismissLoading()

            val dialog = AlertDialog.Builder(this@LocationFromGoogleMapActivity)
            dialog.setMessage("Please Turn On GPS Settings to have better Experience!")
            dialog.setPositiveButton("Ok") { dialog, which -> //this will navigate user to the device location settings screen
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)

            }
            val alert = dialog.create()
            alert.show()
            return false
        } else {
            return true
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        networkLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                try {
                    if (locationByNetwork == null) {
                        locationByNetwork = location

                    }
                    latitude = locationByNetwork?.latitude.toString()
                    longitude = locationByNetwork?.longitude.toString()

                    try {
                        geoCoderAddress.getCompleteAddress(
                            locationByNetwork!!.latitude, locationByNetwork!!.longitude,
                            binding.tvAddress
                        ) {
                            address = it
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                    recenterCamera()

                    locationManager.removeUpdates(networkLocationListener)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}

        }

        if (hasNetwork) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                // 60000,
                3000,
                0F,
                networkLocationListener
            )
        }

    }

    private fun clickEvent() {
        val fromLocation = intent.getBooleanExtra("isFromLocationClick", false)
        val toLocation = intent.getBooleanExtra("isToLocationClick", false)
        val isSelectOnMapClick = intent.getBooleanExtra("isSelectOnMapClick", false)

        binding.SelectLocationButton
            .setOnClickListener {
                if (isMapLoaded) {


                    if (fromLocation) {

                        ( application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_FROM_LOCATION, address)
                        ( application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_FROM_LOCATION, latitude)
                        ( application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_FROM_LOCATION, longitude)


                    }

                    if (toLocation) {

                        ( application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_TO_LOCATION, address)
                        ( application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_TO_LOCATION, latitude)
                        ( application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_TO_LOCATION, longitude)


                    }



                    if (isSelectOnMapClick) {
                        val intent = Intent()
                        intent.putExtra("address", address)
                        intent.putExtra("isFromLocationClick", fromLocation)
                        intent.putExtra("isToLocationClick", toLocation)
                        intent.putExtra("isSelectOnMapClick", true)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            }

        binding.layoutHeader.ivBack.setOnClickListener {
            finish()
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        if (::networkLocationListener.isInitialized) {
            locationManager.removeUpdates(networkLocationListener)
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //get ad size
    private val adSize: AdSize
        get() {
            val display = this.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adsParent.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun loadBanner() {
        (application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
            this,
            binding.adsParent,
            AdSize.LARGE_BANNER,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {}
            })
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun getLocationFromPref() {
        latitude =  (application as RawGpsApp).appContainer.prefs.getString(Constants.LATITUDE_FROM_LOCATION, "").toString()
        longitude =  (application as RawGpsApp).appContainer.prefs.getString(Constants.LONGITUDE_FROM_LOCATION, "").toString()
        var origin: Point? = null

        if (latitude!!.isNotEmpty() && longitude!!.isNotEmpty()) {
            origin = Point.fromLngLat(longitude!!.toDouble(), latitude!!.toDouble())
        }

        if (locationByNetwork == null && longitude!!.isNotEmpty() && latitude!!.isNotEmpty()) {
            locationByNetwork = Location("");//provider name is unnecessary
            locationByNetwork?.latitude = origin!!.latitude();//your coords of course
            locationByNetwork?.longitude = origin.longitude();
            recenterCamera()
        } else {
            recenterCamera()
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            map = googleMap ?: return
            googleMap.setOnMyLocationClickListener(this)
            enableMyLocation()
            map.uiSettings.isCompassEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = false
            if (mapStyle == "Satellite") {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE

            }

            if (mapStyle == "Traffic") {
                map.isTrafficEnabled = true
            }

            binding.recenterLocationButton.setOnClickListener {
                recenterCamera()
            }


            map.setOnMapClickListener {
                latitude = it.latitude.toString()
                longitude = it.longitude.toString()
                if (marker != null) {
                    marker?.remove()
                    marker = null
                }
                try {
                    marker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(latitude!!.toDouble(), longitude!!.toDouble()))
                            .title("")
                            .anchor(0f, 0f)
                            .infoWindowAnchor(0.5f, 0.5f)
                            .draggable(false)
                    )!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                DialogUtils.showLoadingDialog(this)
                try {
                    geoCoderAddress.getCompleteAddress(
                        it.latitude, it.longitude,
                        binding.tvAddress
                    ) {
                        address = it
                        binding.tvAddress.setText(it)
                        DialogUtils.dismissLoading()

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    DialogUtils.dismissLoading()

                }




              //  map.addMarker(MarkerOptions().position(it))
            }


            isMapLoaded = true

            recenterCamera()
        } catch (e: Exception) {
            e.printStackTrace()
        }



        DialogUtils.dismissLoading()


    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        if (!::map.isInitialized) return
        // [START maps_check_location_permission]
        map.isMyLocationEnabled = true
        // [END maps_check_location_permission]

    }

    override fun onMyLocationButtonClick(): Boolean {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Suppress("UNUSED_PARAMETER")
    fun recenterCamera() {
        val cameraPos: CameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude!!.toDouble(), longitude!!.toDouble()))
            .zoom(15.5f)
            .bearing(0f)
            .tilt(25f)
            .build()
        checkReadyThen {
            changeCamera(
                CameraUpdateFactory.newCameraPosition(cameraPos),
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {


                    }

                    override fun onCancel() {

                    }
                })
        }
        DialogUtils.dismissLoading()

    }

    // [START_EXCLUDE silent]
    /**
     * When the map is not ready the CameraUpdateFactory cannot be used. This should be used to wrap
     * all entry points that call methods on the Google Maps API.
     *
     * @param stuffToDo the code to be executed if the map is initialised
     */
    private fun checkReadyThen(stuffToDo: () -> Unit) {
        if (!::map.isInitialized) {
//            Toast.makeText(
//                this@LocationFromGoogleMapActivity,
//                R.string.map_not_ready,
//                Toast.LENGTH_SHORT
//            ).show()

        } else {
            stuffToDo()
        }
    }


    /**
     * Change the camera position by moving or animating the camera depending on the state of the
     * animate toggle button.
     */
    private fun changeCamera(update: CameraUpdate, callback: GoogleMap.CancelableCallback? = null) {
        // The duration must be strictly positive so we make it at least 1.
        map.animateCamera(update, 1, callback)
    }
////////////////////////////////////////////////////////////////////////////////////////////////////


}