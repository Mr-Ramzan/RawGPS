package com.otl.gps.navigation.map.route.utilities

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.otl.gps.navigation.map.route.databinding.LoadingDialogBinding

object DialogUtils {

    lateinit var  loadingDialog: Dialog

    fun showLoadingDialog(activity: Activity?) {

        if(::loadingDialog.isInitialized && loadingDialog.isShowing){
             return
        }
        var loadingDialogBinding = LoadingDialogBinding.inflate(activity?.layoutInflater!!)
        ////////////////////////////////////////////////////////////////////////////////////////////
        loadingDialog = Dialog(activity)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
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