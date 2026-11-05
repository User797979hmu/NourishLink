package com.nourishlink.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.nourishlink.app.data.SessionManager
import com.nourishlink.app.data.local.DatabaseSeeder
import com.nourishlink.app.data.local.UserDao
import com.nourishlink.app.data.local.UserEntity
import com.nourishlink.app.data.model.LeaderboardEntryDto
import com.nourishlink.app.data.model.UserDto
import com.nourishlink.app.util.IsoTime
import com.nourishlink.app.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: UserDto) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Accounts, profile/settings and the leaderboard — all stored on this device.
 * Passwords are salted + hashed (PBKDF2) before they're saved; the plain text is never kept.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val seeder: DatabaseSeeder
) {

    suspend fun register(
        name: String, email: String, password: String, role: String,
        organisation: String?, phone: String? = null, address: String? = null
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            if (name.isBlank() || email.isBlank() || password.isEmpty()) {
                AuthResult.Error("Name, email and password are required")
            } else if (password.length < 8) {
                AuthResult.Error("Password must be at least 8 characters")
            } else {
                seeder.ensureSeeded()
                val normalisedEmail = email.trim().lowercase()
                if (userDao.getByEmail(normalisedEmail) != null) {
                    AuthResult.Error("An account with that email already exists")
                } else {
                    val entity = UserEntity(
                        name = name.trim(),
                        email = normalisedEmail,
                        passwordHash = PasswordHasher.hash(password),
                        authProvider = "local",
                        role = if (role == "donor") "donor" else "beneficiary",
                        organisation = organisation?.takeIf { it.isNotBlank() },
                        phone = phone?.takeIf { it.isNotBlank() },
                        address = address?.takeIf { it.isNotBlank() },
                        createdAt = IsoTime.now()
                    )
                    val id = userDao.insert(entity).toInt()
                    startSession(entity.copy(id = id))
                }
            }
        } catch (e: SQLiteConstraintException) {
            AuthResult.Error("An account with that email already exists")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Something went wrong. Please try again.")
        }
    }

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            seeder.ensureSeeded()
            val user = userDao.getByEmail(email.trim().lowercase())
            if (user == null ||
                user.authProvider != "local" ||
                !PasswordHasher.verify(password, user.passwordHash)
            ) {
                AuthResult.Error("Invalid email or password")
            } else {
                startSession(user)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Something went wrong. Please try again.")
        }
    }

    /** Saves any non-null field; null means "leave unchanged". */
    suspend fun updateProfile(
        name: String? = null,
        organisation: String? = null,
        phone: String? = null,
        profilePicture: String? = null,
        address: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        language: String? = null,
        notificationsEnabled: Boolean? = null,
        emailNotificationsEnabled: Boolean? = null,
        pushNotificationsEnabled: Boolean? = null,
        listingNotificationsEnabled: Boolean? = null,
        leaderboardOptIn: Boolean? = null
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val sessionUserId = sessionManager.currentUser?.id
            val current = if (sessionUserId != null) userDao.getById(sessionUserId) else null
            if (current == null) {
                AuthResult.Error("User not found")
            } else {
                val updated = current.copy(
                    name = name?.takeIf { it.isNotBlank() } ?: current.name,
                    organisation = organisation ?: current.organisation,
                    phone = phone ?: current.phone,
                    profilePicture = profilePicture ?: current.profilePicture,
                    address = address ?: current.address,
                    latitude = latitude ?: current.latitude,
                    longitude = longitude ?: current.longitude,
                    language = language ?: current.language,
                    notificationsEnabled = notificationsEnabled ?: current.notificationsEnabled,
                    emailNotificationsEnabled = emailNotificationsEnabled ?: current.emailNotificationsEnabled,
                    pushNotificationsEnabled = pushNotificationsEnabled ?: current.pushNotificationsEnabled,
                    listingNotificationsEnabled = listingNotificationsEnabled ?: current.listingNotificationsEnabled,
                    leaderboardOptIn = leaderboardOptIn ?: current.leaderboardOptIn
                )
                userDao.update(updated)
                val dto = updated.toDto()
                sessionManager.currentUser = dto
                AuthResult.Success(dto)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not update profile")
        }
    }

    suspend fun getLeaderboard(): List<LeaderboardEntryDto> = withContext(Dispatchers.IO) {
        try {
            userDao.getLeaderboard().mapIndexed { index, u ->
                LeaderboardEntryDto(
                    rank = index + 1,
                    id = u.id,
                    displayName = u.organisation?.takeIf { it.isNotBlank() } ?: u.name,
                    role = u.role,
                    impactPoints = u.impactPoints,
                    mealsRedistributed = u.mealsRedistributed,
                    kgSaved = u.kgSaved,
                    badges = u.badgeList()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun logout() = sessionManager.clear()

    fun currentUser(): UserDto? = sessionManager.currentUser

    suspend fun recordListingCreated(servings: Int) {
        withContext(Dispatchers.IO) {
            val userId = sessionManager.currentUser?.id ?: return@withContext
            val current = userDao.getById(userId) ?: return@withContext
            val addedPoints = servings * 5
            val addedKg = maxOf(1, servings / 3)
            val updated = current.copy(
                impactPoints = current.impactPoints + addedPoints,
                mealsRedistributed = current.mealsRedistributed + servings,
                kgSaved = current.kgSaved + addedKg,
                badges = UserEntity.joinBadges(
                    (current.badgeList() + "Surplus Donor").distinct()
                )
            )
            userDao.update(updated)
            sessionManager.currentUser = updated.toDto()
        }
    }

    /** Demo sign-in without an account. Reuses one stored guest profile, reset to its defaults each time. */
    suspend fun loginAsGuest(role: String = "beneficiary") {
        withContext(Dispatchers.IO) {
            seeder.ensureSeeded()
            val existing = userDao.getByEmail(GUEST_EMAIL)
            val guest = UserEntity(
                id = existing?.id ?: 0,
                name = if (role == "donor") "Guest Donor" else "Guest Beneficiary",
                email = GUEST_EMAIL,
                passwordHash = null,
                authProvider = "guest",
                role = role,
                organisation = "Demo Organisation",
                phone = "+27 82 000 0000",
                address = "Sandton, Johannesburg",
                language = "en",
                notificationsEnabled = false,
                impactPoints = 0,
                mealsRedistributed = 0,
                kgSaved = 0,
                badges = UserEntity.joinBadges(emptyList()),
                createdAt = existing?.createdAt ?: IsoTime.now()
            )
            val id = if (existing == null) {
                userDao.insert(guest).toInt()
            } else {
                userDao.update(guest)
                existing.id
            }
            sessionManager.currentUser = guest.copy(id = id).toDto()
        }
    }

    private fun startSession(user: UserEntity): AuthResult {
        val dto = user.toDto()
        sessionManager.currentUser = dto
        return AuthResult.Success(dto)
    }

    companion object {
        private const val GUEST_EMAIL = "guest@nourishlink.demo"
    }
}
