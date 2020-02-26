package com.mathgeniusguide.project8

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "notificationChannel",
                "Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "Notification Channel"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
    }
}