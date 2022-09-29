package com.otl.gps.navigation.map.route.manager.adManagers

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import application.RawGpsApp
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.otl.gps.navigation.map.route.R
import java.util.*

class AppOpenAdHelper {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    /**
     * Keep track of the time an app open ad is loaded to ensure you don't show an expired ad.
     */
    private var loadTime: Long = 0

    /**
     * Load an ad.
     *
     * @param context the context of the activity that loads the ad
     */
    fun loadAd(context: Activity?) {

        if((context?.application as RawGpsApp).appContainer.prefs.areAdsRemoved())
        {
            return
        }
        // Do not load ad if there is an unused ad or one is already loading.
        if (isLoadingAd || isAdAvailable) {
            return
        }
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            context.getString(R.string.gps_appopen_id),
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                /**
                 * Called when an app open ad has loaded.
                 *
                 * @param ad the loaded app open ad.
                 */
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.e(LOG_TAG, "onAdLoaded.")
                    //Toast.makeText(context, "onAdLoaded", //Toast.LENGTH_SHORT).show();
                }

                /**
                 * Called when an app open ad has failed to load.
                 *
                 * @param loadAdError the error.
                 */
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(LOG_TAG, "onAdFailedToLoad: " +  loadAdError.cause)


                    isLoadingAd = false
                    Log.e(LOG_TAG, "onAdFailedToLoad: " + loadAdError.message)
                    //Toast.makeText(context, "onAdFailedToLoad", //Toast.LENGTH_SHORT).show();
                }
            })
    }

    /**
     * Check if ad was loaded more than n hours ago.
     */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }
    // Ad references in the app open beta will time out after four hours, but this time limit
    // may change in future beta versions. For details, see:
    // https://support.google.com/admob/answer/9341964?hl=en
    /**
     * Check if ad exists and can be shown.
     */
    val isAdAvailable: Boolean
        get() =// Ad references in the app open beta will time out after four hours, but this time limit
        // may change in future beta versions. For details, see:
            // https://support.google.com/admob/answer/9341964?hl=en
            appOpenAd != null /*&& wasLoadTimeLessThanNHoursAgo(4)*/
    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity                 the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     */
    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: RawGpsApp.OnShowAdCompleteListener =
            object : RawGpsApp.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    // Empty because the user will go back to the activity that shows the ad.
                }
            }
    ) {


        if((activity.application as RawGpsApp).appContainer.prefs.areAdsRemoved()){
            onShowAdCompleteListener.onShowAdComplete()
            return
        }
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable) {
            onShowAdCompleteListener.onShowAdComplete()
            loadAd(activity)
            return
        }
        appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            /** Called when full screen content is dismissed.  */
            override fun onAdDismissedFullScreenContent() {
                // Set the reference to null so isAdAvailable() returns false.
                appOpenAd = null
                isShowingAd = false
                //Toast.makeText(activity, "onAdDismissedFullScreenContent", //Toast.LENGTH_SHORT).show();
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
            }

            /** Called when fullscreen content failed to show.  */
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false

                //Toast.makeText(activity, "onAdFailedToShowFullScreenContent", //Toast.LENGTH_SHORT).show();
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
            }

            /** Called when fullscreen content is shown.  */
            override fun onAdShowedFullScreenContent() {
                //Toast.makeText(activity, "onAdShowedFullScreenContent", //Toast.LENGTH_SHORT).show();
            }
        }
        isShowingAd = true
        appOpenAd!!.show(activity)
    }
    private val LOG_TAG = "AppOpenAdManager"
}