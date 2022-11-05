package com.otl.gps.navigation.map.route.view.activity.onboarding.slideFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.otl.gps.navigation.map.route.databinding.FragmentSlideThreeBinding


class SlideThreeFragment : Fragment() {

    private lateinit var binding: FragmentSlideThreeBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSlideThreeBinding.inflate(layoutInflater)
        return binding.root
    }


}