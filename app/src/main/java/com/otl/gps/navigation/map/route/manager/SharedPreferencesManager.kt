package com.otl.gps.navigation.map.route.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SharedPreferencesManager(mContext: Context) {

    private var sharedPreferences: SharedPreferences? = null
    private var FIRST_LAUNCH_KEY: String = "LAUNCH_FIRST_TIME"
    private var NAVIGATED_THRICE: String = "ONCE_NAVIGATED"
    private var NAVIGATION_COUNT: String = "NavCount"
    private var NAVIGATION_MAX_COUNT: String = "NavCountMAX"
    private var PREMIUM_SCREEN_THRESH_HOLD: String = "premium_directions_max_count"
    private var APP_LAUNCH_COUNT: String = "app_launch_count"


    ///////////////////////////////////////////////////////////////////////
    private var ADS_REMOVED: String = "MAPS_ADS_REMOED"
    private var APP_OPEN_DISABLED: String = "APP_OPEN_AD_IS_DISABLED_TEMP_OR_FORALL"

    init {
        if (sharedPreferences == null) {
            sharedPreferences =
                mContext.getSharedPreferences("private-prefs", Context.MODE_PRIVATE)
        }
    }

    fun removeKey(key: String?) {

        if (sharedPreferences != null) {
            sharedPreferences!!.edit().remove(key).apply()
        }

    }

    fun clear() {
        if (sharedPreferences != null) {
            sharedPreferences!!.edit().clear().apply()
        }
    }

    operator fun contains(key: String?): Boolean {
        return sharedPreferences!!.contains(key)
    }

    fun getInt(key: String?, defaultvalue: Int): Int {
        return sharedPreferences!!.getInt(key, defaultvalue)
    }

    fun getAppLaunchCount(defaultvalue: Int = 0): Int {
        return sharedPreferences!!.getInt(APP_LAUNCH_COUNT, defaultvalue)
    }

    fun getLong(key: String?, defaultvalue: Long): Long {
        return sharedPreferences!!.getLong(key, defaultvalue)
    }

    fun getString(key: String?, defaultvalue: String?): String? {
        return sharedPreferences!!.getString(key, defaultvalue)
    }

    fun getBoolean(key: String?, defaultvalue: Boolean): Boolean {
        return sharedPreferences!!.getBoolean(key, defaultvalue)
    }

    fun areAdsRemoved(defaultvalue: Boolean = false): Boolean {
        return sharedPreferences!!.getBoolean(ADS_REMOVED, defaultvalue)
    }
    fun canShowAppOpenAds(defaultvalue: Boolean = true): Boolean {
        return sharedPreferences!!.getBoolean(APP_OPEN_DISABLED, defaultvalue)
    }

    fun getFirstLaunch(defaultvalue: Boolean = true): Boolean {
        return sharedPreferences!!.getBoolean(FIRST_LAUNCH_KEY, defaultvalue)
    }

    fun getPremiumScreenThreshHold(value: Int = 0): Int {
        return sharedPreferences!!.getInt(PREMIUM_SCREEN_THRESH_HOLD, value)
    }

    fun getNavigationCount(value: Int = 0): Int {
        return sharedPreferences!!.getInt(NAVIGATION_COUNT, value)
    }

    fun getNavigationMaxCount(value: Int = 0): Int {
        return sharedPreferences!!.getInt(NAVIGATION_MAX_COUNT, value)
    }

    fun getMaxTriesReached(defaultvalue: Boolean = false): Boolean {
        return sharedPreferences!!.getBoolean(NAVIGATED_THRICE, defaultvalue)
    }

    fun getFloat(key: String?, defaultvalue: Float): Float {
        return sharedPreferences!!.getFloat(key, defaultvalue)
    }

    fun setString(key: String?, value: String?) {
        sharedPreferences!!.edit().putString(key, value).apply()
    }

    fun setInt(key: String?, value: Int) {
        sharedPreferences!!.edit().putInt(key, value).apply()
    }

    fun getInt(key: String?): Int {
        return sharedPreferences!!.getInt(key, -1)
    }

    fun setLong(key: String?, value: Long) {
        sharedPreferences!!.edit().putLong(key, value).apply()
    }

    fun getLong(key: String?): Long {
        return sharedPreferences!!.getLong(key, -1)
    }

    fun setBoolean(key: String?, value: Boolean) {
        sharedPreferences!!.edit().putBoolean(key, value).apply()
    }

    fun setFirstTimeLaunch(value: Boolean = false) {
        sharedPreferences!!.edit().putBoolean(FIRST_LAUNCH_KEY, value).apply()
    }

    fun setThreshHold(value: Int = 0) {
        sharedPreferences!!.edit().putInt(PREMIUM_SCREEN_THRESH_HOLD, value).apply()
    }

    fun setNavigationCount(value: Int = 0) {
        sharedPreferences!!.edit().putInt(NAVIGATION_COUNT, value).apply()
    }

    fun setAppLaunchCount(value: Int = 0) {
        sharedPreferences!!.edit().putInt(APP_LAUNCH_COUNT, value).apply()
        Log.e("SetAppLaunchCount====>", "=============>${getAppLaunchCount()}")
    }

    fun setNavigationMAXCount(value: Int = 0) {
        sharedPreferences!!.edit().putInt(NAVIGATION_MAX_COUNT, value).apply()
    }

    fun setMaxTriesReached(value: Boolean = false) {
        sharedPreferences!!.edit().putBoolean(NAVIGATED_THRICE, value).apply()
    }

    fun removeAds(defaultvalue: Boolean = true) {
        sharedPreferences!!.edit().putBoolean(ADS_REMOVED, defaultvalue).apply()
    }

    fun disableAppOpenAds(defaultvalue: Boolean = true) {
        sharedPreferences!!.edit().putBoolean(APP_OPEN_DISABLED, defaultvalue).apply()
    }

    fun getBoolean(key: String?): Boolean {
        return sharedPreferences!!.getBoolean(key, false)
    }

    fun setFloat(key: String?, value: Float) {
        sharedPreferences!!.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String?): Float {
        return sharedPreferences!!.getFloat(key, 0f)
    }

    fun resetAdsPurchase() {
        sharedPreferences!!.edit().putBoolean(ADS_REMOVED, false).apply()
    }
}