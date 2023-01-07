package application

import android.R
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.otl.gps.navigation.map.route.interfaces.di.AppDIs
import com.otl.gps.navigation.map.route.manager.adManagers.AppOpenAdHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RawGpsApp : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {
    private var appOpenAdManager: AppOpenAdHelper? = null
    private var currentActivity: Activity? = null
    lateinit var appContainer: AppDIs

    override fun onCreate() {
        super<Application>.onCreate()

        appContext = this.applicationContext
       // init(this)
        appContainer = AppDIs(this)
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        instance = this

        setupAdsSdks()

        setUpData()

    }
    private fun setUpData(){

        CoroutineScope(Dispatchers.Main).launch {
            appContext = this@RawGpsApp.applicationContext
        }

    }




    private fun setupAdsSdks() {
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(
                listOf(
                    "E291B7660C265B0D0C5D40391432AD50",
                    "FC3CBBF0547BF29D2D2ACC7FCC4D994A",
                    "D7EF04558C5B8D0CF1FB26F41EC46227",
                    "5905EC31C4E2C9A46477531EE9F8F145",
                    "1499CBADF62E0EDDD4E7F473B22BADC3",
                    "68A6859490620B2FD12EF92967109EE7",
                    "3D309D176F044E6107C595AAC3688F64"
                )
            ).build()
        )


        MobileAds.initialize(this)
        {
            isSDKInitialized = true
            val str = StringBuilder()
            it.adapterStatusMap.forEach { (key, value) ->
                println("$key = $value")
                str.append("${key.length}===>${value.description}\n")
                str.append("${key.length}===>${value.initializationState}\n")
            }
            Log.e("Ads SDK Status", str.toString())
        }
        appOpenAdManager = AppOpenAdHelper()

    }

    /**
     * ActivityLifecycleCallback methods.
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        if (activity !is AppCompatActivity) {
            return
        }

        if (!appOpenAdManager!!.isAdAvailable) {
            appOpenAdManager!!.loadAd(activity)
        }
        else
        {
        }
    }
    override fun onActivityStarted(activity: Activity) {
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if (!appOpenAdManager!!.isShowingAd) {
            currentActivity = activity
        }
    }
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    /**
     * Shows an app open ad.
     * @param activity                 the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class.
        appOpenAdManager!!.showAdIfAvailable(activity, onShowAdCompleteListener)
    }
    /**
     * Interface definition for a callback to be invoked when an app open ad is complete
     * (i.e. dismissed or fails to show).
     */
    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    //    /**
    //     * LifecycleObserver method that shows the app open ad when the app moves to foreground.
    //     */
    //    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    //    protected void onMoveToForeground() {
    //        // Show the ad (if available) when the app moves to foreground.
    //        appOpenAdManager.showAdIfAvailable(currentActivity);
    //    }
    override fun onStart(owner: LifecycleOwner) {
        // Show the ad (if available) when the app moves to foreground.
          appOpenAdManager!!.showAdIfAvailable(currentActivity!!)
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////


    companion object
    {
        var instance: RawGpsApp? = null
        var isSDKInitialized = true
        var isConsentGiven = false
        lateinit var appContext: Context
        // Remote Config keys



    }
}