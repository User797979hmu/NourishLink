package com.nourishlink.app.data.repository

import androidx.lifecycle.LiveData
import com.nourishlink.app.data.local.ListingDao
import com.nourishlink.app.data.local.ListingEntity
import com.nourishlink.app.util.Geo
import com.nourishlink.app.util.IsoTime

/**
 * The surplus feed. Listings live in the on-device database; this class also does the filtering
 * (category, radius, search) and the "smart matching" ranking (closer + sooner-expiring first).
 */
class ListingRepository(private val dao: ListingDao) {

    /** Every listing that is still available. Re-emits automatically when anything changes. */
    fun observeAvailable(): LiveData<List<ListingEntity>> = dao.observeAvailable()

    suspend fun getById(id: Int): ListingEntity? = dao.getById(id)

    suspend fun createListing(listing: ListingEntity): Long = dao.insert(listing)

    /**
     * Applies the feed filters and sorts by a combined distance + urgency score
     * (lower = closer to you and closer to its collect-by time = shown first).
     * Each returned listing carries its distance from ([lat], [lng]).
     */
    fun filterAndRank(
        listings: List<ListingEntity>,
        category: String?,
        maxDistanceKm: Int?,
        query: String?,
        lat: Double,
        lng: Double
    ): List<ListingEntity> {
        val term = query?.trim()?.takeIf { it.isNotEmpty() }
        val nowMs = System.currentTimeMillis()

        val withDistance = listings
            .filter { category == null || it.category == category }
            .filter { listing ->
                term == null ||
                    listing.title.contains(term, ignoreCase = true) ||
                    (listing.description?.contains(term, ignoreCase = true) ?: false) ||
                    (listing.vendorName?.contains(term, ignoreCase = true) ?: false) ||
                    (listing.address?.contains(term, ignoreCase = true) ?: false)
            }
            .map { it.copy(distanceKm = Geo.distanceKm(lat, lng, it.latitude, it.longitude)) }
            .filter { maxDistanceKm == null || maxDistanceKm <= 0 || (it.distanceKm ?: 0.0) <= maxDistanceKm }

        fun score(l: ListingEntity): Double {
            val collectMs = IsoTime.parseMillis(l.collectBy) ?: nowMs
            val hoursRemaining = maxOf(0.0, (collectMs - nowMs) / 3_600_000.0)
            return (l.distanceKm ?: 0.0) * 2 + hoursRemaining
        }
        return withDistance
            .map { it to score(it) }
            .sortedBy { it.second }
            .map { it.first }
    }
}
