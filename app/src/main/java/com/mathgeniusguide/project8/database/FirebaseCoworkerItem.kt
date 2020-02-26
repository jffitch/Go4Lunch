package com.mathgeniusguide.project8.database

class FirebaseCoworkerItem {
    companion object Factory {
        fun create(): FirebaseCoworkerItem = FirebaseCoworkerItem()
    }
    var id: String? = null
    var username: String? = null
    var restaurant: String? = null
    var restaurantName: String? = null
    var liked: String? = null
    var photo: String? = null
}