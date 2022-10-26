package com.otl.gps.navigation.map.route.view.fragment.places

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.location.LocationListener
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import application.RawGpsApp
import com.abl.gpstracker.navigation.maps.routefinder.app.utils.GeoCoderAddress
import com.abl.gpstracker.navigation.maps.routefinder.app.view.maps.PlacesViewModel
import com.google.android.gms.ads.AdSize
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.adapters.SearchPlacesAdapter
import com.otl.gps.navigation.map.route.databinding.ActivitySerchPlacesBinding
import com.otl.gps.navigation.map.route.databinding.FragmentPlacesBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.interfaces.PlacesAdapterListener
import com.otl.gps.navigation.map.route.model.NavEvent
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.Constants.UPDATE_CANCEL_BUTTON
import com.otl.gps.navigation.map.route.utilities.Constants.preparePlacesList
import com.otl.gps.navigation.map.route.utilities.DialogUtils
import org.greenrobot.eventbus.EventBus
import java.text.DateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [PlacesSerchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class PlacesSerchFragment : Fragment() , OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private lateinit var binding: FragmentPlacesBinding
    private lateinit var map: GoogleMap
    private var mapStyle: String = "default"
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private var isMapLoaded = false
    private var latitude: String = ""
    private var longitude: String = ""
    private var TAG = "SelectLocationActivity"




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentPlacesBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        loadMap()
        //Loading Banner Ads
        loadBanner()
        loadInter()
        setListeners()
        setupRv()


        ////////////////////////////////////////////////////////////////////////////
        mRequestingLocationUpdates = false
        mLastUpdateTime = ""

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mSettingsClient = LocationServices.getSettingsClient(requireActivity())

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
        /////////////////////////////////////////////////////////////////////////////////
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                    startLocationUpdates()
                }

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {}
                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()


    }

    private fun setListeners() {

        binding.ivBack.setOnClickListener {
            EventBus.getDefault().post(NavEvent(Constants.NAV_BACK))
        }
        binding.searchButton.setOnClickListener {

            if (binding.searchBox.text.toString().isNotEmpty()) {

                startMap( binding.searchBox.text.toString())

            } else {
                Toast.makeText(requireContext(), "No search terms found", Toast.LENGTH_SHORT).show()
                binding.searchBox.requestFocus()

            }
        }

    }


    public fun setupRv(){
        binding.bottomSheet.placesRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = SearchPlacesAdapter(preparePlacesList(),requireContext(),object:
                PlacesAdapterListener {
                override fun clickItem(poiName: String) {
                    startMap(poiName)
                }
            })
        }
    }



    private fun loadMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }
    override fun onDestroy() {
        super.onDestroy()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    private fun startMap(type: String) {

        val gmmIntentUri =
            Uri.parse("geo:" + mCurrentLocation?.latitude + "," + mCurrentLocation?.longitude + "?q=" + type)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            showInterAds {
                startActivity(mapIntent)
            }
        }else{
           Toast.makeText(requireContext(),"Cannot Perform This Action In Your Region",Toast.LENGTH_SHORT).show()
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    var canShowInter = false


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
            isMapLoaded = true
            recenterCamera()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        DialogUtils.dismissLoading()


    }

    @Suppress("UNUSED_PARAMETER")
    fun recenterCamera() {
        val cameraPos: CameraPosition = CameraPosition.Builder()
            .target(LatLng(mCurrentLocation?.latitude!!.toDouble(), mCurrentLocation?.longitude!!.toDouble()))
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
    private fun loadBanner() {
        (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
            requireActivity(),
            binding.adsParent,
            AdSize.LARGE_BANNER,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {

                }
            }
        )
    }

    private fun hideShowInappsButton(){
        if ((requireActivity().application as RawGpsApp).appContainer.prefs.areAdsRemoved()) {
            EventBus.getDefault().post(NavEvent(UPDATE_CANCEL_BUTTON))

        }
    }


///+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
///=============================================================================================
///+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Provides access to the Location Settings API.
     */
    private var mSettingsClient: SettingsClient? = null

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private var mLocationSettingsRequest: LocationSettingsRequest? = null

    /**
     * Callback for Location events.
     */
    private var mLocationCallback: LocationCallback? = null

    /**
     * Represents a geographical location.
     */
    private var mCurrentLocation: Location? = null

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private var mRequestingLocationUpdates: Boolean? = null

    /**
     * Time when the location was updated represented as a String.
     */
    private var mLastUpdateTime: String? = null

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    KEY_REQUESTING_LOCATION_UPDATES
                )
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING)
            }
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     *
     *
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     *
     *
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest!!.fastestInterval =
            FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Creates a callback for receiving location events.
     */
    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mCurrentLocation = locationResult.lastLocation
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                recenterCamera()
                stopLocationUpdates()
            }
        }
    }

    /**
     * Uses a [com.google.android.gms.location.LocationSettingsRequest.Builder] to build
     * a [com.google.android.gms.location.LocationSettingsRequest] that is used for checking
     * if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> Log.i(TAG, "User agreed to make required location settings changes.")
                Activity.RESULT_CANCELED -> { Log.i(TAG, "User chose not to make required location settings changes.")
                    mRequestingLocationUpdates = false
                }
            }
        }
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    fun startUpdatesButtonHandler(view: View?)
    {
        if (!mRequestingLocationUpdates!!) {
            mRequestingLocationUpdates = true
            startLocationUpdates()
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     */
    fun stopUpdatesButtonHandler(view: View?) {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocationUpdates()
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient!!.checkLocationSettings(mLocationSettingsRequest!!)
            .addOnSuccessListener(requireActivity()) {
                Log.i(TAG, "All location settings are satisfied.")
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest!!,
                    mLocationCallback!!, Looper.myLooper()!!
                )
            }
            .addOnFailureListener(requireActivity()) { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i(
                            TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings "
                        )
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings."
                        Log.e(TAG, errorMessage)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        mRequestingLocationUpdates = false
                    }
                }
            }
    }






    /**
     * Removes location updates from the FusedLocationApi.
     */
    private fun stopLocationUpdates() {
        if (!mRequestingLocationUpdates!!) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.")
            return
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
            .addOnCompleteListener(requireActivity()) {
                mRequestingLocationUpdates = false
            }
    }

    override fun onResume() {
        super.onResume()
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
//        if (mRequestingLocationUpdates && checkPermissions()) {
//            startLocationUpdates();
//        } else if (!checkPermissions()) {
//            requestPermissions();
//        }
    }

    override fun onPause() {
        super.onPause()

        // Remove location updates to save battery.
        stopLocationUpdates()
    }

    /**
     * Stores activity data in the Bundle.
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates!!)
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation)
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime)
        super.onSaveInstanceState(savedInstanceState)
    }

    companion object {
        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Code used in requesting runtime permissions.
         */
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        /**
         * Constant used in the location settings dialog.
         */
        private const val REQUEST_CHECK_SETTINGS = 0x1

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000

        /**
         * The fastest rate for active location updates. Exact. Updates will never be more frequent
         * than this value.
         */
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2

        // Keys for storing activity state in the Bundle.
        private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
        private const val KEY_LOCATION = "location"
        private const val KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string"
        private const val TAG = "MP aCTIVITY"
    }





    val locationForGfResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            // Handle the Intent
        }
    }


}