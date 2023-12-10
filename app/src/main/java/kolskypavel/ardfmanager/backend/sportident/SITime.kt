package kolskypavel.ardfmanager.backend.sportident

import java.io.Serializable
import java.time.Duration
import java.time.LocalTime

/**
 * Wrapper class for calculating the split times
 */
class SITime(
    private var time: LocalTime,
    private var dayOfWeek: Int = 0,
    private var week: Int = 0
) : Serializable {

    private var seconds: Long = 0


    constructor() : this(LocalTime.MIDNIGHT)

    fun calculateSeconds() {
        seconds += week * SIConstants.SECONDS_WEEK + dayOfWeek * SIConstants.SECONDS_DAY + time.toSecondOfDay()
    }

    override fun toString(): String {
        return "$time;$dayOfWeek;$week"
    }

    fun addHalfDay() {
        time = time.plusHours(12)
        calculateSeconds()
    }

    //Getters and setters
    fun getTime() = time
    fun getDayOfWeek() = dayOfWeek
    fun getWeek() = week

    fun setTime(newTime: LocalTime) {
        time = newTime
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

    companion object {
        @Throws(IllegalArgumentException::class)
        fun from(string: String): SITime {
            try {
                val split = string.split(";")
                val time = LocalTime.parse(split[0])
                val dayOfWeek = split[1].toInt()
                val week = split[2].toInt()

                return SITime(time, dayOfWeek, week)

            } catch (e: Exception) {
                throw java.lang.IllegalArgumentException("Error when parsing SI time")
            }
        }

        fun split(start: SITime, end: SITime): Duration {
            return Duration.ofSeconds(end.seconds - start.seconds)
        }

        fun difference(start: SITime, end: SITime): Duration {
            return Duration.ofSeconds(kotlin.math.abs(end.seconds - start.seconds))
        }
    }
}