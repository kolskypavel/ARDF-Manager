package kolskypavel.ardfmanager.backend

import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.sportident.SIReaderState
import kotlinx.coroutines.Job

/** Process-wide UI state shared between the active race, SI reader, and result-service worker. */
data class AppState(
    /** Race currently selected in the UI, or null before selection. */
    var currentRace: Race? = null,
    /** Latest SportIdent reader state shown in the UI. */
    var siReaderState: SIReaderState,
    /** Active result-service coroutine, when live publishing is running. */
    var resultServiceJob: Job? = null
)
