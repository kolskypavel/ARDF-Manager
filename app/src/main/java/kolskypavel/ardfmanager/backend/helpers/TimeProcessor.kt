package kolskypavel.ardfmanager.backend.helpers

import kolskypavel.ardfmanager.backend.DataProcessor
import org.openardf.radioomanager.shared.time.DurationFormatter
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Android-facing time formatting and race-clock helper functions. */
object TimeProcessor {
    /** Formats a date-time as HH:mm for compact display. */
    fun hoursMinutesFormatter(time: LocalDateTime): String {
        return DateTimeFormatter.ofPattern("HH:mm").format(time).toString()
    }

    /** Formats a date-time for human-readable generated text and JSON compatibility. */
    fun formatDisplayLocalDateTime(time: LocalDateTime): String {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(time).toString()
    }

    /** Formats a date-time with the ISO local date-time formatter. */
    fun formatIsoLocalDateTime(time: LocalDateTime): String {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(time).toString()
    }

    /** Formats a date as yyyy-MM-dd. */
    fun formatLocalDate(time: LocalDate): String {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(time).toString()
    }

    /** Formats a time as HH:mm:ss. */
    fun formatLocalTime(time: LocalTime): String {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(time).toString()
    }

    /** Converts a duration to mm:ss or HH:mm:ss according to the user's time-format preference. */
    fun durationToFormattedString(
        duration: Duration,
        useMinutes: Boolean
    ): String {
        return DurationFormatter.secondsToFormattedString(duration.seconds, useMinutes)
    }

    /** Parses the app's minute-style duration string into a duration. */
    @Throws(IllegalArgumentException::class)
    fun minuteStringToDuration(string: String): Duration {
        return Duration.ofSeconds(DurationFormatter.minuteStringToSeconds(string))
    }

    /** Converts a competitor start offset into an absolute race date-time. */
    fun getAbsoluteDateTimeFromRelativeTime(
        startDateTime: LocalDateTime,
        relativeStartTime: Duration
    ): LocalDateTime {
        return startDateTime.plusSeconds(relativeStartTime.seconds)
    }


    /** Returns whether the competitor has reached their scheduled start time. */
    fun hasStarted(
        startDateTime: LocalDateTime,
        relativeStartTime: Duration,
        curTime: LocalDateTime
    ): Boolean {
        return (curTime.isAfter(
            getAbsoluteDateTimeFromRelativeTime(
                startDateTime,
                relativeStartTime
            )
        ))
    }

    /** Returns elapsed run time since start, or null if the competitor has not started yet. */
    fun runDurationFromStart(
        startDateTime: LocalDateTime,
        relativeStartTime: Duration,
        curTime: LocalDateTime
    ): Duration? {
        if (hasStarted(startDateTime, relativeStartTime, curTime)) {
            return Duration.between(startDateTime + relativeStartTime, curTime)
        }
        return null
    }

    /** Formats elapsed run time since start, or returns an empty string before the start time. */
    fun runDurationFromStartString(
        startDateTime: LocalDateTime,
        relativeStartTime: Duration,
        dataProcessor: DataProcessor,
        curTime: LocalDateTime
    ): String {
        if (hasStarted(startDateTime, relativeStartTime, curTime)) {
            return durationToFormattedString(
                Duration.between(
                    startDateTime + relativeStartTime,
                    curTime
                ), dataProcessor.useMinuteTimeFormat()
            )
        }
        return ""
    }

    /** Returns whether the current time is still inside the competitor's race time limit. */
    fun isInLimit(
        startDateTime: LocalDateTime,
        relativeStartTime: Duration,
        timeLimit: Duration,
        curTime: LocalDateTime
    ): Boolean {
        return if (hasStarted(startDateTime, relativeStartTime, curTime)) {
            curTime.isBefore(startDateTime.plusSeconds(timeLimit.seconds))
        } else true
    }

    /** Returns remaining time to the competitor's limit, or null before the start time. */
    fun durationToLimit(
        startDateTime: LocalDateTime,
        relativeStartTime: Duration,
        timeLimit: Duration,
        curTime: LocalDateTime
    ): Duration? {
        if (hasStarted(startDateTime, relativeStartTime, curTime)) {
            return Duration.between(
                curTime,
                (startDateTime + relativeStartTime).plusSeconds(timeLimit.seconds)
            )
        }
        return null
    }
}
