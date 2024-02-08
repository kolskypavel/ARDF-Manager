package kolskypavel.ardfmanager.backend.room

import android.content.Context
import androidx.room.Room
import kolskypavel.ardfmanager.backend.room.database.CategoryDatabase
import kolskypavel.ardfmanager.backend.room.database.CompetitorDatabase
import kolskypavel.ardfmanager.backend.room.database.ControlPointDatabase
import kolskypavel.ardfmanager.backend.room.database.EventDatabase
import kolskypavel.ardfmanager.backend.room.database.PunchDatabase
import kolskypavel.ardfmanager.backend.room.database.ReadoutDatabase
import kolskypavel.ardfmanager.backend.room.database.ResultDatabase
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result
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
            "result-database"
        )
        .build()

    private val punchDatabase: PunchDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            PunchDatabase::class.java,
            "punch-database"
        )
        .build()

    private val resultDatabase: ResultDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ResultDatabase::class.java,
            "result-database"
        )
        .build()

    //Events
    fun getEvents(): Flow<List<Event>> = eventDatabase.eventDao().getEvents()
    suspend fun getEvent(id: UUID): Event = eventDatabase.eventDao().getEvent(id)
    suspend fun createEvent(event: Event) = eventDatabase.eventDao().createEvent(event)
    suspend fun updateEvent(event: Event) = eventDatabase.eventDao().updateEvent(event)
    suspend fun deleteEvent(id: UUID) = eventDatabase.eventDao().deleteEvent(id)


    //Categories
    fun getCategoriesFlowForEvent(eventId: UUID): Flow<List<Category>> =
        categoryDatabase.categoryDao().getCategoriesFlowForEvent(eventId)

    fun getCategoriesForEvent(eventId: UUID): List<Category> =
        categoryDatabase.categoryDao().getCategoriesForEvent(eventId)

    suspend fun getCategory(id: UUID) = categoryDatabase.categoryDao().getCategory(id)
    suspend fun createCategory(category: Category) =
        categoryDatabase.categoryDao().createCategory(category)

    suspend fun updateCategory(category: Category) =
        categoryDatabase.categoryDao().updateCategory(category)

    suspend fun deleteCategory(id: UUID) = categoryDatabase.categoryDao().deleteCategory(id)

    suspend fun createControlPoint(cp: ControlPoint) =
        controlPointDatabase.controlPointDao().createControlPoint(cp)


    //Control point
    suspend fun getControlPointsByCategory(categoryId: UUID) =
        controlPointDatabase.controlPointDao().getControlPointsByCategory(categoryId)

    suspend fun getControlPointByName(eventId: UUID, name: String) =
        controlPointDatabase.controlPointDao().getControlPointByName(eventId, name)

    suspend fun deleteControlPointsByCategory(categoryId: UUID) =
        controlPointDatabase.controlPointDao().deleteControlPointsByCategory(categoryId)


    //Competitors
    suspend fun getCompetitor(id: UUID): Competitor =
        competitorDatabase.competitorDao().getCompetitor(id)

    suspend fun getCompetitorBySINumber(siNumber: Int, eventId: UUID): Competitor? =
        competitorDatabase.competitorDao().getCompetitorBySINumber(siNumber, eventId)

    fun getCompetitorsByEvent(eventId: UUID): Flow<List<Competitor>> =
        competitorDatabase.competitorDao().getCompetitorsByEvent(eventId)

    suspend fun getCompetitorsByCategory(categoryId: UUID) =
        competitorDatabase.competitorDao().getCompetitorsByCategory(categoryId)

    suspend fun createCompetitor(competitor: Competitor) =
        competitorDatabase.competitorDao().createCompetitor(competitor)

    suspend fun updateCompetitor(competitor: Competitor) =
        competitorDatabase.competitorDao().updateCompetitor(competitor)

    suspend fun deleteCompetitor(id: UUID) = competitorDatabase.competitorDao().deleteCompetitor(id)

    suspend fun checkIfSINumberExists(siNumber: Int, eventId: UUID): Int =
        competitorDatabase.competitorDao().checkIfSINumberExists(siNumber, eventId)

    //READOUTS
    suspend fun getReadoutsByEvent(eventId: UUID) =
        readoutDatabase.readoutDao().getReadoutsByEvent(eventId)

    suspend fun getReadoutBySINumber(siNumber: Int, eventId: UUID) =
        readoutDatabase.readoutDao().getReadoutForSINumber(siNumber, eventId)

    suspend fun getReadoutsByCompetitor(competitorId: UUID): Readout? =
        readoutDatabase.readoutDao().getReadoutByCompetitor(competitorId)

    suspend fun getReadouts(id: UUID) = readoutDatabase.readoutDao().getReadout(id)

    suspend fun getReadoutsByCategory(categoryId: UUID) =
        readoutDatabase.readoutDao().getReadoutByCategory(categoryId)

    suspend fun getReadoutsForNullCategory(eventId: UUID) =
        readoutDatabase.readoutDao().getReadoutsForNullCategory(eventId)

    suspend fun createReadout(readout: Readout) =
        readoutDatabase.readoutDao().createReadout(readout)

    suspend fun checkIfReadoutExistsById(siNumber: Int, eventId: UUID) =
        readoutDatabase.readoutDao().checkIfReadoutExistsById(siNumber, eventId)

    suspend fun deleteReadout(id: UUID) = readoutDatabase.readoutDao().deleteReadout(id)

    //PUNCHES
    suspend fun createPunch(punch: Punch) = punchDatabase.punchDao().createPunch(punch)

    suspend fun getPunchesByReadout(readoutId: UUID) =
        punchDatabase.punchDao().getPunchesByReadout(readoutId)

    suspend fun getPunchesByCompetitor(competitorId: UUID) =
        punchDatabase.punchDao().getPunchesByCompetitor(competitorId)

    suspend fun deletePunchesByReadoutId(resultId: UUID) =
        punchDatabase.punchDao().deletePunchesByReadoutId(resultId)

    suspend fun deletePunchesByEvent(eventId: UUID) =
        punchDatabase.punchDao().deletePunchesByEvent(eventId)


    //Results
    suspend fun getResultsByCategory(categoryId: UUID) =
        resultDatabase.resultDao().getResultByCategory(categoryId)

    suspend fun getResultByCompetitor(competitorId: UUID) =
        resultDatabase.resultDao().getResultByCompetitor(competitorId)

    suspend fun createResult(result: Result) = resultDatabase.resultDao().createResult(result)

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