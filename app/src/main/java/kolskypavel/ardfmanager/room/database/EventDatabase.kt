package kolskypavel.ardfmanager.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.room.dao.EventDao
import kolskypavel.ardfmanager.room.entitity.Event

@Database(entities = [Event::class], version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}