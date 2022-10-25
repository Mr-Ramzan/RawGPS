package com.otl.gps.navigation.map.route.model


data class PlacesModel(
    val predictions: List<Prediction>,
    val status: String
)