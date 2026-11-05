package com.nourishlink.app.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClaimDao {

    @Query("SELECT * FROM claims WHERE beneficiaryId = :userId ORDER BY claimedAt DESC, id DESC")
    fun observeForUser(userId: Int): LiveData<List<ClaimEntity>>

    @Query("SELECT DISTINCT beneficiaryId FROM claims WHERE listingId = :listingId AND status != 'cancelled'")
    suspend fun getClaimantIds(listingId: Int): List<Int>

    @Insert
    suspend fun insert(claim: ClaimEntity): Long
}
