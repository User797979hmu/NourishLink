package com.nourishlink.app.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ListingDao {

    @Query("SELECT * FROM listings WHERE status = 'available' ORDER BY createdAt DESC")
    fun observeAvailable(): LiveData<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ListingEntity?

    @Insert
    suspend fun insert(listing: ListingEntity): Long

    @Insert
    suspend fun insertAll(listings: List<ListingEntity>): List<Long>

    @Query("UPDATE listings SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun count(): Int
}
