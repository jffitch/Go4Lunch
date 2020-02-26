package com.mathgeniusguide.go4lunch.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mathgeniusguide.project8.database.CoworkerDao
import com.mathgeniusguide.project8.database.CoworkerItem

@Database(entities = [RestaurantItem::class, CoworkerItem::class], version = 2, exportSchema = false)
abstract class RestaurantDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun coworkerDao(): CoworkerDao

    companion object {
        private var INSTANCE: RestaurantDatabase? = null
        var TEST_MODE = false

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `CoworkerItem` (`id` TEXT NOT NULL PRIMARY KEY, `username` TEXT, `restaurant` TEXT, `restaurantname` TEXT, `liked` TEXT, `photo` TEXT)")
            }
        }

        fun getDataBase(context: Context): RestaurantDatabase? {
            if (INSTANCE == null) {
                synchronized(RestaurantDatabase::class) {
                    if (TEST_MODE) {
                        INSTANCE = Room.inMemoryDatabaseBuilder(context.applicationContext, RestaurantDatabase::class.java)
                            .allowMainThreadQueries()
                            .addMigrations(MIGRATION_1_2)
                            .build()
                    } else {
                        INSTANCE = Room.databaseBuilder(context.applicationContext, RestaurantDatabase::class.java, "myDB")
                            .addMigrations(MIGRATION_1_2)
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