package com.mathgeniusguide.project8.connectivity

import android.content.Context
import android.net.ConnectivityManager

fun Context.isOnline() : Boolean {
    val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = connectivityManager.activeNetworkInfo
    return info != null && info.isConnected
}