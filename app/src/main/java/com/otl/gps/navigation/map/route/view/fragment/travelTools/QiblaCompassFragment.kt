package com.otl.gps.navigation.map.route.view.fragment.travelTools

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import application.RawGpsApp
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdSize
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.FragmentQiblaCompassBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.view.fragment.travelTools.qiblacompass.CompassQibla
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

class QiblaCompassFragment : Fragment(){



    /**
     * Called when the activity is first created.
     */

    //private var adsUtill: MyAdsUtill? = null
    private var currentDegree = 0f
    private var sensorManager: SensorManager? = null
    private lateinit var binding: FragmentQiblaCompassBinding
    private var currentCompassDegree = 0f
    private var currentNeedleDegree = 0f


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentQiblaCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupBg() {

        try {
            Glide.with(this).load(R.drawable.home_bg).into(binding.bgImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBanner()
        setupBg()
        sensorManager = requireActivity().getSystemService(Activity.SENSOR_SERVICE) as SensorManager

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


        setListeners()
    }

    private fun setListeners(){
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
//      sensorManager!!.registerListener( this,sensorManager!!.getDefaultSensor(3),   SensorManager.SENSOR_DELAY_GAME  )
        updateUI()

    }

    override fun onPause() {
        super.onPause()
        //sensorManager!!.unregisterListener(this)
        stopLocationUpdates()
    }

//    override fun onSensorChanged(event: SensorEvent) {
//
//        val degree = Math.round(event.values[0]).toFloat()
//        binding!!.txtDegrees.text = "Rotation: " + java.lang.Float.toString(degree) + " degrees"
//        val ra = RotateAnimation(
//            currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f,
//            Animation.RELATIVE_TO_SELF, 0.5f
//        )
//        ra.duration = 120
//        ra.fillAfter = true
//        binding!!.imgCompass.startAnimation(ra)
//        currentDegree = -degree
//
//    }

//    override fun onAccuracyChanged(p1: Sensor, p2: Int) {}
//    private fun setListeners() {
//        binding!!.backButton.setOnClickListener { requireActivity().onBackPressed() }
//    }

    private fun loadBanner() {
        (requireActivity().application as RawGpsApp).appContainer.myAdsUtill?.AddSquareBannerToLayout(
            requireActivity(),
            binding.adsContainer,
            AdSize.MEDIUM_RECTANGLE,
            object : AdLoadedCallback {

                override fun addLoaded(success: Boolean?) {}

            })
    }

    private fun setUpQiblaComapassBuilder(){
        try {
            CompassQibla.Builder(requireActivity() as AppCompatActivity, mCurrentLocation!!)
                .onGetLocationAddress { address ->
                    binding.tvLocation.text = buildString {
                        append(address.locality)
                        append(", ")
                        append(address.subAdminArea)
                    }
                }
                .onDirectionChangeListener { qiblaDirection ->
                        val rotateCompass = RotateAnimation(
                            currentCompassDegree,
                            qiblaDirection.compassAngle,
                            Animation.RELATIVE_TO_SELF,
                            0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f
                        ).apply {
                            duration = 1000
                        }
                        currentCompassDegree = qiblaDirection.compassAngle
                        binding.imgCompass.startAnimation(rotateCompass)


                        val rotateNeedle = RotateAnimation(
                            currentNeedleDegree,
                            qiblaDirection.needleAngle,
                            Animation.RELATIVE_TO_SELF,
                            0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f
                        ).apply {
                            duration = 1000
                        }
                        currentNeedleDegree = qiblaDirection.needleAngle
                        binding.qiblaPointer.startAnimation(rotateNeedle)

                }
                .build()
        }catch (e:Exception){e.printStackTrace()}
    }
//==============================================================================================
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
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
    //============================================================================================//

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
            updateUI()
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
        mLocationRequest!!.priority = Priority.PRIORITY_HIGH_ACCURACY
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


                setUpQiblaComapassBuilder()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> Log.i(
                    TAG, "User agreed to make required location settings changes."
                )
                Activity.RESULT_CANCELED -> {
                    Log.i(TAG, "User chose not to make required location settings changes.")
                    mRequestingLocationUpdates = false
                    updateUI()
                }
            }
        }
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    fun startUpdatesButtonHandler(view: View?) {
        if (!mRequestingLocationUpdates!!) {
            mRequestingLocationUpdates = true
            setButtonsEnabledState()
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
    private fun startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient!!.checkLocationSettings(mLocationSettingsRequest!!)
            .addOnSuccessListener(requireActivity()) {
                Log.i(TAG, "All location settings are satisfied.")
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest!!,
                    mLocationCallback!!, Looper.myLooper()!!
                )
                updateUI()
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
                        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_LONG).show()
                        mRequestingLocationUpdates = false
                    }
                }
                updateUI()
            }
    }

    /**
     * Updates all UI fields.
     */
    private fun updateUI() {
//        setButtonsEnabledState();
//        updateLocationUI();
    }

    /**
     * Disables both buttons when functionality is disabled due to insuffucient location settings.
     * Otherwise ensures that only one button is enabled at any time. The Start Updates button is
     * enabled if the user is not requesting location updates. The Stop Updates button is enabled
     * if the user is requesting location updates.
     */
    private fun setButtonsEnabledState() {

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
                setButtonsEnabledState()
            }
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


    val locationForGfResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                // Handle the Intent
            }
        }







}