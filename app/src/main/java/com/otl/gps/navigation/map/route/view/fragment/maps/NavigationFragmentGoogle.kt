package com.otl.gps.navigation.map.route.view.fragment.maps


import com.otl.gps.navigation.map.route.R
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import application.RawGpsApp
import com.abl.gpstracker.navigation.maps.routefinder.app.utils.GeoCoderAddress
import com.google.android.gms.ads.AdSize
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.otl.gps.navigation.map.route.databinding.FragmentRouteFinderGoogleBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.model.NavEvent
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.DialogUtils
import com.otl.gps.navigation.map.route.view.activity.maps.PickLocationActivity
import org.greenrobot.eventbus.EventBus


class NavigationFragmentGoogle : Fragment(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private lateinit var binding: FragmentRouteFinderGoogleBinding
    private lateinit var locationManager: LocationManager
    private var locationByNetwork: Location? = null
    private lateinit var networkLocationListener: LocationListener
    private val TAG = "RouteFinderActivity"
    private var isToLocationClick = false
    private var isFromLocationClick = false
    private var hintFromLocation = ""
    private var hintToLocation = ""
    private var addrSrcLocation = ""
    private var addrDestLocation = ""
    private var latSrcLocation = ""
    private var longSrcLocation = ""
    private var latDestLocation = ""
    private var longDestLocation = ""
    private var isUserFrmMyLocationClick = false
    private var isUserFrmMapLocationClick = false
    private var addressFromMyLocation: String? = null
    private var addressToMyLocation: String? = null


    private var isFindAddressClick = false


    private lateinit var map: GoogleMap
    private var mapStyle: String = "default"
    private var sourceMarker: Marker? = null
    private var destMarker: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRouteFinderGoogleBinding.inflate(layoutInflater)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DialogUtils.showLoadingDialog(requireActivity())
        loadMap()
        loadBanner()
        initObjects()
        getLocationAndPermission()
        clickEvent()
        loadInter()



    }


    /**
     * Checking Permissions using Dexter
     */
    private fun getLocationAndPermission() {
        if (checkGPSStatus()) {
            Dexter.withContext(requireContext())
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
                                requireContext(),
                                "Permissions Are Necessery!",
                                Toast.LENGTH_SHORT
                            ).show()
                            DialogUtils.dismissLoading()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken
                    ) {
                        Toast.makeText(requireActivity(), "Permission Denied", Toast.LENGTH_SHORT)
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
                requireContext(),
                "Please Make sure GPS/Network is on",
                Toast.LENGTH_SHORT
            ).show()
        }


    }


    private fun initObjects() {
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        binding.transitRoute.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue))
    }


    //LoadGoogle Map Async...
    private fun loadMap() {

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this@NavigationFragmentGoogle)
        getLocationFromPref()

    }

    private fun getLocationFromPref() {

        addrSrcLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                Constants.ADDRESS_FROM_LOCATION,
                ""
            )
                .toString()
        latSrcLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                Constants.LATITUDE_FROM_LOCATION,
                ""
            ).toString()
        longSrcLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                Constants.LONGITUDE_FROM_LOCATION,
                ""
            ).toString()
        hintFromLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                "hintFromLocation",
                "My Location"
            ).toString()
        binding.myLocationText.hint = hintFromLocation
        binding.myLocationText.setText(addrSrcLocation)


        if (locationByNetwork == null && longSrcLocation.isNotEmpty() && latSrcLocation.isNotEmpty()) {
            locationByNetwork = Location("");//provider name is unnecessary
            locationByNetwork?.latitude = latSrcLocation.toDouble();//your coords of course
            locationByNetwork?.longitude = longSrcLocation.toDouble();
            updateMarkers()
        } else {
            locationByNetwork?.latitude = latSrcLocation.toDouble();//your coords of course
            locationByNetwork?.longitude = longSrcLocation.toDouble();
            updateMarkers()
        }


    }

    private fun getCurntAddrFromPrefs() {

        addrSrcLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                Constants.ADDRESS_FROM_LOCATION,
                ""
            ).toString()

        addrDestLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                Constants.ADDRESS_TO_LOCATION,
                ""
            ).toString()

        hintFromLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                "hintFromLocation",
                "My Location"
            ).toString()

        hintToLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                "hintToLocation",
                "Choose Destination"
            ).toString()

        binding.myLocationText.hint = hintFromLocation
        binding.destinationText.hint = hintToLocation


    }


    private fun getDstAddrFromPrefs() {
        addrDestLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                Constants.ADDRESS_TO_LOCATION,
                ""
            )
                .toString()

        latDestLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                Constants.LATITUDE_TO_LOCATION,
                ""
            )
                .toString()

        longDestLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                Constants.LONGITUDE_TO_LOCATION,
                ""
            )
                .toString()

        hintToLocation =
            (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
                "hintToLocation",
                "Choose Destination"
            ).toString()
        binding.destinationText.hint = hintToLocation

        binding.destinationText.setText(addrDestLocation)

        updateMarkers()

    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        networkLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                try {
                    locationByNetwork = location
                    latSrcLocation = locationByNetwork!!.latitude.toString()
                    longSrcLocation = locationByNetwork!!.longitude.toString()
                    GeoCoderAddress(requireContext()).getCompleteAddress(
                        locationByNetwork!!.latitude,
                        locationByNetwork!!.longitude
                    ) {
                        addressFromMyLocation = it
                        checkMyLocation()
                    }
                    locationManager.removeUpdates(networkLocationListener)
                    checkMyLocation()
                } catch (E: Exception) {
                    E.printStackTrace()
                }
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
                5000,
                0F,
                networkLocationListener
            )
        }
    }

    private fun updateMarkers() {
        val builder: LatLngBounds.Builder = LatLngBounds.Builder()

        try {
            if (sourceMarker != null) {
                sourceMarker?.remove()
                sourceMarker = null
            }

            if (destMarker != null) {
                destMarker?.remove()
                destMarker = null
            }
            map.clear()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        //Update the placement of the markers.....
        if (locationByNetwork?.latitude != null && locationByNetwork?.longitude != null) {
            try {
//                sourceMarker = map.addMarker(
//                    MarkerOptions()
//                        .position(  LatLng(   locationByNetwork?.latitude!!, locationByNetwork?.longitude!!  )  )
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                        .title(addrSrcLocation)
//                        .infoWindowAnchor(0.5f, 0.5f)
//                        .anchor(0.9f, 0.1f)
//                        .draggable(false)
//                )
//==============================================================================================================================
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!latDestLocation.isNullOrEmpty() && !longDestLocation.isNullOrEmpty()) {
            try {
                destMarker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latDestLocation.toDouble(), longDestLocation.toDouble()))
                        .title(addrDestLocation)
                        .anchor(1f, 1f)
                        .infoWindowAnchor(0.5f, 0.5f)
                        .draggable(false)
                )
//                drawCurveOnMap(
//                    map,
//                    LatLng(latDestLocation.toDouble(), longDestLocation.toDouble()),
//                    LatLng(locationByNetwork?.latitude!!, locationByNetwork?.longitude!!)
//                )
                isRouteDraw = true
                binding.startNavigationButton.visibility = View.VISIBLE
                setNavClickListener()


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!latDestLocation.isEmpty() && !longDestLocation.isEmpty()) {

            builder.include(LatLng(locationByNetwork!!.latitude,locationByNetwork!!.longitude))
            builder.include(LatLng(latDestLocation.toDouble(),longDestLocation.toDouble()))
            val bounds = builder.build()
            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val padding = (width * 0.20).toInt() // offset from edges of the map 10% of screen
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height/2, padding)
            map.animateCamera(cu)

        } else {

            recenterCamera()

        }
    }


    fun drawCurveOnMap(googleMap: GoogleMap, latLng1: LatLng, latLng2: LatLng) {

//        //Adding marker is optional here, you can move out from here.
//        googleMap.addMarker(
//            MarkerOptions().position(latLng1).icon(BitmapDescriptorFactory.defaultMarker())
//        )
//        googleMap.addMarker(
//            MarkerOptions().position(latLng2).icon(BitmapDescriptorFactory.defaultMarker())
//        )

        val k = 0.2 //curve radius
        var h = SphericalUtil.computeHeading(latLng1, latLng2)
        var d = 0.0
        val p: LatLng?

        //The if..else block is for swapping the heading, offset and distance
        //to draw curve always in the upward direction
        if (h < 0) {
            d = SphericalUtil.computeDistanceBetween(latLng2, latLng1)
            h = SphericalUtil.computeHeading(latLng2, latLng1)
            //Midpoint position
            p = SphericalUtil.computeOffset(latLng2, d * 0.5, h)
        } else {
            d = SphericalUtil.computeDistanceBetween(latLng1, latLng2)

            //Midpoint position
            p = SphericalUtil.computeOffset(latLng1, d * 0.5, h)
        }

        //Apply some mathematics to calculate position of the circle center
        val x = (1 - k * k) * d * 0.5 / (2 * k)
        val r = (1 + k * k) * d * 0.5 / (2 * k)

        val c = SphericalUtil.computeOffset(p, x, h + 90.0)

        //Calculate heading between circle center and two points
        val h1 = SphericalUtil.computeHeading(c, latLng1)
        val h2 = SphericalUtil.computeHeading(c, latLng2)

        //Calculate positions of points on circle border and add them to polyline options
        val numberOfPoints = 1000 //more numberOfPoints more smooth curve you will get
        val step = (h2 - h1) / numberOfPoints

        //Create PolygonOptions object to draw on map
        val polygon = PolygonOptions()


        //Create a temporary list of LatLng to store the points that's being drawn on map for curve
        val temp = arrayListOf<LatLng>()

        //iterate the numberOfPoints and add the LatLng to PolygonOptions to draw curve
        //and save in temp list to add again reversely in PolygonOptions
        for (i in 0 until numberOfPoints) {
            val latlng = SphericalUtil.computeOffset(c, r, h1 + i * step)
            polygon.add(latlng) //Adding in PolygonOptions
            temp.add(latlng)    //Storing in temp list to add again in reverse order
        }

        //iterate the temp list in reverse order and add in PolygonOptions
        for (i in (temp.size - 1) downTo 1) {
            polygon.add(temp[i])
        }

        polygon.strokeColor(Color.BLUE)
        polygon.strokeWidth(12f)
//        polygon.strokePattern(listOf(Dash(30f), Gap(50f))) //Skip if you want solid line
        googleMap.addPolygon(polygon)

        temp.clear() //clear the temp list
    }


    private fun getKms(distance: Double): Double {
        return distance * 0.621371;

    }

    private fun getMiles(distance: Double): Double {
        return distance * 1.60934;

    }


    private fun clickEvent() {

        binding.backButton.setOnClickListener {
            EventBus.getDefault().post(NavEvent(Constants.NAV_BACK))
        }

        binding.myLocationText.setOnClickListener {
            isFindAddressClick = false
            val intent = Intent(requireContext(), PickLocationActivity::class.java)
            intent.putExtra("isFromLocationClick", true)
            // intent.putExtra(Constants.ADDRESS_MY_LOCATION, addressMyLocation)
            intent.putExtra(Constants.ADDRESS_FROM_MY_LOCATION, addressFromMyLocation)
            // intent.putExtra(Constants.ADDRESS_TO_MY_LOCATION, addressToMyLocation)
            intent.putExtra(Constants.LATITUDE_MY_LOCATION, latSrcLocation)
            intent.putExtra(Constants.LONGITUDE_MY_LOCATION, longSrcLocation)
            resultLauncher.launch(intent)
        }


//      get search result


        binding.destinationText.setOnClickListener {
            isFindAddressClick = false
            val intent = Intent(requireContext(), PickLocationActivity::class.java)

            intent.putExtra("isToLocationClick", true)
            // intent.putExtra(Constants.ADDRESS_MY_LOCATION, addressMyLocation)
            intent.putExtra(Constants.ADDRESS_TO_MY_LOCATION, addressToMyLocation)
            //  intent.putExtra(Constants.ADDRESS_FROM_MY_LOCATION, addressFromMyLocation)
            intent.putExtra(Constants.LATITUDE_MY_LOCATION, latSrcLocation)
            intent.putExtra(Constants.LONGITUDE_MY_LOCATION, longSrcLocation)
            destResultLauncher.launch(intent)

        }


        binding.backButton.setOnClickListener {
            EventBus.getDefault().post(NavEvent(Constants.NAV_BACK))
        }

        binding.showRouteButton.setOnClickListener {
        }

    }


    private var isRouteDraw = false
    private fun setNavClickListener() {
        binding.startNavigationButton.setOnClickListener {
            if (isRouteDraw) {
//                if () {
//                }
                showInterAds {
                    uriGoToMap()
                }
            } else {
                if (binding.myLocationText.text.isNullOrBlank() || binding.destinationText.text.isNullOrBlank())
                    Toast.makeText(
                        requireContext(),
                        "Please select both start snd destination location to begin Navigation",
                        Toast.LENGTH_LONG
                    ).show()
            }
        }
    }

    private fun uriGoToMap(routeType: String = "d") {
        val uri = Uri.parse(
            "http://maps.google.com/maps?saddr=" + latSrcLocation + "," + longSrcLocation
                    + " &daddr=" + latDestLocation + "," + longDestLocation + " &dirflg=" + routeType
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

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


    var navigationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                (requireActivity().application as RawGpsApp).appContainer.prefs.setString(
                    Constants.ADDRESS_TO_LOCATION,
                    ""
                )
                (requireActivity().application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LATITUDE_TO_LOCATION,
                    ""
                )
                (requireActivity().application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LONGITUDE_TO_LOCATION,
                    ""
                )
                if (isRouteDraw) {
                    isRouteDraw = false
                }
                try {


                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                getDstAddrFromPrefs()
            }

        }


    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                isFromLocationClick = data?.getBooleanExtra("isFromLocationClick", false)!!
                isToLocationClick = data.getBooleanExtra("isToLocationClick", false)
                isUserFrmMyLocationClick = data.getBooleanExtra("isMyLocationClick", false)
                isUserFrmMapLocationClick = data.getBooleanExtra("isSelectOnMapClick", false)
                getLocationFromPref()


            }
        }


    var destResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                isFromLocationClick = data?.getBooleanExtra("isFromLocationClick", false)!!
                isToLocationClick = data.getBooleanExtra("isToLocationClick", false)
                isUserFrmMyLocationClick = data.getBooleanExtra("isMyLocationClick", false)
                isUserFrmMapLocationClick = data.getBooleanExtra("isSelectOnMapClick", false)
                getDstAddrFromPrefs()

            }
        }


    private var distanceUnit = Constants.KM


    ///check textfield of myLocation is empty or not if empty save current location to
    // sharedpreference and update sharepreference to show current location in mylocation field

    private fun checkMyLocation() {

//        Toast.makeText(context,"miles"+addressFromMyLocation,Toast.LENGTH_SHORT).show()
//        Toast.makeText(context,"miles"+road.mDuration,Toast.LENGTH_SHORT).show()
//        Toast.makeText(context,"miles"+ addressFromMyLocation,Toast.LENGTH_SHORT).show()
//        binding.myLocationText.text = addressFromLocationSharedPref
        (requireActivity().application as RawGpsApp).appContainer.prefs.setString(
            Constants.ADDRESS_FROM_LOCATION,
            addressFromMyLocation
        )
        (requireActivity().application as RawGpsApp).appContainer.prefs.setString(
            Constants.LATITUDE_FROM_LOCATION,
            latSrcLocation
        )
        (requireActivity().application as RawGpsApp).appContainer.prefs.setString(
            Constants.LONGITUDE_FROM_LOCATION,
            longSrcLocation
        )
        getLocationFromPref()
    }

    var canShowNativeAd = false
    var adsReloadTry = 0


    override fun onDestroy() {
        if (::networkLocationListener.isInitialized) {

            locationManager.removeUpdates(networkLocationListener)

        }
        super.onDestroy()
    }


    var canShowInter = false


    //Loading Interstitial Ads
    private fun loadInter() {

        if ((requireActivity().application as RawGpsApp).appContainer.myAdsUtill.mInterstitialAd == null) {
            (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.loadInterestitial(
                requireActivity()
            ) {
                canShowInter = it
            }
        } else {
            canShowInter = true
        }

    }

    private fun showInterAds(shown: (success: Boolean) -> Unit) {
        if (canShowInter) {
            (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.showInterestitial(
                requireActivity()
            ) {
                shown(it)
            }
        } else {
            shown(false)
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


        } catch (e: Exception) {
            e.printStackTrace()
        }
//        googleMap.setOnCameraIdleListener {
//            // Cleaning all the markers.
//            if (googleMap != null) {
//                googleMap.clear()
//            }
//        }
        setUpLayersControl()
        DialogUtils.dismissLoading()
    }

    private var initialTilt = 25f
    private fun toggle2D3D() {
        try {
            if (latSrcLocation.isNullOrEmpty() || longSrcLocation.isNullOrEmpty()) {
                return
            }
            val cameraPos: CameraPosition = CameraPosition.Builder()
                .target(LatLng(latSrcLocation.toDouble(), longSrcLocation.toDouble()))
                .zoom(15.5f)
                .bearing(0f)
                .tilt(
                    if (initialTilt == 25f) {
                        initialTilt = 60f
                        60f
                    } else {
                        initialTilt = 25f
                        25f
                    }
                )
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

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }
    private fun setUpLayersControl() {
        binding.layersSelectionButton.setOnClickListener {
            LayersDialog.showMapsLayersDialog(requireActivity(),map){
                toggle2D3D()
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
            .target(LatLng(latSrcLocation!!.toDouble(), longSrcLocation!!.toDouble()))
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
    }

    private fun loadBanner() {
        (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
            requireActivity(),
            binding.adsParent,
            AdSize.LARGE_BANNER,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {
                    Log.d("Add Load Callback", "is ad loaded========>" + success)
                }
            })
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
            // Toast.makeText(requireContext(), R.string.map_not_ready, Toast.LENGTH_SHORT).show()
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
////////////////////////////////////////////////////////////////////////////////////////////////////

}