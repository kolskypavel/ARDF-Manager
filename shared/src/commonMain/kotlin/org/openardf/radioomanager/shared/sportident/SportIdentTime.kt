package org.openardf.radioomanager.shared.sportident

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

    fun getHour() = hour
    fun getMinute() = minute
    fun getSecond() = second
    fun getDayOfWeek() = dayOfWeek
    fun getWeek() = week
    fun getSeconds() = seconds

    fun getTimeString(): String {
        return "%02d:%02d:%02d".format(hour, minute, second)
    }

    override fun toString(): String {
        return "${getTimeString()},$dayOfWeek,$week"
    }

    fun setTime(hour: Int, minute: Int, second: Int = 0) {
        validateTime(hour, minute, second)
        this.hour = hour
        this.minute = minute
        this.second = second
        calculateSeconds()
    }

    fun setDayOfWeek(newDayOfWeek: Int) {
        dayOfWeek = newDayOfWeek
        calculateSeconds()
    }

    fun setWeek(newWeek: Int) {
        week = newWeek
        calculateSeconds()
    }

    fun addHalfDay() {
        if (hour >= 12) {
            dayOfWeek++
            adjustDayWeek()
        }
        setFromSeconds(seconds + 12 * 60 * 60)
    }

    fun addDay() {
        dayOfWeek++
        adjustDayWeek()
        calculateSeconds()
    }

    fun addSeconds(secondsToAdd: Long) {
        setFromSeconds(seconds + secondsToAdd)
    }

    fun isAtOrAfter(other: SportIdentTime): Boolean {
        return seconds >= other.seconds
    }

    fun isAfter(other: SportIdentTime): Boolean {
        return seconds > other.seconds
    }

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

        fun split(start: SportIdentTime, end: SportIdentTime): Long {
            return end.seconds - start.seconds
        }

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
