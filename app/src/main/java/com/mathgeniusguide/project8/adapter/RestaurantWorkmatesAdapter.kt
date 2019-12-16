package com.mathgeniusguide.project8.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.ChosenRestaurantItem
import kotlinx.android.synthetic.main.workmates_item.view.*

class RestaurantWorkmatesAdapter (private val items: List<ChosenRestaurantItem>, val context: Context, val navController: NavController) : RecyclerView.Adapter<RestaurantWorkmatesAdapter.ViewHolder> () {
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
        holder.parent.setOnClickListener {
            (context as MainActivity).chattingWith = i.id ?: ""
            navController.navigate(R.id.action_chat)
        }
    }

    class ViewHolder (view : View) : RecyclerView.ViewHolder(view) {
        val userImage = view.userImage
        val userChoice = view.userChoice
        val parent = view
    }
}