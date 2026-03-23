package com.example.sportsbook.ui.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Formats an ISO date string (yyyy-MM-dd) to display format (dd-MM-yyyy).
 * Returns the original string if parsing fails.
 */
fun String.toDisplayDate(): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val output = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val date = input.parse(this) ?: return this
        output.format(date)
    } catch (_: Exception) {
        this
    }
}

/**
 * Formats an ISO date string (yyyy-MM-dd) to a friendly format (e.g. "Mon, 18 Mar 2026").
 */
fun String.toFriendlyDate(): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val output = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
        val date = input.parse(this) ?: return this
        output.format(date)
    } catch (_: Exception) {
        this
    }
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
