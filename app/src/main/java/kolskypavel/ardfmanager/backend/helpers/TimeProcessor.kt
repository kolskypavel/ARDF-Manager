package kolskypavel.ardfmanager.backend.helpers

import kolskypavel.ardfmanager.backend.DataProcessor
import org.openardf.radioomanager.shared.time.DurationFormatter
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TimeProcessor {
    fun hoursMinutesFormatter(time: LocalDateTime): String {
        return DateTimeFormatter.ofPattern("HH:mm").format(time).toString()
    }

    // Formats the given LocalDateTime to a human readable form
    fun formatDisplayLocalDateTime(time: LocalDateTime): String {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(time).toString()
    }

    //  Formats the given LocalDateTime to ISO format
    fun formatIsoLocalDateTime(time: LocalDateTime): String {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(time).toString()
    }

    fun formatLocalDate(time: LocalDate): String {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(time).toString()
    }

    fun formatLocalTime(time: LocalTime): String {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(time).toString()
    }

    // Converts a Duration to a string in the format "mm:ss" or "HH:mm:ss"
    fun durationToFormattedString(
        duration: Duration,
        useMinutes: Boolean
    ): String {
        return DurationFormatter.secondsToFormattedString(duration.seconds, useMinutes)
    }

    @Throws(IllegalArgumentException::class)
    fun minuteStringToDuration(string: String): Duration {
        return Duration.ofSeconds(DurationFormatter.minuteStringToSeconds(string))
    }

    fun getAbsoluteDateTimeFromRelativeTime(
        startDateTime: LocalDateTime,
        relativeStartTime: Duration
    ): LocalDateTime {
        return startDateTime.plusSeconds(relativeStartTime.seconds)
    }


    /**
     * If a competitor is started or not
     */
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

    // Calculates the duration from competitor's start till now
    fun runDurationFromStart(
        startDateTime: LocalDateTime,
        relativeStartTime: Duration,
        curTime: LocalDateTime
    ): Duration? {
        //Check if the competitor started
        if (hasStarted(startDateTime, relativeStartTime, curTime)) {
            return Duration.between(startDateTime + relativeStartTime, curTime)
        }
        return null
    }

    // Calculates the duration from competitor's start till now and returns it as a string
    fun runDurationFromStartString(
        startDateTime: LocalDateTime,
        relativeStartTime: Duration,
        dataProcessor: DataProcessor,
        curTime: LocalDateTime
    ): String {
        //Check if the competitor started
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

    /**
     * If a competitor is in limit or not
     */
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

    //Calculates the duration to limit -
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
