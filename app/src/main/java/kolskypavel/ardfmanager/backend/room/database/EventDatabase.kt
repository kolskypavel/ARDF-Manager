package kolskypavel.ardfmanager.backend.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.backend.room.dao.AliasDao
import kolskypavel.ardfmanager.backend.room.dao.CategoryDao
import kolskypavel.ardfmanager.backend.room.dao.CompetitorDao
import kolskypavel.ardfmanager.backend.room.dao.ControlPointDao
import kolskypavel.ardfmanager.backend.room.dao.PunchDao
import kolskypavel.ardfmanager.backend.room.dao.RaceDao
import kolskypavel.ardfmanager.backend.room.dao.ResultDao
import kolskypavel.ardfmanager.backend.room.entitity.Alias
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.Result

@Database(
    entities = [Race::class,
        Category::class,
        Alias::class,
        Competitor::class,
        ControlPoint::class,
        Punch::class,
        Result::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeTypeConverter::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun raceDao(): RaceDao
    abstract fun aliasDao(): AliasDao
    abstract fun categoryDao(): CategoryDao
    abstract fun competitorDao(): CompetitorDao
    abstract fun controlPointDao(): ControlPointDao
    abstract fun punchDao(): PunchDao
    abstract fun resultDao(): ResultDao
}