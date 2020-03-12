package com.mathgeniusguide.project8.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.bumptech.glide.Glide
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.adapter.RestaurantWorkmatesAdapter
import com.mathgeniusguide.project8.util.Constants
import com.mathgeniusguide.project8.util.FirebaseFunctions.updateLiked
import com.mathgeniusguide.project8.util.FirebaseFunctions.updateRestaurant
import com.mathgeniusguide.project8.util.FirebaseFunctions.updateRestaurantName
import com.mathgeniusguide.project8.util.Functions.setNotificationAlarm
import com.mathgeniusguide.project8.util.NotificationWorker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.restaurant_info_include.*
import kotlinx.android.synthetic.main.restaurant_tabs_include.*
import kotlinx.android.synthetic.main.restaurant_fragment.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RestaurantFragment: Fragment() {
    var isLiked = false;
    lateinit var act: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.restaurant_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // declare activity shorthand variable
        act = activity as MainActivity
        // toolbar visible and back arrow as drawer icon
        act.toolbar.visibility = View.VISIBLE
        act.toolbar.setNavigationIcon(R.drawable.drawer)
        // hide autocomplete until search button clicked
        act.autocomplete.visibility = View.GONE
        act.autocompleteRV.visibility = View.GONE
        // chosenPlace will be set in MainActivity whenever navigation to this fragment happens, load that restaurant
        val chosenPlace = act.chosenPlace!!
        // load list of coworkers eating at this restaurant
        val list = act.firebaseCoworkerList.filter{it.restaurant == chosenPlace.id}
        // load restaurant image
        Glide.with(context!!).load("${Constants.BASE_URL}photo?maxwidth=400&key=${Constants.API_KEY}&photo_reference=${chosenPlace.image}").into(restaurantImage)
        // load restaurant name and address directly
        restaurantText.text = chosenPlace.name
        restaurantAddress.text = chosenPlace.address
        // rating received is from 1-5, show stars equal to floor(rating - 1)
        star3.visibility = if (chosenPlace.rating > 4) View.VISIBLE else View.GONE
        star2.visibility = if (chosenPlace.rating > 3) View.VISIBLE else View.GONE
        star1.visibility = if (chosenPlace.rating > 2) View.VISIBLE else View.GONE
        if (list.isEmpty()) {
            // if no coworkers joining, hide coworker list
            restaurantWorkmatesRV.visibility = View.GONE
            restaurantWorkmatesRVPlaceholder.visibility = View.VISIBLE
        } else {
            // if coworkers joining, set up RecyclerView
            restaurantWorkmatesRV.layoutManager = LinearLayoutManager(context)
            restaurantWorkmatesRV.adapter = RestaurantWorkmatesAdapter(list, context!!, findNavController())
        }
        // checkbox starts clicked if this restaurant is already your chosen restaurant
        restaurantCheckbox.isChecked = chosenPlace.id == act.firebaseCoworkerList.first { it.username == act.username }.restaurant
        // restaurant is liked if in list of liked restaurants
        isLiked = act.restaurantsLiked.any { it == chosenPlace.id }
        // set tab color for LIKE button based on whether this restaurant is liked
        setTabColor(isLiked)
        // when checkbox is clicked, set chosen restaurant to this restaurant if checked, or nothing if not checked
        // save restaurant name and id to firebase and to loaded list
        // create notification if checked
        restaurantCheckbox.setOnClickListener {
            updateRestaurant(act.userkey, if ((it as CheckBox).isChecked) chosenPlace.id else "", act.database)
            updateRestaurantName(act.userkey, if ((it as CheckBox).isChecked) chosenPlace.name else "", act.database)
            act.firebaseCoworkerList.first { it.username == act.username }.restaurant = if (it.isChecked) chosenPlace.id else ""
            act.firebaseCoworkerList.first { it.username == act.username }.restaurantName = if (it.isChecked) chosenPlace.name else ""
            setNotificationAlarm(it.isChecked, act.notificationTime, act.username, context!!)
        }
        // when call tab is clicked, open dialer with phone number displayed
        callTab.setOnClickListener {
            if (chosenPlace.phone == null) {
                Toast.makeText(context, resources.getString(R.string.no_phone), Toast.LENGTH_LONG).show()
            } else {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:${chosenPlace.phone}")
                startActivity(callIntent)
            }
        }
        // when like tab is clicked, add restaurant to list of liked restaurants if liked, remove if not liked
        // set like tab color based on whether restaurant is liked
        // update firebase
        likeTab.setOnClickListener {
            isLiked = !isLiked
            if (isLiked) {
                act.restaurantsLiked.add(chosenPlace.id)
            } else {
                act.restaurantsLiked = act.restaurantsLiked.filter { it != chosenPlace.id }.toMutableList()
            }
            setTabColor(isLiked)
            updateLiked(act.userkey, act.restaurantsLiked, act.database)
        }
        // when website tab is clicked, open webpage
        websiteTab.setOnClickListener {
            if (chosenPlace.website == null) {
                Toast.makeText(context, resources.getString(R.string.no_website), Toast.LENGTH_LONG).show()
            } else {
                findNavController().navigate(R.id.action_website)
            }
        }
    }

    // like tab is green when liked, orange when not liked
    fun setTabColor(isLiked: Boolean) {
        val tabColor = if (isLiked) R.color.green else R.color.restaurant_tabs
        likeIcon.setColorFilter(ContextCompat.getColor(context!!, tabColor))
        likeText.setTextColor(ContextCompat.getColor(context!!, tabColor))
    }
}