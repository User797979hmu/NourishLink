package com.nourishlink.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A short logistics message attached to a listing (donor <-> beneficiary coordination). */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listingId: Int,
    val senderId: Int,
    val recipientId: Int? = null,
    val text: String,
    val createdAt: String
)

/** A message joined with its sender's display info, as read by the chat list. */
data class MessageRow(
    val id: Int,
    val listingId: Int,
    val senderId: Int,
    val recipientId: Int?,
    val text: String,
    val createdAt: String,
    val senderName: String?,
    val senderOrg: String?
)
