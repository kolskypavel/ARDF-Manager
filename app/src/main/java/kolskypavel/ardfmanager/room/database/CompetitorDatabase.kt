package kolskypavel.ardfmanager.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.room.dao.CompetitorDao
import kolskypavel.ardfmanager.room.entitity.Competitor

@Database(entities = [Competitor::class], version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class CompetitorDatabase : RoomDatabase() {
    abstract fun CompetitorDao(): CompetitorDao
}