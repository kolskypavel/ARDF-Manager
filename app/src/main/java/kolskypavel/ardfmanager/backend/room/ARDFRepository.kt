package kolskypavel.ardfmanager.backend.room

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import kolskypavel.ardfmanager.backend.room.database.EventDatabase
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
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

    //Events
    fun getEvents(): Flow<List<Event>> = eventDatabase.eventDao().getEvents()
    suspend fun getEvent(id: UUID): Event = eventDatabase.eventDao().getEvent(id)
    suspend fun createEvent(event: Event) = eventDatabase.eventDao().createEvent(event)
    suspend fun updateEvent(event: Event) = eventDatabase.eventDao().updateEvent(event)
    suspend fun deleteEvent(id: UUID) = eventDatabase.eventDao().deleteEvent(id)


    //Categories
    fun getCategoryDataFlowForEvent(eventId: UUID) =
        eventDatabase.categoryDao().getCategoryFlowForEvent(eventId)

    fun getCategoriesForEvent(eventId: UUID): List<Category> =
        eventDatabase.categoryDao().getCategoriesForEvent(eventId)

    suspend fun getCategory(id: UUID) =
        eventDatabase.categoryDao().getCategory(id)

    suspend fun getCategoryData(id: UUID, eventId: UUID) =
        eventDatabase.categoryDao().getCategoryData(id, eventId)

    suspend fun getHighestCategoryOrder(eventId: UUID) =
        eventDatabase.categoryDao().getHighestCategoryOrder(eventId)

    suspend fun getCategoryByName(name: String, eventId: UUID) =
        eventDatabase.categoryDao().getCategoryByName(name, eventId)

    suspend fun getCategoryByMaxAge(maxAge: Int, eventId: UUID) =
        eventDatabase.categoryDao().getCategoryByMaxAge(maxAge, eventId)

    suspend fun getCategoryByBirthYear(birthYear: Int, woman: Boolean, eventId: UUID): Category? =
        eventDatabase.categoryDao().getCategoryByAge(birthYear, woman, eventId)

    suspend fun createOrUpdateCategory(category: Category) =
        eventDatabase.categoryDao().createOrUpdateCategory(category)

    suspend fun deleteCategory(id: UUID) = eventDatabase.categoryDao().deleteCategory(id)

    suspend fun createControlPoint(cp: ControlPoint) =
        eventDatabase.controlPointDao().createControlPoint(cp)


    //Control point
    suspend fun getControlPointsByCategory(categoryId: UUID) =
        eventDatabase.controlPointDao().getControlPointsByCategory(categoryId)

    suspend fun getControlPointByName(eventId: UUID, name: String) =
        eventDatabase.controlPointDao().getControlPointByName(eventId, name)

    suspend fun getControlPointByCode(eventId: UUID, code: Int) =
        eventDatabase.controlPointDao().getControlPointByCode(eventId, code)

    suspend fun deleteControlPointsByCategory(categoryId: UUID) =
        eventDatabase.controlPointDao().deleteControlPointsByCategory(categoryId)


    //Competitors
    suspend fun getCompetitor(id: UUID) =
        eventDatabase.competitorDao().getCompetitor(id)

    suspend fun getCompetitorBySINumber(siNumber: Int, eventId: UUID): Competitor? =
        eventDatabase.competitorDao().getCompetitorBySINumber(siNumber, eventId)

    suspend fun getHighestStartNumberByEvent(eventId: UUID) =
        eventDatabase.competitorDao().getHighestStartNumberByEvent(eventId)

    fun getCompetitorDataFlowByEvent(eventId: UUID): Flow<List<CompetitorData>> =
        eventDatabase.competitorDao().getCompetitorDataFlow(eventId)

    suspend fun getCompetitorDataByEvent(eventId: UUID): List<CompetitorData> =
        eventDatabase.competitorDao().getCompetitorData(eventId)

    suspend fun getCompetitorsByCategory(categoryId: UUID) =
        eventDatabase.competitorDao().getCompetitorsByCategory(categoryId)

    suspend fun createCompetitor(competitor: Competitor) =
        eventDatabase.competitorDao().createCompetitor(competitor)

    suspend fun deleteCompetitor(id: UUID) = eventDatabase.competitorDao().deleteCompetitor(id)

    suspend fun deleteAllCompetitors(eventId: UUID) =
        eventDatabase.competitorDao().deleteAllCompetitors(eventId)

    suspend fun checkIfSINumberExists(siNumber: Int, eventId: UUID): Int =
        eventDatabase.competitorDao().checkIfSINumberExists(siNumber, eventId)

    suspend fun checkIfStartNumberExists(startNumber: Int, eventId: UUID): Int =
        eventDatabase.competitorDao().checkIfStartNumberExists(startNumber, eventId)


    //READOUTS
    fun getReadoutDataByEvent(eventId: UUID) =
        eventDatabase.readoutDao().getReadoutDataByEvent(eventId)

    suspend fun getReadoutBySINumber(siNumber: Int, eventId: UUID) =
        eventDatabase.readoutDao().getReadoutForSINumber(siNumber, eventId)

    suspend fun getReadoutsByCompetitor(competitorId: UUID): Readout? =
        eventDatabase.readoutDao().getReadoutByCompetitor(competitorId)

    suspend fun createReadout(readout: Readout) =
        eventDatabase.readoutDao().createReadout(readout)

    suspend fun checkIfReadoutExistsById(siNumber: Int, eventId: UUID) =
        eventDatabase.readoutDao().checkIfReadoutExistsById(siNumber, eventId)

    suspend fun deleteReadout(id: UUID) = eventDatabase.readoutDao().deleteReadout(id)

    suspend fun deleteReadoutForCompetitor(competitorId: UUID) =
        eventDatabase.readoutDao().deleteReadoutByCompetitor(competitorId)

    //PUNCHES
    suspend fun createPunch(punch: Punch) = eventDatabase.punchDao().createPunch(punch)

    suspend fun getPunchesByReadout(readoutId: UUID) =
        eventDatabase.punchDao().getPunchesByReadout(readoutId)

    suspend fun getPunchesByCompetitor(competitorId: UUID) =
        eventDatabase.punchDao().getPunchesByCompetitor(competitorId)


    //Results
    suspend fun getResultsByCategory(categoryId: UUID) =
        eventDatabase.resultDao().getResultByCategory(categoryId)

    suspend fun getResultByCompetitor(competitorId: UUID) =
        eventDatabase.resultDao().getResultByCompetitor(competitorId)

    suspend fun getResultByReadout(readoutId: UUID) =
        eventDatabase.resultDao().getResultByReadout(readoutId)


    suspend fun createResult(result: Result) = eventDatabase.resultDao().createResult(result)
    suspend fun saveReadoutAndResult(
        readout: Readout,
        punches: ArrayList<Punch>,
        result: Result
    ) {
        eventDatabase.withTransaction {
            eventDatabase.readoutDao().createReadout(readout)
            punches.forEach { punch -> eventDatabase.punchDao().createPunch(punch) }
            eventDatabase.resultDao().createResult(result)
        }
    }

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