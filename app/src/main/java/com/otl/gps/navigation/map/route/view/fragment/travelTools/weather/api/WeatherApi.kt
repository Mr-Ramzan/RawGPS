package com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.api

import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.utils.Constants

class WeatherApi {

    fun getCurrentWeather(latitude: String, longitude: String) : String {

        return "${Constants.BASE_URL}lat=$latitude&lon=$longitude&appid=${Constants.OPEN_WEATHER_API_KEY}"
    }


    fun getCitiesWeather(city: String): String {
        return "${Constants.BASE_URL}q=$city&appid=${Constants.OPEN_WEATHER_API_KEY}"
    }


}