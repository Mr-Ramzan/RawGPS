package com.otl.gps.navigation.map.route.view.activity.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import application.RawGpsApp
import com.abl.gpstracker.navigation.maps.routefinder.app.utils.GeoCoderAddress
import com.abl.gpstracker.navigation.maps.routefinder.app.view.maps.PlacesViewModel
import com.bumptech.glide.Glide
import com.otl.gps.navigation.map.route.R
import com.google.android.gms.ads.AdSize
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.otl.gps.navigation.map.route.databinding.ActivitySerchPlacesBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.interfaces.PlacesAdapterListener
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.retrofitApi.ApiHelper
import com.otl.gps.navigation.map.route.utilities.retrofitApi.RetrofitBuilder
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.otl.gps.navigation.map.route.databinding.ActivityLocationFromGoogleMapBinding
import com.otl.gps.navigation.map.route.utilities.DialogUtils


class PickLocationActivity : AppCompatActivity(), PlacesAdapterListener, OnMapReadyCallback,
GoogleMap.OnMyLocationButtonClickListener,
GoogleMap.OnMyLocationClickListener {

    lateinit var locationManager: LocationManager
    private var locationByNetwork: Location? = null
    private var isMapLoaded = false
    private var marker: Marker? = null
    private lateinit var networkLocationListener: LocationListener
    private var latitude: String = ""
    private var longitude: String = ""
    private lateinit var geoCoderAddress: GeoCoderAddress
    private lateinit var map: GoogleMap
    private var mapStyle: String = "default"
    private lateinit var binding: ActivitySerchPlacesBinding
    private var TAG = "SelectLocationActivity"
    private lateinit var viewModel: PlacesViewModel
    private var fromLocation = false
    private var toLocation = false
    private var isFindAddressClick = false
    private lateinit var geocoAddress: GeoCoderAddress
    private var address: String? = null
    private var searchLatitude: String? = null
    private var searchLongitude: String? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySerchPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.searchResultsView.apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            isVisible = true
        }
        binding.searchBox.performClick()
        geocoAddress = GeoCoderAddress(this)
        clickEvent()
        setupViewModel()
        loadBanner()
//        ==================================================
        loadMap()
        //loadNativeBanner()
        loadBanner()
        checkPermissionBeforeLocation()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geoCoderAddress = GeoCoderAddress(this)

    }


    override fun onDestroy() {
        super.onDestroy()
        if (::networkLocationListener.isInitialized) {
            locationManager.removeUpdates(networkLocationListener)
        }
    }


    private fun loadMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this@PickLocationActivity)
        getLocationFromPref()

    }

    /**
     * Checking Permissions using Dexter
     */

    private fun checkPermissionBeforeLocation() {
        if (checkGPSStatus()) {
            Dexter.withContext(this@PickLocationActivity)
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
                                this@PickLocationActivity,
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
                            this@PickLocationActivity,
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
                this@PickLocationActivity,
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
                this@PickLocationActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

            val dialog = AlertDialog.Builder(this@PickLocationActivity)
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

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiServicePlaces))
        ).get(PlacesViewModel::class.java)

    }


    fun Context.hideKeyboard(view: View) {

        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

    }


    private fun clickEvent() {
        fromLocation = intent.getBooleanExtra("isFromLocationClick", false)
        toLocation = intent.getBooleanExtra("isToLocationClick", false)
        isFindAddressClick = intent.getBooleanExtra("isFindAddressClick", false)
        val addressMyLocation = intent.getStringExtra(Constants.ADDRESS_MY_LOCATION)
        val addressFromMyLocation = intent.getStringExtra(Constants.ADDRESS_FROM_MY_LOCATION)
        val addressToMyLocation = intent.getStringExtra(Constants.ADDRESS_TO_MY_LOCATION)
        val latitudeMyLocation = intent.getStringExtra(Constants.LATITUDE_MY_LOCATION)
        val longitudeMyLocation = intent.getStringExtra(Constants.LONGITUDE_MY_LOCATION)


//        binding.llSelectOnMap.setOnClickListener {
//            if (fromLocation) {
//                val intent = Intent(this, LocationFromGoogleMapActivity::class.java)
//                intent.putExtra("isFromLocationClick", true)
//                intent.putExtra("isSelectOnMapClick", true)
//                pickLocationResultsLauncher.launch(intent)
//            }
//            if (toLocation) {
//                val intent = Intent(this, LocationFromGoogleMapActivity::class.java)
//                intent.putExtra("isToLocationClick", true)
//                intent.putExtra("isSelectOnMapClick", true)
//                pickLocationResultsLauncher.launch(intent)
//            }
//            if (isFindAddressClick) {
//                val intent = Intent(this, LocationFromGoogleMapActivity::class.java)
//                intent.putExtra("isFindAddressClick", true)
//                pickLocationResultsLauncher.launch(intent)
//            }
//        }




        binding.closeResultsBtn.setOnClickListener {
            binding.llSelect.visibility = View.GONE
        }

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


                        val intent = Intent()
                        intent.putExtra("address", address)
                        intent.putExtra("isFromLocationClick", fromLocation)
                        intent.putExtra("isToLocationClick", toLocation)
                        intent.putExtra("isSelectOnMapClick", true)
                        setResult(Activity.RESULT_OK, intent)
                        finish()

                }
            }


        ////////////////////////////////////////////////////////////////////////////////////////////

        binding.searchButton.setOnClickListener {
            binding.llSelect.visibility=View.VISIBLE

            if (binding.searchBox.text.toString().isNotEmpty()) {
                binding.searchResultsView.search(binding.searchBox.text.toString())

            } else {
                Toast.makeText(this, "No search terms found", Toast.LENGTH_SHORT).show()
                binding.searchBox.requestFocus()

            }
        }


        binding.searchBox.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                binding.searchResultsView.isVisible = hasFocus
                if(!hasFocus){

                   val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java) as InputMethodManager
                   imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

                }
            }


        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.searchButton.performClick()
                return@setOnEditorActionListener true
            }
            false
        }


        binding.searchResultsView.addSearchListener(object : SearchResultsView.SearchListener {

//            override fun onCategoryResult(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo)
//            {
//                Toast.makeText(applicationContext, "Category search results shown", Toast.LENGTH_SHORT).show()
//            }

//            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
//                Toast.makeText(applicationContext, "Search suggestions shown", Toast.LENGTH_SHORT).show()
//            }


            override fun onSearchResult(
                searchResult: SearchResult,
                responseInfo: ResponseInfo
            ) {

                Log.d("SearchResults", "=====>$searchResult")
                //----------------------------------------------------------------------------------
                address = formattedAddress(searchResult.name, searchResult.address!!)
                searchLatitude = searchResult.coordinate?.latitude().toString()
                searchLongitude = searchResult.coordinate?.longitude().toString()
                fromLocation = intent.getBooleanExtra("isFromLocationClick", false)
                toLocation = intent.getBooleanExtra("isToLocationClick", false)
                isFindAddressClick = intent.getBooleanExtra("isFindAddressClick", false)

                //save search latitude and longitude in sharedpreferences
                if (fromLocation) {
                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_FROM_LOCATION, address)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_FROM_LOCATION, searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_FROM_LOCATION, searchLongitude)
                }
                if (toLocation) {
                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)

                    (application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_TO_LOCATION, address)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_TO_LOCATION, searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_TO_LOCATION, searchLongitude)
                }


                val intent = Intent()
                intent.putExtra("searchResultLat", searchResult.coordinate?.latitude().toString())
                intent.putExtra("searchResultLong", searchResult.coordinate?.longitude().toString())
                setResult(RESULT_OK, intent)
                finish()
                //----------------------------------------------------------------------------------
            }

            override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
//                binding.simpleSearchView.setQuery(historyRecord.name, true)
                binding.searchResultsView.search(historyRecord.name)
            }

            override fun onPopulateQueryClicked(
                suggestion: SearchSuggestion,
                responseInfo: ResponseInfo
            ) {
//                binding.simpleSearchView.setQuery(suggestion.name, true)
                binding.searchResultsView.search(suggestion.name)
            }

            override fun onFeedbackClicked(responseInfo: ResponseInfo) {
            }
        })


        //------------------------------------------------------------------------------------//
        binding.ivBack.setOnClickListener {
            finish()
        }


    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    fun View.hideKeyboard() {
        val imm = ContextCompat.getSystemService(context, InputMethodManager::class.java) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    fun formattedAddress(name: String, address: SearchAddress): String {

        var sddressString = ""

        if (!name.isNullOrEmpty()) {
            sddressString += name
        }
        if (!address.houseNumber.isNullOrEmpty()) {
            sddressString += address.houseNumber
        }

        if (!address.street.isNullOrEmpty()) {
            sddressString += address.street
        }

        if (!address.place.isNullOrEmpty()) {
            sddressString += address.place
        }

        if (!address.locality.isNullOrEmpty()) {
            sddressString += address.locality
        }

        if (!address.district.isNullOrEmpty()) {
            sddressString += address.district
        }
        if (!address.country.isNullOrEmpty()) {
            sddressString += address.country
        }
        return sddressString

    }


    override fun clickItem(address: String) {
        // Toast.makeText(this, address, Toast.LENGTH_LONG).show()
        val latLng = geocoAddress.getLatLng(address)
        val latitude = latLng?.latitude.toString()
        val longitude = latLng?.longitude.toString()
        Log.d(TAG, latitude.toString())
        Log.d(TAG, longitude.toString())


        if (fromLocation) {
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.ADDRESS_FROM_LOCATION,
                address
            )

            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.LATITUDE_FROM_LOCATION,
                latitude
            )
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.LONGITUDE_FROM_LOCATION,
                longitude
            )

            val intent = Intent()
            intent.putExtra("isFromLocationClick", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        if (toLocation) {
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.ADDRESS_TO_LOCATION, address
            )
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.LATITUDE_TO_LOCATION,
                latitude
            )
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.LONGITUDE_TO_LOCATION,
                longitude
            )

            val intent = Intent()
            intent.putExtra("isToLocationClick", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }


    }

    private var pickLocationResultsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }

    private var sutoCompleteResultsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data

                //----------------------------------------------------------------------------------
                val feature = PlaceAutocomplete.getPlace(data)
                address = feature.text()
                searchLatitude = (feature.geometry() as Point).latitude().toString()
                searchLongitude = (feature.geometry() as Point).longitude().toString()


                fromLocation = intent.getBooleanExtra("isFromLocationClick", false)
                toLocation = intent.getBooleanExtra("isToLocationClick", false)
                isFindAddressClick = intent.getBooleanExtra("isFindAddressClick", false)

                //save search latitude and longitude in sharedpreferences
                if (fromLocation) {
                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_FROM_LOCATION, address)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_FROM_LOCATION, searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_FROM_LOCATION, searchLongitude)
                }
                if (toLocation) {
                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)

                    (application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_TO_LOCATION, address)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_TO_LOCATION, searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_TO_LOCATION, searchLongitude)
                }


                val intent = Intent()
                intent.putExtra(
                    "searchResultLat",
                    (feature.geometry() as Point).latitude().toString()
                )
                intent.putExtra(
                    "searchResultLong",
                    (feature.geometry() as Point).longitude().toString()
                )
                setResult(RESULT_OK, intent)
                finish()
                //----------------------------------------------------------------------------------


            }
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

        if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
            origin = Point.fromLngLat(longitude.toDouble(), latitude.toDouble())
        }

        if (locationByNetwork == null && longitude.isNotEmpty() && latitude.isNotEmpty()) {
            locationByNetwork = Location("")//provider name is unnecessary
            locationByNetwork?.latitude = origin!!.latitude()//your coords of course
            locationByNetwork?.longitude = origin.longitude()
            recenterCamera()
        } else {
            recenterCamera()
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            map = googleMap
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
                            .position(LatLng(latitude.toDouble(), longitude.toDouble()))
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
                        binding.tvAddress.text = it
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
            .target(LatLng(latitude.toDouble(), longitude.toDouble()))
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

