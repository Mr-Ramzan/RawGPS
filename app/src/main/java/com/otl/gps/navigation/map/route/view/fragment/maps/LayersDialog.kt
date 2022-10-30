package com.otl.gps.navigation.map.route.view.fragment.maps

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import com.google.android.gms.maps.GoogleMap
import com.otl.gps.navigation.map.route.databinding.LayersDialogBinding

object LayersDialog {

    lateinit var  loadingDialog: Dialog
    fun showMapsLayersDialog(activity: Activity?, map: GoogleMap,togglAngle:()->Unit,togglTraffic: (traffic:Boolean) -> Unit) {

        if(::loadingDialog.isInitialized && loadingDialog.isShowing){
             return
        }
        var loadingDialogBinding = LayersDialogBinding.inflate(activity?.layoutInflater!!)
        ////////////////////////////////////////////////////////////////////////////////////////////
        loadingDialog = Dialog(activity)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(true)
        ////////////////////////////////////////////////////////////////////////////////////////////

        loadingDialogBinding.trafficToggleButton.setOnClickListener {
            map.isTrafficEnabled = !map.isTrafficEnabled
            togglTraffic( map.isTrafficEnabled)
            dismissLoading()
        }
        loadingDialogBinding.angleButton.setOnClickListener {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            togglAngle()
            dismissLoading()
        }

        loadingDialogBinding.satelliteToggleButton.setOnClickListener {
            if (map.mapType == GoogleMap.MAP_TYPE_SATELLITE)
            {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            else
            {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
            dismissLoading()
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        loadingDialog.setContentView(loadingDialogBinding.root)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog.show()
    }



    fun dismissLoading(){

        if(::loadingDialog.isInitialized && loadingDialog.isShowing){
            loadingDialog.dismiss()
        }

    }





}