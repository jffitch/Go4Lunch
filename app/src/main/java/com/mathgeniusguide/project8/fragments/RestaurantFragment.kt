package com.mathgeniusguide.project8.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.adapter.PlaceAdapter
import com.mathgeniusguide.project8.adapter.RestaurantWorkmatesAdapter
import com.mathgeniusguide.project8.util.Constants
import kotlinx.android.synthetic.main.list_view.*
import kotlinx.android.synthetic.main.restaurant_info.*
import kotlinx.android.synthetic.main.restaurant_view.*

class RestaurantFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.restaurant_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chosenPlace = (activity as MainActivity).chosenPlace!!
        val list = (activity as MainActivity).chosenRestaurantList.filter{it.restaurant == chosenPlace.id}.map{it.username!!}
        Glide.with(context!!).load("${Constants.BASE_URL}photo?maxwidth=400&key=${Constants.API_KEY}&photo_reference=${chosenPlace.image}").into(restaurantImage)
        restaurantText.text = chosenPlace.name
        restaurantAddress.text = chosenPlace.address
        restaurantWorkmatesRV.layoutManager = LinearLayoutManager(context)
        restaurantWorkmatesRV.adapter = RestaurantWorkmatesAdapter(list, context!!)

    }
}