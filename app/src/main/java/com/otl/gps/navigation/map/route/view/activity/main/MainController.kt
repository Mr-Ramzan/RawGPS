package com.otl.gps.navigation.map.route.view.activity.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import application.RawGpsApp
import com.abl.gpstracker.navigation.maps.routefinder.app.utils.GeoCoderAddress
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.otl.gps.navigation.map.route.BuildConfig
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.ActivityMainBinding
import com.otl.gps.navigation.map.route.model.NavEvent
import com.otl.gps.navigation.map.route.receivers.NotificationReceiver
import com.otl.gps.navigation.map.route.utilities.*
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_MORE_APPS
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_POLICY
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_REMMOVE_ADS
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_SHARE
import com.otl.gps.navigation.map.route.utilities.Constants.BACK_AND_EXIT
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_COMING_SOON
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_COMPASS
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_CURRENCY_CONVERTER
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_EXPLORE_PLACES
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_LOCATION
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_PLACES_LIST
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_PREMIUM
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_PREVIEW_SAVED_PLACES
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_ROUTE
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_SATELLITE
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_SAVED_PLACES
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_SPEEDOMETER
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_TRAFFIC
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_TRAVEL_TOOLS
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_WEATHER
import com.otl.gps.navigation.map.route.utilities.Constants.NAV_BACK
import com.otl.gps.navigation.map.route.utilities.Constants.OPEN_DRAWER
import com.otl.gps.navigation.map.route.view.activity.goPro.PremiumActivity
import com.otl.gps.navigation.map.route.view.activity.spedometer.SpeedoMeterActivity
import com.otl.gps.navigation.map.route.view.fragment.dialogs.ExitDialogFragment
import com.otl.gps.navigation.map.route.view.fragment.places.PreviewSavedPlacesActivity
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.WeatherActivity
import com.prongbang.appupdate.AppUpdateInstallerListener
import com.prongbang.appupdate.AppUpdateInstallerManager
import com.prongbang.appupdate.InAppUpdateInstallerManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal
import java.text.DateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class MainController : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var addressFromMyLocation: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showConsent()
        EventBus.getDefault().register(this)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START)
        setupNoInternetPopup()
        initReminderNotification()
        loadInter()
        setListeners()
        try {
            navController =
                (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment).navController
            ////////////////////////////////////////////////////////////////////////////////////////
        } catch (e: Exception) {
            e.printStackTrace()
        }
        appUpdateInstallerManager.addAppUpdateListener(appUpdateInstallerListener)
        appUpdateInstallerManager.startCheckUpdate()
        ////////////////////////////////////////////////////////////////////////////
        mRequestingLocationUpdates = false
        mLastUpdateTime = ""

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
        checkPermissionAndGetLocation()
    }


    override fun onDestroy() {
        removePrefs()
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


    /**
     * setting listeners
     */
    private fun setListeners() {

//        close drawer on click
        binding.close.setOnClickListener {

            toggleDrawer()

        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        binding.cancelSub.setOnClickListener {
            EventBus.getDefault().post(NavEvent(Constants.ACTION_CANCEL_SUB))
        }


        binding.moreAppButton.setOnClickListener {
            try {
                val url = "https://play.google.com/store/apps/dev?id=5659298684953405740"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        binding.rateAppButton.setOnClickListener {
            try {
                val url =
                    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


        binding.shareAppButton.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
                var shareMessage = "\nLet me recommend you this application\n\n"
                shareMessage =
                    """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: java.lang.Exception) {
                //e.toString();
            }
        }


        binding.privacyPolicyButton.setOnClickListener {
            val url = "https://omegatechlab.blogspot.com/p/privacy-policy.html"
            gotoPrivacyPolicy(url)
        }
    }

    @Subscribe
    fun onEvent(eve: NavEvent) {

        when (eve.event) {

            OPEN_DRAWER -> {
//                toggleDrawer()
            }

            NAVIGATE_ROUTE -> {
                navigateRoute()
            }

            ACTION_POLICY -> {
                gotoPrivacyPolicy()
            }


            BACK_AND_EXIT -> {
                super.onBackPressed()
            }

            NAVIGATE_LOCATION -> {
                navigateMyLocation()

            }


            NAVIGATE_SATELLITE -> {
                navigateSatellite()
            }


            NAVIGATE_EXPLORE_PLACES -> {
                navigateExplorePlaces()

            }

            NAVIGATE_PREVIEW_SAVED_PLACES -> {
                showInterAds {
                    previewSavedPlaces()
                }
            }
            NAVIGATE_TRAVEL_TOOLS -> {

                navigateTravelTools()

            }

            NAVIGATE_TRAFFIC -> {
                navigateTraffic()
            }

            NAVIGATE_PLACES_LIST -> {
                showInterAds {
                    navigatePlaces()
                }
            }

            NAVIGATE_COMPASS -> {
                navigateCompass()

            }

            NAVIGATE_WEATHER -> {
                navigateWeather()
            }
            NAVIGATE_COMING_SOON -> {
                showComingSoon()
            }
            Constants.NAVIGATE_QIBLA_COMPASS -> {
                navigateQiblaCompass()
            }
            NAVIGATE_SAVED_PLACES -> {
                showInterAds {
                    navigateSavedPlaces()
                }
            }
            NAVIGATE_SPEEDOMETER -> {
                navigateSpeedoMeter()
            }
            NAVIGATE_PREMIUM -> {
                openPremiumPopup()
            }

            NAV_BACK -> {
                onBackPressed()

            }


            ACTION_REMMOVE_ADS -> {
            }

            ACTION_MORE_APPS -> {
                moreApps()
            }
            ACTION_SHARE -> {
                shareApp()
            }
        }

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun openPremiumPopup() {
        try {
            var intent: Intent = Intent(this, PremiumActivity::class.java)
            intent.putExtra(Constants.PREMIUM_FROM, Constants.FROM_HOME)
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun previewSavedPlaces() {
        try {
//            val navBuilder = NavOptions.Builder()
//            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
//                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
//            val bundle = bundleOf("title" to "")
//            navController.navigate(R.id.navigation_saved_places, bundle, navBuilder.build())

            var previewPlaceIntent =
                Intent(this, PreviewSavedPlacesActivity::class.java)
            startActivity(previewPlaceIntent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun navigateSavedPlaces() {
        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(
                R.anim.slide_out_left
            )
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(
                    R.anim.slide_out_right
                )
            val bundle = bundleOf("title" to "")
            navController.navigate(R.id.navigation_saved_places, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateSpeedoMeter() {
        try {
            startActivity(Intent(this, SpeedoMeterActivity::class.java))

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateQiblaCompass() {
        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(
                R.anim.slide_out_left
            )
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(
                    R.anim.slide_out_right
                )
            val bundle = bundleOf("style" to "")
            navController.navigate(R.id.navigation_qibla_compass, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateRoute() {

        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
            val bundle = bundleOf(
            )
            navController.navigate(R.id.navigation_routes_frag, bundle, navBuilder.build())

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


    }


    private fun navigateMyLocation() {

        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
            val bundle = bundleOf(
                "style" to "default",
                "title" to ""
            )
            navController.navigate(R.id.navigation_my_loc_frag, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun navigateSatellite() {
        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
            val bundle = bundleOf(
                "style" to "Satellite",
            )
            navController.navigate(R.id.navigation_my_loc_frag, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateTraffic() {
        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
            val bundle = bundleOf("style" to "Traffic")
            navController.navigate(R.id.navigation_my_loc_frag, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun navigatePlaces() {

        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
            val bundle = bundleOf("poi" to "", "title" to "")
            navController.navigate(R.id.navigation_places_list, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateExplorePlaces() {

        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
            val bundle = bundleOf("title" to "")
            navController.navigate(R.id.navigation_explore_places, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun navigateTravelTools() {

        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
            val bundle = bundleOf("title" to "")
            navController.navigate(R.id.navigation_travel_tool, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }


    private fun navigateWeather() {
        try {

            val intent = Intent(this, WeatherActivity::class.java)
            Helper.startActivity(this, intent, false)
//            val navBuilder = NavOptions.Builder()
//            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
//                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
//            val bundle = bundleOf(
//                "style" to "",
//            )
            //  navController.navigate(R.id.navigation_weather, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateCompass() {
        try {
            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
            val bundle = bundleOf("style" to "")
            navController.navigate(R.id.navigation_compass, bundle, navBuilder.build())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun navigateClock() {
        try {

            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
            val bundle = bundleOf("style" to "Background", "title" to "")
            // navController.navigate(R.id.navigation_clock, bundle, navBuilder.build())

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    /**
     * toggle Drawer
     */
    private fun toggleDrawer() {

//        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            binding.drawerLayout.closeDrawer(GravityCompat.START)
//        } else {
//            binding.drawerLayout.openDrawer(GravityCompat.START)
//        }

    }

    /**
     * Sharing App as playstore link
     */
    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                """
        ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
        """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * More Apps navigate to Developer page On Playstore
     */
    private fun moreApps() {
        val url = "https://play.google.com/store/apps/dev?id="
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }


//    private lateinit var adsUtill: MyAdsUtill

    override fun onBackPressed() {


        if (navController.currentDestination?.id == R.id.navigation_places_list || navController.currentDestination?.id == R.id.navigation_saved_places) {


            navController.navigateUp()

        } else if (navController.currentDestination?.id != R.id.nav_home) {

            showInterAds {
                navController.navigateUp()
            }
        } else {

            try {

                val addPhotoBottomDialogFragment: ExitDialogFragment =
                    ExitDialogFragment.newInstance()
                addPhotoBottomDialogFragment.show(
                    supportFragmentManager, "exit dialog!"
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }


//        PopupUtil.showDeleteDialog(
//            this,
//            "Exit App",
//            "Are you sure you want to exit?"
//        )
//        {
//            if (it) {
//                super.onBackPressed()
//            }
//        }

        }
    }

    private fun showComingSoon(msg: String = "Coming Soon!") {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * Open Policy URL
     */
    private fun gotoPrivacyPolicy(url: String = "https://theomegatechlab.blogspot.com/p/privacy-policy_27.html") {
        try {
            if (url.startsWith("http") || url.startsWith("https")) {
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    // Below condition checks if any app is available to handle intent
                    startActivity(this)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(this, "No Application found to open the Policy URL", Toast.LENGTH_SHORT)
                .show()
        }
    }


    /**
     *     Logic Related To Interstitial in main activity
     */
    var canShowInter = false
    private fun loadInter() {
        if ((application as RawGpsApp).appContainer.myAdsUtill.mInterstitialAd == null) {
            (application as RawGpsApp).appContainer.myAdsUtill.loadInterestitial(this) {
                canShowInter = it
            }
        } else {
            canShowInter = true
        }
    }

    private fun showInterAds(shown: (success: Boolean) -> Unit) {
        if (canShowInter) {
            (application as RawGpsApp).appContainer.myAdsUtill.showInterestitial(this) {
                shown(it)
            }
        } else {
            shown(false)
        }
    }


    /**
     * No Internet Handler Dialog
     */
    private fun setupNoInternetPopup() {
        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder(
            this,
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                    }
                }

                cancelable = false // Optional
                noInternetConnectionTitle = "No Internet" // Optional
                noInternetConnectionMessage =
                    "Check your Internet connection and try again." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Please turn on" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Mobile data" // Optional

                onAirplaneModeTitle = "No Internet" // Optional
                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
                pleaseTurnOffText = "Please turn off" // Optional
                airplaneModeOffButtonText = "Airplane mode" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
        }.build()
    }




    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Inapp Updates
    private val appUpdateInstallerManager: AppUpdateInstallerManager by lazy {
        InAppUpdateInstallerManager(this)
    }

    private val appUpdateInstallerListener by lazy {
        object : AppUpdateInstallerListener() {
            // On downloaded but not installed.
            override fun onDownloadedButNotInstalled() {
                updateConsentDialog()
            }

            // On failure
            override fun onFailure(e: Exception) {

            }

            // On not update
            override fun onNotUpdate() {

            }

            // On cancelled update
            override fun onCancelled() {

            }
        }
    }

    public fun updateConsentDialog() {
        var alertDialogBuilder = AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("An update has just been downloaded")
        alertDialogBuilder.setPositiveButton(
            "Install"
        ) { dialog, which ->
            try {
                appUpdateInstallerManager.completeUpdate()
                dialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        alertDialogBuilder.setNegativeButton("Cancel")
        { dialog, which ->
            try {
                dialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Location Related Stuf Dirty One
     */
    private fun checkPermissionAndGetLocation() {

        /////////////////////////////////////////////////////////////////////////////////
        Dexter.withContext(this)
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
        /////////////////////////////////////////////////////////////////////////////////
    }


    //check location service is on or off
    private fun checkGPSStatus(): Boolean {
        var gps_enabled = false
        var network_enabled = false

        var locationManager =
            this@MainController.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        } catch (ex: java.lang.Exception) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: java.lang.Exception) {
        }


        if (!gps_enabled && !network_enabled) {
            val dialog = AlertDialog.Builder(this@MainController)
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


////////////////////////////////////////////////////////////////////////////////////////////////
//            LOCATION CODE
////////////////////////////////////////////////////////////////////////////////////////////////
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

    // UI Widgets.
    private val mStartUpdatesButton: Button? = null
    private val mStopUpdatesButton: Button? = null
    private val mLastUpdateTimeTextView: TextView? = null
    private val mLatitudeTextView: TextView? = null
    private val mLongitudeTextView: TextView? = null

    // Labels.
    private val mLatitudeLabel: String? = null
    private val mLongitudeLabel: String? = null
    private val mLastUpdateTimeLabel: String? = null

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private var mRequestingLocationUpdates: Boolean? = null

    /**
     * Time when the location was updated represented as a String.
     */
    private var mLastUpdateTime: String? = null


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
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Creates a callback for receiving location events.
     */
    private var gotLocationOnce = false
    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (gotLocationOnce) {
                    return
                }
                mCurrentLocation = locationResult.lastLocation
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                GeoCoderAddress(this@MainController).getCompleteAddress(
                    mCurrentLocation!!.latitude,
                    mCurrentLocation!!.longitude
                )
                {
                    addressFromMyLocation = it
                    setCurrentLocationInPrefs()
                }
                setCurrentLocationInPrefs()
                stopLocationUpdates()

                gotLocationOnce = true

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
                    TAG,
                    "User agreed to make required location settings changes."
                )
                Activity.RESULT_CANCELED -> {
                    Log.i(TAG, "User chose not to make required location settings changes.")
                    mRequestingLocationUpdates = false
                    updateUI()
                }
            }
        }
    }


    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    private fun initReminderNotification() {
        checkAndAskForBatteryOptimization()
        val start: LocalDateTime = LocalDateTime.now()
        // Hour + 1, set Minute and Second to 00
        val end: LocalDateTime = start.plusMinutes(3).truncatedTo(ChronoUnit.HOURS)
        // Get Duration
        val duration: Duration = Duration.between(start, end)
        val millis: Long = duration.toMillis()
        val delayTimeInMillis = System.currentTimeMillis() + millis
        val intent = Intent(this, NotificationReceiver::class.java)
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        pendingIntent = PendingIntent.getBroadcast(
            this,
            100101,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            delayTimeInMillis,
            delayTimeInMillis,
            pendingIntent
        )


    }

    private fun checkAndAskForBatteryOptimization() {
        try {
            val dialog: AlertDialog? = BatteryOptimizationUtil.getBatteryOptimizationDialog(
                this,
                {
                    Toast.makeText(this, "User Accepted", Toast.LENGTH_SHORT).show()
                }
            ) {
                Toast.makeText(this, "User Denied", Toast.LENGTH_SHORT).show()

            }
            dialog?.show()

        } catch (e: java.lang.Exception) {

            e.printStackTrace()
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
            .addOnSuccessListener(this) {
                Log.i(TAG, "All location settings are satisfied.")
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest!!,
                    mLocationCallback!!, Looper.myLooper()!!
                )
                updateUI()
            }
            .addOnFailureListener(this) { e ->
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
                            rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings."
                        Log.e(TAG, errorMessage)
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
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
        if (mRequestingLocationUpdates!!) {
            mStartUpdatesButton!!.isEnabled = false
            mStopUpdatesButton!!.isEnabled = true
        } else {
            mStartUpdatesButton!!.isEnabled = true
            mStopUpdatesButton!!.isEnabled = false
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
            .addOnCompleteListener(this) {
                mRequestingLocationUpdates = false
                setButtonsEnabledState()
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
        updateUI()
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


    val locationForGfResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                // Handle the Intent
            }
        }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setCurrentLocationInPrefs() {
        (application as RawGpsApp).appContainer.prefs.setString(
            Constants.ADDRESS_FROM_LOCATION,
            addressFromMyLocation
        )
        (application as RawGpsApp).appContainer.prefs.setString(
            Constants.LATITUDE_FROM_LOCATION,
            mCurrentLocation?.latitude.toString()
        )
        (application as RawGpsApp).appContainer.prefs.setString(
            Constants.LONGITUDE_FROM_LOCATION,
            mCurrentLocation?.longitude.toString()
        )
    }


    private fun removePrefs() {

        (application as RawGpsApp).appContainer.prefs.removeKey(Constants.ADDRESS_FROM_LOCATION)
        (application as RawGpsApp).appContainer.prefs.removeKey(Constants.ADDRESS_TO_LOCATION)
        (application as RawGpsApp).appContainer.prefs.removeKey(Constants.LATITUDE_FROM_LOCATION)
        (application as RawGpsApp).appContainer.prefs.removeKey(Constants.LONGITUDE_FROM_LOCATION)
        (application as RawGpsApp).appContainer.prefs.removeKey(Constants.LATITUDE_TO_LOCATION)
        (application as RawGpsApp).appContainer.prefs.removeKey(Constants.LONGITUDE_TO_LOCATION)


    }

    /**
     * show Consent method
     */
    private fun showConsent() {
        val isAdsRemoved = (application as RawGpsApp).appContainer!!.prefs!!.areAdsRemoved()!!
        if (!isAdsRemoved) {
            UserConsent.CheckUserConsent(
                getString(R.string.app_name),
                this,
                true,
                false,
                R.drawable.app_logo
            )
        }

    }


}