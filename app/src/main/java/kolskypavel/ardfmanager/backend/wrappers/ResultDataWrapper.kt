package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Result
import java.io.Serializable

/**
 * Used for collapsable recycler view for result display
 */
data class ResultDataWrapper(
    var result: Result? = null,
    var punches: List<Punch> = emptyList(),
    var competitor: Competitor? = null,
    var category: Category? = null,
    var isChild: Int = 0,
    var subList: MutableList<ResultDataWrapper> = ArrayList(),
    var isExpanded: Boolean = false
) : Serializable

