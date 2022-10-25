package com.otl.gps.navigation.map.route.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import application.RawGpsApp
import com.google.android.gms.ads.AdSize
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback


object PopupUtil
{

    public fun showDeleteDialog(context:Context, Header:String, Desc:String, done:(success:Boolean)->Unit) {
        val deleteDialog: AlertDialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exit, null)
        val yes = dialogView.findViewById<TextView>(R.id.btn_yes)
        val no = dialogView.findViewById<TextView>(R.id.btn_no)
        val nativeAd = dialogView.findViewById<FrameLayout>(R.id.nativeAdd)

        try {
//            utill.showSmallNativeAd(
//                context as Activity, Constants.POPUP_NATIVE,
//                nativeAd,
//                prefs?.areAdsRemoved()!!,false
//            )

            ((context as Activity).application as RawGpsApp).appContainer.myAdsUtill .AddBannerToLayout(
                context as Activity,
                nativeAd,
                AdSize.MEDIUM_RECTANGLE,
                object : AdLoadedCallback {
                    override fun addLoaded(success: Boolean?) {

                    }

                }
            )

        }catch (e:Exception){e.printStackTrace()}
        dialogView.findViewById<TextView>(R.id.header_text).text = Header
        dialogView.findViewById<TextView>(R.id.disc_text).text = Desc
        val dialogBuilder = AlertDialog.Builder(
            context
        )
        deleteDialog = dialogBuilder.create()
        deleteDialog.setView(dialogView)
        yes.setOnClickListener {
            done(true)
            deleteDialog.dismiss()
        }
        no.setOnClickListener { deleteDialog.dismiss() }
        deleteDialog.show()
    }


    @SuppressLint("SetTextI18n")
    public fun showPermissionSettingPopup(context:Context, done:(success:Boolean)->Unit) {
        val deleteDialog: AlertDialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exit, null)
        val yes = dialogView.findViewById<TextView>(R.id.btn_yes)
        val no = dialogView.findViewById<TextView>(R.id.btn_no)
        val nativeAd = dialogView.findViewById<FrameLayout>(R.id.nativeAdd)
        yes.text = "Open Settings"
        no.text = "Cancel"
        nativeAd.visibility= GONE

        dialogView.findViewById<TextView>(R.id.header_text).text = context.getString(R.string.permissionDeniedHeader)
        dialogView.findViewById<TextView>(R.id.disc_text).text = context.getString(R.string.storage_concent_desc)
        val dialogBuilder = AlertDialog.Builder(
            context
        )
        deleteDialog = dialogBuilder.create()
        deleteDialog.setView(dialogView)
        yes.setOnClickListener {
            done(true)
            deleteDialog.dismiss()
        }
        no.setOnClickListener {
            done(false)

            deleteDialog.dismiss() }
        deleteDialog.show()
    }

}