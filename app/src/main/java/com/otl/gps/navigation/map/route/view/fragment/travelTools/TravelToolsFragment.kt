package com.otl.gps.navigation.map.route.view.fragment.travelTools

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import application.RawGpsApp
import com.bumptech.glide.Glide

import com.google.android.gms.ads.AdSize
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.TravelToolsBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.model.NavEvent
import com.otl.gps.navigation.map.route.utilities.Constants
import org.greenrobot.eventbus.EventBus


class TravelToolsFragment : Fragment() {

    private lateinit var binding: TravelToolsBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TravelToolsBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBg()
        initListeners()
        //loadBanner()
        loadNativeBanner()
        loadInter()
    }

//    private fun loadBanner() {
//        (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
//            requireActivity(),
//            binding.adsParent,
//            AdSize.MEDIUM_RECTANGLE,
//            object : AdLoadedCallback {
//                override fun addLoaded(success: Boolean?) {
//                    Log.d("Add Load Callback", "is ad loaded========>" + success)
//                }
//            })
//    }
    var canShowNativeAd = false
    var adsReloadTry = 0
    /**
     * Loading ads once if not loaded
     * there will be max three tries if once ad loaded it will not be loaded again but if not code will ask
     */
    private fun loadNativeBanner() {

        if (!(requireActivity().application as RawGpsApp).appContainer.prefs.areAdsRemoved()) {
            (requireActivity().application as RawGpsApp).appContainer.myAdsUtill?.loadSmallNativeAd(
                requireActivity(),
                true,
                object : AdLoadedCallback {

                    override fun addLoaded(success: Boolean?) {


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

            val isAdsRemoved =
                (requireActivity().application as RawGpsApp).appContainer.prefs.areAdsRemoved()
            if (!isAdsRemoved) {

                if (canShowNativeAd)
                {
                    (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.showSmallNativeAd(
                        requireActivity(),
                        Constants.BIG_NATIVE,
                        binding.adsParent, true, false
                    )
                }
                else
                {
                    binding.adsParent.visibility = View.GONE
                }


            } else {
                binding.adsParent.visibility = View.GONE
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //==========================================================================================////
    private fun setupBg() {

        try {
            Glide.with(this).load(R.drawable.home_bg).into(binding.homeBg)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun initListeners() {

        binding.backButton.setOnClickListener {
            EventBus.getDefault().post(NavEvent(Constants.NAV_BACK))
        }

        binding.weather.setOnClickListener {
            EventBus.getDefault().post(NavEvent(Constants.NAVIGATE_WEATHER))
        }


        binding.qiblaCompass.setOnClickListener {
            EventBus.getDefault().post(NavEvent(Constants.NAVIGATE_QIBLA_COMPASS))
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
            (requireActivity().application as RawGpsApp).appContainer.myAdsUtill?.showInterestitial(
                requireActivity()
            ) {
                shown(it)
            }
        } else {
            shown(false)
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////////////////////////
//    var canShowNativeAd = false
//    var adsReloadTry = 0
//
//    /**
//     * Loading ads once if not loaded
//     * there will be max three tries if once ad loaded it will not be loaded again but if not code will ask
//     */
//    private fun loadNativeBanner() {
//
//        if (!GpsTrackerApp.prefs?.areAdsRemoved()!!) {
//            (requireActivity().application as GpsTrackerApp).appContainer?.myAdsUtill?.loadSmallNativeAd(
//                requireActivity(),
//                true,
//                object : AdLoadedCallback {
//                    override fun addLoaded(success: Boolean?) {
//                        if (success != null && success) {
//                            adsReloadTry += 1
//                            canShowNativeAd = success
//                            showNativeAd()
//                        } else {
//
//                            /////////////////////////////
//                            if (success == null || !success) {
//                                canShowNativeAd = false
//                                binding.adsParent.visibility = View.GONE
//
//                            } else {
//                                canShowNativeAd = success
//                            }
//                            /////////////////////////////
//                            adsReloadTry += 1
//                            if (adsReloadTry < Constants.ADS_RELOAD_MAX_TRIES) {
//                                loadNativeBanner()
//                            }
//                        }
//                    }
//                }
//            )
//        }
//
//    }
//
//    private fun showNativeAd() {
//        val preferences =
//            requireActivity().getSharedPreferences("PREF_NAME", AppCompatActivity.MODE_PRIVATE)
//        val isAdsRemoved = preferences.getBoolean(Constants.PREF_REMOVE_ADS, false)
//        if (!isAdsRemoved) {
//            if (canShowNativeAd) {
//                (requireActivity().application as GpsTrackerApp).appContainer?.myAdsUtill?.showSmallNativeAd(
//                    requireActivity(),
//                    Constants.START_NATIVE_SMALL,
//                    binding.adsParent,
//                    true,
//                    false
//                )
//            } else {
//                binding.adsParent.visibility = View.GONE
//            }
//        } else {
//            binding.adsParent.visibility = View.GONE
//        }
//    }

}
