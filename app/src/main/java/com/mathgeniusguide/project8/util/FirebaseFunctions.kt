package com.mathgeniusguide.project8.util

import com.google.firebase.database.DatabaseReference
import com.mathgeniusguide.project8.database.ChatItem
import com.mathgeniusguide.project8.database.FirebaseCoworkerItem
import java.text.SimpleDateFormat
import java.util.*

object FirebaseFunctions {
    fun createCoworker(username: String, restaurant: String, restaurantName: String, liked: String, photo: String, database: DatabaseReference): String {
        // create coworker entry for FireBase
        // id created automatically
        val newItem = database.child(Constants.CHOSEN_RESTAURANTS).push()
        val firebaseCoworkerItem = FirebaseCoworkerItem.create()
        firebaseCoworkerItem.id = newItem.key
        firebaseCoworkerItem.username = username
        firebaseCoworkerItem.restaurant = restaurant
        firebaseCoworkerItem.restaurantName = restaurantName
        firebaseCoworkerItem.liked = liked
        firebaseCoworkerItem.photo = photo
        newItem.setValue(firebaseCoworkerItem)
        // return id for use in other functions
        return firebaseCoworkerItem.id!!
    }

    fun updateRestaurant(itemKey: String, restaurant: String, database: DatabaseReference) {
        // update restaurant id in FireBase
        val itemReference = database.child(Constants.CHOSEN_RESTAURANTS).child(itemKey)
        itemReference.child("restaurant").setValue(restaurant)
    }

    fun updateRestaurantName(itemKey: String, restaurantName: String, database: DatabaseReference) {
        // update restaurant name in FireBase
        val itemReference = database.child(Constants.CHOSEN_RESTAURANTS).child(itemKey)
        itemReference.child("restaurantName").setValue(restaurantName)
    }


    fun updateLiked(itemKey: String, liked: MutableList<String>, database: DatabaseReference) {
        // update liked restaurants in FireBase
        val itemReference = database.child(Constants.CHOSEN_RESTAURANTS).child(itemKey)
        itemReference.child("liked").setValue(liked.joinToString(" , "))
    }

    // create chat item in firebase
    fun createChat(from: String, to: String, text: String, database: DatabaseReference) {
        // create chat entry in FireBase
        // id created automatically
        // timestamp set to now
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val newItem = database.child(Constants.CHATS).push()
        val chatItem = ChatItem.create()
        chatItem.id = newItem.key
        chatItem.from = from
        chatItem.to = to
        chatItem.text = text
        chatItem.timestamp = timestamp
        newItem.setValue(chatItem)
    }
}