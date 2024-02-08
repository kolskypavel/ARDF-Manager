package kolskypavel.ardfmanager.backend.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import kolskypavel.ardfmanager.backend.room.dao.ControlPointDao
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Event

@Database(
    entities = [ControlPoint::class, Category::class, Event::class],
    version = 1,
    exportSchema = false
)
abstract class ControlPointDatabase : RoomDatabase() {
    abstract fun controlPointDao(): ControlPointDao
}