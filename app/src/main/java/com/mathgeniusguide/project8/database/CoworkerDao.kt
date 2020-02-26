package com.mathgeniusguide.project8.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mathgeniusguide.project8.database.CoworkerItem

@Dao
interface CoworkerDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCoworkerItemIfNotExists(coworkerItem: CoworkerItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoworkerItem(coworkerItem: CoworkerItem)

    @Update
    fun updateCoworkerItem(coworkerItem: CoworkerItem)

    @Delete
    fun deleteCoworkerItem(coworkerItem: CoworkerItem)

    @Query("SELECT * FROM CoworkerItem")
    fun loadCoworkers(): LiveData<List<CoworkerItem>>
}