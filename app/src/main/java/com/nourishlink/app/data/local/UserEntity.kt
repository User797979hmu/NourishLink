package com.nourishlink.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nourishlink.app.data.model.UserDto
import com.nourishlink.app.util.Geo

/**
 * An account stored on this device. `passwordHash` is a salted PBKDF2 hash (see PasswordHasher) —
 * the plain-text password is never stored. `badges` is a "|"-separated list.
 */
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String? = null,
    val authProvider: String = "local",      // "local" | "guest"
    val role: String = "beneficiary",        // "donor" | "beneficiary"
    val organisation: String? = null,
    val phone: String? = null,
    val profilePicture: String? = null,
    val latitude: Double = Geo.DEFAULT_LAT,
    val longitude: Double = Geo.DEFAULT_LNG,
    val address: String? = null,
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val listingNotificationsEnabled: Boolean = true,
    val leaderboardOptIn: Boolean = true,
    val impactPoints: Int = 0,
    val mealsRedistributed: Int = 0,
    val kgSaved: Int = 0,
    val badges: String = "",
    val createdAt: String
) {
    fun badgeList(): List<String> =
        if (badges.isBlank()) emptyList() else badges.split(BADGE_SEPARATOR).filter { it.isNotBlank() }

    fun toDto() = UserDto(
        id = id,
        name = name,
        email = email,
        role = role,
        organisation = organisation,
        phone = phone ?: "",
        profilePicture = profilePicture ?: "",
        latitude = latitude,
        longitude = longitude,
        address = address ?: "",
        language = language,
        notificationsEnabled = notificationsEnabled,
        emailNotificationsEnabled = emailNotificationsEnabled,
        pushNotificationsEnabled = pushNotificationsEnabled,
        listingNotificationsEnabled = listingNotificationsEnabled,
        leaderboardOptIn = leaderboardOptIn,
        impactPoints = impactPoints,
        mealsRedistributed = mealsRedistributed,
        kgSaved = kgSaved,
        badges = badgeList(),
        authProvider = authProvider
    )

    companion object {
        const val BADGE_SEPARATOR = "|"
        fun joinBadges(badges: List<String>): String = badges.joinToString(BADGE_SEPARATOR)
    }
}
