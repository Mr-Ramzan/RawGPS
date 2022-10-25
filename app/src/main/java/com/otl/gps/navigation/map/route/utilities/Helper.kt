package com.otl.gps.navigation.map.route.utilities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.text.SpannableString
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.otl.gps.navigation.map.route.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


object Helper {

    var CITY_CLOCK_NAME = "city_clock"

    //    ad view types
    public val POPUP_NATIVE = "popup_ads"
    public val START_NATIVE_SMALL = "native_small_button_start"
    public val STYLE_LIST_NATIVE_SMALL = "native_small_style_list"
    public val START_NATIVE = "native_samll_start"
    public val MENU_NATIVE = "native_samll_menu"
    public val BIG_NATIVE = "native_big"

    /*==========================================================
                              IN APPS
    ===========================================================*/
    val PREF_REMOVE_ADS: String = "ads_removed"
    public val PREF_REMOVE_AD_INAPP =
        "com.abl.stickermaker.forwhatsapp.remove_ads"


    /////////////////////////////////////////////////////////////////////////////
    //Navigation Events
    const val NAVIGATE_3D_Map_Traffic = "3d_Map"
    const val NAVIGATE_3D_Map_Satellite = "3d_Map_satelite"
    const val NAVIGATE_Earth_Map_Traffic = "earth_Map"
    const val NAVIGATE_Traffic_Map_Satellie = "traffic_MapSatellite"

    const val NAVIGATE_3D_Map = "3d_Map_satelite"
    const val NAVIGATE_Earth_Map = "earth_Map"
    const val NAVIGATE_Traffic_Map = "traffic_MapSatellite"

    fun startActivity(mActivity: Activity, mIntent: Intent, isFinish: Boolean) {
        mActivity.startActivity(mIntent)
        runAnimation(mActivity)
        if (isFinish) {
            mActivity.finish()
        }
    }

    fun startActivityReverse(mActivity: Activity, mIntent: Intent, isFinish: Boolean) {
        mActivity.startActivity(mIntent)
        runReverseAnimation(mActivity)
        if (isFinish) {
            mActivity.finish()
        }
    }

    @JvmStatic
    fun showToast(context: Context?, message: String) {
        var spannableString = SpannableString(message)
        Toast.makeText(context, spannableString, Toast.LENGTH_LONG).show()
    }

    fun showGpsDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Your GPS seems to be disabled, please enable it to continue !")
        builder.setCancelable(true)
        builder.setPositiveButton(
            "Yes"
        ) { dialog, which: Int ->
            dialog.dismiss()
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        builder.show()
    }


    private fun runAnimation(mActivity: Activity) {
        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun runReverseAnimation(mActivity: Activity) {
        mActivity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }


    fun getFormattedDate(
        strDate: String,
        sourceFormat: String,
        destinyFormat: String,
        locale: Locale
    ): String {
        var df = SimpleDateFormat(sourceFormat, locale)
        var date: Date? = null
        try {
            date = df.parse(strDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        df = SimpleDateFormat(destinyFormat, locale)
        return df.format(date)
    }


    fun getStringToDate(strDate: String, sourceFormate: String): Date? {
        var df = SimpleDateFormat(sourceFormate)
        var date: Date? = null
        try {
            date = df.parse(strDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return date

    }


    fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) {
            false
        } else {
            android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    fun hideKeyboard(activity: Activity) {
        // Check if no view has focus:
        val view = activity.currentFocus
        if (view != null) {
            val inputManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager!!.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodatDate(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)

        return formatted
    }

    fun getCurrentTime(): String {
        val dt = Date()
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        val time1 = sdf.format(dt)
        return time1
    }


    fun getClockCityName(str: String): String {
        return str.substringAfterLast("/")
    }



    fun convertTimeZoneToTime(timeZone: String?): String? {

        var time: String
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone(timeZone)
        var hours = (calendar[Calendar.HOUR_OF_DAY])
        var mints = (calendar[Calendar.MINUTE])
        time = "$hours:${mints}"
        if (hours > 12) {
            if (mints < 10) {
                time = "${hours - 12}:0${mints} PM"
            } else {
                time = "${hours - 12}:${mints} PM"
            }
        } else if (hours == 0) {
            if (mints < 10) {
                time = "${hours + 12}:0${mints} AM"
            } else {
                time = "${hours + 12}:${mints} AM"
            }
        } else if (hours < 12) {
            if (hours < 10) {
                if (mints < 10) {
                    time = "0${hours}:0${mints} AM"
                } else {
                    time = "0${hours}:${mints} AM"
                }
            } else {
                if (mints < 10) {
                    time = "${hours}:0${mints} AM"
                } else {
                    time = "${hours}:${mints} AM"
                }
            }
            if (mints < 10) {
                time = "${hours}:0${mints} AM"
            } else {
                time = "${hours}:${mints} AM"
            }
        }
        return time

    }
}

