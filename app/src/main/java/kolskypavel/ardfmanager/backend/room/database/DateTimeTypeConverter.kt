package kolskypavel.ardfmanager.backend.room.database

import androidx.room.TypeConverter
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Room type converters for Java time types and SportIdent time values. */
class DateTimeTypeConverter {
    /** Converts a local date-time to its ISO-8601 string form. */
    @TypeConverter
    fun fromDateTime(date: LocalDateTime): String {
        return date.toString()
    }

    /** Parses a local date-time from its ISO-8601 string form. */
    @TypeConverter
    fun toDateTime(stringDate: String): LocalDateTime {
        return LocalDateTime.parse(stringDate)
    }

    /** Parses a local time from its ISO-8601 string form. */
    @TypeConverter
    fun fromLocalTime(stringTime: String): LocalTime {
        return LocalTime.parse(stringTime)
    }

    /** Converts a local time to its ISO-8601 string form. */
    @TypeConverter
    fun toLocalTime(time: LocalTime): String {
        return time.toString()
    }

    /** Converts a local date to its ISO-8601 string form. */
    @TypeConverter
    fun fromDate(date: LocalDate): String {
        return date.toString()
    }

    /** Parses a local date from its ISO-8601 string form. */
    @TypeConverter
    fun toDate(stringDate: String): LocalDate {
        return LocalDate.parse(stringDate)
    }

    /** Converts a duration to its ISO-8601 string form. */
    @TypeConverter
    fun fromDuration(duration: Duration): String {
        return duration.toString()
    }

    /** Parses a duration from its ISO-8601 string form. */
    @TypeConverter
    fun toDuration(stringDuration: String): Duration {
        return Duration.parse(stringDuration)
    }

    /** Converts SportIdent time to its serialized string form. */
    @TypeConverter
    fun fromSITime(siTime: SITime): String {
        return siTime.toString()
    }

    /** Parses SportIdent time from its serialized string form. */
    @TypeConverter
    fun toSITime(string: String): SITime {
        return SITime.from(string)
    }

}
