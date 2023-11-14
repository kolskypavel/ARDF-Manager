package kolskypavel.ardfmanager.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class EventsViewModel : ViewModel() {
    private val dataProcessor = DataProcessor.get()
    private val _events: MutableStateFlow<List<Event>> = MutableStateFlow(emptyList())
    val events: StateFlow<List<Event>> get() = _events.asStateFlow()


    fun createEvent(
        event: Event
    ) = dataProcessor.createEvent(event)

    fun modifyEvent(
        event: Event
    ) = dataProcessor.modifyEvent(event) 

    fun deleteEvent(id: UUID) = dataProcessor.deleteEvent(id)

    init {
        viewModelScope.launch {
            dataProcessor.getEvents().collect {
                _events.value = it
            }
        }
    }
}