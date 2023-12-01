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

    fun difference(siTime: SITime): Duration {
        return Duration.ZERO
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
    }
}