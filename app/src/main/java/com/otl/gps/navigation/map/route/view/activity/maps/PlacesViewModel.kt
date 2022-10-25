package com.abl.gpstracker.navigation.maps.routefinder.app.view.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.otl.gps.navigation.map.route.utilities.Resource
import com.otl.gps.navigation.map.route.view.activity.maps.Repository
import kotlinx.coroutines.Dispatchers

class PlacesViewModel(private val repository: Repository) : ViewModel() {

    fun getPlacesData(input:String) = liveData(Dispatchers.IO) {
    //    emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.getPlacesData(input)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, msg = exception.message ?: "Error Occurred"))
        }
    }
}