package com.mathgeniusguide.go4lunch.database

import android.content.Context
import androidx.room.*

@Database(entities = [RestaurantItem::class], version = 1, exportSchema = false)
abstract class RestaurantDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao

    companion object {
        private var INSTANCE: RestaurantDatabase? = null
        var TEST_MODE = false

        fun getDataBase(context: Context): RestaurantDatabase? {
            if (INSTANCE == null) {
                synchronized(RestaurantDatabase::class) {
                    if (TEST_MODE) {
                        INSTANCE = Room.inMemoryDatabaseBuilder(context.applicationContext, RestaurantDatabase::class.java)
                            .allowMainThreadQueries()
                            .build()
                    } else {
                        INSTANCE = Room.databaseBuilder(context.applicationContext, RestaurantDatabase::class.java, "myDB")
                            .build()
                    }
                }
            }
            return INSTANCE
        }

        fun destroyDataBase() {
            INSTANCE = null
        }
    }
}