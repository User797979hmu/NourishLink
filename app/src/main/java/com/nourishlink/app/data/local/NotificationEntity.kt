package com.nourishlink.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val body: String,
    val listingId: Int? = null,
    val isRead: Boolean = false,
    val createdAt: String
)
