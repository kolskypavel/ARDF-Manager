package kolskypavel.ardfmanager.ui.races

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData
import kolskypavel.ardfmanager.backend.room.enums.ProviderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

/** ViewModel backing race selection, import/export, and provider download flows. */
class RaceViewModel : ViewModel() {
    private val dataProcessor = DataProcessor.get()
    private val _races: MutableStateFlow<List<Race>> = MutableStateFlow(emptyList())
    val races: StateFlow<List<Race>> get() = _races.asStateFlow()


    /** Creates a race on a background dispatcher. */
    fun createRace(
        race: Race
    ) = CoroutineScope(Dispatchers.IO).launch { dataProcessor.createRace(race) }

    /** Updates a race on a background dispatcher. */
    fun updateRace(
        race: Race
    ) = CoroutineScope(Dispatchers.IO).launch { dataProcessor.updateRace(race) }

    /** Deletes a race by id on a background dispatcher. */
    fun deleteRace(id: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.deleteRace(id)
        }
    }

    /** Saves an imported full-race payload on a background dispatcher. */
    fun saveRaceData(raceData: RaceData) = CoroutineScope(Dispatchers.IO).launch {
        dataProcessor.saveRaceData(raceData)
    }

    /** Imports a full race backup from a selected URI. */
    fun importRaceData(
        uri: Uri
    ) = runBlocking {
        dataProcessor.importRaceData(uri)
    }

    /** Downloads race data from an online provider. */
    fun fetchProviderRaceData(providerType: ProviderType, apiKey: String, context: Context) =
        runBlocking {
            dataProcessor.fetchProviderRaceData(providerType, apiKey, context)
        }

    /** Exports a full race backup to a selected URI. */
    fun exportRaceData(
        uri: Uri, raceId: UUID
    ) = runBlocking {
        dataProcessor.exportRaceData(uri, raceId)
    }

    /** Observes races and publishes them sorted by start time. */
    init {
        viewModelScope.launch {
            dataProcessor.getRaces().collect { races ->
                _races.value = races.sortedBy { it.startDateTime }
            }
        }
    }
}
