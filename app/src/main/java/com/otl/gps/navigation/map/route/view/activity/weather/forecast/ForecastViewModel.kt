package com.otl.gps.navigation.map.route.view.activity.weather.forecast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.otl.gps.navigation.map.route.view.activity.weather.model.forecastModel

class ForecastViewModel : ViewModel() {

    var date = ""
    var temperature = ""
    var weatherIcon = ""
    var min_temperature = ""
    var max_temperΩature = ""

    var liveData = MutableLiveData<ArrayList<forecastModel>>()

    var newlist = arrayListOf<forecastModel>()

    fun add(forecast: forecastModel){
        newlist.add(forecast)
        liveData.value = newlist
    }


}