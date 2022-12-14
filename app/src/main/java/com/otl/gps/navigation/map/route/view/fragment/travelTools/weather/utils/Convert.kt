package com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.utils

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*

class Convert {

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    fun convertDate(input: String): String {

        val inFormat = SimpleDateFormat("dd.MM.yyyy")

        val date: Date = inFormat.parse(input)

        val outFormat = SimpleDateFormat("EEEE")

        val day: String = outFormat.format(date)

        return day
    }

    fun convertTemp(temperature: String?): String {
        try {
            if (temperature.isNullOrEmpty()) {
                return ""
            }
            val temp = ((((temperature)?.toFloat()!! - 273.15)).toInt()).toString()

            return temp
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }


}