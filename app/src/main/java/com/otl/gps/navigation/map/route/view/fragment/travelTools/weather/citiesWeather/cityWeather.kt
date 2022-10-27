package com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.citiesWeather

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.api.WeatherApi
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.model.citiesModel
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.utils.Constants
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.utils.Convert
import java.util.*
import kotlin.collections.ArrayList

class cityWeather(private val viewModel: cityWeatherViewModel) {


    fun showCitiesWeather(context: Context?) {

        val cities = Constants.CITIES
        val list = ArrayList<String>()
        val locales: Array<String> = Locale.getISOCountries()
//        val cities = ArrayList<String>()
        for (countryCode in locales) {
            val obj = Locale("", countryCode)
            System.out.println("Country Name = " + obj.displayCountry)
            list.add(obj.getDisplayCountry())
        }

        //loop through cities
        for (city in cities) {

            val queue = Volley.newRequestQueue(context)
            val jsonRequest = JsonObjectRequest(
                Request.Method.GET,
                WeatherApi().getCitiesWeather(city),
                null,
                { response ->

                    //city
                    viewModel.city = response.getString("name")

                    //description
                    viewModel.description =
                        response.getJSONArray("weather").getJSONObject(0).getString("main")

                    //icon
                    viewModel.icon =
                        response.getJSONArray("weather").getJSONObject(0).getString("icon")

                    //temperature
                    viewModel.temperature =
                        Convert().convertTemp(response.getJSONObject("main").getString("temp"))

                    //water drop and wind speed
                    viewModel.water_drop =
                        response.getJSONObject("main").getString("humidity") + "\t%"
                    viewModel.wind_speed =
                        response.getJSONObject("wind").getString("speed") + "\tkm/h"


                    //get lat and long
                    viewModel.lat = response.getJSONObject("coord").getString("lat").toString()
                    viewModel.long = response.getJSONObject("coord").getString("lon").toString()


                    //max and min temperature
                    viewModel.min_temp =
                        Convert().convertTemp(response.getJSONObject("main").getString("temp_min"))
                    viewModel.max_temp =
                        Convert().convertTemp(response.getJSONObject("main").getString("temp_max"))


                    val model = citiesModel(
                        viewModel.isCityFav, viewModel.city, viewModel.temperature,
                        viewModel.icon, viewModel.description,
                        viewModel.wind_speed, viewModel.water_drop,
                        viewModel.min_temp, viewModel.max_temp,
                        viewModel.lat, viewModel.long
                    )

                    viewModel.add(model)


                },
                {
                    //Toast.makeText(context, "ERROR", Toast.LENGTH_LONG).show()
                })
            queue.add(jsonRequest)

        }

    }


}