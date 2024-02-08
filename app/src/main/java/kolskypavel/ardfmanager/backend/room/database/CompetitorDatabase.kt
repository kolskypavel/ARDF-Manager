package kolskypavel.ardfmanager.backend.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.backend.room.dao.CompetitorDao
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Event

@Database(
    entities = [Competitor::class, Category::class, Event::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeTypeConverter::class)
abstract class CompetitorDatabase : RoomDatabase() {
    abstract fun competitorDao(): CompetitorDao
}