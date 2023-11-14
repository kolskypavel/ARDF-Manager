package kolskypavel.ardfmanager.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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


    private val _readouts: MutableStateFlow<List<Readout>> = MutableStateFlow(emptyList())
    val readouts: StateFlow<List<Readout>> get() = _readouts.asStateFlow()

    /**
     * Updates the current selected event and corresponding data
     */
    suspend fun setEvent(id: UUID) {
        _event.postValue(dataProcessor.getEvent(id))

        runBlocking {
            dataProcessor.getCategoriesForEvent(id).collect {
                _categories.value = it
            }
            dataProcessor.getCompetitorsForEvent(id).collect {
                _competitors.value = it
            }

            dataProcessor.getReadoutsForEvent(id).collect {
                _readouts.value = it
            }
        }
    }

    fun createCategory(category: Category, siCodes: String) =
        dataProcessor.createCategory(category, siCodes)

    fun updateCategory(category: Category, siCodes: String) =
        dataProcessor.updateCategory(category, siCodes)
}