package com.mathgeniusguide.project8.util

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mathgeniusguide.go4lunch.database.RestaurantItem
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.NearbyPlace
import java.text.SimpleDateFormat
import java.util.*

fun Context.isOnline(): Boolean {
    val connectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = connectivityManager.activeNetworkInfo
    return info != null && info.isConnected
}

fun String.toExpiration(resources: Resources): String {
    val today = Date()
    val tomorrow = Date(today.time + 86400000)
    val fullDateSdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    if (this == resources.getString(R.string.open_24) || this == resources.getString(R.string.open_24_7)) {
        return fullDateSdf.format(tomorrow)
    }
    val regex = Regex("[0-9]+:[0-9]+")
    if (!regex.containsMatchIn(this)) {
        return fullDateSdf.format(today)
    }
    val dateOnlySdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val timeOnlySdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val todayDate = dateOnlySdf.format(today)
    val tomorrowDate = dateOnlySdf.format(tomorrow)
    val todayTime = timeOnlySdf.format(today)
    val expirationTime = timeOnlySdf.format(timeOnlySdf.parse(regex.find(this)!!.value))
    if (todayTime > expirationTime) {
        return "${tomorrowDate} ${expirationTime}"
    } else {
        return "${todayDate} ${expirationTime}"
    }
}

fun NearbyPlace.toRestaurantItem(resources: Resources): RestaurantItem {

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
        name = this.name,
        expiration = this.time.toExpiration(resources)
    )
    return restaurantItem
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}