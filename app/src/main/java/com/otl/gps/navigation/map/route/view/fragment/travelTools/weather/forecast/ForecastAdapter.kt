package com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.forecast

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.model.forecastModel
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.utils.Convert
import com.otl.gps.navigation.map.route.view.fragment.travelTools.weather.utils.IconManager

class ForecastAdapter(var mList: List<forecastModel>,
                      val context: Context) : RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.deatail_forecast_card, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    @SuppressLint("SetTextI18n", "NewApi")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = mList[position]


        holder.day.text = Convert().convertDate(data.dayOfTheWeek)
        holder.temperature.text = "${data.temperature}°"
        holder.min_temp.text = "${data.min_temp}° ~ ${data.max_temp}°"
//        holder.max_temp.text = ""

        val icon = data.icon

        //set background
        Glide.with(context)
            .load(IconManager().getforecastIcon(icon))
            .into(holder.weather_icon)


//        holder.card_background.setBackgroundColor(
//            IconManager().getColor(Convert().convertDate(data.dayOfTheWeek))
//        )
//
//        holder.cardView.outlineAmbientShadowColor = IconManager()
//            .getColor(
//                Convert()
//                .convertDate(data.dayOfTheWeek))
//        holder.cardView.outlineSpotShadowColor = IconManager()
//            .getColor(
//                Convert()
//                .convertDate(data.dayOfTheWeek))
        
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val day: TextView = itemView.findViewById(R.id.day)
        val temperature: TextView = itemView.findViewById(R.id.temperature)
        val weather_icon: ImageView = itemView.findViewById(R.id.weatherIcon)
//        val card_background: RelativeLayout = itemView.findViewById(R.id.viewBG)
        val min_temp: TextView = itemView.findViewById(R.id.min_temp)
        val max_temp: TextView = itemView.findViewById(R.id.max_temp)
    }



}

