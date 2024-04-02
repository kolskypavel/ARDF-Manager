package kolskypavel.ardfmanager.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.wrappers.ReadoutDataWrapper
import kolskypavel.ardfmanager.backend.wrappers.ResultDisplayWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

/**
 * Represents the current selected event and its data - properties, categories, competitors and readouts
 */
class SelectedEventViewModel : ViewModel() {
    private val dataProcessor = DataProcessor.get()
    private val _event = MutableLiveData<Event>()

    val event: LiveData<Event> get() = _event
    private val _categories: MutableStateFlow<List<Category>> = MutableStateFlow(emptyList())
    val categories: StateFlow<List<Category>> get() = _categories.asStateFlow()

    private val _competitors: MutableStateFlow<List<Competitor>> = MutableStateFlow(emptyList())
    val competitors: StateFlow<List<Competitor>> get() = _competitors.asStateFlow()

    private val _readoutData: MutableStateFlow<List<ReadoutDataWrapper>> =
        MutableStateFlow(emptyList())
    val readoutData: StateFlow<List<ReadoutDataWrapper>> get() = _readoutData.asStateFlow()


    private val _resultData: MutableStateFlow<List<ResultDisplayWrapper>> =
        MutableStateFlow(emptyList())
    val resultData: StateFlow<List<ResultDisplayWrapper>> get() = _resultData.asStateFlow()

    private val _competitorsCategories: MutableStateFlow<List<CompetitorData>> =
        MutableStateFlow(emptyList())
    val competitorsCategories: StateFlow<List<CompetitorData>>
        get() =
            _competitorsCategories.asStateFlow()

    /**
     * Updates the current selected event and corresponding data
     */
    fun setEvent(id: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            val event = dataProcessor.setCurrentEvent(id)
            _event.postValue(event)

            launch {
                dataProcessor.getCompetitorsForEvent(id).collect {
                    _competitors.value = it
                }
            }
            launch {
                dataProcessor.getCategoriesForEvent(id).collect {
                    _categories.value = it
                }
            }
            launch {
                dataProcessor.getCompetitorCategoriesByEvent(id).collect {
                    _competitorsCategories.value = it
                }
            }

            launch {
                dataProcessor.getReadoutDataByEvent(id).collect {
                    _readoutData.value = it
                }
            }
        }
    }

    fun updateEvent(event: Event) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.updateEvent(event)
            _event.postValue(event)
        }
    }

    //Category
    suspend fun getCategory(id: UUID) = dataProcessor.getCategory(id)

    fun getCategoryByName(string: String): Category? {
        return runBlocking {
            return@runBlocking dataProcessor.getCategoryByName(string, event.value!!.id)
        }
    }

    fun getCategoryByMaxAge(maxAge: Int): Category? {
        return runBlocking {
            return@runBlocking dataProcessor.getCategoryByMaxAge(maxAge, event.value!!.id)
        }
    }

    fun createCategory(category: Category, controlPoints: List<ControlPoint>) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.createCategory(category, controlPoints)
        }
    }

    fun updateCategory(category: Category, controlPoints: List<ControlPoint>) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.updateCategory(category, controlPoints)
        }

    fun deleteCategory(categoryId: UUID) =
        CoroutineScope(Dispatchers.IO).launch { dataProcessor.deleteCategory(categoryId) }


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
                dataProcessor.getCurrentEvent().id,
                categoryId,
                null,
                null, 0, 0, 1,
                beacon = false, separator = false
            )
        )
        return controlPoints
    }

    fun checkIfControlPointNameExists(siCode: Int?, name: String): Boolean {
        //TODO: Fix
        runBlocking {
            dataProcessor.getControlPointByName(event.value!!.id, name)
        }
        return false
    }

    fun adjustControlPoints(
        controlPoints: ArrayList<ControlPoint>,
        eventType: EventType
    ) = dataProcessor.adjustControlPoints(controlPoints, eventType)

    fun getCodesNameFromControlPoints(controlPoints: List<ControlPoint>): Pair<String, String> =
        dataProcessor.getCodesNameFromControlPoints(controlPoints)

    //Competitor
    fun createCompetitor(
        competitor: Competitor,
        modifiedPunches: Boolean,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.createOrUpdateCompetitor(
                competitor,
                modifiedPunches,
                punches,
                manualStatus
            )
        }

    fun updateCompetitor(competitor: Competitor, changed: Boolean) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.updateCompetitor(
                competitor
            )
        }

    fun deleteCompetitor(competitorId: UUID) =
        CoroutineScope(Dispatchers.IO).launch { dataProcessor.deleteCompetitor(competitorId) }

    fun deleteAllCompetitors() =
        CoroutineScope(Dispatchers.IO).launch {
            event.value?.let {
                dataProcessor.deleteAllCompetitors(
                    it.id
                )
            }
        }

    /**
     * Checks if the SI number is unique
     */
    fun checkIfSINumberExists(siNumber: Int): Boolean {
        if (event.value != null) {
            return dataProcessor.checkIfSINumberExists(siNumber, event.value!!.id)
        }
        return true
    }


    fun getPunchRecordsForCompetitor(
        create: Boolean,
        competitor: Competitor
    ): ArrayList<Punch> {
        var punchRecords = ArrayList<Punch>()
//
        //New or existing competitor
        if (create) {
            //Add start and finish punch
            punchRecords.add(
                Punch(
                    UUID.randomUUID(),
                    dataProcessor.getCurrentEvent().id,
                    null,
                    competitor.id,
                    null,
                    SIRecordType.START,
                    0,
                    0,
                    null,
                    null,
                    PunchStatus.VALID
                )
            )
            punchRecords.add(
                Punch(
                    UUID.randomUUID(),
                    dataProcessor.getCurrentEvent().id,
                    null,
                    competitor.id,
                    null,
                    SIRecordType.FINISH,
                    0,
                    0,
                    null,
                    null,
                    PunchStatus.VALID
                )
            )

        } else {
            runBlocking {
                punchRecords = ArrayList(dataProcessor.getPunchesByCompetitor(competitor.id))
            }
        }
        return punchRecords
    }

    fun getLastReadCard() = dataProcessor.getLastReadCard()

    fun deleteReadout(id: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.deleteReadout(id)
        }
    }

    fun getPunchesByResult(resultId: UUID): List<Punch> {
        return runBlocking {
            return@runBlocking dataProcessor.getPunchesByReadout(resultId)
        }
    }


}