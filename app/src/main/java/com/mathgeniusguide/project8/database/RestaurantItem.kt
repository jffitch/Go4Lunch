package com.mathgeniusguide.go4lunch.database

import androidx.room.*

@Entity
data class RestaurantItem(
    @PrimaryKey
    val id: String,
    val address: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    val image: String,
    val phone: String?,
    val website: String?,
    val name: String
)