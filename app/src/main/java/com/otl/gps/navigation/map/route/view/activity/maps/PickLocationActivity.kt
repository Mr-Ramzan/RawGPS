package com.otl.gps.navigation.map.route.view.activity.maps

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import application.RawGpsApp
import com.abl.gpstracker.navigation.maps.routefinder.app.utils.GeoCoderAddress
import com.abl.gpstracker.navigation.maps.routefinder.app.view.maps.PlacesViewModel
import com.bumptech.glide.Glide
import com.otl.gps.navigation.map.route.R
import com.google.android.gms.ads.AdSize
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.otl.gps.navigation.map.route.databinding.ActivitySerchPlacesBinding
import com.otl.gps.navigation.map.route.interfaces.AdLoadedCallback
import com.otl.gps.navigation.map.route.interfaces.PlacesAdapterListener
import com.otl.gps.navigation.map.route.utilities.Constants
import com.otl.gps.navigation.map.route.utilities.retrofitApi.ApiHelper
import com.otl.gps.navigation.map.route.utilities.retrofitApi.RetrofitBuilder
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete


class PickLocationActivity : AppCompatActivity(), PlacesAdapterListener {
    private lateinit var binding: ActivitySerchPlacesBinding
    private var TAG = "SelectLocationActivity"
    private lateinit var viewModel: PlacesViewModel
    private var fromLocation = false
    private var toLocation = false
    private var isFindAddressClick = false
    private lateinit var geocoAddress: GeoCoderAddress
    private var address: String? = null
    private var searchLatitude: String? = null
    private var searchLongitude: String? = null
    var canShowNativeAd = false
    var canShowInter = false
    var adsReloadTry = 0

    private fun setupBg() {
        try {
            Glide.with(this).load(R.drawable.home_bg).into(binding.homeBgView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySerchPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBg()
        binding.searchResultsView.apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            isVisible = true
        }
        binding.searchBox.performClick()
        geocoAddress = GeoCoderAddress(this)
        clickEvent()
        setupViewModel()
        loadBanner()

    }





    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiServicePlaces))
        ).get(PlacesViewModel::class.java)

    }


    fun Context.hideKeyboard(view: View) {

        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

    }


    private fun clickEvent() {
        fromLocation = intent.getBooleanExtra("isFromLocationClick", false)
        toLocation = intent.getBooleanExtra("isToLocationClick", false)
        isFindAddressClick = intent.getBooleanExtra("isFindAddressClick", false)
        val addressMyLocation = intent.getStringExtra(Constants.ADDRESS_MY_LOCATION)
        val addressFromMyLocation = intent.getStringExtra(Constants.ADDRESS_FROM_MY_LOCATION)
        val addressToMyLocation = intent.getStringExtra(Constants.ADDRESS_TO_MY_LOCATION)
        val latitudeMyLocation = intent.getStringExtra(Constants.LATITUDE_MY_LOCATION)
        val longitudeMyLocation = intent.getStringExtra(Constants.LONGITUDE_MY_LOCATION)


        binding.llSelectOnMap.setOnClickListener {
            if (fromLocation) {
                val intent = Intent(this, LocationFromGoogleMapActivity::class.java)
                intent.putExtra("isFromLocationClick", true)
                intent.putExtra("isSelectOnMapClick", true)
                pickLocationResultsLauncher.launch(intent)

            }
            if (toLocation) {
                val intent = Intent(this, LocationFromGoogleMapActivity::class.java)
                intent.putExtra("isToLocationClick", true)
                intent.putExtra("isSelectOnMapClick", true)
                pickLocationResultsLauncher.launch(intent)

            }
            if (isFindAddressClick) {
                val intent = Intent(this, LocationFromGoogleMapActivity::class.java)
                intent.putExtra("isFindAddressClick", true)
                pickLocationResultsLauncher.launch(intent)

            }
        }

        binding.llMyLocation.setOnClickListener {

            if (fromLocation) {
                (application as RawGpsApp).appContainer.prefs .setString(
                    Constants.ADDRESS_FROM_LOCATION,
                    addressFromMyLocation
                )

                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LATITUDE_FROM_LOCATION,
                    latitudeMyLocation
                )
                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LONGITUDE_FROM_LOCATION,
                    longitudeMyLocation
                )

                val intent = Intent()
                intent.putExtra("isFromLocationClick", true)
                intent.putExtra("isMyLocationClick", true)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            if (toLocation) {
                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.ADDRESS_TO_LOCATION, addressToMyLocation
                )
                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LATITUDE_TO_LOCATION,
                    latitudeMyLocation
                )
                (application as RawGpsApp).appContainer.prefs.setString(
                    Constants.LONGITUDE_TO_LOCATION,
                    longitudeMyLocation
                )
                val intent = Intent()
                intent.putExtra("isToLocationClick", true)
                intent.putExtra("isMyLocationClick", true)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            if (isFindAddressClick) {
//                val intent = Intent(this, FindAddressActivity::class.java)
//                intent.putExtra("isMyLocationClick", true)
//                startActivity(intent)
            }
        }

        //        binding.searchLocation.setOnClickListener {
        //            val intent = PlaceAutocomplete.IntentBuilder()
        //                .accessToken(getString(R.string.mapbox_access_token))
        //                .placeOptions(
        //                    PlaceOptions.builder()
        //                        .backgroundColor(Color.parseColor("#EEEEEE"))
        //                        .limit(10)
        //                        .country("pk")
        //                        .build(PlaceOptions.MODE_CARDS)
        //                )
        //                .build(this)
        //            sutoCompleteResultsLauncher.launch(intent)
        //        }


        ////////////////////////////////////////////////////////////////////////////////////////////

//==================================================================================================
//        binding.simpleSearchView.queryHint = getString(R.string.query_hint)
//        binding.simpleSearchView.isFocusable = true
//        binding.simpleSearchView.isIconified = false
//
//        binding.simpleSearchView.requestFocusFromTouch()
//        binding.simpleSearchView.onFocusChangeListener =
//            View.OnFocusChangeListener { _, hasFocus ->
//                binding.searchResultsView.isVisible = hasFocus
//            }
//==================================================================================================
//        binding.simpleSearchView.setOnQueryTextListener(object :
//            SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String): Boolean {
//                return false }
//            override fun onQueryTextChange(newText: String): Boolean {
//                binding.searchResultsView.search(newText)
//                return false } }
//        )
//==================================================================================================
        binding.searchButton.setOnClickListener {

            if (binding.searchBox.text.toString().isNotEmpty()) {

                binding.searchResultsView.search(binding.searchBox.text.toString())

            } else {

                Toast.makeText(this, "No search terms found", Toast.LENGTH_SHORT).show()
                binding.searchBox.requestFocus()

            }
        }


        binding.searchBox.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                binding.searchResultsView.isVisible = hasFocus
                if(!hasFocus){

                   val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java) as InputMethodManager
                   imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

                }
            }


        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.searchButton.performClick()
                return@setOnEditorActionListener true
            }
            false
        }


        binding.searchResultsView.addSearchListener(object : SearchResultsView.SearchListener {

//            override fun onCategoryResult(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo)
//            {
//                Toast.makeText(applicationContext, "Category search results shown", Toast.LENGTH_SHORT).show()
//            }

//            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
//                Toast.makeText(applicationContext, "Search suggestions shown", Toast.LENGTH_SHORT).show()
//            }


            override fun onSearchResult(
                searchResult: SearchResult,
                responseInfo: ResponseInfo
            ) {

                Log.d("SearchResults", "=====>$searchResult")
                //----------------------------------------------------------------------------------
                address = formattedAddress(searchResult.name, searchResult.address!!)
                searchLatitude = searchResult.coordinate?.latitude().toString()
                searchLongitude = searchResult.coordinate?.longitude().toString()
                fromLocation = intent.getBooleanExtra("isFromLocationClick", false)
                toLocation = intent.getBooleanExtra("isToLocationClick", false)
                isFindAddressClick = intent.getBooleanExtra("isFindAddressClick", false)

                //save search latitude and longitude in sharedpreferences
                if (fromLocation) {
                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_FROM_LOCATION, address)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_FROM_LOCATION, searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_FROM_LOCATION, searchLongitude)
                }
                if (toLocation) {
                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)

                    (application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_TO_LOCATION, address)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_TO_LOCATION, searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_TO_LOCATION, searchLongitude)
                }


                val intent = Intent()
                intent.putExtra("searchResultLat", searchResult.coordinate?.latitude().toString())
                intent.putExtra("searchResultLong", searchResult.coordinate?.longitude().toString())
                setResult(RESULT_OK, intent)
                finish()
                //----------------------------------------------------------------------------------
            }

            override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
//                binding.simpleSearchView.setQuery(historyRecord.name, true)
                binding.searchResultsView.search(historyRecord.name)
            }

            override fun onPopulateQueryClicked(
                suggestion: SearchSuggestion,
                responseInfo: ResponseInfo
            ) {
//                binding.simpleSearchView.setQuery(suggestion.name, true)
                binding.searchResultsView.search(suggestion.name)
            }

            override fun onFeedbackClicked(responseInfo: ResponseInfo) {
            }
        })


        //------------------------------------------------------------------------------------//
        binding.ivBack.setOnClickListener {
            finish()
        }


    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    fun View.hideKeyboard() {
        val imm = ContextCompat.getSystemService(context, InputMethodManager::class.java) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public fun formattedAddress(name: String, address: SearchAddress): String {

        var sddressString = ""

        if (!name.isNullOrEmpty()) {
            sddressString += name
        }
        if (!address.houseNumber.isNullOrEmpty()) {
            sddressString += address.houseNumber
        }

        if (!address.street.isNullOrEmpty()) {
            sddressString += address.street
        }

        if (!address.place.isNullOrEmpty()) {
            sddressString += address.place
        }

        if (!address.locality.isNullOrEmpty()) {
            sddressString += address.locality
        }

        if (!address.district.isNullOrEmpty()) {
            sddressString += address.district
        }
        if (!address.country.isNullOrEmpty()) {
            sddressString += address.country
        }
        return sddressString

    }


    override fun clickItem(address: String) {
        // Toast.makeText(this, address, Toast.LENGTH_LONG).show()
        val latLng = geocoAddress.getLatLng(address)
        val latitude = latLng?.latitude.toString()
        val longitude = latLng?.longitude.toString()
        Log.d(TAG, latitude.toString())
        Log.d(TAG, longitude.toString())


        if (fromLocation) {
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.ADDRESS_FROM_LOCATION,
                address
            )

            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.LATITUDE_FROM_LOCATION,
                latitude
            )
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.LONGITUDE_FROM_LOCATION,
                longitude
            )

            val intent = Intent()
            intent.putExtra("isFromLocationClick", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        if (toLocation) {
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.ADDRESS_TO_LOCATION, address
            )
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.LATITUDE_TO_LOCATION,
                latitude
            )
            (application as RawGpsApp).appContainer.prefs.setString(
                Constants.LONGITUDE_TO_LOCATION,
                longitude
            )

            val intent = Intent()
            intent.putExtra("isToLocationClick", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        if (isFindAddressClick) {
//            (application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_TO_LOCATION, address)
//            (application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_TO_LOCATION, latitude)
//            (application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_TO_LOCATION, longitude)
//            val intent = Intent()
//            intent.putExtra("isPlacesSearchClick", true)
//            intent.putExtra("address", address)
//            intent.putExtra("latitude", latitude)
//            intent.putExtra("longitude", longitude)
//            setResult(Activity.RESULT_OK, intent)
//            finish()

        }
    }

    private var pickLocationResultsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }

    private var sutoCompleteResultsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data

                //----------------------------------------------------------------------------------
                val feature = PlaceAutocomplete.getPlace(data)
                address = feature.text()
                searchLatitude = (feature.geometry() as Point).latitude().toString()
                searchLongitude = (feature.geometry() as Point).longitude().toString()


                fromLocation = intent.getBooleanExtra("isFromLocationClick", false)
                toLocation = intent.getBooleanExtra("isToLocationClick", false)
                isFindAddressClick = intent.getBooleanExtra("isFindAddressClick", false)

                //save search latitude and longitude in sharedpreferences
                if (fromLocation) {
                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_FROM_LOCATION, address)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_FROM_LOCATION, searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_FROM_LOCATION, searchLongitude)
                }
                if (toLocation) {
                    Log.e("TAG", ">>>FETCh" + address + searchLongitude + searchLatitude)

                    (application as RawGpsApp).appContainer.prefs.setString(Constants.ADDRESS_TO_LOCATION, address)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LATITUDE_TO_LOCATION, searchLatitude)
                    (application as RawGpsApp).appContainer.prefs.setString(Constants.LONGITUDE_TO_LOCATION, searchLongitude)
                }


                val intent = Intent()
                intent.putExtra(
                    "searchResultLat",
                    (feature.geometry() as Point).latitude().toString()
                )
                intent.putExtra(
                    "searchResultLong",
                    (feature.geometry() as Point).longitude().toString()
                )
                setResult(RESULT_OK, intent)
                finish()
                //----------------------------------------------------------------------------------


            }
        }




    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun loadBanner() {
        (application as RawGpsApp).appContainer.myAdsUtill.AddBannerToLayout(
            this,
            binding.adsParent,
            AdSize.LARGE_BANNER,
            object : AdLoadedCallback {
                override fun addLoaded(success: Boolean?) {}
            })
    }
}

