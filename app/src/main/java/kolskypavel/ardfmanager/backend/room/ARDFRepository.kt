package kolskypavel.ardfmanager.backend.room

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import kolskypavel.ardfmanager.backend.room.database.EventDatabase
import kolskypavel.ardfmanager.backend.room.entitity.Alias
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
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

    //Races
    fun getRaces(): Flow<List<Race>> = eventDatabase.raceDao().getRaces()
    suspend fun getRace(id: UUID): Race = eventDatabase.raceDao().getRace(id)
    suspend fun createRace(race: Race) = eventDatabase.raceDao().createRace(race)
    suspend fun updateRace(race: Race) = eventDatabase.raceDao().updateRace(race)
    suspend fun deleteRace(id: UUID) = eventDatabase.raceDao().deleteRace(id)


    //Aliases
    suspend fun createAlias(alias: Alias) = eventDatabase.aliasDao().createOrUpdateAlias(alias)

    suspend fun getAliasesByRace(raceId: UUID) =
        eventDatabase.aliasDao().getAliasesByRace(raceId)

    //Categories
    fun getCategoryDataFlowForRace(raceId: UUID) =
        eventDatabase.categoryDao().getCategoryFlowForRace(raceId)

    suspend fun getCategoriesForRace(raceId: UUID): List<Category> =
        eventDatabase.categoryDao().getCategoriesForRace(raceId)

    suspend fun getCategory(id: UUID) =
        eventDatabase.categoryDao().getCategory(id)

    suspend fun getCategoryData(id: UUID, raceId: UUID) =
        eventDatabase.categoryDao().getCategoryData(id, raceId)

    suspend fun getCategoryDataForRace(raceId: UUID) =
        eventDatabase.categoryDao().getCategoryDataForRace(raceId)

    suspend fun getHighestCategoryOrder(raceId: UUID) =
        eventDatabase.categoryDao().getHighestCategoryOrder(raceId)

    suspend fun getCategoryByName(name: String, raceId: UUID) =
        eventDatabase.categoryDao().getCategoryByName(name, raceId)

    suspend fun getCategoryByMaxAge(maxAge: Int, raceId: UUID) =
        eventDatabase.categoryDao().getCategoryByMaxAge(maxAge, raceId)

    suspend fun getCategoryByBirthYear(birthYear: Int, woman: Boolean, raceId: UUID): Category? =
        eventDatabase.categoryDao().getCategoryByAge(birthYear, woman, raceId)

    suspend fun createOrUpdateCategory(category: Category) =
        eventDatabase.categoryDao().createOrUpdateCategory(category)

    suspend fun deleteCategory(id: UUID) = eventDatabase.categoryDao().deleteCategory(id)

    suspend fun createControlPoint(cp: ControlPoint) =
        eventDatabase.controlPointDao().createControlPoint(cp)


    //Control point
    suspend fun getControlPointsByCategory(categoryId: UUID) =
        eventDatabase.controlPointDao().getControlPointsByCategory(categoryId)

    suspend fun getControlPointByCode(raceId: UUID, code: Int) =
        eventDatabase.controlPointDao().getControlPointByCode(raceId, code)

    suspend fun deleteControlPointsByCategory(categoryId: UUID) =
        eventDatabase.controlPointDao().deleteControlPointsByCategory(categoryId)

    //Aliases
    suspend fun createOrUpdateAlias(aliass: Alias) =
        eventDatabase.aliasDao().createOrUpdateAlias(aliass)

    //Competitors
    suspend fun getCompetitor(id: UUID) =
        eventDatabase.competitorDao().getCompetitor(id)

    suspend fun getCompetitorBySINumber(siNumber: Int, raceId: UUID): Competitor? =
        eventDatabase.competitorDao().getCompetitorBySINumber(siNumber, raceId)

    suspend fun getHighestStartNumberByRace(raceId: UUID) =
        eventDatabase.competitorDao().getHighestStartNumberByRace(raceId)

    fun getCompetitorDataFlowByRace(raceId: UUID): Flow<List<CompetitorData>> =
        eventDatabase.competitorDao().getCompetitorDataFlow(raceId)

    suspend fun getCompetitorDataByRace(raceId: UUID): List<CompetitorData> =
        eventDatabase.competitorDao().getCompetitorData(raceId)

    suspend fun getCompetitorsByCategory(categoryId: UUID) =
        eventDatabase.competitorDao().getCompetitorsByCategory(categoryId)

    suspend fun createCompetitor(competitor: Competitor) =
        eventDatabase.competitorDao().createCompetitor(competitor)

    suspend fun deleteCompetitor(id: UUID) = eventDatabase.competitorDao().deleteCompetitor(id)

    suspend fun deleteAllCompetitorsByRace(raceId: UUID) =
        eventDatabase.competitorDao().deleteAllCompetitorsByRace(raceId)

    suspend fun checkIfSINumberExists(siNumber: Int, raceId: UUID): Int =
        eventDatabase.competitorDao().checkIfSINumberExists(siNumber, raceId)

    suspend fun checkIfStartNumberExists(startNumber: Int, raceId: UUID): Int =
        eventDatabase.competitorDao().checkIfStartNumberExists(startNumber, raceId)


    //READOUTS
    fun getReadoutDataByRace(raceId: UUID) =
        eventDatabase.readoutDao().getReadoutDataByRace(raceId)

    suspend fun getReadoutDataByReadout(readoutId: UUID): ReadoutData? =
        eventDatabase.readoutDao().getReadoutDataByReadout(readoutId)

    suspend fun getReadoutBySINumber(siNumber: Int, raceId: UUID) =
        eventDatabase.readoutDao().getReadoutForSINumber(siNumber, raceId)

    suspend fun getReadoutsByCompetitor(competitorId: UUID): Readout? =
        eventDatabase.readoutDao().getReadoutByCompetitor(competitorId)

    suspend fun createReadout(readout: Readout) =
        eventDatabase.readoutDao().createOrUpdateReadout(readout)

    suspend fun checkIfReadoutExistsById(siNumber: Int, raceId: UUID) =
        eventDatabase.readoutDao().checkIfReadoutExistsById(siNumber, raceId)

    suspend fun deleteReadout(id: UUID) = eventDatabase.readoutDao().deleteReadout(id)

    suspend fun deleteReadoutForCompetitor(competitorId: UUID) =
        eventDatabase.readoutDao().deleteReadoutByCompetitor(competitorId)

    suspend fun deleteAllReadoutsByRace(raceId: UUID) =
        eventDatabase.readoutDao().deleteAllReadoutsByRace(raceId)


    //PUNCHES
    suspend fun createPunch(punch: Punch) = eventDatabase.punchDao().createOrUpdatePunch(punch)

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


    suspend fun createResult(result: Result) =
        eventDatabase.resultDao().createOrUpdateResult(result)

    suspend fun saveReadoutAndResult(
        readout: Readout,
        punches: ArrayList<Punch>,
        result: Result
    ) {
        eventDatabase.withTransaction {
            eventDatabase.readoutDao().createOrUpdateReadout(readout)
            eventDatabase.punchDao().deletePunchesByReadout(readout.id)
            punches.forEach { punch -> eventDatabase.punchDao().createOrUpdatePunch(punch) }
            eventDatabase.resultDao().createOrUpdateResult(result)
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