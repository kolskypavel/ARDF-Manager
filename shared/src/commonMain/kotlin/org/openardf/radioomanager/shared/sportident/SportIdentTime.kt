package org.openardf.radioomanager.shared.sportident

/** Platform-neutral SportIdent time value with day/week rollover support. */
class SportIdentTime(
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    private var dayOfWeek: Int = 0,
    private var week: Int = 0
) {
    private var hour: Int = hour
    private var minute: Int = minute
    private var second: Int = second
    private var seconds: Long = 0

    init {
        validateTime(this.hour, this.minute, this.second)
        calculateSeconds()
    }

    constructor(other: SportIdentTime) : this(
        other.hour,
        other.minute,
        other.second,
        other.dayOfWeek,
        other.week
    )

    constructor(totalSeconds: Long) : this() {
        setFromSeconds(totalSeconds)
    }

    /** Returns the hour component in 24-hour SI time. */
    fun getHour() = hour
    /** Returns the minute component. */
    fun getMinute() = minute
    /** Returns the second component. */
    fun getSecond() = second
    /** Returns the SI day-of-week component. */
    fun getDayOfWeek() = dayOfWeek
    /** Returns the SI week component. */
    fun getWeek() = week
    /** Returns the absolute SI time value in seconds. */
    fun getSeconds() = seconds

    /** Formats the time-of-day component as HH:mm:ss. */
    fun getTimeString(): String {
        return "%02d:%02d:%02d".format(hour, minute, second)
    }

    override fun toString(): String {
        return "${getTimeString()},$dayOfWeek,$week"
    }

    /** Sets the time-of-day fields and recalculates absolute seconds. */
    fun setTime(hour: Int, minute: Int, second: Int = 0) {
        validateTime(hour, minute, second)
        this.hour = hour
        this.minute = minute
        this.second = second
        calculateSeconds()
    }

    /** Sets the SI day-of-week field and recalculates absolute seconds. */
    fun setDayOfWeek(newDayOfWeek: Int) {
        dayOfWeek = newDayOfWeek
        calculateSeconds()
    }

    /** Sets the SI week field and recalculates absolute seconds. */
    fun setWeek(newWeek: Int) {
        week = newWeek
        calculateSeconds()
    }

    /** Adds twelve hours, advancing day/week when the current time is in the afternoon. */
    fun addHalfDay() {
        if (hour >= 12) {
            dayOfWeek++
            adjustDayWeek()
        }
        setFromSeconds(seconds + 12 * 60 * 60)
    }

    /** Adds one SI day, rolling into the next week after day six. */
    fun addDay() {
        dayOfWeek++
        adjustDayWeek()
        calculateSeconds()
    }

    /** Adds a number of seconds and recalculates all derived components. */
    fun addSeconds(secondsToAdd: Long) {
        setFromSeconds(seconds + secondsToAdd)
    }

    /** Returns true when this value is equal to or later than another SI time. */
    fun isAtOrAfter(other: SportIdentTime): Boolean {
        return seconds >= other.seconds
    }

    /** Returns true when this value is later than another SI time. */
    fun isAfter(other: SportIdentTime): Boolean {
        return seconds > other.seconds
    }

    /** Compares absolute SI seconds, treating null as zero seconds for legacy parity. */
    fun compareTo(other: SportIdentTime?): Int {
        return seconds.compareTo(other?.seconds ?: 0)
    }

    private fun calculateSeconds() {
        seconds = week * SportIdentCodes.SECONDS_WEEK +
                dayOfWeek * SportIdentCodes.SECONDS_DAY +
                hour * 3600L +
                minute * 60L +
                second
    }

    private fun setFromSeconds(totalSeconds: Long) {
        seconds = totalSeconds
        hour = ((totalSeconds / 3600) % 24).toInt()
        minute = ((totalSeconds / 60) % 60).toInt()
        second = (totalSeconds % 60).toInt()
        week = (totalSeconds / SportIdentCodes.SECONDS_WEEK).toInt()
        dayOfWeek = ((totalSeconds / SportIdentCodes.SECONDS_DAY) % 7).toInt()
    }

    private fun adjustDayWeek() {
        if (dayOfWeek > 6) {
            week++
            dayOfWeek %= 7
        }
    }

    companion object {
        /** Parses the stable storage format emitted by toString: HH:mm:ss,day,week. */
        fun from(value: String): SportIdentTime {
            try {
                val split = value.split(",")
                val time = split[0].split(":")
                return SportIdentTime(
                    hour = time[0].toInt(),
                    minute = time[1].toInt(),
                    second = time[2].toInt(),
                    dayOfWeek = split[1].toInt(),
                    week = split[2].toInt()
                )
            } catch (e: Exception) {
                throw IllegalArgumentException("Error when parsing SI time", e)
            }
        }

        /** Returns signed elapsed seconds from start to end. */
        fun split(start: SportIdentTime, end: SportIdentTime): Long {
            return end.seconds - start.seconds
        }

        /** Returns absolute elapsed seconds between two SI times. */
        fun difference(start: SportIdentTime, end: SportIdentTime): Long {
            return kotlin.math.abs(end.seconds - start.seconds)
        }

        private fun validateTime(hour: Int, minute: Int, second: Int) {
            require(hour in 0..23) { "Invalid hour" }
            require(minute in 0..59) { "Invalid minute" }
            require(second in 0..59) { "Invalid second" }
        }
    }
}
