package com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.citiesWeather

interface cityDetailInterface{

    fun onItemClick(
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
    )
}