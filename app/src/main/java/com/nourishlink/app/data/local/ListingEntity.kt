package com.nourishlink.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A surplus-food listing posted by a donor.
 */
@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val donorId: Int,
    val title: String,
    val category: String,
    val description: String?,
    val quantityServings: Int,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?,
    val collectBy: String,
    val status: String,
    val vendorName: String? = "Donor Vendor",
    val dietaryFlags: String? = "", // comma-separated, e.g. "halal, vegan"
    val distanceKm: Double? = 0.0,  // filled in per-user when the feed is ranked
    val createdAt: String,
    val updatedAt: String
)
