package com.mathgeniusguide.go4lunch.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RestaurantDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRestaurantItemIfNotExists(restaurantItem: RestaurantItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRestaurantItem(restaurantItem: RestaurantItem)

    @Update
    fun updateRestaurantItem(restaurantItem: RestaurantItem)

    @Delete
    fun deleteRestaurantItem(restaurantItem: RestaurantItem)

    @Query("SELECT * FROM RestaurantItem")
    fun selectAll(): LiveData<List<RestaurantItem>>

    @Query("SELECT id FROM RestaurantItem")
    fun selectIds(): LiveData<List<String>>
}