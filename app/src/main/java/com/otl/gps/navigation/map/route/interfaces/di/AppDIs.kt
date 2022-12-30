package com.otl.gps.navigation.map.route.interfaces.di

import android.app.Application
import com.otl.gps.navigation.map.route.manager.SharedPreferencesManager
import com.otl.gps.navigation.map.route.manager.adManagers.AdmobAdsHelper
import com.otl.gps.navigation.map.route.manager.adManagers.AppOpenAdHelper

class AppDIs(application: Application) {
    val myAdsUtill: AdmobAdsHelper by lazy { AdmobAdsHelper(application.applicationContext) }
    val prefs: SharedPreferencesManager by lazy { SharedPreferencesManager(application.applicationContext) }
    val appOpenAdManager: AppOpenAdHelper by lazy { AppOpenAdHelper() }
}