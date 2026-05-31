package org.openardf.radioomanager.shared.time

/** Shared duration formatting and parsing helpers used by import/export and UI adapters. */
object DurationFormatter {
    /** Formats seconds as either mmm:ss or HH:mm:ss, matching the existing Android behavior. */
    fun secondsToFormattedString(totalSeconds: Long, useMinutes: Boolean): String {
        val absSeconds = kotlin.math.abs(totalSeconds)

        return if (useMinutes) {
            val minutes = totalSeconds / 60
            val seconds = absSeconds % 60

            if (kotlin.math.abs(minutes) <= 99) {
                "%02d:%02d".format(minutes, seconds)
            } else {
                "%d:%02d".format(minutes, seconds)
            }
        } else {
            val hours = totalSeconds / 3600
            val minutes = (absSeconds % 3600) / 60
            val seconds = absSeconds % 60

            "%02d:%02d:%02d".format(hours, minutes, seconds)
        }
    }

    /** Parses an mmm:ss duration string and returns total seconds. */
    fun minuteStringToSeconds(value: String): Long {
        val parts = value.split(":")
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid time format. Expected format: mmm:ss")
        }

        val minutes = parts[0].toLongOrNull() ?: throw IllegalArgumentException("Invalid minutes")
        val seconds = parts[1].toLongOrNull() ?: throw IllegalArgumentException("Invalid seconds")

        if (minutes < 0 || seconds < 0 || seconds >= 60) {
            throw IllegalArgumentException("Invalid time values in input: $value")
        }

        return minutes * 60 + seconds
    }
}
