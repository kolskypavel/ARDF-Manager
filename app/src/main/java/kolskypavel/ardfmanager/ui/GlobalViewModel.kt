package kolskypavel.ardfmanager.ui

import androidx.lifecycle.ViewModel
import kolskypavel.ardfmanager.room.entitity.Event

class GlobalViewModel : ViewModel() {
    private lateinit var curEvent: Event
}