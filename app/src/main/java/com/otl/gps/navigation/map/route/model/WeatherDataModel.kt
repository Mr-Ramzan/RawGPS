package com.otl.gps.navigation.map.route.model

data class WeatherData(
    val main: Main? = Main(),
    val sys: Sys? = Sys(),
    val weather: List<Weather> = ArrayList(),
    val name: String = "",
    val cod: Int = -111
)

data class Main(var temp: Double? = 0.0)
data class Weather(var description: String = "")
data class Sys(var country: String = "")