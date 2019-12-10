package com.mathgeniusguide.project8.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.ChosenRestaurantItem
import kotlinx.android.synthetic.main.workmates_item.view.*

class RestaurantWorkmatesAdapter (private val items: List<ChosenRestaurantItem>, val context: Context) : RecyclerView.Adapter<RestaurantWorkmatesAdapter.ViewHolder> () {
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
        holder.userChoice.text = String.format(context.resources.getString(R.string.is_joining), i.username)
    }

    class ViewHolder (view : View) : RecyclerView.ViewHolder(view) {
        val userImage = view.userImage
        val userChoice = view.userChoice
        val parent = view
    }
}