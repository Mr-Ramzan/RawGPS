package com.otl.gps.navigation.map.route.view.activity.weather.model


data class citiesModel(
    var isCityFav: Boolean,
    val city: String, val temp: String,
    val icon: String, val description: String,
    val wind_speed: String, val water_drop: String,
    val mintemp: String, val maxtemp: String,
    val lat: String, val long: String
){

}