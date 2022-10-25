package com.otl.gps.navigation.map.route.model

import androidx.room.PrimaryKey
import io.realm.RealmObject


open class SavedPlace(
    @PrimaryKey
    var name: String = "",
    var address: String = "",
    var latitude: String = "",
    var longitude: String = "",
    var description: String = ""
): RealmObject()