package com.otl.gps.navigation.map.route.view.activity.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import application.RawGpsApp
import com.abl.gpstracker.navigation.maps.routefinder.app.utils.GeoCoderAddress
import com.abl.gpstracker.navigation.maps.routefinder.app.view.maps.PlacesViewModel
import com.airbnb.lottie.L
import com.google.android.gms.ads.AdSize
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.ActivitySerchPlacesBinding
import com.otl.gps.navigation.map.route.databinding.AddressItemBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.interfaces.PlacesAdapterListener
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.DialogUtils
import com.otl.gps.navigation.map.route.utilities.FirebaseUtils
import com.otl.gps.navigation.map.route.utilities.retrofitApi.ApiHelper
import com.otl.gps.navigation.map.route.utilities.retrofitApi.RetrofitBuilder
import okhttp3.internal.assertThreadDoesntHoldLock


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
        //        binding.searchResultsView.apply {
        //            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
        //            isVisible = true
        //        }
        binding.searchBox.performClick()
        geocoAddress = GeoCoderAddress(this)
        clickEvent()
        setupViewModel()
        //==================================================
        loadMap()

        if (FirebaseUtils.isNativeUnderMaps) {
            loadNativeBanner()
        } else {
            loadBanner()
        }
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


        binding.SelectLocationButton
            .setOnClickListener {
                if (isMapLoaded) {

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
                    }

                    if (toLocation) {
                        (application as RawGpsApp).appContainer.prefs.setString(
                            Constants.ADDRESS_TO_LOCATION,
                            address
                        )
                        (application as RawGpsApp).appContainer.prefs.setString(
                            Constants.LATITUDE_TO_LOCATION,
                            latitude
                        )
                        (application as RawGpsApp).appContainer.prefs.setString(
                            Constants.LONGITUDE_TO_LOCATION,
                            longitude
                        )
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
            binding.searchButton.isEnabled = false

            binding.llSelect.visibility = View.VISIBLE

            if (binding.searchBox.text.toString().isNotEmpty()) {
                openAddressDropdown(fetchAddressesByName(binding.searchBox.text.toString()))
                binding.searchButton.isEnabled = true

            } else {
                Toast.makeText(this, "No search terms found", Toast.LENGTH_SHORT).show()
                binding.searchBox.requestFocus()
                binding.searchButton.isEnabled = true

            }
        }


        binding.searchBox.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.llSelect.visibility = View.VISIBLE
                } else {
                    binding.llSelect.visibility = View.GONE
                }
                if (!hasFocus) {

                    val imm = ContextCompat.getSystemService(
                        this,
                        InputMethodManager::class.java
                    ) as InputMethodManager
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


//        binding.searchResultsView.addSearchListener(object : SearchResultsView.SearchListener {
//
////            override fun onCategoryResult(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo)
////            {
////                Toast.makeText(applicationContext, "Category search results shown", Toast.LENGTH_SHORT).show()
////            }
//
////            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
////                Toast.makeText(applicationContext, "Search suggestions shown", Toast.LENGTH_SHORT).show()
////            }
//
//
//            override fun onSearchResult(
//                searchResult: SearchResult,
//                responseInfo: ResponseInfo
//            ) {
//
//                Log.d("SearchResults", "=====>$searchResult")
//                //----------------------------------------------------------------------------------
//                address = formattedAddress(searchResult.name, searchResult.address!!)
//                searchLatitude = searchResult.coordinate?.latitude().toString()
//                searchLongitude = searchResult.coordinate?.longitude().toString()
//                fromLocation = intent.getBooleanExtra("isFromLocationClick", false)
//                toLocation = intent.getBooleanExtra("isToLocationClick", false)
//                isFindAddressClick = intent.getBooleanExtra("isFindAddressClick", false)
//
//                //save search latitude and longitude in sharedpreferences
//                if (fromLocation) {
//                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)
//                    (application as RawGpsApp).appContainer.prefs.setString(
//                        Constants.ADDRESS_FROM_LOCATION,
//                        address
//                    )
//                    (application as RawGpsApp).appContainer.prefs.setString(
//                        Constants.LATITUDE_FROM_LOCATION,
//                        searchLatitude
//                    )
//                    (application as RawGpsApp).appContainer.prefs.setString(
//                        Constants.LONGITUDE_FROM_LOCATION,
//                        searchLongitude
//                    )
//                }
//                if (toLocation) {
//                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)
//
//                    (application as RawGpsApp).appContainer.prefs.setString(
//                        Constants.ADDRESS_TO_LOCATION,
//                        address
//                    )
//                    (application as RawGpsApp).appContainer.prefs.setString(
//                        Constants.LATITUDE_TO_LOCATION,
//                        searchLatitude
//                    )
//                    (application as RawGpsApp).appContainer.prefs.setString(
//                        Constants.LONGITUDE_TO_LOCATION,
//                        searchLongitude
//                    )
//                }
//
//
//                val intent = Intent()
//                intent.putExtra("searchResultLat", searchResult.coordinate?.latitude().toString())
//                intent.putExtra("searchResultLong", searchResult.coordinate?.longitude().toString())
//                setResult(RESULT_OK, intent)
//                finish()
//                //----------------------------------------------------------------------------------
//            }
//
//            override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
////                binding.simpleSearchView.setQuery(historyRecord.name, true)
//                binding.searchResultsView.search(historyRecord.name)
//            }
//
//            override fun onPopulateQueryClicked(
//                suggestion: SearchSuggestion,
//                responseInfo: ResponseInfo
//            ) {
////                binding.simpleSearchView.setQuery(suggestion.name, true)
//                binding.searchResultsView.search(suggestion.name)
//            }
//
//            override fun onFeedbackClicked(responseInfo: ResponseInfo) {
//            }
//        })
//

        //------------------------------------------------------------------------------------//
        binding.ivBack.setOnClickListener {
            finish()
        }


    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private fun fetchAddressesByName(name: String): ArrayList<Address>? {

        return GeoCoderAddress(this).getAddressFromName(name)

    }

    private fun openAddressDropdown(addresses: ArrayList<Address>?) {

        if (!addresses.isNullOrEmpty()) {

            binding.addressContainer.removeAllViews()
            for (item in addresses!!) {

                addAddressToLayout(item, binding.addressContainer)


            }

        } else {
            Toast.makeText(this, "Address Not Found!", Toast.LENGTH_LONG).show()
        }
    }

    private fun addAddressToLayout(address: Address, parent: LinearLayout) {
        var addressItemBinding: AddressItemBinding = AddressItemBinding.inflate(layoutInflater)
        addressItemBinding.placeNameText.text = address.featureName.toString()
        addressItemBinding.addressText.text = address.getAddressLine(0).toString()
        addressItemBinding.coordinatesText.text =
            address.latitude.toString() + "," + address.longitude.toString()
        addressItemBinding.root.setOnClickListener {
            Log.d("SearchResults", "=====>$address")
            //----------------------------------------------------------------------------------
            this.address = address.getAddressLine(0).toString()
            searchLatitude = address.latitude.toString()
            searchLongitude = address.longitude.toString()
            fromLocation = intent.getBooleanExtra("isFromLocationClick", false)
            toLocation = intent.getBooleanExtra("isToLocationClick", false)
            isFindAddressClick = intent.getBooleanExtra("isFindAddressClick", false)

            //save search latitude and longitude in sharedpreferences
            if (fromLocation) {
                Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)
                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.ADDRESS_FROM_LOCATION,
                    this.address
                )
                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LATITUDE_FROM_LOCATION,
                    searchLatitude
                )
                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LONGITUDE_FROM_LOCATION,
                    searchLongitude
                )
            }
            if (toLocation) {
                Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)

                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.ADDRESS_TO_LOCATION,
                    this.address
                )
                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LATITUDE_TO_LOCATION,
                    searchLatitude
                )
                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LONGITUDE_TO_LOCATION,
                    searchLongitude
                )
            }


            val intent = Intent()
            intent.putExtra("searchResultLat", searchLatitude.toString())
            intent.putExtra("searchResultLong", searchLongitude.toString())
            setResult(RESULT_OK, intent)
            finish()
            //----------------------------------------------------------------------------------
        }
        parent.addView(addressItemBinding.root)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    fun View.hideKeyboard() {
        val imm = ContextCompat.getSystemService(
            context,
            InputMethodManager::class.java
        ) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
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
        latitude = (application as RawGpsApp).appContainer.prefs.getString(
            Constants.LATITUDE_FROM_LOCATION,
            ""
        ).toString()
        longitude = (application as RawGpsApp).appContainer.prefs.getString(
            Constants.LONGITUDE_FROM_LOCATION,
            ""
        ).toString()
        var origin: LatLng? = null

        if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
            origin = LatLng( latitude.toDouble(),longitude.toDouble())
        }

        if (locationByNetwork == null && longitude.isNotEmpty() && latitude.isNotEmpty()) {
            locationByNetwork = Location("")//provider name is unnecessary
            locationByNetwork?.latitude = origin!!.latitude//your coords of course
            locationByNetwork?.longitude = origin.longitude
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
        if (latitude.isNullOrEmpty() || longitude.isNullOrEmpty()) {
            return
        }
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
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


    var canShowNativeAd = false
    var adsReloadTry = 0

    /**
     * Loading ads once if not loaded
     * there will be max three tries if once ad loaded it will not be loaded again but if not code will ask
     */
    private fun loadNativeBanner() {

        if (!(application as RawGpsApp).appContainer!!.prefs!!.areAdsRemoved()) {
            (application as RawGpsApp).appContainer?.myAdsUtill?.loadSmallNativeAd(
                this@PickLocationActivity,
                true,
                object : AdLoadedCallback {

                    override fun addLoaded(success: Boolean?) {

                        if (isDestroyed) {
                            return
                        }

                        if (success != null && success) {
                            adsReloadTry += 1
                            canShowNativeAd = success
                            showNativeAd()
                        } else {

                            /////////////////////////////
                            if (success == null || !success) {
                                canShowNativeAd = false
                                binding.adsParent.visibility = View.GONE

                            } else {
                                canShowNativeAd = success
                            }
                            /////////////////////////////
                            adsReloadTry += 1
                            if (adsReloadTry < Constants.ADS_RELOAD_MAX_TRIES) {
                                loadNativeBanner()
                            }
                        }
                    }
                }
            )
        }

    }

    private fun showNativeAd() {
        try {
            if (isDestroyed) {
                return
            }
            val isAdsRemoved =
                (application as RawGpsApp).appContainer.prefs.areAdsRemoved()
            if (!isAdsRemoved) {

                if (canShowNativeAd) {
                    (application as RawGpsApp).appContainer.myAdsUtill.showSmallNativeAd(
                        this,
                        Constants.START_NATIVE_SMALL,
                        binding.adsParent, true, false
                    )
                } else {
                    binding.adsParent.visibility = View.GONE
                }


            } else {
                binding.adsParent.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

