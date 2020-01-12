package com.mathgeniusguide.project8.connectivity

import android.content.Context
import android.net.ConnectivityManager
import com.mathgeniusguide.go4lunch.database.RestaurantItem
import com.mathgeniusguide.project8.util.NearbyPlace

fun Context.isOnline(): Boolean {
    val connectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = connectivityManager.activeNetworkInfo
    return info != null && info.isConnected
}

fun NearbyPlace.toRestaurantItem(): RestaurantItem {
    val restaurantItem = RestaurantItem(
        id = this.id,
        address = this.address,
        time = this.time,
        latitude = this.latitude,
        longitude = this.longitude,
        rating = this.rating,
        image = this.image,
        phone = this.phone,
        website = this.website,
        name = this.name
    )
    return restaurantItem
}