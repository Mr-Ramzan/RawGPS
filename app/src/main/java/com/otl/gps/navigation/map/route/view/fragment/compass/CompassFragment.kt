package com.otl.gps.navigation.map.route.view.fragment.compass

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import application.RawGpsApp
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdSize
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.FragmentCompassBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.utilities.Constants

class CompassFragment : Fragment(), SensorEventListener {
    /**
     * Called when the activity is first created.
     */
//    private var adsUtill: MyAdsUtill? = null
    private var currentDegree = 0f
    private var sensorManager: SensorManager? = null
    private var binding: FragmentCompassBinding? = null






    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        return binding!!.root
    }


    private fun setupBg() {

        try {
            Glide.with(this).load(R.drawable.home_bg).into(binding!!.bgImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBg()
//        loadBanner()
        loadNativeBanner()
        sensorManager = requireActivity().getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(
            this,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val degree = Math.round(event.values[0]).toFloat()
        binding!!.txtDegrees.text = "Rotation: " + java.lang.Float.toString(degree) + " degrees"
        val ra = RotateAnimation(
            currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        ra.duration = 120
        ra.fillAfter = true
        binding!!.imgCompass.startAnimation(ra)
        currentDegree = -degree
    }

    override fun onAccuracyChanged(p1: Sensor, p2: Int) {}
    private fun setListeners() {
        binding!!.backButton.setOnClickListener { requireActivity().onBackPressed() }
    }

//    private fun loadBanner() {
//        (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.AddSquareBannerToLayout(
//            requireActivity(),
//            binding!!.adsContainer,
//            AdSize.MEDIUM_RECTANGLE,
//            object : AdLoadedCallback {
//                override fun addLoaded(success: Boolean?) {}
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
                        if (isDetached) {
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
                                binding?.adsContainer?.visibility = View.GONE

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
            if (isDetached) {
                return
            }
            val isAdsRemoved =
                (requireActivity().application as RawGpsApp).appContainer.prefs.areAdsRemoved()
            if (!isAdsRemoved) {
                if (canShowNativeAd)
                {
                    (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.showSmallNativeAd(
                        requireActivity(),
                        Constants.BIG_NATIVE,
                        binding?.adsContainer!!, true, true
                    )
                }
                else
                {
                    binding?.adsContainer?.visibility = View.GONE
                }
            } else {
                binding?.adsContainer?.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}