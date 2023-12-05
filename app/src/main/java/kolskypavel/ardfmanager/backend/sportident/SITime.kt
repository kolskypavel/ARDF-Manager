package kolskypavel.ardfmanager.backend.sportident

import java.io.Serializable
import java.time.Duration
import java.time.LocalTime

/**
 * Wrapper class for calculating the split times
 */
class SITime(
    var time: LocalTime,
    var dayOfWeek: Int = 0,
    var week: Int = 0
) : Serializable {

    constructor() : this(LocalTime.MIDNIGHT)

    override fun toString(): String {
        return "$time;$dayOfWeek;$week"
    }

    fun addHalfDay() {
        time = time.plusHours(12)
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

        fun split(time1: SITime, time2: SITime): Duration {
            if (time1.week == time2.week) {
                if (time1.dayOfWeek == time2.dayOfWeek) {
                    return Duration.between(time2.time, time1.time)
                }

            } else if (time1.week > time2.week) {

            } else {

            }
            return Duration.ZERO
        }

        fun difference(time1: SITime, time2: SITime): Duration {
            //  return abs(split(time1, time2))
            return Duration.ZERO
        }
    }
}