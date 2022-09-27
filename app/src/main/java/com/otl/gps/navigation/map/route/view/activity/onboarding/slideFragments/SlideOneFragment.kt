package com.otl.gps.navigation.map.route.view.activity.onboarding.slideFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.otl.gps.navigation.map.route.databinding.FragmentSlideOneBinding


class SlideOneFragment : Fragment() {

    private lateinit var binding: FragmentSlideOneBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSlideOneBinding.inflate(layoutInflater)
        return binding.root
    }




}