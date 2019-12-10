package com.mathgeniusguide.project8.database

class ChosenRestaurantItem {
    companion object Factory {
        fun create(): ChosenRestaurantItem = ChosenRestaurantItem()
    }
    var id: String? = null
    var username: String? = null
    var restaurant: String? = null
    var photo: String? = null
}