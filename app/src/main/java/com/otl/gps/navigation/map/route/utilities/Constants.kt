package com.otl.gps.navigation.map.route.utilities

import com.otl.gps.navigation.map.route.model.PlacesItem

object Constants {

    val KM = "km"
    val MILES = "miles"

    val ADS_RELOAD_MAX_TRIES = 3
    var IMAGE_DATA_INTENT = "Image_Data"
    var canShowAppOpen = true
    const val PREMIUM_FROM = "from_key"
    const val FROM_ONBOAARDING = "onboarding"
    const val FROM_SPLASH = "splash"
    const val FROM_HOME = "home"
    const val FROM_NAVIGATION = "navigation"
     var MAP_BOX_ACCESS_TOKEN = "sk.eyJ1IjoiamF3YWRhbWphZCIsImEiOiJjbDVzMDN5c2cwN3FvM2Zxd3I2NHMyaDR6In0.QSbUNh97LzqUmx00LcvJpQ"

    /////////////////////////////////////////////////////////////


    /*==========================================================
                          IN APPS
    ===========================================================*/
    val PREF_REMOVE_ADS: String = "ads_removed"
    const val PREF_REMOVE_AD_INAPP = "com.abl.gpstracker.navigation.maps.routefinder.app.remove_ads"
    const val PREMIUM_ADS = "com.abl.gps.maps.premium.subscription"

    /////////////////////////////////////////////////////////////////////////////
    //Navigation Events
    const val UPDATE_CANCEL_BUTTON = "cancel_button"
    const val NAVIGATE_ROUTE = "routes_fragment"
    const val OPEN_DRAWER = "open_nav_drawer"
    const val CLOSE_DRAWER = "close_drawer"
    const val NAVIGATE_LOCATION = "location_frag"
    const val NAVIGATE_SATELLITE = "satellite_frag"
    const val NAVIGATE_TRAFFIC = "traffic_frag"
    const val NAVIGATE_DRIVING_MODE  = "driving_mode"
    const val NAVIGATE_COMPASS = "compass"
    const val NAVIGATE_WEATHER = "weather"
    const val NAVIGATE_TRAVEL_TOOLS = "travel_tools"
    const val NAVIGATE_EXPLORE_PLACES = "explore_places"
    const val NAVIGATE_CURRENCY_CONVERTER = "currency_converter"
    const val NAVIGATE_PLACES_LIST = "nearby_list"
    const val NAVIGATE_PLACES = "nearby"
    const val NAVIGATE_CLOCK = "world_clock"
    const val ACTION_SHARE = "share"
    const val ACTION_CANCEL_SUB = "cancel_sub"
    const val ACTION_SUBSCCRIBED = "subscribed"
    const val ACTION_REMMOVE_ADS = "removeAds"
    const val NAVIGATE_PREMIUM = "premium_frag"
    const val ACTION_POLICY = "policy"
    const val ACTION_MORE_APPS = "MORE_APPS"
    const val NAV_SAVE_SCREEN = "goto_save_screen"
    const val NAV_BACKGROUND_SCREEN = "goto_backgrounds_screens"
    const val NAV_BACK = "go_back"

    ///////////////////////////////////////////////////////////////////////////////////
    //ad view types
    const val POPUP_NATIVE = "popup_ads"
    const val START_NATIVE_SMALL = "native_small_button_start"
    const val STYLE_LIST_NATIVE_SMALL = "native_small_style_list"
    const val START_NATIVE = "native_samll_start"
    const val MENU_NATIVE = "native_samll_menu"
    const val BIG_NATIVE = "native_big"

///////////////////////////////////////////////////////////////////////////////////

    val TAG_WHOLE_FRAGMENT_PREVIEW = "whole_frag_preview"
    var isImplementingNewSuits = false
    var isImplementingBGs = false
    var GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id="

///////////////////////////////////////////////////////////////////////////////////

    const val BASE_URL_WEATHER = "https://api.openweathermap.org/data/2.5/"
    const val WEATHER_APP_ID = "weather?units=metric&appid=885fa0df7151e061eb835f6c766e1a9d"
    const val BASE_URL_PLACES = "https://maps.googleapis.com/maps/api/place/autocomplete/"
    const val PLACES_APP_ID = "json?&types=geocode&key=AIzaSyCCAu6JEMV_h7wQor8A4Nrmd9yuHMI6sH0"

///////////////////////////////////////////////////////////////////////////////////


    const val ACTION_TYPE = "action_type"
    const val IMAGE_DELETED = "image_deleted"
    const val ADDRESS_FROM_LOCATION = "addressFromLocation_"
    const val ADDRESS_TO_LOCATION = "addressToLocation_"
    const val LATITUDE_FROM_LOCATION = "latitudeFromLocation_"
    const val LONGITUDE_FROM_LOCATION = "longitudeFromLocation_"
    const val LATITUDE_TO_LOCATION = "latitudeToLocation_"
    const val LONGITUDE_TO_LOCATION = "longitudeToLocation_"
    const val MEAN_BY_CAR = "routed-car/route/v1/driving/"
    const val MEAN_BY_BIKE = "routed-bike/route/v1/driving/"
    const val MEAN_BY_FOOT = "routed-foot/route/v1/driving/"
    const val ADDRESS_MY_LOCATION = "addressMyLocation_"
    const val ADDRESS_FROM_MY_LOCATION = "addressFromMyLocation_"
    const val ADDRESS_TO_MY_LOCATION = "addressToMyLocation_"
    const val LATITUDE_MY_LOCATION = "latitudeMyLocation_"
    const val LONGITUDE_MY_LOCATION = "longitudeMyLocation_"

///////////////////////////////////////////////////////////////////////////////////
//    var selectedMarker = R.drawable.bus_train_marker


    lateinit var nearbyPlaces: ArrayList<PlacesItem>
    fun preparePlacesList(): ArrayList<PlacesItem>{
        if(!Constants::nearbyPlaces.isInitialized)
        {
            nearbyPlaces = ArrayList()

        }else{
            nearbyPlaces.clear()
        }

//        nearbyPlaces.add(PlacesItem(1 , R.drawable.train_stations,R.drawable.bus_train_marker ,"Train Station","Station") , )
//        nearbyPlaces.add(PlacesItem(2 , R.drawable.airplanes,R.drawable.airports_marker ,"Airport" ,"Airport"))
//        nearbyPlaces.add(PlacesItem(3 , R.drawable.bus,R.drawable.bus_train_marker ,"Bus Station" ,"Bus station"))
//        nearbyPlaces.add(PlacesItem(4 , R.drawable.bank,R.drawable.historic_buildings_marker ,"Bank" ,"Bank"))
//        nearbyPlaces.add(PlacesItem(5 , R.drawable.atm,R.drawable.historic_buildings_marker ,"ATM" ,"ATM"))
////      nearbyPlaces.add(PlacesItem(6 , R.drawable.airplanes,"Real Estate Agency" ,""))
//        nearbyPlaces.add(PlacesItem(7 , R.drawable.temple,R.drawable.prayer_places ,"Temple" ,"Church"))
//        nearbyPlaces.add(PlacesItem(8 , R.drawable.mosque,R.drawable.prayer_places ,"Mosque" ,"Mosque"))
//        nearbyPlaces.add(PlacesItem(9 , R.drawable.doctor,R.drawable.general_marker ,"Doctor" ,"Doctors"))
//        nearbyPlaces.add(PlacesItem(10, R.drawable.vet,R.drawable.general_marker ,"Veterinary Care","Veterinary surgery"))
//        nearbyPlaces.add(PlacesItem(11, R.drawable.dentist,R.drawable.general_marker ,"Dentist","Dentist"))
//        nearbyPlaces.add(PlacesItem(12, R.drawable.pharmecy,R.drawable.shops_marker ,"Pharmacy","Pharmacy"))
//        nearbyPlaces.add(PlacesItem(13, R.drawable.fire_station,R.drawable.general_buildings_marker ,"Fire Station","Fire station"))
//        nearbyPlaces.add(PlacesItem(14, R.drawable.fuel,R.drawable.general_marker ,"Gas Station","Fuel"))
//        nearbyPlaces.add(PlacesItem(15, R.drawable.carwash,R.drawable.general_marker ,"Car Wash","Car wash"))
//        nearbyPlaces.add(PlacesItem(16, R.drawable.mechanic,R.drawable.bus_train_marker ,"Car Repair","Car repair"))
//        nearbyPlaces.add(PlacesItem(17, R.drawable.restaurant,R.drawable.bar_cafe_marker ,"Restaurant","Restaurant"))
//        nearbyPlaces.add(PlacesItem(18, R.drawable.cafe,R.drawable.bar_cafe_marker ,"Cafe","Cafe"))
//        nearbyPlaces.add(PlacesItem(19, R.drawable.shopping_mall,R.drawable.shops_marker ,"Shopping Mall","Mall"))
//        nearbyPlaces.add(PlacesItem(20, R.drawable.superstore,R.drawable.shops_marker ,"Departmental Store","Supermarket"))
//        nearbyPlaces.add(PlacesItem(21, R.drawable.jewlary_shop,R.drawable.shops_marker ,"Jewelry Store","Jewelry shop"))
//        nearbyPlaces.add(PlacesItem(22, R.drawable.night_club,R.drawable.sports_clubs_marker ,"Night Club","Night club"))
//        nearbyPlaces.add(PlacesItem(23, R.drawable.casino,R.drawable.historic_buildings_marker ,"Casino","Casino"))
//        nearbyPlaces.add(PlacesItem(24, R.drawable.salon,R.drawable.general_marker ,"Beauty Salon",""))
//        nearbyPlaces.add(PlacesItem(25, R.drawable.bar,R.drawable.bar_cafe_marker ,"Bar","Bar"))
//        nearbyPlaces.add(PlacesItem(26, R.drawable.zoo,R.drawable.general_marker ,"Zoo","Zoo"))
//        nearbyPlaces.add(PlacesItem(28, R.drawable.theater,R.drawable.general_buildings_marker ,"Movie Theater","Cinema"))
//        nearbyPlaces.add(PlacesItem(29, R.drawable.school,R.drawable.general_buildings_marker ,"School","School"))
//        nearbyPlaces.add(PlacesItem(31, R.drawable.bookshop,R.drawable.shops_marker ,"Book Store","Book shop"))
//        nearbyPlaces.add(PlacesItem(32, R.drawable.library,R.drawable.general_buildings_marker ,"Library","Library"))
//        nearbyPlaces.add(PlacesItem(33, R.drawable.stadium,R.drawable.sports_marker ,"Stadium","Stadium"))
//        nearbyPlaces.add(PlacesItem(34, R.drawable.park,R.drawable.parks_nature_marker ,"Park","Park"))
////            nearbyPlaces.add(PlacesItem(35, R.drawable.airplanes,"Aquarium",""))
//        nearbyPlaces.add(PlacesItem(36, R.drawable.camping_site,R.drawable.parks_nature_marker ,"Campground","Camp site"))
//        nearbyPlaces.add(PlacesItem(37, R.drawable.town_hall,R.drawable.historic_buildings_marker ,"City Hall","Town Hall"))
//        nearbyPlaces.add(PlacesItem(38, R.drawable.museum,R.drawable.historic_buildings_marker ,"Museum","Museum"))
        return nearbyPlaces
    }

///////////////////////////////////////////////////////////////////////////////////
//    var navigationRoutes : navigationRoutesList<NavigationRoute> = listOf()
}