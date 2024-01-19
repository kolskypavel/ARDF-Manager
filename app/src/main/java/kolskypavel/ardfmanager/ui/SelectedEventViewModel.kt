package kolskypavel.ardfmanager.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.wrappers.ResultDataWrapper
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

    private val _readoutData: MutableStateFlow<List<ResultDataWrapper>> =
        MutableStateFlow(emptyList())
    val readoutData: StateFlow<List<ResultDataWrapper>> get() = _readoutData.asStateFlow()

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
                dataProcessor.getResultDataByEvent(id).collect {
                    _readoutData.value = it
                }
            }
        }
    }

    //Category
    suspend fun getCategory(id: UUID) = dataProcessor.getCategory(id)

    fun createCategory(category: Category) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.createCategory(category)
        }
    }

    fun updateCategory(category: Category) = CoroutineScope(Dispatchers.IO).launch {
        dataProcessor.updateCategory(category)
    }

    fun deleteCategory(categoryId: UUID) =
        CoroutineScope(Dispatchers.IO).launch { dataProcessor.deleteCategory(categoryId) }

    //Competitor
    fun createCompetitor(
        competitor: Competitor,
        modifiedPunches: Boolean,
        punches: ArrayList<Punch>
    ) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.createCompetitor(
                competitor,
                modifiedPunches,
                punches
            )
        }

    fun updateCompetitor(competitor: Competitor, changed: Boolean) =
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.updateCompetitor(
                competitor,
                changed
            )
        }

    fun deleteCompetitor(competitorId: UUID) =
        CoroutineScope(Dispatchers.IO).launch { dataProcessor.deleteCompetitor(competitorId) }

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
            dataProcessor.deleteResult(id)
        }
    }
}