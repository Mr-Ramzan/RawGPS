package com.abl.gpstracker.navigation.maps.routefinder.app.view.activities.splash.slideFragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.otl.gps.navigation.map.route.databinding.FragmentSlideTwoBinding


class SlideTwoFragment : Fragment() {

    private lateinit var binding: FragmentSlideTwoBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSlideTwoBinding.inflate(layoutInflater)
        return binding.root
    }



}