package kolskypavel.ardfmanager.backend.helpers

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TimeProcessor {
    fun getHoursMinutesFromTime(time: LocalTime): String {
        return DateTimeFormatter.ofPattern("HH:mm").format(time).toString()
    }

    fun durationToMinuteString(duration: Duration): String {
        val seconds = duration.seconds
        return if (kotlin.math.abs(seconds / 60) <= 99) {
            String.format("%02d:%02d", seconds / 60, kotlin.math.abs(seconds) % 60);
        } else if (kotlin.math.abs(seconds / 60) <= 999) {
            String.format("%03d:%02d", seconds / 60, kotlin.math.abs(seconds) % 60)
        } else {
            String.format("%04d:%02d", seconds / 60, kotlin.math.abs(seconds) % 60)
        }
    }

    fun runDurationFromStart(startDate: LocalDate, startTime: LocalTime): Duration? {
        //Check if the competitor started
        if (LocalDate.now().isAfter(startDate) || LocalDate.now().isEqual(startDate)) {
            return Duration.between(startDate.atTime(startTime), LocalDateTime.now())
        }
        return null
    }

    fun durationToLimit(startDate: LocalDate, startTime: LocalTime, timeLimit: Duration) {

    }
}