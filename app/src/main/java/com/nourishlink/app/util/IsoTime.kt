package com.nourishlink.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** ISO-8601 UTC timestamps (e.g. 2026-11-05T14:30:00.000Z), matching what the feed/claims screens display. */
object IsoTime {

    private const val WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val WITHOUT_MILLIS = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    fun now(): String = format(System.currentTimeMillis())

    fun hoursFromNow(hours: Int): String = format(System.currentTimeMillis() + hours * 3_600_000L)

    fun minutesAgo(minutes: Int): String = format(System.currentTimeMillis() - minutes * 60_000L)

    fun format(millis: Long): String = formatter(WITH_MILLIS).format(Date(millis))

    /** Returns epoch millis, or null if [iso] isn't a recognised timestamp. */
    fun parseMillis(iso: String?): Long? {
        if (iso.isNullOrBlank()) return null
        for (pattern in listOf(WITH_MILLIS, WITHOUT_MILLIS)) {
            try {
                return formatter(pattern).parse(iso)?.time
            } catch (e: java.text.ParseException) {
                // try next pattern
            }
        }
        return null
    }

    // SimpleDateFormat isn't thread-safe, so build a fresh one per call (cheap at this scale).
    private fun formatter(pattern: String) = SimpleDateFormat(pattern, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}
