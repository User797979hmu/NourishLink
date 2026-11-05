package com.nourishlink.app.data.repository

import androidx.room.withTransaction
import com.nourishlink.app.data.SessionManager
import com.nourishlink.app.data.local.AppDatabase
import com.nourishlink.app.data.local.MessageEntity
import com.nourishlink.app.data.local.NotificationEntity
import com.nourishlink.app.data.model.MessageDto
import com.nourishlink.app.util.IsoTime

/** Short logistics messages on a listing (e.g. "I'll be there at 4pm"), stored on this device. */
class MessageRepository(
    private val database: AppDatabase,
    private val sessionManager: SessionManager
) {
    private val messageDao = database.messageDao()
    private val listingDao = database.listingDao()
    private val claimDao = database.claimDao()
    private val notificationDao = database.notificationDao()

    suspend fun getMessages(listingId: Int): List<MessageDto> =
        messageDao.getForListing(listingId).map {
            MessageDto(
                id = it.id,
                listingId = it.listingId,
                senderId = it.senderId,
                recipientId = it.recipientId,
                senderName = it.senderOrg?.takeIf { org -> org.isNotBlank() } ?: it.senderName ?: "Unknown",
                text = it.text,
                createdAt = it.createdAt
            )
        }

    /** Saves the message and notifies the other party. Returns false if it couldn't be sent. */
    suspend fun send(listingId: Int, text: String): Boolean {
        val user = sessionManager.currentUser ?: return false
        val body = text.trim()
        if (body.isEmpty()) return false

        return database.withTransaction<Boolean> {
            val listing = listingDao.getById(listingId)
            if (listing == null) {
                false
            } else {
                val now = IsoTime.now()
                messageDao.insert(
                    MessageEntity(listingId = listingId, senderId = user.id, text = body, createdAt = now)
                )

                // Anyone else involved with this listing gets a notification: the donor if a
                // beneficiary wrote, or the people who reserved it if the donor wrote.
                val candidates: List<Int> = if (user.id == listing.donorId) {
                    claimDao.getClaimantIds(listingId)
                } else {
                    listOf(listing.donorId)
                }
                val recipients = candidates.filter { it != user.id }.distinct()

                for (recipientId in recipients) {
                    notificationDao.insert(
                        NotificationEntity(
                            userId = recipientId,
                            title = "New Logistics Message",
                            body = "${user.name}: ${body.take(50)}",
                            listingId = listingId,
                            createdAt = now
                        )
                    )
                }
                true
            }
        }
    }
}
