package com.otl.gps.navigation.map.route.utilities

import com.otl.gps.navigation.map.route.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object FirebaseUtils {
    const val REMOTE_CTA_COLOR_IS_BLUE = "native_ad_cta"

    fun getRemoteConfig(): FirebaseRemoteConfig? {
        val mFireBaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        mFireBaseRemoteConfig.fetchAndActivate()
        return mFireBaseRemoteConfig
    }
}