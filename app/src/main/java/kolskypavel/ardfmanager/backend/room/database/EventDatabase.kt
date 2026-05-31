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
import kolskypavel.ardfmanager.backend.room.dao.ResultServiceDao
import kolskypavel.ardfmanager.backend.room.entity.Alias
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.entity.ResultService

/** Room database that stores all Android event administration data. */
@Database(
    entities = [Race::class,
        Category::class,
        Alias::class,
        Competitor::class,
        ControlPoint::class,
        Punch::class,
        Result::class,
        ResultService::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateTimeTypeConverter::class)
abstract class EventDatabase : RoomDatabase() {
    /** DAO for race records. */
    abstract fun raceDao(): RaceDao
    /** DAO for control-point aliases. */
    abstract fun aliasDao(): AliasDao
    /** DAO for category and course records. */
    abstract fun categoryDao(): CategoryDao
    /** DAO for competitor records. */
    abstract fun competitorDao(): CompetitorDao
    /** DAO for category control points. */
    abstract fun controlPointDao(): ControlPointDao
    /** DAO for SportIdent punches. */
    abstract fun punchDao(): PunchDao
    /** DAO for SI readout results. */
    abstract fun resultDao(): ResultDao
    /** DAO for live-result service settings. */
    abstract fun resultServiceDao(): ResultServiceDao
}
