package kolskypavel.ardfmanager.room.database

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.LocalTime

class DateTypeConverter {
    @TypeConverter
    fun fromDateTime(date: LocalDateTime): String {
        return date.toString()
    }

    @TypeConverter
    fun toDateTime(stringDate: String): LocalDateTime {
        return LocalDateTime.parse(stringDate);
    }

    @TypeConverter
    fun fromLocalTime(stringTime: String): LocalTime {
        return LocalTime.parse(stringTime);
    }

    @TypeConverter
    fun toLocalTime(time: LocalTime): String {
        return time.toString()
    }
}