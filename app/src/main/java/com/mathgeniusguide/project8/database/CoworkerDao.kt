package com.mathgeniusguide.project8.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CoworkerDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCoworkerItemIfNotExists(coworkerItem: CoworkerRoomdbItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoworkerItem(coworkerItem: CoworkerRoomdbItem)

    @Update
    fun updateCoworkerItem(coworkerItem: CoworkerRoomdbItem)

    @Delete
    fun deleteCoworkerItem(coworkerItem: CoworkerRoomdbItem)

    @Query("SELECT * FROM CoworkerRoomdbItem")
    fun loadCoworkers(): LiveData<List<CoworkerRoomdbItem>>
}