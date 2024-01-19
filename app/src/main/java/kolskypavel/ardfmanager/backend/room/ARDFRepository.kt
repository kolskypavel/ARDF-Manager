package kolskypavel.ardfmanager.backend.room

import android.content.Context
import androidx.room.Room
import kolskypavel.ardfmanager.backend.room.database.CategoryDatabase
import kolskypavel.ardfmanager.backend.room.database.CompetitorDatabase
import kolskypavel.ardfmanager.backend.room.database.ControlPointDatabase
import kolskypavel.ardfmanager.backend.room.database.EventDatabase
import kolskypavel.ardfmanager.backend.room.database.PunchDatabase
import kolskypavel.ardfmanager.backend.room.database.ResultDatabase
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
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

    private val resultDatabase: ResultDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ResultDatabase::class.java,
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

    //Events
    fun getEvents(): Flow<List<Event>> = eventDatabase.eventDao().getEvents()
    suspend fun getEvent(id: UUID): Event = eventDatabase.eventDao().getEvent(id)
    suspend fun createEvent(event: Event) = eventDatabase.eventDao().createEvent(event)
    suspend fun updateEvent(event: Event) = eventDatabase.eventDao().updateEvent(event)
    suspend fun deleteEvent(id: UUID) = eventDatabase.eventDao().deleteEvent(id)


    //Categories
    fun getCategoriesForEvent(eventId: UUID): Flow<List<Category>> =
        categoryDatabase.categoryDao().getCategoriesForEvent(eventId)

    suspend fun getCategory(id: UUID) = categoryDatabase.categoryDao().getCategory(id)
    suspend fun createCategory(category: Category) =
        categoryDatabase.categoryDao().createCategory(category)

    suspend fun updateCategory(category: Category) =
        categoryDatabase.categoryDao().updateCategory(category)

    suspend fun deleteCategory(id: UUID) = categoryDatabase.categoryDao().deleteCategory(id)

    suspend fun deleteCategoriesByEvent(eventId: UUID) =
        categoryDatabase.categoryDao().deleteCategoriesByEvent(eventId)

    suspend fun createControlPoint(cp: ControlPoint) =
        controlPointDatabase.controlPointDao().createControlPoint(cp)


    //Control point
    suspend fun getControlPointsByCategory(categoryId: UUID) =
        controlPointDatabase.controlPointDao().getControlPointsByCategory(categoryId)

    suspend fun deleteControlPointsByCategory(categoryId: UUID) =
        controlPointDatabase.controlPointDao().deleteControlPointsByCategory(categoryId)

    suspend fun deleteControlPointsByEvent(eventId: UUID) =
        controlPointDatabase.controlPointDao().deleteControlPointsByEvent(eventId)


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

    suspend fun deleteCompetitorsByEvent(id: UUID) =
        competitorDatabase.competitorDao().deleteCompetitorsByEvent(id)

    suspend fun checkIfSINumberExists(siNumber: Int, eventId: UUID): Int =
        competitorDatabase.competitorDao().checkIfSINumberExists(siNumber, eventId)

    //READOUTS
    suspend fun getResultsByEvent(eventId: UUID) =
        resultDatabase.resultDao().getResultsByEvent(eventId)

    suspend fun getResultBySINumber(siNumber: Int, eventId: UUID) =
        resultDatabase.resultDao().getResultForSINumber(siNumber, eventId)

    suspend fun getResultByCompetitor(competitorId: UUID): Result? =
        resultDatabase.resultDao().getResultByCompetitor(competitorId)

    suspend fun getResult(id: UUID) = resultDatabase.resultDao().getResult(id)

    suspend fun createResult(result: Result) =
        resultDatabase.resultDao().createResult(result)

    suspend fun checkIfResultExistsById(siNumber: Int, eventId: UUID) =
        resultDatabase.resultDao().checkIfResultExistsById(siNumber, eventId)

    suspend fun deleteResult(id: UUID) = resultDatabase.resultDao().deleteResult(id)

    suspend fun deleteResultsByEvent(eventId: UUID) =
        resultDatabase.resultDao().deleteResultsByEvent(eventId)

    //PUNCHES
    suspend fun createPunch(punch: Punch) = punchDatabase.punchDao().createPunch(punch)

    suspend fun getPunchesByResult(resultId: UUID) =
        punchDatabase.punchDao().getPunchesByResult(resultId)

    suspend fun getPunchesByCompetitor(competitorId: UUID) =
        punchDatabase.punchDao().getPunchesByCompetitor(competitorId)

    suspend fun deletePunchesByResultId(resultId: UUID) =
        punchDatabase.punchDao().deletePunchesByResultId(resultId)

    suspend fun deletePunchesByEvent(eventId: UUID) =
        punchDatabase.punchDao().deletePunchesByEvent(eventId)


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