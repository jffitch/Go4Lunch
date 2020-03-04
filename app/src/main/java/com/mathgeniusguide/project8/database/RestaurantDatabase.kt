package com.mathgeniusguide.go4lunch.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mathgeniusguide.project8.database.CoworkerDao
import com.mathgeniusguide.project8.database.CoworkerRoomdbItem

@Database(entities = [RestaurantRoomdbItem::class, CoworkerRoomdbItem::class], version = 3, exportSchema = false)
abstract class RestaurantDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun coworkerDao(): CoworkerDao

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