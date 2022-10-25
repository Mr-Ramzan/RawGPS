package com.otl.gps.navigation.map.route.model

data class WeatherData(
    val main: Main,
    val sys: Sys,
    val weather: List<Weather>,
    val name: String,
    val cod: Int
)

data class Main(val temp: Double)
data class Weather(val description: String)
data class Sys(val country: String)