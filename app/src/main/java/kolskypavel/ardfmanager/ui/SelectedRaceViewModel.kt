package kolskypavel.ardfmanager.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.wrappers.ResultDisplayWrapper
import kolskypavel.ardfmanager.backend.wrappers.StatisticsWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

/**
 * Represents the current selected race and its data - properties, categories, competitors and readouts
 */
class SelectedRaceViewModel : ViewModel() {
    private val dataProcessor = DataProcessor.get()
    private val _race = MutableLiveData<Race>()

    val race: LiveData<Race> get() = _race
    private val _categories: MutableStateFlow<List<CategoryData>> = MutableStateFlow(emptyList())
    val categories: StateFlow<List<CategoryData>> get() = _categories.asStateFlow()


    private val _readoutData: MutableStateFlow<List<ReadoutData>> =
        MutableStateFlow(emptyList())
    val readoutData: StateFlow<List<ReadoutData>> get() = _readoutData.asStateFlow()


    private val _resultData: MutableStateFlow<List<ResultDisplayWrapper>> =
        MutableStateFlow(emptyList())
    val resultData: StateFlow<List<ResultDisplayWrapper>> get() = _resultData.asStateFlow()

    private val _competitorData: MutableStateFlow<List<CompetitorData>> =
        MutableStateFlow(emptyList())
    val competitorData: StateFlow<List<CompetitorData>>
        get() =
            _competitorData.asStateFlow()

    fun getCurrentRace(): Race {
        if (race.value != null) {
            return race.value!!
        }
        throw IllegalStateException("Race value accessed without race set")
    }

    /**
     * Updates the current selected race and corresponding data
     */
    fun setRace(id: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            val race = dataProcessor.setCurrentRace(id)
            _race.postValue(race)

            launch {
                dataProcessor.getCategoryDataFlowForRace(id).collect {
                    _categories.value = it
                }
            }
            launch {
                dataProcessor.getCompetitorDataFlowByRace(id).collect {
                    _competitorData.value = it
                }
            }

            launch {
                dataProcessor.getReadoutDataByRace(id).collect {
                    _readoutData.value = it
                }
            }
            launch {
                dataProcessor.getResultDataByRace(id).collect {
                    _resultData.value = it
                }
            }
        }
    }

    fun updateRace(race: Race) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.updateRace(race)
            _race.postValue(race)
        }
    }

    //Category
    suspend fun getCategory(id: UUID) = dataProcessor.getCategory(id)

    fun getCategories(): List<Category> = categories.value.map { it.category }
    fun getCategoryByName(string: String): Category? {
        return runBlocking {
            return@runBlocking dataProcessor.getCategoryByName(string, getCurrentRace().id)
        }
    }

    fun getCategoryByMaxAge(maxAge: Int): Category? {
        return runBlocking {
            return@runBlocking dataProcessor.getCategoryByMaxAge(maxAge, getCurrentRace().id)
        }
    }

    fun getHighestCategoryOrder(raceId: UUID): Int {
        return runBlocking {
            return@runBlocking dataProcessor.getHighestCategoryOrder(raceId)
        }
    }

    fun createCategory(category: Category, controlPoints: List<ControlPoint>) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.createCategory(category, controlPoints)
        }
    }

    fun updateCategory(category: Category, controlPoints: List<ControlPoint>?) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.updateCategory(category, controlPoints)
        }

    fun duplicateCategory(categoryData: CategoryData) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.duplicateCategory(categoryData)
        }
    }

    fun deleteCategory(categoryId: UUID, raceId: UUID) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.deleteCategory(
                categoryId,
                raceId
            )
        }


    fun getControlPointsByCategory(categoryId: UUID): ArrayList<ControlPoint> {
        val controlPoints =
            runBlocking {
                ArrayList(dataProcessor.getControlPointsByCategory(categoryId))
            }

        //Add the first control point
        controlPoints.add(
            0,
            ControlPoint(
                UUID.randomUUID(),
                dataProcessor.getCurrentRace().id,
                categoryId,
                -1,
                0, null, 0, 1,
                beacon = false, separator = false
            )
        )
        return controlPoints
    }

    //TODO: Complete / remove
    fun checkIfControlPointNameExists(siCode: Int?, name: String): Boolean {
        runBlocking {
            dataProcessor.getControlPointByName(race.value!!.id, name)
        }
        return false
    }

    fun adjustControlPoints(
        controlPoints: ArrayList<ControlPoint>,
        raceType: RaceType
    ) = dataProcessor.adjustControlPoints(controlPoints, raceType)


    //Competitor
    fun getCompetitors(): List<Competitor> =
        competitorData.value.map { it.competitorCategory.competitor }

    fun getCompetitor(id: UUID) = runBlocking {
        return@runBlocking dataProcessor.getCompetitor(id)
    }

    fun createOrUpdateCompetitor(competitor: Competitor) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.createOrUpdateCompetitor(competitor)
        }

    fun deleteCompetitor(competitorId: UUID, deleteReadout: Boolean) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.deleteCompetitor(
                competitorId,
                deleteReadout
            )
        }

    fun deleteAllCompetitors() =
        CoroutineScope(Dispatchers.IO).launch {
            race.value?.let {
                dataProcessor.deleteAllCompetitors(
                    it.id
                )
            }
        }

    suspend fun getStatistics(raceId: UUID): StatisticsWrapper =
        dataProcessor.getStatisticsByRace(raceId)

    /**
     * Checks if the SI number is unique
     */
    fun checkIfSINumberExists(siNumber: Int): Boolean {
        if (race.value != null) {
            return dataProcessor.checkIfSINumberExists(siNumber, race.value!!.id)
        }
        return true
    }

    fun checkIfStartNumberExists(siNumber: Int): Boolean {
        if (race.value != null) {
            return dataProcessor.checkIfStartNumberExists(siNumber, race.value!!.id)
        }
        return true
    }

    fun getLastReadCard() = dataProcessor.getLastReadCard()

    fun processManualPunches(
        readout: Readout,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) = CoroutineScope(Dispatchers.IO).launch {
        dataProcessor.processManualPunches(readout, punches, manualStatus)
    }

    fun getReadoutBySINumber(siNumber: Int, raceId: UUID) =
        runBlocking {
            return@runBlocking dataProcessor.getReadoutBySINumber(siNumber, raceId)
        }

    fun getReadoutByCompetitor(competitorId: UUID) = runBlocking {
        return@runBlocking dataProcessor.getReadoutByCompetitor(competitorId)
    }

    fun deleteReadout(id: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.deleteReadout(id)
        }
    }

    fun getPunchesByReadout(readoutId: UUID): List<Punch> {
        return runBlocking {
            return@runBlocking dataProcessor.getPunchesByReadout(readoutId)
        }
    }

    fun importCompetitors() {

    }
}