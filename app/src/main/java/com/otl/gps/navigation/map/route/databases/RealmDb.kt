package com.otl.gps.navigation.map.route.databases


import android.content.Context
import com.otl.gps.navigation.map.route.model.SavedPlace
import io.realm.Realm
import io.realm.RealmConfiguration


class RealmDB {
    private val realm: Realm? = Realm.getDefaultInstance()

    fun deletePlace(placeToDelete: SavedPlace?, success: (sucess: Boolean) -> Unit) {
//        try {
        if (realm == null || placeToDelete == null) {
            return
        }

        realm.executeTransaction {

            val result = realm.where(SavedPlace::class.java).equalTo("name", placeToDelete.name)
                .equalTo("address", placeToDelete.address)
                .equalTo("longitude", placeToDelete.longitude)
                .equalTo("latitude", placeToDelete.latitude)
                .findFirst()
            if (result != null) {
                result.deleteFromRealm()
                success(true)
            } else {
                success(false)
            }
        }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            success(false)
//        }
    }

    fun checkIfExist(primaryKey: String, exists: (exists: Boolean) -> Unit) {
        try {
            if (realm == null || primaryKey.isEmpty()) {
                return
            }

            realm.executeTransaction {

                val result =
                    realm.where(SavedPlace::class.java).equalTo("name", primaryKey).findFirst()
                if (result != null) {
                    exists(true)
                } else {
                    exists(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            exists(true)
        }
    }


//    if comming from check if exist no transaction block is required but if directly being called
    fun updateSavedItem(placeToSave: SavedPlace?) {
        try {
            if (realm == null || placeToSave == null) {
                return
            }
            val place = SavedPlace()
            place.longitude = placeToSave.longitude
            place.latitude = placeToSave.latitude
            place.address = placeToSave.address
            place.name = placeToSave.name
            place.description = placeToSave.description
            realm.insertOrUpdate(place)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSavedPlaces(): ArrayList<SavedPlace> {
        var savedPlaces = ArrayList<SavedPlace>()
        if (realm != null) {
            val result = realm.where(SavedPlace::class.java).findAllAsync()
            if (result != null) {

                savedPlaces.addAll(result)
                return savedPlaces
            }
        }
        return savedPlaces
    }


    companion object {

        @JvmStatic
        fun init(context: Context?) {
            Realm.init(context!!)
            val namedb = "SavedPlaces.db"
            val config = RealmConfiguration.Builder()
                .name(namedb)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .build()
            Realm.setDefaultConfiguration(config)
        }
    }

}