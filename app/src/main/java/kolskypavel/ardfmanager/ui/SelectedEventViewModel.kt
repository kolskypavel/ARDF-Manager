package kolskypavel.ardfmanager.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.wrappers.RecordWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    private val _readouts: MutableStateFlow<List<Readout>> = MutableStateFlow(emptyList())
    val readouts: StateFlow<List<Readout>> get() = _readouts.asStateFlow()

    /**
     * Updates the current selected event and corresponding data
     */
    fun setEvent(id: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            val event = dataProcessor.setReaderEvent(id)
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
                dataProcessor.getReadoutsByEvent(id).collect {
                    _readouts.value = it
                }
            }
        }
    }

    //Category
    fun createCategory(category: Category, siCodes: String) =
        dataProcessor.createCategory(category, siCodes)

    fun updateCategory(category: Category, siCodes: String) =
        dataProcessor.updateCategory(category, siCodes)

    fun deleteCategory(categoryId: UUID) = dataProcessor.deleteCategory(categoryId)

    //Competitor
    fun createCompetitor(competitor: Competitor) = dataProcessor.createCompetitor(competitor)

    fun updateCompetitor(competitor: Competitor) = dataProcessor.updateCompetitor(competitor)

    fun checkIfSINumberExists(siNumber: Int): Boolean {
        if (event.value != null) {
            return dataProcessor.checkIfSINumberExists(siNumber, event.value!!.id)
        }
        return true
    }

    fun getPunchRecordsForCompetitor(
        create: Boolean,
        competitor: Competitor
    ): ArrayList<RecordWrapper> {
        val punchRecords = ArrayList<RecordWrapper>()
//
//        //New or existing competitor
//        if (create) {
//            //Add start and finish punch
//            punchRecords.add(SIPort.PunchData(null, null, PunchType.START))
//            punchRecords.add(PunchRecordsWrapper(null, null, PunchType.FINISH))
//
//            return punchRecords
//        } else {
//            runBlocking {
//                val orig = dataProcessor.getPunchesForCompetitor(competitor.id)
//                var punchArr = ArrayList<Punch>(orig)
//
//            }
//        }
        return punchRecords
    }
}