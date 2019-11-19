package com.mathgeniusguide.project8.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.ChosenRestaurantItem
import com.mathgeniusguide.project8.responses.NearbyPlace
import com.mathgeniusguide.project8.util.Constants
import kotlinx.android.synthetic.main.place_item.view.*

class PlaceAdapter (private val items: List<NearbyPlace>, val context: Context, val chosenRestaurantList: List<ChosenRestaurantItem>) : RecyclerView.Adapter<PlaceAdapter.ViewHolder> () {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.place_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val i = items[position]
        if (i.image != "") {
            Glide.with(context).load("${Constants.BASE_URL}photo?maxheight=90&key=${Constants.API_KEY}&photo_reference=${i.image}").into(holder.placeImage)
        }
        holder.placeDistance.text = "${i.distance}m"
        holder.star3.visibility = if (i.rating > 4) View.VISIBLE else View.GONE
        holder.star2.visibility = if (i.rating > 3) View.VISIBLE else View.GONE
        holder.star1.visibility = if (i.rating > 2) View.VISIBLE else View.GONE
        holder.placeName.text = i.name
        holder.placeDetails.text = i.address.split(",")[0]
        holder.placeTime.text = i.time
        val count = chosenRestaurantList.filter{it.restaurant == i.id}.size
        holder.placeWorkmatesCount.text = count.toString()
        holder.placeWorkmates.visibility = if (count == 0) View.INVISIBLE else View.VISIBLE
        holder.parent.setOnClickListener {

        }
    }

    class ViewHolder (view : View) : RecyclerView.ViewHolder(view) {
        val placeImage = view.placeImage
        val placeDistance = view.placeDistance
        val placeWorkmates = view.placeWorkmates
        val placeWorkmatesCount = view.placeWorkmatesCount
        val star3 = view.star3
        val star2 = view.star2
        val star1 = view.star1
        val placeName = view.placeName
        val placeDetails = view.placeDetails
        val placeTime = view.placeTime
        val parent = view
    }
}