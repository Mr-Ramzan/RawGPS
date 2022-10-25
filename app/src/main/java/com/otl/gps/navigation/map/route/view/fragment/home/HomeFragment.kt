package com.otl.gps.navigation.map.route.view.fragment.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import application.RawGpsApp
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdSize
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.FragmentHomeBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.model.NavEvent
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_POLICY
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_REMMOVE_ADS
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_COMPASS
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_CURRENCY_CONVERTER
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_EXPLORE_PLACES
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_LOCATION
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_ROUTE
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_SATELLITE
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_TRAFFIC
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_TRAVEL_TOOLS
import com.otl.gps.navigation.map.route.utilities.Constants.OPEN_DRAWER
import org.greenrobot.eventbus.EventBus

class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupBg()
        return root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBanner()
        setListeners()
    }

    private fun setupBg() {

        try {
            Glide.with(this).load(R.drawable.home_bg).into(binding.bg)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun setListeners() {

        binding.routeFinderButton.setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAVIGATE_ROUTE))
        }

        binding.myLocation.setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAVIGATE_LOCATION))
        }

        binding.satelliteButton.setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAVIGATE_SATELLITE))
        }

        binding.trafficView.setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAVIGATE_TRAFFIC))
        }


        binding.nearbyPlaces .setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAVIGATE_EXPLORE_PLACES))
        }


        binding.travelToolsButton .setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAVIGATE_TRAVEL_TOOLS))
        }
        binding.compass.setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAVIGATE_COMPASS))
        }


        binding.currencyConverterButton.setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAVIGATE_CURRENCY_CONVERTER))
        }


        binding.speedometerButton.setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAVIGATE_TRAVEL_TOOLS))
        }


        binding.menuButton.setOnClickListener {
            EventBus.getDefault().post(NavEvent(OPEN_DRAWER))
        }


    }

    private fun loadBanner() {
        (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
            requireActivity(),
            binding.adsParent,
            AdSize.MEDIUM_RECTANGLE,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {
                    Log.d("Add Load Callback", "is ad loaded========>" + success)
                }
            })
    }

}