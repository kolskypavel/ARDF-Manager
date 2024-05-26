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
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SITime
import kolskypavel.ardfmanager.backend.wrappers.PunchEditItemWrapper
import kolskypavel.ardfmanager.backend.wrappers.ResultDisplayWrapper
import kolskypavel.ardfmanager.backend.wrappers.StatisticsWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.util.UUID

/**
 * Represents the current selected event and its data - properties, categories, competitors and readouts
 */
class SelectedEventViewModel : ViewModel() {
    private val dataProcessor = DataProcessor.get()
    private val _event = MutableLiveData<Event>()

    val event: LiveData<Event> get() = _event
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

    fun getCurrentEvent(): Event {
        if (event.value != null) {
            return event.value!!
        }
        throw IllegalStateException("Event value accessed without event set")
    }

    /**
     * Updates the current selected event and corresponding data
     */
    fun setEvent(id: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            val event = dataProcessor.setCurrentEvent(id)
            _event.postValue(event)

            launch {
                dataProcessor.getCategoryDataFlowForEvent(id).collect {
                    _categories.value = it
                }
            }
            launch {
                dataProcessor.getCompetitorDataFlowByEvent(id).collect {
                    _competitorData.value = it
                }
            }

            launch {
                dataProcessor.getReadoutDataByEvent(id).collect {
                    _readoutData.value = it
                }
            }
            launch {
                dataProcessor.getResultDataByEvent(id).collect {
                    _resultData.value = it
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

    fun getCategories(): List<Category> = categories.value.map { it.category }
    fun getCategoryByName(string: String): Category? {
        return runBlocking {
            return@runBlocking dataProcessor.getCategoryByName(string, getCurrentEvent().id)
        }
    }

    fun getCategoryByMaxAge(maxAge: Int): Category? {
        return runBlocking {
            return@runBlocking dataProcessor.getCategoryByMaxAge(maxAge, getCurrentEvent().id)
        }
    }

    fun getHighestCategoryOrder(eventId: UUID): Int {
        return runBlocking {
            return@runBlocking dataProcessor.getHighestCategoryOrder(eventId)
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

    fun deleteCategory(categoryId: UUID, eventId: UUID) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.deleteCategory(
                categoryId,
                eventId
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
                dataProcessor.getCurrentEvent().id,
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
            dataProcessor.getControlPointByName(event.value!!.id, name)
        }
        return false
    }

    fun adjustControlPoints(
        controlPoints: ArrayList<ControlPoint>,
        eventType: EventType
    ) = dataProcessor.adjustControlPoints(controlPoints, eventType)


    //Competitor
    fun createOrUpdateCompetitor(
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

    fun deleteCompetitor(competitorId: UUID, deleteReadout: Boolean) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.deleteCompetitor(
                competitorId,
                deleteReadout
            )
        }

    fun deleteAllCompetitors() =
        CoroutineScope(Dispatchers.IO).launch {
            event.value?.let {
                dataProcessor.deleteAllCompetitors(
                    it.id
                )
            }
        }

    suspend fun getStatistics(eventId: UUID): StatisticsWrapper =
        dataProcessor.getStatisticsByEvent(eventId)

    /**
     * Checks if the SI number is unique
     */
    fun checkIfSINumberExists(siNumber: Int): Boolean {
        if (event.value != null) {
            return dataProcessor.checkIfSINumberExists(siNumber, event.value!!.id)
        }
        return true
    }

    fun checkIfStartNumberExists(siNumber: Int): Boolean {
        if (event.value != null) {
            return dataProcessor.checkIfStartNumberExists(siNumber, event.value!!.id)
        }
        return true
    }

    fun getPunchRecordsForCompetitor(
        competitor: Competitor
    ): ArrayList<PunchEditItemWrapper> {
        var punchRecords = ArrayList<PunchEditItemWrapper>()
        runBlocking {

            val punches = dataProcessor.getPunchesByCompetitor(competitor.id)

            //New or existing competitor
            if (punches.isEmpty()) {
                //Add start and finish punch
                punchRecords.add(
                    PunchEditItemWrapper(
                        Punch(
                            UUID.randomUUID(),
                            dataProcessor.getCurrentEvent().id,
                            null,
                            competitor.id,
                            null,
                            SIRecordType.START,
                            0,
                            0,
                            SITime(LocalTime.MIN),
                            null,
                            PunchStatus.VALID
                        ), true, true, true, true
                    )
                )
                punchRecords.add(
                    PunchEditItemWrapper(
                        Punch(
                            UUID.randomUUID(),
                            dataProcessor.getCurrentEvent().id,
                            null,
                            competitor.id,
                            null,
                            SIRecordType.FINISH,
                            0,
                            0,
                            SITime(LocalTime.MIN),
                            null,
                            PunchStatus.VALID
                        ), true, true, true, true
                    )
                )

            } else {

                punchRecords = PunchEditItemWrapper.getWrappers(
                    ArrayList(punches)
                )
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

    fun getPunchesByReadout(readoutId: UUID): List<Punch> {
        return runBlocking {
            return@runBlocking dataProcessor.getPunchesByReadout(readoutId)
        }
    }

    fun importCompetitors() {

    }
}