package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Result
import java.io.Serializable

/**
 * Used for collapsable recycler view for result display
 */
data class ReadoutDataWrapper(
    var result: Result? = null,
    var competitor: Competitor? = null,
    var category: Category? = null,

) : Serializable

