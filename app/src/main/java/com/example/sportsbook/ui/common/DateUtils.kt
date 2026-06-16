package com.example.sportsbook.ui.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Parses a date string that may be in various formats:
 *   yyyy-MM-dd
 *   yyyy-MM-dd HH:mm:ss
 *   yyyy-MM-dd'T'HH:mm:ss
 * Returns the parsed Date or null.
 */
private fun parseFlexibleDate(value: String): Date? {
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
    )
    for (fmt in formats) {
        try {
            val sdf = SimpleDateFormat(fmt, Locale.US)
            sdf.isLenient = false
            val d = sdf.parse(value)
            if (d != null) return d
        } catch (_: Exception) { /* try next */ }
    }
    return null
}

/**
 * Formats an ISO date string to display format (dd-MM-yyyy).
 * Handles yyyy-MM-dd, yyyy-MM-dd HH:mm:ss, and ISO-T variants.
 */
fun String.toDisplayDate(): String {
    val date = parseFlexibleDate(this) ?: return this
    return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(date)
}

/**
 * Formats a date string to a friendly format (e.g. "Mon, 18 Mar 2026").
 * Handles all common date/datetime formats from the backend.
 */
fun String.toFriendlyDate(): String {
    val date = parseFlexibleDate(this) ?: return this
    return SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(date)
}

/**
 * Formats a date string to a pretty short format (e.g. "Sat, Jun 14").
 */
fun String.toPrettyDate(): String {
    val date = parseFlexibleDate(this) ?: return this
    return SimpleDateFormat("EEE, MMM dd", Locale.US).format(date)
}

/**
 * Formats an ISO datetime string to display format (dd-MM-yyyy HH:mm).
 */
fun String.toDisplayDateTime(): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val output = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
        val date = input.parse(this) ?: return this
        output.format(date)
    } catch (_: Exception) {
        // Try with milliseconds
        try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val output = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
            val date = input.parse(this) ?: return this
            output.format(date)
        } catch (_: Exception) {
            this
        }
    }
}

/**
 * Formats a time string (HH:mm:ss or HH:mm) to short display (HH:mm).
 */
fun String.toDisplayTime(): String {
    return if (this.length >= 5) this.substring(0, 5) else this
}

/**
 * Converts epoch millis to ISO date string (yyyy-MM-dd) for API use.
 */
fun Long.toIsoDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(this))
}

/**
 * Converts epoch millis to display date string (dd-MM-yyyy).
 */
fun Long.toDisplayDate(): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    return sdf.format(Date(this))
}
