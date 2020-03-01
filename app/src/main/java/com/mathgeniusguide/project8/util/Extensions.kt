package com.mathgeniusguide.project8.util

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mathgeniusguide.go4lunch.database.RestaurantRoomdbItem
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.CoworkerRoomdbItem
import com.mathgeniusguide.project8.database.FirebaseCoworkerItem
import com.mathgeniusguide.project8.database.RestaurantItem
import java.text.SimpleDateFormat
import java.util.*

fun Context.isOnline(): Boolean {
    val connectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = connectivityManager.activeNetworkInfo
    return info != null && info.isConnected
}

// set expiration time for Room Database entry based on displayed opening time information
fun String.toExpiration(resources: Resources): String {
    val today = Date()
    val tomorrow = Date(today.time + 86400000)
    val fullDateSdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    // if open for next 24 hours, entry expires in 24 hours
    if (this == resources.getString(R.string.open_24) || this == resources.getString(R.string.open_24_7)) {
        return fullDateSdf.format(tomorrow)
    }
    // if no time is displayed, expires immediately
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
    // if displayed time has already past today, expires tomorrow at that time
    // if displayed time has not already past today, expires today at that time
    if (todayTime > expirationTime) {
        return "${tomorrowDate} ${expirationTime}"
    } else {
        return "${todayDate} ${expirationTime}"
    }
}

// parse notification time from entered value
fun String.fixTime(): String {
    // split by every non-digit character, convert to integer
    val list = this.split("\\D".toRegex()).map {if (it.isNotEmpty()) it.toInt() else 0}
    // hour, minute, and second are elements 0, 1, and 2 from resulting list
    // if element doesn't exist, assume 0
    var hour = if (list.isNotEmpty()) list[0] else 0
    var minute = if (list.size >= 2) list[1] else 0
    var second = if (list.size >= 3) list[2] else 0
    // if second, minute, or hour are too high, carry over
    minute += second / 60
    second %= 60
    hour += minute / 60
    minute %= 60
    hour %= 24
    // generate properly formatted time string
    return arrayOf(hour, minute, second).map {it.toString().padStart(2, '0')}.joinToString(":")
}

// convert API restaurant item to Room Database restaurant item
fun RestaurantItem.toRestaurantRoomdbItem(resources: Resources): RestaurantRoomdbItem {
    val restaurantItem = RestaurantRoomdbItem(
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

// convert Room Database coworker item to Firebase coworker item
fun CoworkerRoomdbItem.toFirebaseCoworkerItem(): FirebaseCoworkerItem {
    val item = FirebaseCoworkerItem()
    item.id = this.id
    item.liked = this.liked
    item.photo = this.photo
    item.restaurant = this.restaurant
    item.restaurantName = this.restaurantname
    item.username = this.username
    return item
}

// convert Firebase coworker item to Room Database coworker item
fun FirebaseCoworkerItem.toCoworkerRoomdbItem(): CoworkerRoomdbItem {
    val item = CoworkerRoomdbItem(
        id = this.id!!,
        liked = this.liked!!,
        photo = this.photo!!,
        restaurant = this.restaurant!!,
        restaurantname = this.restaurantName!!,
        username = this.username!!
    )
    return item
}

// allow LiveData to be observed once
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}