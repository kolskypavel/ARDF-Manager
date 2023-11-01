package kolskypavel.ardfmanager.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kolskypavel.ardfmanager.room.ARDFRepository
import kolskypavel.ardfmanager.room.entitity.Event
import kolskypavel.ardfmanager.room.entitity.EventBand
import kolskypavel.ardfmanager.room.entitity.EventLevel
import kolskypavel.ardfmanager.room.entitity.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class EventsViewModel : ViewModel() {
    private val ardfRepository = ARDFRepository.get()
    private val _events: MutableStateFlow<List<Event>> = MutableStateFlow(emptyList())
    val events: StateFlow<List<Event>> get() = _events.asStateFlow()


    fun createEvent(
        name: String,
        date: LocalDate,
        startTime: LocalTime,
        eventType: EventType,
        eventLevel: EventLevel,
        eventBand: EventBand
    ) {
        val e = Event(UUID.randomUUID(), name, date, startTime, eventType, eventLevel, eventBand)
        runBlocking {
            ardfRepository.createEvent(e)
        }
    }

    fun modifyEvent(
        position: Int, name: String,
        date: LocalDate,
        startTime: LocalTime,
        eventType: EventType,
        eventLevel: EventLevel,
        eventBand: EventBand
    ) {
        val curId = events.value[position].id
        val e = Event(curId, name, date, startTime, eventType, eventLevel, eventBand)
        runBlocking {
            ardfRepository.updateEvent(e)
        }
    }

    fun deleteEvent(position: Int) {
        val curId = events.value[position].id
        runBlocking {
            ardfRepository.deleteEvent(curId)
        }
    }

    init {
        viewModelScope.launch {
            ardfRepository.getEvents().collect {
                _events.value = it
            }
        }
    }
}