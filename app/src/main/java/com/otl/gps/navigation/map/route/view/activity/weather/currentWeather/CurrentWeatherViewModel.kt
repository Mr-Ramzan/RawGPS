package com.otl.gps.navigation.map.route.view.activity.weather.currentWeather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CurrentWeatherViewModel : ViewModel() {

    var city = ""
    var icon = ""
    var description = ""
    var temperature = ""
    var wind_speed = ""
    var water_drop = ""
    var min_temp = ""
    var max_temp = ""
    var lat = ""
    var long = ""

    val WeatherService : MutableLiveData<String> by lazy{
        MutableLiveData<String>()
    }

}