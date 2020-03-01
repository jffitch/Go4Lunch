package com.mathgeniusguide.go4lunch.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RestaurantDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRestaurantItemIfNotExists(restaurantItem: RestaurantRoomdbItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRestaurantItem(restaurantItem: RestaurantRoomdbItem)

    @Update
    fun updateRestaurantItem(restaurantItem: RestaurantRoomdbItem)

    @Delete
    fun deleteRestaurantItem(restaurantItem: RestaurantRoomdbItem)

    @Query("SELECT * FROM RestaurantRoomdbItem")
    fun selectAll(): LiveData<List<RestaurantRoomdbItem>>

    @Query("SELECT id, expiration, latitude, longitude FROM RestaurantRoomdbItem")
    fun selectIds(): LiveData<List<RestaurantRoomdbItem>>
}