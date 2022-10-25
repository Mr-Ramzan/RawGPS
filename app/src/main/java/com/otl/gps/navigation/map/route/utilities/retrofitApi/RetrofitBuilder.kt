package com.otl.gps.navigation.map.route.utilities.retrofitApi

import com.otl.gps.navigation.map.route.utilities.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

    private fun getRetrofit(baseUrl:String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService = getRetrofit(Constants.BASE_URL_WEATHER).create(ApiService::class.java)
    val apiServicePlaces: ApiService = getRetrofit(Constants.BASE_URL_PLACES).create(ApiService::class.java)
}