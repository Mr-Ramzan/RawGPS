package com.otl.gps.navigation.map.route.view.fragment.travelTools.weather

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import application.RawGpsApp
import com.bumptech.glide.Glide

import com.google.android.gms.ads.AdSize
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.ActivityWeatherDetailBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.citiesWeather.CityAdapter
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.forecast.ForecastAdapter
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.forecast.ForecastViewModel
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.forecast.forecastWeather
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.utils.IconManager
import java.text.SimpleDateFormat
import java.util.*

class WeatherDetailActivity : AppCompatActivity(){
    private var _binding: ActivityWeatherDetailBinding? = null


    lateinit var forecastViewModel: ForecastViewModel
    private val binding get() = _binding!!

    //shared resources
    var data_city: String = ""
    var data_description: String = ""
    var data_icon: String = ""
    var data_temperature: String = ""
    var data_wind_speed: String = ""
    var data_water_drop: String = ""
    var data_min: String = ""
    var data_max: String = ""
    var data_lat: String = ""
    var data_long: String = ""

    //adapter
    var adapter: CityAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWeatherDetailBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)

        forecastViewModel = ViewModelProvider(this).get(ForecastViewModel::class.java)
        intent.let {
            val bundle = it.extras
            //get city details
            data_city = bundle!!.getString("city").toString()
            data_description = bundle.getString("description").toString()
            data_icon = bundle.getString("icon").toString()
            data_temperature = bundle.getString("temperature").toString()
            data_wind_speed = bundle.getString("wind_speed").toString()
            data_water_drop = bundle.getString("water_drop").toString()
            data_min = bundle.getString("min_temp").toString()
            data_max = bundle.getString("max_temp").toString()
            data_lat = bundle.getString("lat").toString()
            data_long = bundle.getString("long").toString()
            //request data from api
            this.let { forecastWeather(forecastViewModel, it).getForecast(data_lat, data_long) }
            //set data
            _binding!!.homeCity.text = data_city

            this.let {
                Glide.with(it)
                    .load(IconManager().getIcon(data_icon))
                    .into(binding.weatherIcon)
            }
            if (data_description.equals("Sunny")) {
                Glide.with(this)
                    .load(R.drawable.sunny_image)
                    .into(binding.weatherBackgroundImage)
            } else if (data_description.equals("Clouds")) {

                Glide.with(this)
                    .load(R.drawable.cloudy_background)
                    .into(binding.weatherBackgroundImage)
            } else if (data_description.equals("Clear")) {

                Glide.with(this)
                    .load(R.drawable.windy_background)
                    .into(binding.weatherBackgroundImage)
            } else if (data_description.equals("Thunderstorm")) {

                Glide.with(this)
                    .load(R.drawable.thunderstorm_background)
                    .into(binding.weatherBackgroundImage)
            } else if (data_description.equals("Haze")) {

                Glide.with(this)
                    .load(R.drawable.sunny_image)
                    .into(binding.weatherBackgroundImage)
            } else if (data_description.equals("Rainy")) {

                Glide.with(this)
                    .load(R.drawable.rainy_background)
                    .into(binding.weatherBackgroundImage)
            }else{

                Glide.with(this)
                    .load(R.drawable.sunny_image)
                    .into(binding.weatherBackgroundImage)
            }

            _binding!!.status.text = data_description


            _binding!!.temperature.text = "$data_temperature\t°"
            _binding!!.feelLikeValue.text = "$data_temperature\t°"

            _binding!!.waterDrop.text = data_water_drop

            _binding!!.windSpeed.text = data_wind_speed

            binding.homeCity.text = "$data_city"

        }


        getWeatherForecast()
        clickEvents()
        loadBanner()
        getDateTime()
    }

    private fun clickEvents() {
        _binding!!.back.setOnClickListener {
            onBackPressed()
        }
        binding.celcius.setOnClickListener {
            binding.celcius.setBackgroundResource(R.drawable.ic_c_selected_white)
            binding.celcius.background =
                resources.getDrawable(R.drawable.button_background_drawable)
            binding.farhenhite.setBackgroundResource(R.drawable.ic_f_unselected)

            _binding!!.temperature.text = "$data_temperature\t°"
            _binding!!.feelLikeValue.text = "$data_temperature\t°"
        }
        binding.farhenhite.setOnClickListener {
            binding.farhenhite.setBackgroundResource(R.drawable.ic_f_selected_white)

            binding.farhenhite.background =
                resources.getDrawable(R.drawable.button_background_drawable)
            binding.celcius.setBackgroundResource(R.drawable.ic_c_unselected)
            var fahrenheit = ((data_temperature.toInt() * 9) / 5) + 32;
            _binding!!.temperature.text = "$fahrenheit K"
            _binding!!.feelLikeValue.text = "$fahrenheit K"
        }

    }


    private fun loadBanner() {
        (application as RawGpsApp).appContainer.myAdsUtill?.AddBannerToLayout(

            this,
            binding.bannerAd,
            AdSize.LARGE_BANNER,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {

                    Toast.makeText(this@WeatherDetailActivity, "Banner shown", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun getDateTime() {
        val currentTime = SimpleDateFormat("HH : mm", Locale.getDefault()).format(Date())
        val simpleDateFormat = SimpleDateFormat("EEEE , dd MMMM yyyy ", Locale.getDefault())
        val date = Date()
        binding.time.text = simpleDateFormat.format(date)
    }

    //get weather forecast
    private fun getWeatherForecast() {
        forecastViewModel.liveData.observe(this, androidx.lifecycle.Observer {
            binding.forcastCard.visibility = View.VISIBLE
            //recyclerView
            binding.detailedCityRecyclerview.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL, false
            )

            val adapter = applicationContext?.let { ForecastAdapter(forecastViewModel.newlist, it) }
            binding.detailedCityRecyclerview.adapter = adapter

            binding.detailedCityRecyclerview.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.recycler_view_anim
                )
            )

        })
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        data_city = null.toString()
        data_description = null.toString()
        data_icon = null.toString()
        data_temperature = null.toString()
        data_wind_speed = null.toString()
        data_water_drop = null.toString()
        data_min = null.toString()
        data_max = null.toString()
        data_lat = null.toString()
        data_long = null.toString()
    }


}