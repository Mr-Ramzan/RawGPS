package com.otl.gps.navigation.map.route.view.fragment.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import application.RawGpsApp
import com.google.android.gms.ads.AdSize
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.ExitBottomSheetDialogBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.model.NavEvent
import com.otl.gps.navigation.map.route.utilities.Constants
import org.greenrobot.eventbus.EventBus

class ExitDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: ExitBottomSheetDialogBinding

    //    private static final String ARG_PARAM1 = "param1";
    //    private String type;
    private var activity: Activity? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            activity = context
        }
        try {
        } catch (e: ClassCastException) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement TextClicked");
            Log.d("Error BottomS", e.message.toString())
        }
    }






    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.MyTransparentBottomSheetDialogTheme)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {

            dialog?.setCancelable(true)
            dialog?.setCanceledOnTouchOutside(true)

            if (dialog != null && dialog!!.window != null) {

                dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.btnYes.setOnClickListener(this)
        binding.btnNo.setOnClickListener(this)
        isCancelable = true
        Handler(Looper.getMainLooper()).postDelayed({

            try {
                if (dialog != null && dialog!!.window != null) {
                    dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#44000000")))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },500)

//        Handler(Looper.getMainLooper()).postDelayed({
        loadBanner()
//        },700)
    }


    private fun loadBanner() {
        (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
            requireActivity(),
            binding.bannerAd,
            AdSize.LARGE_BANNER,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {
                    Log.d("Add Load Callback", "is ad loaded========>" + success)
                }
            })
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = ExitBottomSheetDialogBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_yes) {
            EventBus.getDefault().post(NavEvent(Constants.BACK_AND_EXIT))

        } else if (id == R.id.btn_no) {
            this.dialog?.dismiss()

            }
        else
            if (id == R.id.close)
            {
                this.dialog?.dismiss()
        }
    }

    override fun onStop() {
        try {
            if (dialog != null && dialog!!.window != null) {
                dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }


    companion object {
        @JvmStatic
        fun newInstance(): ExitDialogFragment {
            //        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1,arg);
//        fragment.setArguments(args);
            return ExitDialogFragment()
        }
    }
}