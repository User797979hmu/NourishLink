package com.nourishlink.app.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Geo {

    const val DEFAULT_LAT = -26.1076   // Sandton, Johannesburg
    const val DEFAULT_LNG = 28.0567

    /** Great-circle (haversine) distance in km, rounded to 1 decimal. 0 if either point is unknown. */
    fun distanceKm(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Double {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 0.0
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return Math.round(earthRadiusKm * c * 10) / 10.0
    }
}
