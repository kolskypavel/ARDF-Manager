package kolskypavel.ardfmanager.backend

import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.sportident.SIReaderState

class AppState(var currentEvent: Event? = null, var siReaderState: SIReaderState) {

}