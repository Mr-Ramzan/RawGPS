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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        adsUtill = MyAdsUtill(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBg()
        loadBanner()
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

    private fun loadBanner() {
        (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
            requireActivity(),
            binding!!.adsContainer,
            AdSize.MEDIUM_RECTANGLE,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {}
            })
    }
}