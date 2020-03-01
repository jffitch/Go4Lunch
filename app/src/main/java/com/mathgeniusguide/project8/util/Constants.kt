package com.mathgeniusguide.project8.util

object Constants {
    // API calls
    const val API_KEY = "AIzaSyDMWYwVXRhuhSQ5vcom9iAI2-FH6T6QKDI"
    const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"
    const val FIELDS = "place_id,formatted_address,formatted_phone_number,geometry/location,website,name,rating,opening_hours,photo"

    // firebase database locations
    const val CHOSEN_RESTAURANTS: String = "chosen_restaurants"
    const val CHATS: String = "chats"

    // SharedPreferences location
    const val PREF_LOCATION = "com.mathgeniusguide.go4lunch.pref"

    // sort orders
    const val BY_DISTANCE = 0
    const val BY_RATING = 1
    const val BY_WORKMATES = 2
    const val BY_NAME = 3
}