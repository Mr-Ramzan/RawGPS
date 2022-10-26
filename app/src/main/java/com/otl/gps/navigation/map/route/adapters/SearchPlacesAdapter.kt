package com.otl.gps.navigation.map.route.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.otl.gps.navigation.map.route.databinding.PlacesItemBinding
import com.otl.gps.navigation.map.route.interfaces.PlacesAdapterListener
import com.otl.gps.navigation.map.route.model.PlacesItem
import com.otl.gps.navigation.map.route.utilities.Constants


class SearchPlacesAdapter(var list: List<PlacesItem>, var context: Context, var clickListener: PlacesAdapterListener)
    : RecyclerView.Adapter<SearchPlacesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: PlacesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            PlacesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.title .text = list[position].title .toString()
                binding.placesIcon.setImageResource(list[position].icon)
            }
        }
        holder.itemView.setOnClickListener {
              clickListener.clickItem(list[position].POI_NAME)
        }
    }

    // return the size of languageList
    override fun getItemCount(): Int {
        return list.size
    }

}
