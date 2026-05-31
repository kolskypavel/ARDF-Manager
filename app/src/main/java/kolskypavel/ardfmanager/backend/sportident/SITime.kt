package kolskypavel.ardfmanager.backend.sportident

import org.openardf.radioomanager.shared.sportident.SportIdentTime
import java.io.Serializable
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Wrapper class for calculating the split times
 */
class SITime(
    private var time: LocalTime,
    private var dayOfWeek: Int = 0, //0 - Sunday, 6 - Saturday
    private var week: Int = 0
) : Serializable {

    private var seconds: Long = 0

    constructor() : this(LocalTime.MIDNIGHT, 0, 0)
    constructor(time: LocalTime) : this(time, 0, 0) {
        calculateSeconds()
    }

    constructor(other: SITime) : this(other.time, other.dayOfWeek, other.week) {
        this.seconds = other.seconds
    }

    init {
        calculateSeconds()
    }

    constructor(origSeconds: Long) : this() {
        val sportIdentTime = SportIdentTime(origSeconds)
        this.seconds = sportIdentTime.getSeconds()
        this.time = LocalTime.of(
            sportIdentTime.getHour(),
            sportIdentTime.getMinute(),
            sportIdentTime.getSecond()
        )
        this.week = sportIdentTime.getWeek()
        this.dayOfWeek = sportIdentTime.getDayOfWeek()
    }

    constructor(time: LocalDateTime, startZero: LocalDateTime) : this() {
        this.time = time.toLocalTime()
        this.dayOfWeek = dayOfWeekToSIIndex(time.dayOfWeek)
        this.week = ((Duration.between(
            startZero,
            time
        )).seconds / SIConstants.SECONDS_WEEK).toInt()    //Assume that start 00 is at week 0
        calculateSeconds()
    }

    private fun calculateSeconds() {
        this.seconds =
            this.week * SIConstants.SECONDS_WEEK + this.dayOfWeek * SIConstants.SECONDS_DAY + this.time.toSecondOfDay()
    }

    override fun toString(): String {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return "${time.format(timeFormatter)},$dayOfWeek,$week"
    }

    fun localTimeFormatter(): String {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(time)
    }

    fun addHalfDay() {
        if (time.isAfter(LocalTime.NOON)) {
            dayOfWeek++
            adjustDayWeek()
        }
        this.time = time.plusHours(12)
        calculateSeconds()
    }

    fun addDay() {
        dayOfWeek++
        adjustDayWeek()
        calculateSeconds()
    }

    /**
     * Adjust the weeks and days for SI card 5
     */
    private fun adjustDayWeek() {
        if (dayOfWeek > 6) {
            week++
            dayOfWeek %= 7
        }
    }

    fun addTime(duration: Duration) {
        val sportIdentTime = SportIdentTime(seconds)
        sportIdentTime.addSeconds(duration.seconds)
        this.seconds = sportIdentTime.getSeconds()
        this.time = LocalTime.of(
            sportIdentTime.getHour(),
            sportIdentTime.getMinute(),
            sportIdentTime.getSecond()
        )
        this.week = sportIdentTime.getWeek()
        this.dayOfWeek = sportIdentTime.getDayOfWeek()
    }

    //Getters and setters
    fun getTime() = time
    fun getDayOfWeek() = dayOfWeek
    fun getWeek() = week

    fun getSeconds() = seconds

    fun getTimeString(): String {
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    fun setTime(newTime: LocalTime) {
        this.time = newTime
        calculateSeconds()
    }

    fun setDayOfWeek(newDayOfWeek: Int) {
        this.dayOfWeek = newDayOfWeek
        calculateSeconds()
    }

    fun setWeek(newWeek: Int) {
        this.week = newWeek
        calculateSeconds()
    }

    /**
     * Checks if another SITime is after or equal to this SI time
     */
    fun isAtOrAfter(other: SITime): Boolean {
        return this.seconds >= other.seconds
    }

    fun isAfter(other: SITime): Boolean {
        return this.seconds > other.seconds
    }

    fun compareTo(other: SITime?): Int {
        return this.seconds.compareTo(other?.seconds ?: 0)
    }

    /**
     * Converts SI time to localDateTime when start 00 is provided
     */
    fun toLocalDateTime(startZero: LocalDateTime): LocalDateTime {
        // Start from the startZero date at the SITime's local time
        var candidateDate = startZero.toLocalDate()

        // Find the first date >= startZero.date that has the same SI day index
        while (dayOfWeekToSIIndex(candidateDate.dayOfWeek) != this.dayOfWeek) {
            candidateDate = candidateDate.plusDays(1)
        }

        var candidate = candidateDate.atTime(time)

        // Account for the week offset encoded in this SITime
        if (this.week > 0) {
            candidate = candidate.plusWeeks(this.week.toLong())
        }

        return candidate
    }

    companion object {
        @Throws(IllegalArgumentException::class)
        fun from(string: String): SITime {
            try {
                val sportIdentTime = SportIdentTime.from(string)

                return SITime(
                    LocalTime.of(
                        sportIdentTime.getHour(),
                        sportIdentTime.getMinute(),
                        sportIdentTime.getSecond()
                    ),
                    sportIdentTime.getDayOfWeek(),
                    sportIdentTime.getWeek()
                )

            } catch (e: Exception) {
                throw java.lang.IllegalArgumentException("Error when parsing SI time", e)
            }
        }

        fun split(start: SITime, end: SITime): Duration {
            return Duration.ofSeconds(SportIdentTime.split(start.toShared(), end.toShared()))
        }

        fun difference(start: SITime, end: SITime): Duration {
            return Duration.ofSeconds(SportIdentTime.difference(start.toShared(), end.toShared()))
        }

        // Convert DayOfWeek (1 - Monday, 7 - Sunday) to SI index (0 - Sunday, 6 - Saturday)
        fun dayOfWeekToSIIndex(dayOfWeek: DayOfWeek): Int {
            return when (dayOfWeek) {
                DayOfWeek.MONDAY -> 1
                DayOfWeek.TUESDAY -> 2
                DayOfWeek.WEDNESDAY -> 3
                DayOfWeek.THURSDAY -> 4
                DayOfWeek.FRIDAY -> 5
                DayOfWeek.SATURDAY -> 6
                DayOfWeek.SUNDAY -> 0
            }
        }

        private fun SITime.toShared(): SportIdentTime {
            return SportIdentTime(
                hour = time.hour,
                minute = time.minute,
                second = time.second,
                dayOfWeek = dayOfWeek,
                week = week
            )
        }
    }
}
