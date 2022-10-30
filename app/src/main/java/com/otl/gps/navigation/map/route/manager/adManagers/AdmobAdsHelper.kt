package com.otl.gps.navigation.map.route.manager.adManagers

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import application.RawGpsApp
import application.RawGpsApp.Companion.isSDKInitialized
import com.otl.gps.navigation.map.route.R
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import java.util.*

class AdmobAdsHelper (mContext: Context) {

    var adRequestBundleForConsent: Bundle
        //    var bannerInstace: AdView? = null
//    var bigBannerInstance: AdView? = null
        private set
    public var mNativeAds: NativeAd? = null
    var mInterstitialAd: InterstitialAd? = null
    var mRewardedVideoAd: RewardedAd? = null
    private var consentForAds = true
    var admobInterRequest: AdRequest? = null
    var bannerAdRequest: AdRequest? = null
    var rewardedAdRequest: AdRequest? = null
    var ctx: Context? = null


    //anal
    init {

        this.ctx = mContext
        log("Initing Mobile Ads SDK...")
        adRequestBundleForConsent = Bundle()
        adRequestBundleForConsent.putString("npa", "1")
        consentForAds = RawGpsApp.isConsentGiven
    }


    /* fun Initialize(
         mContext: Activity,
         loadInterestitialByDefault: Boolean,
         loadRewardedAdByDefault: Boolean,
         consentValue: Boolean
     ) {
         try {
             if (!isSDKInitialized) {
                 log("NativeAdsVersion::: 1.0.2")
 //             List<String> testDeviceIds = Arrays.asList("DE56EBC2C99E5B0C8F0AA5A16563F5F5");
 //             RequestConfiguration configuration =
 //                  new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
 //                  MobileAds.setRequestConfiguration(configuration);
                 MobileAds.initialize(mContext) {
                     initializePlayerPrefs(mContext)
                     log("Initing Mobile Ads SDK...")
                     adRequestBundleForConsent = Bundle()
                     adRequestBundleForConsent.putString("npa", "1")
                     consentForAds = consentValue
                     isSDKInitialized = true
                     log("Initing Mobile Ads SDK with Consent Value::" + consentForAds)
                     if (loadInterestitialByDefault) {
                         loadInterestitial(mContext) {}
                     }
                     if (loadRewardedAdByDefault) {
                         loadRewardedVideo(mContext) {}
                     }
                 }
             } else {
                 log("Not Initing AD SDK, its already Initialized..")
             }
         } catch (var5: Exception) {
             log("Error in NativeAdsInit::$var5")
             log("Ads Halted: No Ads Will be Displyed...")
         }
     }*/







    fun loadInterestitial(context: Activity, adLoaded: (success: Boolean) -> Unit) {
        try {

            if ((context.application as RawGpsApp).appContainer.prefs.areAdsRemoved() || !isSDKInitialized) {
                log("Ads are Removed or SDK Not Initiazed: Cant show Interestitial...")
                adLoaded(false)
                return
            }

            val adRequest = AdRequest.Builder().build()
            if (consentForAds) {
                InterstitialAd.load(context, context.getString(R.string.gps_inter_id), adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            mInterstitialAd = interstitialAd
                            log("onAdLoaded")
                            adLoaded(true)
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            // Handle the error
                            log(loadAdError.message)
                            mInterstitialAd = null
                            adLoaded(false)
                        }
                    })
                log("Requesting Personlized Ads")
            } else {
                admobInterRequest = AdRequest.Builder()
                    .addNetworkExtrasBundle(
                        AdMobAdapter::class.java, adRequestBundleForConsent
                    ).build()
                InterstitialAd.load(context, context.getString(R.string.gps_inter_id), admobInterRequest!!,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            mInterstitialAd = interstitialAd
                            adLoaded(true)

                            log("onAdLoaded")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            // Handle the error
                            log(loadAdError.message)
                            mInterstitialAd = null
                            adLoaded(false)



                        }
                    })
                log("Requesting Non Personlized Ads")
            }
        } catch (var2: Exception) {
            log("Unable to Show Inter due to an Exception: in Load Inter: $var2")
        }
    }

    fun showInterestitial(context: Activity, shown:(success:Boolean)->Unit) {
        try {

            if ((context.application as RawGpsApp).appContainer.prefs.areAdsRemoved()!! || !isSDKInitialized)
            {
                shown(true)
                log("Ads are Removed or SDK is not Initiaized")
                return
            }
            if (mInterstitialAd != null)
            {
                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("Dismissed", "Ad was dismissed.")
                        shown(true)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
//                        Logging.logImpressionForAds(context)
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("", "Ad showed fullscreen content.")
                        mInterstitialAd = null
                    }
                }
                mInterstitialAd!!.show(context)
                loadInterestitial(context) {}
            }
            else
            {
                shown(true)
                log("The interstitial wasn't loaded yet.")
                loadInterestitial(context) {}

            }

        } catch (var2: Exception) {
            shown(true)
            log("Unable to ShowInteretiail::: with error $var2")
        }
    }


    fun loadRewardedVideo(context: Activity?, loaded: (success: Boolean) -> Unit) {
        if (!isSDKInitialized) {
            log("SDK Not Initialized ")
        }
        else
        {
            try {
                val adRequest: AdRequest
                if (consentForAds) {
                    adRequest = AdRequest.Builder().build()
                    log("Requesting Personlized Rewarded Ads")
                } else {
                    adRequest = AdRequest.Builder().addNetworkExtrasBundle(
                        AdMobAdapter::class.java, adRequestBundleForConsent
                    ).build()
                    log("Requesting Non Personlized Rewarded Ads")
                }
                RewardedAd.load(context as Context, context.getString(R.string.gps_inter_id), adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            // Handle the error.
                            log(loadAdError.message)
                            mRewardedVideoAd = null
                            if (context != null) {
                                loaded(false)
                            }
                        }

                        override fun onAdLoaded(rewardedAd: RewardedAd) {
                            mRewardedVideoAd = rewardedAd
                            log("Ad was loaded.")
                            if (context != null) {
                                loaded(true)
                            }

                        }


                    })

            } catch (var3: Exception) {
                log("Unable to LoadRewardedVideo with Error:$var3")
            }
        }
    }

    private var loadRewardedLoopCounter = 3
    fun showRewardedVideo(
        context: Activity?,
        reward: (success: Boolean) -> Unit,
        dismiss: (dismissed: Boolean) -> Unit,
        fail: (fail: Boolean) -> Unit
    ) {

        if (!isSDKInitialized) {
            log("SDK Not Initialized...")
        } else {
            try {
                if (mRewardedVideoAd != null) {
                    val activityContext = context!!

                    mRewardedVideoAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                log("Ad was shown.")
                            }


                            override fun onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                log("Ad was dismissed.")
                                if (context != null) {
                                    dismiss(true)
                                }
                                mRewardedVideoAd = null
                                loadRewardedVideo(context) {}
                            }
                        }


                    mRewardedVideoAd!!.show(activityContext) { // Handle the reward.
                        log("The user earned the reward.")
                        log("onRewarded")
                        if (context != null) {
                            reward(true)
                        }
                    }
                } else {
                    log("Rewarded Videos are Not Available.")
                    if(loadRewardedLoopCounter>0){
                        loadRewardedVideo(context) {
                            if(it){
                                showRewardedVideo(context,
                                    reward, dismiss, fail)
                            }
                        }
                        loadRewardedLoopCounter-1
                    }else{
                        loadRewardedLoopCounter= 3
                        return
                    }
                }
            } catch (var2: Exception) {
                log("Unable to ShowRewardedVideo with Error:$var2")
            }
        }
    }
    fun AddSquareBannerToLayout(context: Activity, layout: FrameLayout, size: AdSize, loadCallback: AdLoadedCallback?)
    {

        if ((context.application as RawGpsApp).appContainer.prefs.areAdsRemoved()!!) {
            return
        }
        if (!isSDKInitialized) {
            log("SDK Not Initialized.")
        }
        else
        {
            try {
                var bannerInstace = AdView(context)
                bannerInstace?.setAdSize(size)
                bannerInstace!!.adUnitId = context.getString(R.string.gps_square_banner_id)
                layout.addView(bannerInstace)
                if (consentForAds) {
                    bannerAdRequest = AdRequest.Builder().build()
                    log("Requesting Personlized Banner Ads")
                } else {
                    if (adRequestBundleForConsent == null) {
                        adRequestBundleForConsent = Bundle()
                        val sharedPref = context.getPreferences(0)
                        consentForAds = sharedPref.getBoolean("ConsentValue", true)
                        adRequestBundleForConsent.putString("npa", "1")
                    }
                    bannerAdRequest = AdRequest.Builder()
                        .addNetworkExtrasBundle(
                            AdMobAdapter::class.java, adRequestBundleForConsent
                        ).build()
                    log("Requesting Non Personlized Banner Ads")
                }
                bannerInstace.loadAd(bannerAdRequest!!)
                log("Requesting Banner Ad")
                bannerInstace.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        loadCallback?.addLoaded(true)
                        log("Banner Ad Loaded, Adding to Layout")
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.e("Failed loading banner","=========>${p0.message}")
                        p0
                        layout.removeView(bannerInstace)
                        loadCallback?.addLoaded(false)
                    }

                    override fun onAdOpened() {

                        //Logging.logImpressionForAds(context)
                    }
                    override fun onAdClosed() {}
                }
            }
            catch (var3: Exception)
            {
                log("Unable to AdBannerToLayout with Error:$var3")
            }

        }
    }

    fun AddBannerToLayout(context: Activity, layout: FrameLayout, size: AdSize, loadCallback: AdLoadedCallback?)
    {

        if ((context.application as RawGpsApp).appContainer.prefs.areAdsRemoved()!!) {
            return
        }
        if (!isSDKInitialized) {
            log("SDK Not Initialized.")
        }
        else
        {
            try {
                var bannerInstace = AdView(context)
                bannerInstace?.setAdSize(size)
                bannerInstace!!.adUnitId = context.getString(R.string.gps_banner_id)
                layout.addView(bannerInstace)
                if (consentForAds) {
                    bannerAdRequest = AdRequest.Builder().build()
                    log("Requesting Personlized Banner Ads")
                } else {
                    if (adRequestBundleForConsent == null) {
                        adRequestBundleForConsent = Bundle()
                        val sharedPref = context.getPreferences(0)
                        consentForAds = sharedPref.getBoolean("ConsentValue", true)
                        adRequestBundleForConsent.putString("npa", "1")
                    }
                    bannerAdRequest = AdRequest.Builder()
                        .addNetworkExtrasBundle(
                            AdMobAdapter::class.java, adRequestBundleForConsent
                        ).build()
                    log("Requesting Non Personlized Banner Ads")
                }
                bannerInstace.loadAd(bannerAdRequest!!)
                log("Requesting Banner Ad")
                bannerInstace.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        loadCallback?.addLoaded(true)
                        log("Banner Ad Loaded, Adding to Layout")
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.e("Failed loading banner","=========>${p0.message}")
                        p0
                        layout.removeView(bannerInstace)
                        loadCallback?.addLoaded(false)
                    }

                    override fun onAdOpened() {

                        //Logging.logImpressionForAds(context)
                    }
                    override fun onAdClosed() {}
                }
            }
            catch (var3: Exception)
            {
                log("Unable to AdBannerToLayout with Error:$var3")
            }

        }
    }


    fun loadBanner(context: Activity, size: AdSize, loadCallback: AdLoadedCallback?)
    {

        if ((context.application as RawGpsApp).appContainer.prefs.areAdsRemoved()) {
            return
        }

        if (!isSDKInitialized) {
            log("SDK Not Initialized.")
        }
        else
        {
            try {



                var bannerInstace = AdView(context)
                bannerInstace?.setAdSize(size)
                bannerInstace!!.adUnitId = context.getString(R.string.gps_banner_id)
                if (consentForAds) {
                    bannerAdRequest = AdRequest.Builder().build()
                    log("Requesting Personlized Banner Ads")
                } else {
                    if (adRequestBundleForConsent == null){
                        adRequestBundleForConsent = Bundle()
                        val sharedPref = context.applicationContext.getSharedPreferences("",0)
                        consentForAds = sharedPref.getBoolean("ConsentValue", true)
                        adRequestBundleForConsent.putString("npa", "1")
                    }
                    bannerAdRequest = AdRequest.Builder()
                        .addNetworkExtrasBundle(
                            AdMobAdapter::class.java, adRequestBundleForConsent
                        ).build()
                    log("Requesting Non Personlized Banner Ads")
                }
                bannerInstace!!.loadAd(bannerAdRequest!!)
                log("Requesting Banner Ad")
                bannerInstace!!.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        loadCallback?.addLoaded(true)
                        log("Banner Ad Loaded, Adding to Layout")
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.e("Failed loading banner","=========>${p0.message}")
                        p0

                        loadCallback?.addLoaded(false)
                    }

                    override fun onAdOpened() {

                        //Logging.logImpressionForAds(context)
                    }
                    override fun onAdClosed() {}
                }
            }
            catch (var3: Exception)
            {
                log("Unable to AdBannerToLayout with Error:$var3")
            }

        }
    }


    /**
     * loading big Banner Ads
     */
    fun loadBigBanner(context: Activity, size: AdSize, loadCallback:  AdLoadedCallback?)
    {

        if ((context.application as RawGpsApp).appContainer.prefs.areAdsRemoved()) {
            return
        }

        if (!isSDKInitialized) {
            log("SDK Not Initialized.")
        }
        else
        {
            try {


                var bannerInstace = AdView(context)
                bannerInstace?.setAdSize(size)
                bannerInstace!!.adUnitId = context.getString(R.string.gps_banner_id)
                if (consentForAds) {
                    bannerAdRequest = AdRequest.Builder().build()
                    log("Requesting Personlized Banner Ads")
                } else {
                    if (adRequestBundleForConsent == null){
                        adRequestBundleForConsent = Bundle()
                        val sharedPref = context.applicationContext.getSharedPreferences("",0)
                        consentForAds = sharedPref.getBoolean("ConsentValue", true)
                        adRequestBundleForConsent.putString("npa", "1")
                    }
                    bannerAdRequest = AdRequest.Builder()
                        .addNetworkExtrasBundle(
                            AdMobAdapter::class.java, adRequestBundleForConsent
                        ).build()
                    log("Requesting Non Personlized Banner Ads")
                }
                bannerInstace!!.loadAd(bannerAdRequest!!)
                log("Requesting Banner Ad")
                bannerInstace!!.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        loadCallback?.addLoaded(true)
                        log("Banner Ad Loaded, Adding to Layout")
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.e("Failed loading banner","=========>${p0.message}")
                        p0

                        loadCallback?.addLoaded(false)
                    }

                    override fun onAdOpened() {

                        //Logging.logImpressionForAds(context)
                    }
                    override fun onAdClosed() {}
                }
            }
            catch (var3: Exception)
            {
                log("Unable to AdBannerToLayout with Error:$var3")
            }

        }
    }


 /*   fun loadSmallNativeAd(
        mActivity: Activity,
        willBeEffectedByRemoveAds: Boolean,
        loadCallback: AdLoadedCallback?
    ) {
        if (!isSDKInitialized) {

            log("SDK Not Initialized.")
        } else {
            try {
                if ((context.application as RawGpsApp).appContainer.prefs.areAdsRemoved()!! && willBeEffectedByRemoveAds) {
                    return
                }
                val builder = AdLoader.Builder(mActivity, mActivity.getString(R.string.gps_native_id))
                builder.forNativeAd { nativeAds ->
                    mNativeAds = nativeAds
                    loadCallback?.addLoaded(true)
                }

                val adLoader = builder.withAdListener(object : AdListener() {
                    fun onAdFailedToLoad(errorCode: Int) {
                        log("Failed to load native ad: $errorCode")
                        loadCallback?.addLoaded(false)
                    }
                }).build()
                adLoader.loadAd(AdRequest.Builder().build())
            } catch (var6: Exception) {
                loadCallback?.addLoaded(false)
                log("Unable to LoadSmallNative with Error:$var6")
            }
        }
    }



    fun showSmallNativeAd(
        mActivity: Activity,
        SCREEN_TYPE: String,
        frameLayout: FrameLayout,
        willBeEffectedByRemoveAds: Boolean, showMedia:Boolean
    ) {
        if (!isSDKInitialized) {
            log("SDK Not Initialized.")
            return
        }
        else
        {
            try {
                if ((context.application as RawGpsApp).appContainer.prefs.areAdsRemoved()!! && willBeEffectedByRemoveAds) {
                    return
                }

                if (mNativeAds != null) {
                    val adView: NativeAdView
                    adView = if (SCREEN_TYPE == Constants.POPUP_NATIVE) {
                        mActivity.layoutInflater.inflate(
                            R.layout.native_ad_popup,
                            null as ViewGroup?
                        ) as NativeAdView
                    } else if (SCREEN_TYPE == Constants.STYLE_LIST_NATIVE_SMALL) {
                        mActivity.layoutInflater.inflate(
                            R.layout.native_ads_list_items,
                            null as ViewGroup?
                        ) as NativeAdView
                    }
                    else if (SCREEN_TYPE == Constants.BIG_NATIVE) {
                        mActivity.layoutInflater.inflate(
                            R.layout.native_ad_unified_big,
                            null as ViewGroup?
                        ) as NativeAdView
                    }
                    else if (SCREEN_TYPE == Constants.MENU_NATIVE) {
                        mActivity.layoutInflater.inflate(
                            R.layout.native_ads_list_items,
                            null as ViewGroup?
                        ) as NativeAdView
                    } else {

                        if(showMedia) {
                            mActivity.layoutInflater.inflate(
                                R.layout.native_ad_unified_small,
                                null as ViewGroup?
                            ) as NativeAdView
                        }else{
                            mActivity.layoutInflater.inflate(
                                R.layout.native_ad_unified_small_no_media,
                                null as ViewGroup?
                            ) as NativeAdView
                        }
                    }

                    val mainImageView = adView.findViewById<View>(R.id.small_ad_image) as ImageView
                    adView.imageView = mainImageView

                    if (!showMedia) {
                        adView.imageView?.visibility = View.GONE
                    }else{
                        adView.imageView?.visibility = View.VISIBLE
                    }
                    try {

                        val images = mNativeAds?.images
                        mainImageView.setImageDrawable((images!![0] as NativeAd.Image).drawable)

                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    try {
                        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
                        if (!showMedia) {
                            mediaView.visibility = View.INVISIBLE
                        } else {
                            adView.mediaView = mediaView
                            adView.mediaView?.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
//                            adView.mediaView.ima

                            if (mNativeAds?.mediaContent != null) {
                                mediaView.visibility = View.INVISIBLE
                                mediaView.setMediaContent(mNativeAds!!.mediaContent!!)
                            }
                        }

                    }catch (e:Exception){e.printStackTrace()}
                    adView.headlineView = adView.findViewById(R.id.small_ad_headline)
                    adView.iconView = adView.findViewById(R.id.small_ad_app_icon)
                    adView.starRatingView = adView.findViewById(R.id.small_ad_stars)
                    adView.advertiserView = adView.findViewById(R.id.small_ad_advertiser)
                    adView.bodyView = adView.findViewById(R.id.small_ad_body)
                    adView.callToActionView = adView.findViewById(R.id.small_ad_call_to_action)
                    (Objects.requireNonNull(adView.bodyView) as TextView).text = mNativeAds?.body
                    (Objects.requireNonNull(adView.headlineView) as TextView).text =
                        mNativeAds?.headline
                    (adView.headlineView as TextView).text = mNativeAds?.headline
                    (Objects.requireNonNull(adView.callToActionView) as TextView).text =
                        mNativeAds?.callToAction

                    if (mNativeAds?.icon == null) {
                        try {
                            adView.iconView?.visibility = View.GONE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        try {
                            (adView.iconView as ImageView).setImageDrawable(mNativeAds!!.icon?.drawable)
                            adView.iconView?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    if (mNativeAds?.starRating == null) {
                        try {
                            adView.starRatingView?.visibility = View.GONE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        try {
                            adView.starRatingView?.visibility = View.GONE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            (adView.starRatingView as RatingBar).rating =
                                mNativeAds!!.starRating!!.toFloat()
                            adView.starRatingView?.visibility = View.GONE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (mNativeAds!!.advertiser == null) {
                        try {
                            adView.advertiserView?.visibility = View.GONE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        try {
                            (adView.advertiserView as TextView).text = mNativeAds!!.advertiser
                            adView.advertiserView?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    adView.setNativeAd(mNativeAds!!)
                    frameLayout.removeAllViews()
                    frameLayout.addView(adView)
                    try {
                        loadSmallNativeAd(
                            mActivity,
                            willBeEffectedByRemoveAds,
                            object : AdLoadedCallback {
                                override fun addLoaded(success: Boolean?) {

                                }
                            })
                    }catch (e:java.lang.Exception){e.printStackTrace()}

                } else {

                    loadSmallNativeAd(
                        mActivity,
                        willBeEffectedByRemoveAds,
                        object : AdLoadedCallback {
                            override fun addLoaded(success: Boolean?) {
                                showSmallNativeAd(
                                    mActivity,
                                    SCREEN_TYPE,
                                    frameLayout,
                                    willBeEffectedByRemoveAds,showMedia
                                )
                            }
                        })
                }
            } catch (var5: Exception) {
                log("Unable to showSmallNative with Error:$var5")
            }
        }
    }
*/

    private fun log(msg: String) {
        Log.e("MyAdsUtill", msg)
    }

    interface NativeRewardedAds {
        fun RewardUser()
    }

    public fun onDestroy() {
        if (ctx != null) {
            ctx = null
        }

        if (mNativeAds != null) {
            mNativeAds?.destroy()
            mNativeAds = null
        }
        if (mInterstitialAd != null) {
            mInterstitialAd = null

        }
        if (mRewardedVideoAd != null) {

            mRewardedVideoAd = null
        }
        admobInterRequest = null
        bannerAdRequest = null
        rewardedAdRequest = null


    }

}