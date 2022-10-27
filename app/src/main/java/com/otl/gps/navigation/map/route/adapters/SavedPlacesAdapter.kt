package com.otl.gps.navigation.map.route.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.otl.gps.navigation.map.route.databinding.SavedPlacesItemBinding
import com.otl.gps.navigation.map.route.model.SavedPlace


class SavedPlacesAdapter(var list: List<SavedPlace>, var context: Context, var previewPlace:(place:SavedPlace)->Unit, var deletePlace:(place:SavedPlace)->Unit)
    : RecyclerView.Adapter<SavedPlacesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: SavedPlacesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            SavedPlacesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.placeNameText .text = list[position].name
                binding.addressText.text = (list[position].address)
                binding.coordinatesText.text = ("${list[position].latitude},${list[position].longitude}")

                binding.root.setOnClickListener {
                    previewPlace(list[position])
                }

                binding.deletePlaceButton.setOnClickListener {
                    deletePlace(list[position])
                }

            }
        }



    }

    // return the size of languageList
    override fun getItemCount(): Int {
        return list.size
    }

}
