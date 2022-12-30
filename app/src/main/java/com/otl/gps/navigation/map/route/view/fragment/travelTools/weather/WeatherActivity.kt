package com.otl.gps.navigation.map.route.view.fragment.travelTools.weather

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import application.RawGpsApp
import com.bumptech.glide.Glide
import com.otl.gps.navigation.map.route.R

import com.google.android.gms.ads.AdSize
import com.otl.gps.navigation.map.route.databinding.ActivityWeatherBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.model.NavEvent
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.Constants.LATITUDE_FROM_LOCATION
import com.otl.gps.navigation.map.route.utilities.Constants.LONGITUDE_FROM_LOCATION
import com.otl.gps.navigation.map.route.utilities.Constants.NAV_BACK
import com.otl.gps.navigation.map.route.utilities.Helper
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.citiesWeather.CityAdapter
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.citiesWeather.cityDetailInterface
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.citiesWeather.cityWeather
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.citiesWeather.cityWeatherViewModel
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.currentWeather.CurrentWeather
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.currentWeather.CurrentWeatherViewModel
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.forecast.ForecastAdapter
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.forecast.ForecastViewModel
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.forecast.forecastWeather
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.model.citiesModel
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.utils.BackgroundManager
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.utils.IconManager
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : Fragment(), cityDetailInterface {
    private var _binding: ActivityWeatherBinding? = null


    lateinit var currWViewModel: CurrentWeatherViewModel
    var latitude: String? = null
    var longitude: String? = null

    lateinit var forecastViewModel: ForecastViewModel
    lateinit var viewModel: cityWeatherViewModel
    private val binding get() = _binding!!

    //adapter
    var adapter: CityAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        latitude = (requireActivity().application as RawGpsApp).appContainer.prefs.getString(

            LATITUDE_FROM_LOCATION,
            ""
        )
        longitude = (requireActivity().application as RawGpsApp).appContainer.prefs.getString(
            LONGITUDE_FROM_LOCATION,
            ""
        )


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityWeatherBinding.inflate(layoutInflater)
        return (_binding!!.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        currWViewModel = ViewModelProvider(this).get(CurrentWeatherViewModel::class.java)
        forecastViewModel = ViewModelProvider(this).get(ForecastViewModel::class.java)
        viewModel = ViewModelProvider(this).get(cityWeatherViewModel::class.java)


        CurrentWeather(
            currWViewModel,
            binding.currentLayout
        ).showCurrentLocationData(requireActivity(), latitude!!, longitude!!)
        cityWeather(viewModel).showCitiesWeather(requireActivity().applicationContext)

        //request data from api
        forecastWeather(forecastViewModel, requireActivity()).getForecast(
            latitude!!,
            longitude!!
        )
        setCurrentWeather()
        getWeatherForecast()
        getCitiesWeather()
        //loadInter()
        //loadBanner()
        //loadNativeBanner()
        clickEvents()
        getDateTime()
    }


    private fun clickEvents() {
        _binding!!.back.setOnClickListener {
            EventBus.getDefault().post(NavEvent(NAV_BACK))
        }
        binding.searchCityED.clearFocus()

        binding.searchCityED.setOnClickListener {
            binding.searchCityED.isEnabled = true
            binding.currentLayout.isVisible = false
            binding.searchCardMain.isVisible = true
            binding.searchCard.isVisible = true
            binding.forcastCard.isVisible = false

        }
        binding.searchCardMain.setOnClickListener {

            binding.currentLayout.isVisible = false
            binding.searchCardMain.isVisible = true
            binding.searchCard.isVisible = true
            binding.forcastCard.isVisible = false


        }

        binding.cancelSearch.setOnClickListener {

            binding.currentLayout.isVisible = true
            binding.searchCardMain.isVisible = true
            binding.searchCard.isVisible = false
            binding.forcastCard.isVisible = true


        }
    }


    private fun getDateTime() {
        val currentTime = SimpleDateFormat("HH : mm", Locale.getDefault()).format(Date())
        val simpleDateFormat = SimpleDateFormat("EEEE , dd MMMM yyyy ", Locale.getDefault())
        val date = Date()
        binding.time.text = simpleDateFormat.format(date).replace(",", "\n")
    }


    private fun setCurrentWeather() {
        currWViewModel.WeatherService.observe(requireActivity(), androidx.lifecycle.Observer {
            //set data
            _binding!!.homeCity.text = currWViewModel.city
            Glide.with(this)
                .load(IconManager().getIcon(currWViewModel.icon))
                .into(_binding!!.weatherIcon)

            _binding!!.status.text = currWViewModel.description
            if (currWViewModel.description.equals("Sunny")) {
                Glide.with(this)
                    .load(R.drawable.morning_bg)
                    .into(binding.weatherBackgroundImage)
            } else if (currWViewModel.description.equals("Clouds")) {

                Glide.with(this)
                    .load(R.drawable.morning_bg)
                    .into(binding.weatherBackgroundImage)
            } else if (currWViewModel.description.equals("Clear")) {

                Glide.with(this)
                    .load(R.drawable.morning_bg)
                    .into(binding.weatherBackgroundImage)
            } else if (currWViewModel.description.equals("Thunderstorm")) {

                Glide.with(this)
                    .load(R.drawable.morning_bg)
                    .into(binding.weatherBackgroundImage)
            } else if (currWViewModel.description.equals("Haze")) {

                Glide.with(this)
                    .load(R.drawable.morning_bg)
                    .into(binding.weatherBackgroundImage)
            } else if (currWViewModel.description.equals("Rainy")) {

                Glide.with(this)
                    .load(R.drawable.morning_bg)
                    .into(binding.weatherBackgroundImage)
            } else {

                Glide.with(this)
                    .load(R.drawable.morning_bg)
                    .into(binding.weatherBackgroundImage)
            }

            _binding!!.temperature.text = currWViewModel.temperature + "℃"
            _binding!!.feelLikeValue.text = currWViewModel.temperature
            _binding!!.waterDrop.text = currWViewModel.water_drop
            _binding!!.windSpeed.text = currWViewModel.wind_speed
            _binding!!.weatherBackgroundImage.setBackgroundResource(
                BackgroundManager().getHomeBackground(
                    currWViewModel.description
                )
            )

        })
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
//    private fun loadBanner() {
//        (application as RawGpsApp).appContainer.myAdsUtill?.AddBannerToLayout(
//
//            this,
//            binding.bannerAd,
//
//            AdSize.LARGE_BANNER,
//            object : AdLoadedCallback {
//                override fun addLoaded(success: Boolean?) {
//
//                    // Toast.makeText(this@WeatherActivity, "Banner shown", Toast.LENGTH_SHORT).show()
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
                                binding.bannerAd.visibility = View.GONE

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

                if (canShowNativeAd) {
                    (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.showSmallNativeAd(
                        requireActivity(),
                        Constants.START_NATIVE_SMALL,
                        binding.bannerAd, true, false
                    )
                } else {
                    binding.bannerAd.visibility = View.GONE
                }


            } else {
                binding.bannerAd.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //----------------------------------------------------------------------------------------------/////
    /**
     *     Logic Related To Interstitial in main activity
     */
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
            (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.showInterestitial(
                requireActivity()
            ) {
                shown(it)
            }
        } else {
            shown(false)
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////

    //get weather forecast
    private fun getWeatherForecast() {
        forecastViewModel.liveData.observe(requireActivity(), androidx.lifecycle.Observer {
            binding.forcastCard.visibility = View.VISIBLE
            //recyclerView
            binding.detailedCityRecyclerview.layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.HORIZONTAL, false
            )

            val adapter = requireActivity().applicationContext?.let {
                ForecastAdapter(
                    forecastViewModel.newlist,
                    it
                )
            }
            binding.detailedCityRecyclerview.adapter = adapter

            binding.detailedCityRecyclerview.startAnimation(
                AnimationUtils.loadAnimation(
                    requireActivity(),
                    R.anim.recycler_view_anim
                )
            )

        })
    }

    //get cities weather
    private fun getCitiesWeather() {

        viewModel.liveData.observe(requireActivity(), androidx.lifecycle.Observer {

            binding.citiesRecylerView.layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL, false
            )
            //refresh View
            binding.swipeContainer.setOnRefreshListener {
                adapter?.clear()
                run {
                    Handler().postDelayed(Runnable {
                        adapter?.addAll(viewModel.newlist)
                        binding.swipeContainer.isRefreshing = false
                    }, 3000)
                }
            }
            adapter = CityAdapter(this, viewModel.newlist, viewModel, requireActivity())
            binding.citiesRecylerView.adapter = adapter


            binding.searchCityED.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    binding.currentLayout.isVisible = false
                    binding.searchCardMain.isVisible = true
                    binding.searchCard.isVisible = true
                    binding.forcastCard.isVisible = false
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable) {
                    filter(s.toString(), viewModel.newlist, binding.citiesRecylerView, adapter!!)
                }
            })
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemClick(
        city: String,
        icon: String,
        description: String,
        temperature: String,
        wind_speed: String,
        water_drop: String,
        min_temp: String,
        max_temp: String,
        lat: String,
        long: String
    ) {
        val intent = Intent(requireActivity(), WeatherDetailActivity::class.java)
        intent.putExtra("city", city)
        intent.putExtra("description", description)
        intent.putExtra("temperature", temperature)
        intent.putExtra("wind_speed", wind_speed)
        intent.putExtra("water_drop", water_drop)
        intent.putExtra("min_temp", min_temp)
        intent.putExtra("max_temp", max_temp)
        intent.putExtra("lat", lat)
        intent.putExtra("long", long)
        Helper.startActivity(requireActivity(), intent, false)

    }

    //init filter search
    fun filter(
        city: String,
        data: ArrayList<citiesModel>,
        recyclerView: RecyclerView,
        adapter: CityAdapter
    ) {

        val arrayList: ArrayList<citiesModel> = ArrayList<citiesModel>()

        for (model in data) {

            if (model.city.lowercase(Locale.getDefault()).contains(city)) {
                recyclerView.visibility = View.VISIBLE
                arrayList.add(model)
            } else {
                if (arrayList.isEmpty()) {
                    binding.cancelSearch.isVisible = true
                    recyclerView.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.VISIBLE
                }
            }
            adapter.upDateList(arrayList)
        }
    }


}