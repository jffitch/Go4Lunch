package com.mathgeniusguide.project8.database

import androidx.room.*

@Entity
data class CoworkerRoomdbItem(
    @PrimaryKey
    val id: String,
    val username: String?,
    val restaurant: String?,
    val restaurantname: String?,
    val liked: String?,
    val photo: String?
)