package com.otl.gps.navigation.map.route.utilities

import android.os.Build
import com.otl.gps.navigation.map.route.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object FirebaseUtils {
    const val REMOTE_CTA_COLOR_IS_BLUE = "native_ad_cta"
    const val IS_NATIVE_UNDER_MAPS = "native_under_maps"
     var nativeCTAColorBlue = true
     var isNativeUnderMaps = true

    fun getRemoteConfig(): FirebaseRemoteConfig? {
        val mFireBaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        mFireBaseRemoteConfig?.let {
            it.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    nativeCTAColorBlue = it.getBoolean(REMOTE_CTA_COLOR_IS_BLUE)
                    isNativeUnderMaps = it.getBoolean(IS_NATIVE_UNDER_MAPS)
                    val result = task.result
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        it.all.forEach { (t, _) ->
                        }
                    }
                }
            }
        }
        return mFireBaseRemoteConfig
    }
}