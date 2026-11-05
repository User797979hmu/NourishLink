package com.nourishlink.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A reservation a user made on a listing. Saved on this device the moment they tap "Reserve now".
 */
@Entity(tableName = "claims")
data class ClaimEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listingId: Int,
    val beneficiaryId: Int,
    val listingTitle: String,
    val quantityServings: Int,
    val collectBy: String,
    val address: String?,
    val vendorName: String? = null,
    val status: String = "reserved",   // "reserved" | "collected" | "cancelled"
    val claimedAt: Long = System.currentTimeMillis()
)
