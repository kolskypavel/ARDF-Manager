package kolskypavel.ardfmanager.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.room.dao.ReadoutDao
import kolskypavel.ardfmanager.room.entitity.Readout

@Database(entities = [Readout::class], version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class ReadoutDatabase : RoomDatabase() {
    abstract fun ReadoutDao(): ReadoutDao
}