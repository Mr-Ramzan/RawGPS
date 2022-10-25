package com.otl.gps.navigation.map.route.utilities.retrofitApi

class ApiHelper(private val apiService: ApiService) {

    suspend fun getWeatherData(cityname: String) = apiService.getWeatherDetail(cityname)
    suspend fun getPlacesData(input:String) = apiService.getPlacesData(input)
}