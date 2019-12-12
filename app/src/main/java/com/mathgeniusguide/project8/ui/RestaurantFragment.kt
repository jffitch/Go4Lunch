package com.mathgeniusguide.project8.ui

import android.content.Intent
import android.content.Intent.ACTION_CALL
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.adapter.RestaurantWorkmatesAdapter
import com.mathgeniusguide.project8.util.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.restaurant_info.*
import kotlinx.android.synthetic.main.restaurant_tabs.*
import kotlinx.android.synthetic.main.restaurant_view.*

class RestaurantFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.restaurant_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).toolbar.visibility = View.VISIBLE
        (activity as MainActivity).autocompleteFragment.view?.visibility = View.GONE
        val chosenPlace = (activity as MainActivity).chosenPlace!!
        val list = (activity as MainActivity).chosenRestaurantList.filter{it.restaurant == chosenPlace.id}
        Glide.with(context!!).load("${Constants.BASE_URL}photo?maxwidth=400&key=${Constants.API_KEY}&photo_reference=${chosenPlace.image}").into(restaurantImage)
        restaurantText.text = chosenPlace.name
        restaurantAddress.text = chosenPlace.address
        star3.visibility = if (chosenPlace.rating > 4) View.VISIBLE else View.GONE
        star2.visibility = if (chosenPlace.rating > 3) View.VISIBLE else View.GONE
        star1.visibility = if (chosenPlace.rating > 2) View.VISIBLE else View.GONE
        if (list.isEmpty()) {
            restaurantWorkmatesRV.visibility = View.GONE
            restaurantWorkmatesRVPlaceholder.visibility = View.VISIBLE
        } else {
            restaurantWorkmatesRV.layoutManager = LinearLayoutManager(context)
            restaurantWorkmatesRV.adapter = RestaurantWorkmatesAdapter(list, context!!)
        }
        restaurantCheckbox.isChecked = chosenPlace.id == (activity as MainActivity).chosenRestaurantList.first { it.username == (activity as MainActivity).username }.restaurant
        restaurantCheckbox.setOnClickListener {
            (activity as MainActivity).updateItem((activity as MainActivity).userkey, if ((it as CheckBox).isChecked) chosenPlace.id else "")
            (activity as MainActivity).chosenRestaurantList.first { it.username == (activity as MainActivity).username }.restaurant = if ((it as CheckBox).isChecked) chosenPlace.id else ""
        }
        callTab.setOnClickListener {
            if (chosenPlace.phone == null) {
                Toast.makeText(context, resources.getString(R.string.no_phone), Toast.LENGTH_LONG).show()
            } else {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:${chosenPlace.phone}")
                startActivity(callIntent)
            }
        }
        likeTab.setOnClickListener {

        }
        websiteTab.setOnClickListener {
            if (chosenPlace.website == null) {
                Toast.makeText(context, resources.getString(R.string.no_website), Toast.LENGTH_LONG).show()
            } else {
                findNavController().navigate(R.id.action_website)
            }
        }
    }
}