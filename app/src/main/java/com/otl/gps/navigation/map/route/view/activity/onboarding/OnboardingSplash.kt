package com.otl.gps.navigation.map.route.view.activity.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import application.RawGpsApp
import com.abl.gpstracker.navigation.maps.routefinder.app.view.activities.splash.slideFragments.SlideTwoFragment
import com.bumptech.glide.Glide
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.databinding.ActivityOnboardingSplashBinding
import com.otl.gps.navigation.map.route.view.activity.main.MainController
import com.otl.gps.navigation.map.route.view.activity.onboarding.slideFragments.SlideOneFragment
import com.otl.gps.navigation.map.route.view.activity.onboarding.slideFragments.SlideThreeFragment

class OnboardingSplash  : AppCompatActivity() {
    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    private lateinit var binding: ActivityOnboardingSplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as RawGpsApp).appContainer.prefs.setFirstTimeLaunch(false)
        loadInter()
        hideSystemBars()
        binding = ActivityOnboardingSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSliding()
        setlisteners()
    }


    private fun hideSystemBars() {
        windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }


    private fun showSystemBars() {

        // Hide both the status bar and the navigation bar
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }


    private fun setlisteners() {

        binding.nextButton.setOnClickListener {
            if (binding.viewPager.currentItem == 2) {

            showInterAds {
                var intent: Intent = Intent(this, MainController::class.java)
                startActivity(intent)
                Handler(Looper.getMainLooper()).postDelayed({ finish() }, 700)
            }
            } else {
                binding.viewPager.currentItem = binding.viewPager.currentItem + 1
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                   Slider Related Work
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun initSliding() {
        setUpdata()
        setupfragments()
        setUpViewPager()
    }

    lateinit var fragmentsList: ArrayList<Fragment>
    lateinit var indecatorRecord: ArrayList<Int>

    //    private val sliderHandler: Handler = Handler(Looper.getMainLooper())
    private var pagerCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            try {

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     */
    private fun setUpViewPager() {
        var compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer { page, position ->

            var r: Float = 1F - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f


        }

        binding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@OnboardingSplash) {
                override fun getItemCount(): Int {
                    return fragmentsList.size
                }

                override fun createFragment(position: Int): Fragment {
                    return fragmentsList[position]
                }
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
//                    Toast.makeText(this@SplashOnboardActivity,"position is "+position,Toast.LENGTH_SHORT).show()
                    if (position < 2f) {
                        binding.nextButton.text = "Next"
                    } else {
                        binding.nextButton.text = "Lets Go!"
                    }
                }

            })
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            registerOnPageChangeCallback(pagerCallback)
            adapter?.notifyDataSetChanged()

        }


    }

    private fun setUpdata() {



    }

    private fun setupfragments() {
        if (!::fragmentsList.isInitialized) {
            try {
                fragmentsList = ArrayList()
                fragmentsList.add(SlideOneFragment())
                fragmentsList.add(SlideTwoFragment())
                fragmentsList.add(SlideThreeFragment())

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    var canShowInter = false


    private fun loadInter() {
        if ((application as RawGpsApp).appContainer.myAdsUtill.mInterstitialAd == null) {
            (application as RawGpsApp).appContainer.myAdsUtill.loadInterestitial(this) {
                canShowInter = it
            }
        } else {
            canShowInter = true
        }
    }

    private fun showInterAds(shown: (success: Boolean) -> Unit) {
        if (canShowInter) {
            (application as RawGpsApp).appContainer.myAdsUtill.showInterestitial(this) {
                shown(it)
            }
        } else {
            shown(false)

        }
    }

}