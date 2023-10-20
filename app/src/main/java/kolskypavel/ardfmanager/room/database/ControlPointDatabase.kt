package kolskypavel.ardfmanager.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import kolskypavel.ardfmanager.room.dao.ControlPointDao
import kolskypavel.ardfmanager.room.entitity.ControlPoint

@Database(entities = [ControlPoint::class], version = 1, exportSchema = false)
abstract class ControlPointDatabase : RoomDatabase() {
    abstract fun controlPointDao(): ControlPointDao
}