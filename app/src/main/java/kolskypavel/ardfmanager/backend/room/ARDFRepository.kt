package kolskypavel.ardfmanager.backend.room

import android.content.Context
import androidx.room.Room
import kolskypavel.ardfmanager.backend.room.database.CategoryDatabase
import kolskypavel.ardfmanager.backend.room.database.CompetitorDatabase
import kolskypavel.ardfmanager.backend.room.database.ControlPointDatabase
import kolskypavel.ardfmanager.backend.room.database.EventDatabase
import kolskypavel.ardfmanager.backend.room.database.PunchDatabase
import kolskypavel.ardfmanager.backend.room.database.ReadoutDatabase
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ARDFRepository private constructor(context: Context) {

    private val eventDatabase: EventDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            EventDatabase::class.java,
            "event-database"
        )
        .build()

    private val competitorDatabase: CompetitorDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            CompetitorDatabase::class.java,
            "competitor-database"
        )
        .build()

    private val categoryDatabase: CategoryDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            CategoryDatabase::class.java,
            "category-database"
        )
        .build()

    private val controlPointDatabase: ControlPointDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ControlPointDatabase::class.java,
            "control-point-database"
        )
        .build()

    private val readoutDatabase: ReadoutDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ReadoutDatabase::class.java,
            "readout-database"
        )
        .build()

    private val punchDatabase: PunchDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            PunchDatabase::class.java,
            "punch-database"
        )
        .build()

    //Events
    fun getEvents(): Flow<List<Event>> = eventDatabase.eventDao().getEvents()
    fun getEvent(id: UUID): Event = eventDatabase.eventDao().getEvent(id)
    suspend fun createEvent(event: Event) = eventDatabase.eventDao().createEvent(event)
    suspend fun updateEvent(event: Event) = eventDatabase.eventDao().updateEvent(event)
    suspend fun deleteEvent(id: UUID) {
        eventDatabase.eventDao().deleteEvent(id)
        competitorDatabase.competitorDao().deleteCompetitorsByEvent(id)
        categoryDatabase.categoryDao().deleteCategoriesByEvent(id)
        readoutDatabase.readoutDao().deleteReadoutsByEvent(id)
        punchDatabase.punchDao().deletePunchesByEvent(id)
    }

    //Categories
    fun getCategoriesForEvent(eventId: UUID): Flow<List<Category>> =
        categoryDatabase.categoryDao().getCategoriesForEvent(eventId)

    suspend fun getCategory(id: UUID) = categoryDatabase.categoryDao().getCategory(id)
    suspend fun createCategory(category: Category) =
        categoryDatabase.categoryDao().createCategory(category)

    suspend fun updateCategory(category: Category) =
        categoryDatabase.categoryDao().updateCategory(category)

    suspend fun deleteCategory(id: UUID) = categoryDatabase.categoryDao().deleteCategory(id)

    //Control point
    suspend fun getControlPointsForCategory(categoryId: UUID) =
        controlPointDatabase.controlPointDao().getControlPointsForCategory(categoryId)

    suspend fun deleteControlPointsForCategory(categoryId: UUID) =
        controlPointDatabase.controlPointDao().getControlPointsForCategory(categoryId)

    //Competitors
    fun getCompetitor(id: UUID): Competitor = competitorDatabase.competitorDao().getCompetitor(id)

    suspend fun getCompetitorBySINumber(siNumber: Int, eventId: UUID): Competitor? =
        competitorDatabase.competitorDao().getCompetitorBySINumber(siNumber, eventId)

    fun getCompetitorsForEvent(eventId: UUID): Flow<List<Competitor>> =
        competitorDatabase.competitorDao().getCompetitorsForEvent(eventId)

    suspend fun createCompetitor(competitor: Competitor) =
        competitorDatabase.competitorDao().createCompetitor(competitor)

    suspend fun updateCompetitor(competitor: Competitor) =
        competitorDatabase.competitorDao().updateCompetitor(competitor)

    suspend fun deleteCompetitor(id: UUID) = competitorDatabase.competitorDao().deleteCompetitor(id)

    suspend fun checkIfSINumberExists(siNumber: Int, eventId: UUID): Int =
        competitorDatabase.competitorDao().checkIfSINumberExists(siNumber, eventId)

    //Readouts
    suspend fun getReadoutsForEvent(eventId: UUID) =
        readoutDatabase.readoutDao().getReadoutsForEvent(eventId)

    fun getReadoutBySINumber(siNumber: Int, eventId: UUID) =
        readoutDatabase.readoutDao().getReadoutsForSINumber(siNumber, eventId)

    fun getReadout(id: UUID) = readoutDatabase.readoutDao().getReadout(id)

    fun createReadout(readout: Readout) =
        readoutDatabase.readoutDao().createReadout(readout)

    fun checkIfReadoutExistsById(siNumber: Int, eventId: UUID) =
        readoutDatabase.readoutDao().checkIfReadoutExistsById(siNumber, eventId)

    fun deleteReadout(id: UUID) = readoutDatabase.readoutDao().deleteReadout(id)

    //PUNCHES
    fun createPunch(punch: Punch) = punchDatabase.punchDao().createPunch(punch)

    suspend fun getPunchesByCompetitor(competitorId: UUID) =
        punchDatabase.punchDao().getPunchesForCompetitor(competitorId)

    suspend fun getPunchesBySINumber(siNumber: Int, eventId: UUID) =
        punchDatabase.punchDao().getPunchesForSINumber(siNumber, eventId)

    fun deletePunchesByReadoutId(readoutId: UUID) =
        punchDatabase.punchDao().deletePunchesByReadoutId(readoutId)


    //Results


    //Singleton instantiation
    companion object {
        private var INSTANCE: ARDFRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE =
                    ARDFRepository(context)
            }
        }

        fun get(): ARDFRepository {
            return INSTANCE ?: throw IllegalStateException("ARDFRepository must be initialized")
        }
    }
}