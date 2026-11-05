package com.nourishlink.app.data.repository

import com.nourishlink.app.data.SessionManager
import com.nourishlink.app.data.local.NotificationDao
import com.nourishlink.app.data.local.NotificationEntity
import com.nourishlink.app.data.model.NotificationDto

/** In-app notifications for the signed-in user (stored on this device). */
class NotificationRepository(
    private val dao: NotificationDao,
    private val sessionManager: SessionManager
) {

    suspend fun getMine(): List<NotificationDto> {
        val userId = sessionManager.currentUser?.id ?: return emptyList()
        return dao.getForUser(userId).map { it.toDto() }
    }

    /** Highest notification id the signed-in user currently has (0 if none). */
    suspend fun latestId(): Int {
        val userId = sessionManager.currentUser?.id ?: return 0
        return dao.latestId(userId)
    }

    /** Notifications created after [afterId], oldest first. */
    suspend fun getNewerThan(afterId: Int): List<NotificationDto> {
        val userId = sessionManager.currentUser?.id ?: return emptyList()
        return dao.getNewerThan(userId, afterId).map { it.toDto() }
    }

    private fun NotificationEntity.toDto() = NotificationDto(
        id = id,
        userId = userId,
        title = title,
        body = body,
        listingId = listingId,
        isRead = isRead,
        createdAt = createdAt
    )
}
