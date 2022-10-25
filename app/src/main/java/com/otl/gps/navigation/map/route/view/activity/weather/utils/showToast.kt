package com.otl.gps.navigation.map.route.view.activity.weather.utils

import android.content.Context
import android.widget.Toast

class showToast {

    //show success toast
    fun showSuccess(context: Context?, message: String) {
        if (context != null) {
            Toast.makeText(
                context, message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    //show warning failure toast
    fun showFailure(context: Context?, message: String) {
        if (context != null) {
            Toast.makeText(
                context, message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}