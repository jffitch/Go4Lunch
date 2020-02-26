package com.mathgeniusguide.project8.util

import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.*
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.FirebaseCoworkerItem
import java.util.ArrayList
import java.util.HashMap

class NotificationJobService: JobService() {
    private lateinit var notificationManagerCompat: NotificationManagerCompat
    lateinit var database: DatabaseReference
    var firebaseCoworkerList = ArrayList<FirebaseCoworkerItem>()
    val ANONYMOUS = "anonymous"
    var username = ANONYMOUS
    var title = ""
    var message = ""

    override fun onStartJob(params: JobParameters): Boolean {
        username = params.extras.getString("username") ?: ANONYMOUS
        database = FirebaseDatabase.getInstance().reference
        database.orderByKey().addListenerForSingleValueEvent(itemListener)
        notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    var itemListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            addDataToList(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {
        // get coworker data
        val restaurants = dataSnapshot.child(Constants.CHOSEN_RESTAURANTS).children.iterator()
        while (restaurants.hasNext()) {
            // get current item
            val currentItem = restaurants.next()
            val firebaseCoworkerItem = FirebaseCoworkerItem.create()
            // get current data in a map
            val map = currentItem.value as HashMap<String, Any>
            // key will return Firebase ID
            firebaseCoworkerItem.id = currentItem.key
            // prevent same item from loading repeatedly
            if (firebaseCoworkerList.none { it.id == firebaseCoworkerItem.id }) {
                // get saved data for each coworker
                firebaseCoworkerItem.username = map.get("username") as String?
                firebaseCoworkerItem.restaurant = map.get("restaurant") as String?
                firebaseCoworkerItem.restaurantName = map.get("restaurantName") as String?
                firebaseCoworkerItem.liked = map.get("liked") as String?
                firebaseCoworkerItem.photo = map.get("photo") as String?
                firebaseCoworkerList.add(firebaseCoworkerItem)
            }
        }

        if (firebaseCoworkerList.any { it.username == username }) {
            var coworkers = emptyList<String?>()
            val restaurant = firebaseCoworkerList.first { it.username == username }.restaurant
            val restaurantName = firebaseCoworkerList.first { it.username == username }.restaurantName
            title = String.format(resources.getString(R.string.notification_title), restaurantName)
            if (restaurant != "") {
                coworkers =
                    firebaseCoworkerList.filter { it.restaurant == restaurant }.map { it.username }
            }
            message = when (coworkers.size) {
                0 -> resources.getString(R.string.eating_alone)
                1 -> String.format(resources.getString(R.string.one_person_joining), coworkers[0])
                2 -> String.format(resources.getString(R.string.multiple_people_joining), coworkers[0], coworkers[1])
                else -> String.format(resources.getString(R.string.multiple_people_joining), coworkers.dropLast(1).joinToString(", ") + ",", coworkers[coworkers.lastIndex])
            }
        }

        sendNotification(title, message)
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        // create intent, bundle with variables, notification title, and notification message
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        // create and send notification
        val notification = NotificationCompat.Builder(applicationContext, "notificationChannel")
            .setSmallIcon(R.drawable.image_placeholder)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1, notification)
    }
}