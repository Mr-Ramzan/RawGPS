package com.otl.gps.navigation.map.route.view.fragment.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import application.RawGpsApp
import com.abl.gpstracker.navigation.maps.routefinder.app.utils.GeoCoderAddress
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdSize
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.otl.gps.navigation.map.route.databinding.ActivityMyLocationGoogleBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.DialogUtils
import com.otl.gps.navigation.map.route.R


class GoogleMapsMyLocFragment : Fragment(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {
    private lateinit var binding: ActivityMyLocationGoogleBinding
    lateinit var locationManager: LocationManager
    private var locationByNetwork: Location? = null
    private val TAG = "MyLocationActivity"
    private var isMapLoaded = false
    private lateinit var networkLocationListener: LocationListener
    private lateinit var map: GoogleMap
    private var mapStyle: String = "default"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.also {
            mapStyle = it!!.getString("style", "default")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityMyLocationGoogleBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DialogUtils.showLoadingDialog(requireActivity())
        loadMap()
        //========================================================================================//
        // Check if mylocation is clicked
        // SHow Banner Ad
        // Else Show
        if (mapStyle == "default") {
            loadBanner()
        } else {
            binding.layoutBottom.visibility = View.GONE
            loadBanner()
        }
        //========================================================================================//
        statusBarColor()
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        checkPermissionBeforeLocation()
        clickEvent()


    }


    /**
     * Checking Permissions using Dexter
     */
    private fun checkPermissionBeforeLocation() {
        if (checkGPSStatus()) {
            Dexter.withContext(requireContext())
                .withPermissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            getLocation()
                        }

                        // check for permanent denial of any permission
                        if (!report.areAllPermissionsGranted()) {
                            DialogUtils.dismissLoading()
                            Toast.makeText(
                                requireContext(),
                                "Permissions Are Necessery!",
                                Toast.LENGTH_SHORT
                            ).show()
                            checkPermissionBeforeLocation()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken
                    ) {
                        DialogUtils.dismissLoading()
                        Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT)
                            .show()
                        token.continuePermissionRequest()

                    }
                })
                .onSameThread()
                .check()
        } else {
            /////////////////////////////////////////////////////////////////////////////
            DialogUtils.dismissLoading()
            Toast.makeText(
                requireContext(),
                "Please Make sure GPS/Network is on",
                Toast.LENGTH_SHORT
            ).show()

        }
    }

    private var latitudeMyLocation: String? = null
    private var longitudeMyLocation: String? = null
    private var addressFromMyLocation: String? = null
    private fun checkMyLocation() {
        ( requireActivity().application as RawGpsApp).appContainer.prefs.setString(
            Constants.ADDRESS_FROM_LOCATION,
            addressFromMyLocation
        )

        ( requireActivity().application as RawGpsApp).appContainer.prefs.setString(
            Constants.LATITUDE_FROM_LOCATION,
            latitudeMyLocation
        )

        ( requireActivity().application as RawGpsApp).appContainer.prefs.setString(
            Constants.LONGITUDE_FROM_LOCATION,
            longitudeMyLocation
        )

    }


    //check location service is on or off
    private fun checkGPSStatus(): Boolean {

        var locationManager: LocationManager? = null
        var gps_enabled = false
        var network_enabled = false
        if (locationManager == null) {
            locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

            val dialog = AlertDialog.Builder(requireContext())

            dialog.setMessage("Please Turn On GPS Settings to have better Experience!")
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


    private fun statusBarColor() {

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requireActivity().  window.statusBarColor = resources.getColor(R.color.browser_actions_bg_grey, requireActivity().theme);
//        } else
//            requireActivity(). window.statusBarColor = resources.getColor(R.color.browser_actions_bg_grey);

    }


    private fun loadMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this@GoogleMapsMyLocFragment)

//        Configuration.getInstance().userAgentValue =
//            BuildConfig.APPLICATION_ID.toString() + "/" + BuildConfig.VERSION_NAME
//        binding.map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
//        binding.map.minZoomLevel = 4.0
//        binding.map.zoomController
//        binding.map.isVerticalMapRepetitionEnabled = false
//        binding.map.setMultiTouchControls(true)
//        getAddressFromPref()
//        zoomCamera()


    }


    override fun onDestroy() {
        super.onDestroy()
        if (::networkLocationListener.isInitialized) {
            locationManager.removeUpdates(networkLocationListener)
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {

        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        networkLocationListener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                locationByNetwork = location
                latitudeMyLocation = locationByNetwork!!.latitude.toString()
                longitudeMyLocation = locationByNetwork!!.longitude.toString()

                GeoCoderAddress(requireActivity()).getCompleteAddress(
                    locationByNetwork!!.latitude,
                    locationByNetwork!!.longitude, binding.tvAddress
                ) {
                    addressFromMyLocation = it
                }

                locationManager.removeUpdates(networkLocationListener)

                checkMyLocation()
                zoomCamera()


                recenterCamera()

            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (hasNetwork) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                DialogUtils.dismissLoading()
                return

            }

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                3000,
                0F,
                networkLocationListener
            )
        }

    }


    private var addressFromLocationSharedPref = ""

    private var latitudeFromLocation = ""

    private var longitudeFromLocation = ""

    private fun getAddressFromPref() {
        addressFromLocationSharedPref =
            ( requireActivity().application as RawGpsApp).appContainer.prefs.getString(Constants.ADDRESS_FROM_LOCATION, "")
                .toString()
        latitudeFromLocation =
            ( requireActivity().application as RawGpsApp).appContainer.prefs.getString(Constants.LATITUDE_FROM_LOCATION, "").toString()
        longitudeFromLocation =
            ( requireActivity().application as RawGpsApp).appContainer.prefs.getString(Constants.LONGITUDE_FROM_LOCATION, "").toString()
        binding.tvAddress.setText(addressFromLocationSharedPref)
        binding.tvLatLng.setText("$latitudeFromLocation,$longitudeFromLocation")
        if (locationByNetwork == null && longitudeFromLocation.isNotEmpty() && latitudeFromLocation.isNotEmpty()) {
            locationByNetwork = Location("");//provider name is unnecessary
            locationByNetwork?.latitude = latitudeFromLocation.toDouble();//your coords of course
            locationByNetwork?.longitude = longitudeFromLocation.toDouble();
            zoomCamera()
        } else {
            zoomCamera()
        }
    }

    private fun clickEvent() {


        binding.llShareLocation.setOnClickListener {
            if (isMapLoaded) {
                shareLocation()
            }
        }


        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }


    private fun zoomCamera() {

        if (locationByNetwork?.latitude != null && locationByNetwork?.longitude != null) {

            if (::map.isInitialized) {

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            latitudeMyLocation!!.toDouble(),
                            longitudeMyLocation!!.toDouble()
                        ), 10f
                    )
                )

            } else {

                Handler(Looper.getMainLooper()).postDelayed({ zoomCamera() }, 1000)

            }
            DialogUtils.dismissLoading()
        }
    }


    private fun shareLocation() {

        val uri =
            "http://maps.google.com/maps?daddr=" + locationByNetwork?.latitude + "," + locationByNetwork?.longitude
        val sharingIntent = Intent(Intent.ACTION_SEND)

        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, uri)

        startActivity(Intent.createChooser(sharingIntent, "Share in..."))

    }

    override fun onResume() {
        super.onResume()
        //  binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        // binding.map.onPause()
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(networkLocationListener)
    }


    var canShowNativeAd = false
    var adsReloadTry = 0




    //get ad size
    private val adSize: AdSize
        get() {
            val display = requireActivity().windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewBanner.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                requireContext(),
                adWidth
            )
        }

    private fun loadBanner() {
        ( requireActivity().application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
            requireActivity(),
            binding.adViewBanner,
            AdSize.LARGE_BANNER,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {}
            })
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////


    override fun onMapReady(googleMap: GoogleMap) {
        try {
            map = googleMap ?: return
            isMapLoaded = true
            googleMap.setOnMyLocationClickListener(this)
            enableMyLocation()
            map.uiSettings.isCompassEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = false
            if (mapStyle == "Satellite") {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE

            }
            if (mapStyle == "Traffic") {
                map.isTrafficEnabled = true
                ////////////////////////////////////////////////////////////////////////////////////
                binding.toggleLayerButton.visibility =View.VISIBLE
                binding.toggleLayerButton.setOnClickListener {
                    if(map.mapType==GoogleMap.MAP_TYPE_SATELLITE){
                        Glide.with(requireActivity()).load(R.drawable.streets).into(binding.layerToggleIv)
                        map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    }else{
                        Glide.with(requireActivity()).load(R.drawable.settalite).into(binding.layerToggleIv)
                        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    }
                }

            ////////////////////////////////////////////////////////////////////////////////////




            }

            binding.recenterLocationButton.setOnClickListener {
                recenterCamera()
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

        googleMap.setOnCameraIdleListener {
            // Cleaning all the markers.
            if (googleMap != null) {
                googleMap.clear()
            }
        }

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
//        Toast.makeText(requireContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
//        Toast.makeText(requireContext(), "Current location:\n$location", Toast.LENGTH_LONG).show()

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////


    @Suppress("UNUSED_PARAMETER")
    fun recenterCamera() {
        val cameraPos: CameraPosition = CameraPosition.Builder()
            .target(LatLng(latitudeMyLocation!!.toDouble(), longitudeMyLocation!!.toDouble()))
            .zoom(15.5f)
            .bearing(0f)
            .tilt(25f)
            .build()
        checkReadyThen {
            changeCamera(CameraUpdateFactory.newCameraPosition(cameraPos),
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {

                    }

                    override fun onCancel() {

                    }
                })
        }
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
//            Toast.makeText(requireContext(), R.string.map_not_ready, Toast.LENGTH_SHORT).show()
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
        map.animateCamera(update, 1000, callback)
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
