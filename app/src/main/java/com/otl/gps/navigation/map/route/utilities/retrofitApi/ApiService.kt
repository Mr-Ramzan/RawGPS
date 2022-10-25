package com.otl.gps.navigation.map.route.utilities.retrofitApi



import com.otl.gps.navigation.map.route.model.PlacesModel
import com.otl.gps.navigation.map.route.model.WeatherData
import com.otl.gps.navigation.map.route.utilities.Constants
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET(Constants.WEATHER_APP_ID)
    suspend fun getWeatherDetail(
        @Query("q") cityname: String,
    ): WeatherData

    @GET(Constants.PLACES_APP_ID)
    suspend fun getPlacesData(
        @Query("input") input: String,
    ): PlacesModel

}