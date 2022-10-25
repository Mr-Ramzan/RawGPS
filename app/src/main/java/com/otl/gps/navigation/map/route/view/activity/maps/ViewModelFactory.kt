package com.otl.gps.navigation.map.route.view.activity.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abl.gpstracker.navigation.maps.routefinder.app.view.maps.PlacesViewModel
import com.otl.gps.navigation.map.route.utilities.retrofitApi.ApiHelper


class ViewModelFactory(private val apiHelper: ApiHelper) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
//            return WeatherViewModel(Repository(apiHelper)) as T
//        } else
            if (modelClass.isAssignableFrom(PlacesViewModel::class.java)) {
            return PlacesViewModel(Repository(apiHelper)) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }


}