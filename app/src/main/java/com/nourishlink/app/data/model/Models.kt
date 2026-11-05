package com.nourishlink.app.data.model

/** The signed-in user as the UI sees it (cached in [com.nourishlink.app.data.SessionManager]). */
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val organisation: String?,
    val phone: String? = null,
    val profilePicture: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean? = true,
    val pushNotificationsEnabled: Boolean? = true,
    val listingNotificationsEnabled: Boolean? = true,
    val leaderboardOptIn: Boolean? = true,
    val impactPoints: Int? = 0,
    val mealsRedistributed: Int? = 0,
    val kgSaved: Int? = 0,
    val badges: List<String>? = emptyList(),
    val authProvider: String = "local"
)

data class LeaderboardEntryDto(
    val rank: Int,
    val id: Int,
    val displayName: String,
    val role: String,
    val impactPoints: Int,
    val mealsRedistributed: Int,
    val kgSaved: Int,
    val badges: List<String>
)

data class MessageDto(
    val id: Int,
    val listingId: Int,
    val senderId: Int,
    val recipientId: Int?,
    val senderName: String,
    val text: String,
    val createdAt: String
)

data class NotificationDto(
    val id: Int,
    val userId: Int? = null,
    val title: String,
    val body: String,
    val listingId: Int? = null,
    val isRead: Boolean = false,
    val createdAt: String
)
