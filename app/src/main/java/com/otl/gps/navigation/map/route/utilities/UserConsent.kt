package com.otl.gps.navigation.map.route.utilities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import application.RawGpsApp.Companion.isConsentGiven
import application.RawGpsApp

import com.otl.gps.navigation.map.route.R

object UserConsent {
    var loadInterestitialByDefault = false
    var loadRewardedAdByDefault = false
    fun CheckUserConsent(
        App_Name: String,
        mActivity: Activity?,
        m_loadInterestitialByDefault: Boolean,
        m_loadRewardedAdByDefault: Boolean,
        img_res: Int
    ) {
        try {
            loadInterestitialByDefault = m_loadInterestitialByDefault
            loadRewardedAdByDefault = m_loadRewardedAdByDefault
            showConsent(mActivity,App_Name, img_res)
        } catch (var6: Exception) {
            log("Error in CheckUserConsent::$var6")

            var6.printStackTrace()
        }
    }

    private fun showConsent(activity: Activity?,AppName: String, img_res: Int) {



        if (!((activity as Activity).application as RawGpsApp).appContainer. prefs.getBoolean("isConsentGiven", false)) {
            val consentDialog = Dialog((activity as Context?)!!)
            consentDialog.setContentView(R.layout.gps_ads_concent)
            val app_name = consentDialog.findViewById<View>(R.id.app_name) as TextView
            val icon = consentDialog.findViewById<View>(R.id.app_icon_image) as ImageView
            icon.setImageResource(img_res)
            app_name.text = AppName


            val yes_Button = consentDialog.findViewById<TextView>(R.id.yes_button)
            yes_Button.setOnClickListener {
                ((activity as Activity).application as RawGpsApp).appContainer. prefs.setBoolean("isConsentGiven", true)
                ((activity as Activity).application as RawGpsApp).appContainer. prefs.setBoolean("ConsentValue", true)
              isConsentGiven = true
//            startNativeAds(activity,loadInterestitialByDefault, loadRewardedAdByDefault, true)
              consentDialog.dismiss()
            }

            val no_button = consentDialog.findViewById<TextView>(R.id.no_button)
            no_button.setOnClickListener {
                ((activity as Activity).application as RawGpsApp).appContainer. prefs.setBoolean("isConsentGiven", true)
                ((activity as Activity).application as RawGpsApp).appContainer. prefs.setBoolean("ConsentValue", false)
                isConsentGiven = false

//                startNativeAds(activity,loadInterestitialByDefault, loadRewardedAdByDefault, false)
                consentDialog.dismiss()
            }
            val privacy = consentDialog.findViewById<View>(R.id.privacy) as TextView
            privacy.setOnClickListener {
                openPolicyURL(   url = "https://omegatechlab.blogspot.com/p/privacy-policy.html", context = activity )



            }
            consentDialog.setCanceledOnTouchOutside(false)
            consentDialog.setCancelable(false)
            consentDialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
            consentDialog.show()
        } else {

         isConsentGiven = true
        }
    }


    fun openPolicyURL(url:String, context: Activity?){
        if (url.startsWith("http") || url.startsWith("https")) {
            Intent(Intent.ACTION_VIEW,Uri.parse(url)).apply{
                // Below condition checks if any app is available to handle intent
                if (resolveActivity(context!!.packageManager) != null) {
                    context.startActivity(this)
                }else{
                    Toast.makeText(context,"No Application found to open the Policy URL",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun log(msg: String) {
        Log.d("Consent", msg)
    }
}