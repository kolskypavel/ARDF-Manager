package kolskypavel.ardfmanager.backend.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.backend.room.dao.ReadoutDao
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Readout

@Database(entities = [Readout::class, Event::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeTypeConverter::class)
abstract class ReadoutDatabase : RoomDatabase() {
    abstract fun readoutDao(): ReadoutDao
}