package kolskypavel.ardfmanager.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.room.dao.PunchDao
import kolskypavel.ardfmanager.room.entitity.Punch

@Database(entities = [Punch::class], version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class PunchDatabase : RoomDatabase() {
    abstract fun punchDao(): PunchDao
}