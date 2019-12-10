package com.mathgeniusguide.project8.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.ChosenRestaurantItem
import com.mathgeniusguide.project8.util.NearbyPlace
import kotlinx.android.synthetic.main.workmates_item.view.*

class WorkmatesAdapter (private val items: List<ChosenRestaurantItem>, val context: Context, val placeList: List<NearbyPlace>?) : RecyclerView.Adapter<WorkmatesAdapter.ViewHolder> () {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.workmates_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val i = items[position]
        if (!i.photo.isNullOrEmpty()) {
            Glide.with(context).load(i.photo).into(holder.userImage)
        }
        var restaurantName = ""
        if (placeList != null && placeList.any{it.id == i.restaurant}) {
            restaurantName = placeList.first{it.id == i.restaurant}.name
        } else {
            holder.userChoice.setTextColor(context.resources.getColor(R.color.gray))
        }
        holder.userChoice.text = if (restaurantName == "") String.format(context.resources.getString(R.string.hasnt_decided_yet), i.username) else String.format(context.resources.getString(R.string.is_eating_at), i.username, restaurantName)
    }

    class ViewHolder (view : View) : RecyclerView.ViewHolder(view) {
        val userImage = view.userImage
        val userChoice = view.userChoice
        val parent = view
    }
}