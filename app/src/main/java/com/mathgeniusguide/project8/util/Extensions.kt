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
import com.mathgeniusguide.project8.database.CoworkerItem
import com.mathgeniusguide.project8.database.FirebaseCoworkerItem
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

fun String.fixTime(): String {
    val list = this.split("\\D".toRegex()).map {if (it.isNotEmpty()) it.toInt() else 0}
    var hour = if (list.isNotEmpty()) list[0] else 0
    var minute = if (list.size >= 2) list[1] else 0
    var second = if (list.size >= 3) list[2] else 0
    minute += second / 60
    second %= 60
    hour += minute / 60
    minute %= 60
    hour %= 24
    return arrayOf(hour, minute, second).map {it.toString().padStart(2, '0')}.joinToString(":")
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

fun CoworkerItem.toFirebaseCoworkerItem(): FirebaseCoworkerItem {
    val item = FirebaseCoworkerItem()
    item.id = this.id
    item.liked = this.liked
    item.photo = this.photo
    item.restaurant = this.restaurant
    item.restaurantName = this.restaurantname
    item.username = this.username
    return item
}

fun FirebaseCoworkerItem.toCoworkerItem(): CoworkerItem {
    val item = CoworkerItem(
        id = this.id!!,
        liked = this.liked!!,
        photo = this.photo!!,
        restaurant = this.restaurant!!,
        restaurantname = this.restaurantName!!,
        username = this.username!!
    )
    return item
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}