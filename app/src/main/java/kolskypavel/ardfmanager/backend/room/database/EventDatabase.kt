package kolskypavel.ardfmanager.backend.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.backend.room.dao.EventDao
import kolskypavel.ardfmanager.backend.room.entitity.Event

@Database(entities = [Event::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeTypeConverter::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}