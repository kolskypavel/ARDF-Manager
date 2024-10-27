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

    suspend fun createOrUpdateCategory(category: Category, controlPoints: List<ControlPoint>?) {
        eventDatabase.withTransaction {
            eventDatabase.categoryDao().createOrUpdateCategory(category)

            if (controlPoints != null) {
                deleteControlPointsByCategory(category.id)
                createControlPoints(controlPoints)
            }
        }
    }

    private suspend fun createControlPoints(controlPoints: List<ControlPoint>) {
        controlPoints.forEach { cp ->
            createControlPoint(cp)
        }
    }

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
    suspend fun createOrUpdateAlias(alias: Alias) =
        eventDatabase.aliasDao().createOrUpdateAlias(alias)

    suspend fun deleteAliasesByRace(raceId: UUID) =
        eventDatabase.aliasDao().deleteAliasesByRace(raceId)

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

    //RESULTS
    suspend fun getResult(id: UUID) = eventDatabase.resultDao().getResult(id)

    suspend fun getResultData(resultId:UUID) = eventDatabase.resultDao().getResultData(resultId)

    suspend fun getResultBySINumber(siNumber: Int, raceId: UUID) =
        eventDatabase.resultDao().getResultForSINumber(siNumber, raceId)

    suspend fun getReadoutsByCompetitor(competitorId: UUID): Result? =
        eventDatabase.resultDao().getResultByCompetitor(competitorId)

    suspend fun getResultsByCategory(categoryId: UUID) =
        eventDatabase.resultDao().getResultByCategory(categoryId)

    suspend fun getResultByCompetitor(competitorId: UUID) =
        eventDatabase.resultDao().getResultByCompetitor(competitorId)

    suspend fun createResult(result: Result) =
        eventDatabase.resultDao().createOrUpdateResult(result)

    suspend fun saveResultPunches(
        result: Result,
        punches: List<Punch>
    ) {
        eventDatabase.withTransaction {
            eventDatabase.punchDao().deletePunchesByResult(result.id)
            punches.forEach { punch -> eventDatabase.punchDao().createOrUpdatePunch(punch) }
            eventDatabase.resultDao().createOrUpdateResult(result)
        }
    }

    suspend fun deleteResult(id: UUID) = eventDatabase.resultDao().deleteResult(id)
    suspend fun deleteResultForCompetitor(competitorId: UUID) =
        eventDatabase.resultDao().deleteResultByCompetitor(competitorId)

    suspend fun deleteAllResultsByRace(raceId: UUID) =
        eventDatabase.resultDao().deleteAllResultsByRace(raceId)


    //PUNCHES
    suspend fun createPunch(punch: Punch) = eventDatabase.punchDao().createOrUpdatePunch(punch)

    suspend fun getPunchesByResult(resultId: UUID) =
        eventDatabase.punchDao().getPunchesByResult(resultId)

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