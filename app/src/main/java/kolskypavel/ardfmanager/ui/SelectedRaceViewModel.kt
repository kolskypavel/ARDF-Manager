package kolskypavel.ardfmanager.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.wrappers.ResultWrapper
import kolskypavel.ardfmanager.backend.wrappers.StatisticsWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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


    private val _resultData: MutableStateFlow<List<ResultWrapper>> =
        MutableStateFlow(emptyList())
    val resultData: StateFlow<List<ResultWrapper>> get() = _resultData.asStateFlow()

    private val _competitorData: MutableStateFlow<List<CompetitorData>> =
        MutableStateFlow(emptyList())
    val competitorData: StateFlow<List<CompetitorData>>
        get() =
            _competitorData.asStateFlow()

    @Throws(IllegalStateException::class)
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
                dataProcessor.getReadoutDataFlowByRace(id).collect {
                    _readoutData.value = it
                }
            }
            launch {
                dataProcessor.getResultDataFlowByRace(id).collect {
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

    fun removeReaderRace() = dataProcessor.removeReaderRace()

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
                type = ControlPointType.CONTROL, 0, 1
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

    fun deleteAllCompetitorsByRace() =
        CoroutineScope(Dispatchers.IO).launch {
            race.value?.let {
                dataProcessor.deleteAllCompetitorsByRace(
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

    suspend fun processManualPunches(
        readout: Readout,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) {
        dataProcessor.processManualPunches(readout, punches, manualStatus)
    }

    fun getReadoutDataByReadout(readoutId: UUID): ReadoutData? {
        val data = runBlocking {
            withContext(Dispatchers.IO) {
                return@withContext dataProcessor.getReadoutDataByReadout(readoutId)
            }
        }
        return data
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

    fun deleteAllReadoutsByRace() =
        CoroutineScope(Dispatchers.IO).launch {
            race.value?.let {
                dataProcessor.deleteAllReadoutsByRace(
                    it.id
                )
            }
        }

    fun getPunchesByReadout(readoutId: UUID): List<Punch> {
        return runBlocking {
            return@runBlocking dataProcessor.getPunchesByReadout(readoutId)
        }
    }

    //DATA IMPORT/EXPORT
    fun importData(
        uri: Uri,
        dataType: DataType,
        dataFormat: DataFormat
    ): DataImportWrapper? {
        return runBlocking {
            return@runBlocking dataProcessor.importData(
                uri,
                dataType,
                dataFormat,
                getCurrentRace().id
            )
        }
    }

    fun exportData(
        uri: Uri,
        dataType: DataType,
        dataFormat: DataFormat
    ): Boolean {
        return runBlocking {
            return@runBlocking dataProcessor.exportData(
                uri,
                dataType,
                dataFormat,
                getCurrentRace().id
            )
        }
    }
}