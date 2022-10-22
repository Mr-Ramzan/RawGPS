package com.otl.gps.navigation.map.route.view.activity.splash

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import application.RawGpsApp
import com.bumptech.glide.Glide
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.ActivitySplashBinding
import com.otl.gps.navigation.map.route.view.activity.main.MainController
import com.otl.gps.navigation.map.route.view.activity.onboarding.OnboardingSplash
import kotlin.Exception

class Splash : AppCompatActivity() {


    /**
     * Number of seconds to count down before showing the app open ad. This simulates the time needed
     * to load the app.
     */
    private val COUNTER_TIME = 5L;
    private lateinit var binding: ActivitySplashBinding
    private lateinit var windowInsetsController: WindowInsetsControllerCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        (application as RawGpsApp).appContainer.prefs.setAppLaunchCount((application as RawGpsApp).appContainer.prefs.getAppLaunchCount() + 1)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBg()
        if ((application as RawGpsApp).appContainer.prefs.areAdsRemoved()) {
            (application as RawGpsApp).appContainer.prefs.disableAppOpenAds()
            goToHome()
        } else {
            createTimer(COUNTER_TIME)
        }
    }

    private fun setupBg() {

        try {
            Glide.with(this).load(R.drawable.splash_bg).into(binding.bg)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun hideSystemBars() {

        windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }


    private fun goToOnBoarding() {
        try {
            startActivity(Intent(this@Splash, OnboardingSplash::class.java))
            Handler(Looper.getMainLooper()).postDelayed({ this@Splash.finish() }, 700)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun goToHome() {
        try {
            startActivity(Intent(this@Splash, MainController::class.java))
            Handler(Looper.getMainLooper()).postDelayed({ this@Splash.finish() }, 700)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Create the countdown timer, which counts down to zero and show the app open ad.
     *
     * @param seconds the number of seconds that the timer counts down from
     */
    private fun createTimer(seconds: Long) {
        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {

                val application = application

                // If the application is not an instance of MyApplication, log an error message and
                // start the MainActivity without showing the app open ad.
                if (application !is RawGpsApp) {
                    Log.e("", "Failed to cast application to MyApplication.")
                    goToHome()
                    return
                }
                if (!(application as RawGpsApp).appContainer.prefs.areAdsRemoved()) {
                    // Show the app open ad.
                    (application as RawGpsApp)
                        .showAdIfAvailable(
                            this@Splash, object : RawGpsApp.OnShowAdCompleteListener {
                                override fun onShowAdComplete() {
                                    if ((application as RawGpsApp).appContainer.prefs.getFirstLaunch()) {
                                        goToOnBoarding()
                                    } else {
//                                        Log.e("Max Threshold==========>","======>${prefs.getPremiumScreenThreshHold()}")
//                                        Log.e("AppLaunchCount==========>","======>${prefs.getAppLaunchCount()}")
//                                        if (prefs.getAppLaunchCount() >= prefs.getPremiumScreenThreshHold()) {
//                                            goToPremium()
//                                        } else {
                                        goToHome()
//                                        }
                                    }
                                }
                            })
                } else {

                    if ((application as RawGpsApp).appContainer.prefs.getFirstLaunch()) {

                        goToOnBoarding()

                    } else {


                        goToHome()


                    }


                }
            }
        }
        countDownTimer.start()
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    //    private lateinit var remoteConfig: FirebaseRemoteConfig

    private fun setUpConfig() {
        // Get Remote Config instance.
        // [START get_remote_config_instance]
        remoteConfig = FirebaseRemoteConfig.getInstance()
        // [END get_remote_config_instance]

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. Also use Remote Config
        // Setting to set the minimum fetch interval.
        // [START enable_dev_mode]
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60)
            .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        remoteConfig.setDefaultsAsync(R.xml.connfigs)
        // [END set_default_values]

        fetchWelcome()
    }
    /**
     * Fetch a welcome message from the Remote Config service, and then activate it.
     */
    private fun fetchWelcome() {

        // [START fetch_config_with_callback]
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("GPS APP", "Config params updated: $updated")


                } else {

                }
                fetchConfigs()
            }
        // [END fetch_config_with_callback]
    }
    private fun fetchConfigs() {

        try {

            var maxTries = 3
            maxTries = remoteConfig.getString(GpsTrackerApp.TRIES_KEY).toString().toInt()

            var showPremiumAfterThreshHold = 3

            showPremiumAfterThreshHold =
                remoteConfig.getString(GpsTrackerApp.SHOULD_SHOW_PREMIUM).toString().toInt()

            Constants.MAP_BOX_ACCESS_TOKEN =
                remoteConfig.getString(GpsTrackerApp.ACCESS_TOKEN_KEY).toString()

//            (application as GpsTrackerApp).initializeSearchSDK()
//            Toast.makeText(this,Constants.MAP_BOX_ACCESS_TOKEN,Toast.LENGTH_SHORT).show()


//            Log.d("MAX Tries====","======>${maxTries}")
//            Log.d(" MAX Thresh ====","======>${showPremiumAfterThreshHold}")

            prefs.setNavigationMAXCount(maxTries.toInt())
            prefs.setThreshHold(showPremiumAfterThreshHold)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    */
}

