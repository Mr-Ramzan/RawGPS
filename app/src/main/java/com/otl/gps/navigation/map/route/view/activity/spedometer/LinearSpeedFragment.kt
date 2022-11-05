package com.otl.gps.navigation.map.route.view.activity.spedometer

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.otl.gps.navigation.map.route.databinding.FragmentLinearSpeedBinding
import com.otl.gps.navigation.map.route.interfaces.locationCallback


class LinearSpeedFragment : Fragment(), locationCallback {

    var binding: FragmentLinearSpeedBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLinearSpeedBinding.inflate(inflater, container, false)
//        binding!!.mapBtn.setOnClickListener { view: View? ->
//            startActivity(
//                Intent(
//                    activity, MapActivity::class.java
//                )
//            )
//        }
        return binding!!.root
    }

    override fun locationCallback(location: Location) {
        Log.e("Analog","Analog<============>${location.latitude}/${location.longitude}")
        val speed = (location.speed * 3.6).toFloat()
        binding!!.speedometer.speedTo(speed, 700)
    }
}