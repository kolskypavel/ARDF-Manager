package kolskypavel.ardfmanager.backend

import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.sportident.SIReaderState

class AppState(var currentRace: Race? = null, var siReaderState: SIReaderState) {

}