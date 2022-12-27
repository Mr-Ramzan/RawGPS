package com.otl.gps.navigation.map.route.view.fragment.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import application.RawGpsApp
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdSize
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.FragmentHomeBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.model.NavEvent
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_CANCEL_SUB
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_POLICY
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_SHARE
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_SUBSCCRIBED
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_LOCATION
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_PLACES_LIST
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_PREMIUM
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_ROUTE
import com.otl.gps.navigation.map.route.utilities.Constants.NAVIGATE_TRAVEL_TOOLS
import com.otl.gps.navigation.map.route.utilities.Constants.UPDATE_CANCEL_BUTTON
import games.moisoni.google_iab.BillingConnector
import games.moisoni.google_iab.BillingEventListener
import games.moisoni.google_iab.enums.ErrorType
import games.moisoni.google_iab.enums.ProductType
import games.moisoni.google_iab.models.BillingResponse
import games.moisoni.google_iab.models.ProductInfo
import games.moisoni.google_iab.models.PurchaseInfo
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupBg()
        return root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBiling()
        loadBanner()
        setListeners()
    }

    private fun setupBg() {

        try {
            Glide.with(this).load(R.drawable.home_bg).into(binding.bg)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }




    override fun onAttach(context: Context) {
        EventBus.getDefault().register(this)
        super.onAttach(context)

    }


    override fun onDetach() {
        EventBus.getDefault().unregister(this)
        super.onDetach()
    }

    @Subscribe
    public fun onEvent(event: NavEvent) {
        when (event.event) {
            ACTION_CANCEL_SUB -> {
                cancelSub()
            }
            ACTION_SUBSCCRIBED -> {
                hideShowInappsButton()
            }
        }
    }
    private fun cancelSub() {
        binding.proButton.visibility = View.VISIBLE
        EventBus.getDefault().post(NavEvent(UPDATE_CANCEL_BUTTON))
        billingConnector.unsubscribe(requireActivity(), subscriptionIds.get(0))
    }

    private fun hideShowInappsButton() {
        try {
            if ((requireActivity().application as RawGpsApp).appContainer!!.prefs!!.areAdsRemoved()!!) {
//                binding.unsubscribeText.visibility=View.VISIBLE
                EventBus.getDefault().post(NavEvent(UPDATE_CANCEL_BUTTON))
            }else{

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setListeners() {

        binding.routeFinderButton.setOnClickListener {
            it.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                it.isEnabled = true
            }, 700)
            EventBus.getDefault().post(NavEvent(NAVIGATE_ROUTE))
        }

        binding.myLocation.setOnClickListener {
            it.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                it.isEnabled = true
            }, 700)
            EventBus.getDefault().post(NavEvent(NAVIGATE_LOCATION))
        }

        binding.travelTools.setOnClickListener {
            it.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                it.isEnabled = true
            }, 700)
            EventBus.getDefault().post(NavEvent(NAVIGATE_TRAVEL_TOOLS))
        }


        binding.nearbyPlaces.setOnClickListener {
            it.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                it.isEnabled = true
            }, 700)
            EventBus.getDefault().post(NavEvent(NAVIGATE_PLACES_LIST))
        }


        binding.proButton.setOnClickListener {
            it.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                it.isEnabled = true
            }, 700)
            EventBus.getDefault().post(NavEvent(NAVIGATE_PREMIUM))
        }

        var ROTATE_FROM = 0.0f;
        var ROTATE_TO = -10.0f * 360.0f;// 3.141592654f * 32.0f;
        val r: RotateAnimation
        r = RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        r.duration = 2L * 1500
        r.repeatCount = Animation.INFINITE
        r.repeatMode = Animation.INFINITE
        binding.proButton.startAnimation(r)


        binding.weather.setOnClickListener {
            it.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                it.isEnabled = true
            }, 700)
            EventBus.getDefault().post(NavEvent(Constants.NAVIGATE_SAVED_PLACES))
        }


        binding.policyButton.setOnClickListener {
            it.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                it.isEnabled = true
            }, 700)
            EventBus.getDefault().post(NavEvent(ACTION_POLICY))
        }
        binding.shareAppIb.setOnClickListener {
            it.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                it.isEnabled = true
            }, 700)
            EventBus.getDefault().post(NavEvent(ACTION_SHARE))
        }


        binding.menuButton.setOnClickListener {
            it.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                it.isEnabled = true
            }, 700)
//            EventBus.getDefault().post(NavEvent(OPEN_DRAWER))
        }


    }



    private fun loadBanner() {
        (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
            requireActivity(),
            binding.adsParent,
            AdSize.MEDIUM_RECTANGLE,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {
                    Log.d("Add Load Callback", "is ad loaded========>" + success)
                }
            })
    }

    var canShowNativeAd = false
    var adsReloadTry = 0

    /**
     * Loading ads once if not loaded
     * there will be max three tries if once ad loaded it will not be loaded again but if not code will ask
     */
    private fun loadNativeBanner() {

        if (!(requireActivity().application as RawGpsApp).appContainer.prefs.areAdsRemoved()) {
            (requireActivity().application as RawGpsApp).appContainer.myAdsUtill?.loadSmallNativeAd(
                requireActivity(),
                true,
                object : AdLoadedCallback {

                    override fun addLoaded(success: Boolean?) {

                        if (isDetached) {
                            return
                        }

                        if (success != null && success) {
                            adsReloadTry += 1
                            canShowNativeAd = success
                            showNativeAd()
                        } else {

                            /////////////////////////////
                            if (success == null || !success) {
                                canShowNativeAd = false
                                binding.adsParent.visibility = View.GONE

                            } else {
                                canShowNativeAd = success
                            }
                            /////////////////////////////
                            adsReloadTry += 1
                            if (adsReloadTry < Constants.ADS_RELOAD_MAX_TRIES) {
                                loadNativeBanner()
                            }
                        }
                    }
                }
            )
        }

    }

    private fun showNativeAd() {
        try {
            if (isDetached) {
                return
            }
            val isAdsRemoved =
                (requireActivity().application as RawGpsApp).appContainer.prefs.areAdsRemoved()
            if (!isAdsRemoved) {

                if (canShowNativeAd)
                {
                    (requireActivity().application as RawGpsApp).appContainer.myAdsUtill.showSmallNativeAd(
                        requireActivity(),
                        Constants.BIG_NATIVE,
                        binding.adsParent, true, true
                    )
                }
                else
                {
                    binding.adsParent.visibility = View.GONE
                }


            } else {
                binding.adsParent.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    private lateinit var billingConnector: BillingConnector
    private var subscriptionIds = ArrayList<String>()

    private fun initBiling() {

        if (subscriptionIds.isNullOrEmpty()) {
            subscriptionIds.add(getString(R.string.sub_id_Weekly))
        }

        billingConnector = BillingConnector(requireContext(), getString(R.string.billing_hash))
//          .setConsumableIds(consumableIds)
//          .setNonConsumableIds(nonConsumableIds)
            .setSubscriptionIds(subscriptionIds)
            .autoAcknowledge()
            .autoConsume()
            .enableLogging()
            .connect()

        billingConnector.setBillingEventListener(object : BillingEventListener {
            override fun onProductsFetched(skuDetails: List<ProductInfo>) {

            }

            override fun onPurchasedProductsFetched(
                skuType: ProductType,
                purchases: List<PurchaseInfo?>
            ) {
                /*Provides a list with fetched purchased products*/

                if (purchases.isNullOrEmpty()) {
                    try {
                        (requireActivity().application as RawGpsApp).appContainer.prefs.resetAdsPurchase()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        (requireActivity().application as RawGpsApp).appContainer.prefs.removeAds()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                hideShowInappsButton()
            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo>) {
                /*Callback after a product is purchased*/
                hideShowInappsButton()
            }

            override fun onPurchaseAcknowledged(purchase: PurchaseInfo) {
                /*Callback after a purchase is acknowledged*/

                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */
            }

            override fun onPurchaseConsumed(purchase: PurchaseInfo) {
                /*Callback after a purchase is consumed*/

                /*
                 * Grant user entitlement for CONSUMABLE products here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the user will be able consume the product
                 * without actually paying
                 * */
            }

            override fun onBillingError(
                billingConnector: BillingConnector,
                response: BillingResponse
            ) {
                /*Callback after an error occurs*/
                when (response.errorType) {
                    ErrorType.CLIENT_NOT_READY -> {
                    }
                    ErrorType.CLIENT_DISCONNECTED -> {
                    }
                    ErrorType.CONSUME_ERROR -> {
                    }
                    ErrorType.CONSUME_WARNING -> {
                    }
                    ErrorType.ACKNOWLEDGE_ERROR -> {
                    }
                    ErrorType.ACKNOWLEDGE_WARNING -> {
                    }
                    ErrorType.FETCH_PURCHASED_PRODUCTS_ERROR -> {
                    }
                    ErrorType.BILLING_ERROR -> {
                    }
                    ErrorType.USER_CANCELED -> {
                    }
                    ErrorType.SERVICE_UNAVAILABLE -> {
                    }
                    ErrorType.BILLING_UNAVAILABLE -> {
                    }
                    ErrorType.ITEM_UNAVAILABLE -> {
                    }
                    ErrorType.DEVELOPER_ERROR -> {
                    }
                    ErrorType.ERROR -> {
                    }
                    ErrorType.ITEM_ALREADY_OWNED -> {
                    }
                    ErrorType.ITEM_NOT_OWNED -> {
                    }
                    else -> {}
                }
            }
        })

    }



}