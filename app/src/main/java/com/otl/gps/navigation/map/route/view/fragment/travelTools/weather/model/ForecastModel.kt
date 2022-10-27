package com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.model


data class forecastModel(
    val dayOfTheWeek: String,
    val temperature: String,
    val icon: String, val min_temp: String,
    val max_temp: String
)