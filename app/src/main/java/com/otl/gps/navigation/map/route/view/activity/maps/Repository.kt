package com.otl.gps.navigation.map.route.view.activity.maps

import com.otl.gps.navigation.map.route.utilities.retrofitApi.ApiHelper


class Repository(private val apiHelper: ApiHelper) {

//    suspend fun getWeatherData(cityname: String) = apiHelper.getWeatherData(cityname)
    suspend fun getPlacesData(input:String) = apiHelper.getPlacesData(input)

}