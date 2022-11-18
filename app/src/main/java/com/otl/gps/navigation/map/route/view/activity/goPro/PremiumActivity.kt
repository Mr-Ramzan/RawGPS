package com.otl.gps.navigation.map.route.view.activity.goPro


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import application.RawGpsApp
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.PremiumUserPopupBinding
import com.otl.gps.navigation.map.route.model.NavEvent
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.Constants.ACTION_SUBSCCRIBED
import com.otl.gps.navigation.map.route.utilities.Constants.FROM_HOME
import com.otl.gps.navigation.map.route.utilities.Constants.FROM_NAVIGATION
import com.otl.gps.navigation.map.route.utilities.Constants.FROM_SPLASH
import com.otl.gps.navigation.map.route.utilities.Constants.PREMIUM_FROM
import com.otl.gps.navigation.map.route.view.activity.main.MainController
import com.otl.gps.navigation.map.route.view.activity.splash.Splash
import games.moisoni.google_iab.BillingConnector
import games.moisoni.google_iab.BillingEventListener
import games.moisoni.google_iab.enums.ErrorType
import games.moisoni.google_iab.enums.ProductType
import games.moisoni.google_iab.models.BillingResponse
import games.moisoni.google_iab.models.ProductInfo
import games.moisoni.google_iab.models.PurchaseInfo
import org.greenrobot.eventbus.EventBus


class PremiumActivity : AppCompatActivity() {

    private lateinit var binding: PremiumUserPopupBinding
    private lateinit var billingConnector: BillingConnector
    private var subscriptionIds = ArrayList<String>()
    private  var FROM = ""


    override fun onDestroy() {
        super.onDestroy()
        if (::billingConnector.isInitialized) {
            billingConnector.release()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBiling()
        binding = PremiumUserPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadInter()
        setupView()
        setupClickListeners()
    }


    private fun setupView() {
        try{
            FROM = intent.getStringExtra(PREMIUM_FROM).toString()

            if(FROM.equals(Constants.FROM_NAVIGATION))
            {
                binding.freeTriesOverTitle.visibility = View.VISIBLE
            }
        }catch (e:Exception){e.printStackTrace()}
    }


    private fun setupClickListeners() {

        binding.continueWithAdsButton.setOnClickListener {
            binding.closeButton.performClick()
        }



        binding.closeButton.setOnClickListener {
//            showInterAds {
               when(FROM){
                   FROM_SPLASH->{
                       startHomeScreen()
                   }


                   FROM_HOME->{

                       onBackPressed()
                   }
                   FROM_NAVIGATION->{

                       onBackPressed()
                   }
                   else-> {

                       onBackPressed()
                   }

//               }
            }
        }
        binding.policyButton.setOnClickListener {
            EventBus.getDefault().post(NavEvent(Constants.ACTION_POLICY))
        }
    }


    private fun  startHomeScreen(){
        try {

            startActivity(Intent(this, MainController::class.java))
            Handler(Looper.getMainLooper()).postDelayed({ this.finish() }, 700)

        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }



    private fun subScribed() {

        binding.closeButton.performClick()
        restart()

    }

    private fun setupBillingActions() {

        binding.continueToTrialButton.setOnClickListener {
            try {
                billingConnector.subscribe(this, subscriptionIds.get(0))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.subscribeNow.setOnClickListener {
            try {
                billingConnector.subscribe(this, subscriptionIds.get(0))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initBiling() {

        if (subscriptionIds.isNullOrEmpty()) {
            subscriptionIds.add(getString(R.string.sub_id_Weekly))
        }
        billingConnector = BillingConnector(this, getString(R.string.billing_hash))
//          .setConsumableIds(consumableIds)
//          .setNonConsumableIds(nonConsumableIds)
            .setSubscriptionIds(subscriptionIds)
            .autoAcknowledge()
            .autoConsume()
            .enableLogging()
            .connect()

        billingConnector.setBillingEventListener(object : BillingEventListener {

            override fun onProductsFetched(skuDetails: List<ProductInfo>) {

                /*
         * This will be called products are fetched
         * */
                var skutext = ""
                if (!skuDetails.isNullOrEmpty())
                {
                    skutext = skuDetails[0].getSubscriptionOfferPrice(0,skuDetails[0].subscriptionOfferDetails.size-1)
                }
                binding.skuText.setText(skutext)


                binding.durationText.setText("Week")


                setupBillingActions()

                for (item in skuDetails) {
//                Toast.makeText(requireContext(),"========>"+item.price,Toast.LENGTH_SHORT).show()
                }

            }

            override fun onPurchasedProductsFetched(
                productType: ProductType,
                purchases: MutableList<PurchaseInfo>
            ) {


                /*Provides a list with fetched purchased products*/

                if (purchases.isNullOrEmpty()) {
                    (application as RawGpsApp).appContainer!!. prefs!!.resetAdsPurchase()
                } else {
                    (application as RawGpsApp).appContainer!!. prefs!!.removeAds()
                }
            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo>) {
                /*Callback after a product is purchased*/
                if (purchases.isNullOrEmpty()) {
                    (application as RawGpsApp).appContainer!!. prefs!!.removeAds()
                } else {
                    (application as RawGpsApp).appContainer!!. prefs!!.removeAds()
                }



                subScribed()
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
                Log.e("Billing Err","======================>"+response.errorType+"=====>"+response.debugMessage)
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


    var canShowInter = false


    private fun loadInter() {
        if (   (  application as RawGpsApp) .appContainer.myAdsUtill.mInterstitialAd == null) {
            (  application as RawGpsApp) .appContainer.myAdsUtill.loadInterestitial(this) {
                canShowInter = it
            }
        } else {
            canShowInter = true
        }
    }

    private fun showInterAds(shown: (success: Boolean) -> Unit) {
        if (canShowInter) {
            (  application as RawGpsApp) .appContainer.myAdsUtill.showInterestitial(this) {
                shown(it)
            }
        } else {
            shown(false)

        }
    }
    fun restart() {
        val intent = Intent(this, Splash::class.java)
        this.startActivity(intent)
        finishAffinity()
    }
}