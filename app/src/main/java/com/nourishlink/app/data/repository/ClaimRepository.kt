package com.nourishlink.app.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.withTransaction
import com.nourishlink.app.data.SessionManager
import com.nourishlink.app.data.local.AppDatabase
import com.nourishlink.app.data.local.ClaimEntity
import com.nourishlink.app.data.local.ListingEntity
import com.nourishlink.app.data.local.NotificationEntity
import com.nourishlink.app.util.IsoTime

/**
 * Reservations. Tapping "Reserve now" saves the claim and flips the listing to "reserved" in one
 * database transaction, so a listing can never be reserved twice.
 */
class ClaimRepository(
    private val database: AppDatabase,
    private val sessionManager: SessionManager
) {
    private val claimDao = database.claimDao()
    private val listingDao = database.listingDao()
    private val notificationDao = database.notificationDao()

    /** The signed-in user's own claims, newest first. */
    fun observeMine(): LiveData<List<ClaimEntity>> {
        val userId = sessionManager.currentUser?.id
            ?: return MutableLiveData<List<ClaimEntity>>(emptyList())
        return claimDao.observeForUser(userId)
    }

    /**
     * Reserves [listing] for the signed-in user. Returns the new claim, or null if the listing was
     * already taken (or nobody is signed in).
     */
    suspend fun reserve(listing: ListingEntity): ClaimEntity? {
        val user = sessionManager.currentUser ?: return null

        return database.withTransaction<ClaimEntity?> {
            val current = listingDao.getById(listing.id)
            if (current == null || current.status != "available") {
                null
            } else {
                val now = IsoTime.now()
                listingDao.updateStatus(current.id, "reserved", now)

                val claim = ClaimEntity(
                    listingId = current.id,
                    beneficiaryId = user.id,
                    listingTitle = current.title,
                    quantityServings = current.quantityServings,
                    collectBy = current.collectBy,
                    address = current.address,
                    vendorName = current.vendorName
                )
                val claimId = claimDao.insert(claim).toInt()

                // Confirmation for the person reserving...
                notificationDao.insert(
                    NotificationEntity(
                        userId = user.id,
                        title = "Reservation confirmed",
                        body = "${current.title} — collect by ${current.collectBy.take(16).replace("T", " ")}",
                        listingId = current.id,
                        createdAt = now
                    )
                )
                // ...and a heads-up for the donor who posted it.
                if (current.donorId != user.id) {
                    notificationDao.insert(
                        NotificationEntity(
                            userId = current.donorId,
                            title = "Your listing was reserved",
                            body = "${user.organisation?.takeIf { it.isNotBlank() } ?: user.name} reserved ${current.title}",
                            listingId = current.id,
                            createdAt = now
                        )
                    )
                }
                claim.copy(id = claimId)
            }
        }
    }
}
