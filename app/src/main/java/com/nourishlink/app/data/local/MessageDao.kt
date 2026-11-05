package com.nourishlink.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Query(
        "SELECT m.id AS id, m.listingId AS listingId, m.senderId AS senderId, m.recipientId AS recipientId, " +
            "m.text AS text, m.createdAt AS createdAt, u.name AS senderName, u.organisation AS senderOrg " +
            "FROM messages m INNER JOIN users u ON u.id = m.senderId " +
            "WHERE m.listingId = :listingId ORDER BY m.createdAt ASC, m.id ASC"
    )
    suspend fun getForListing(listingId: Int): List<MessageRow>
}
