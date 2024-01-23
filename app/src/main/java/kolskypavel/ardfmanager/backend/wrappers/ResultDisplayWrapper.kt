package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entitity.Category
import java.io.Serializable

/**
 * Used for displaying the result data
 */
data class ResultDisplayWrapper(
    val category: Category? = null,
    var isChild: Int = 0,
    var subList: MutableList<ReadoutDataWrapper> = ArrayList(),
    var isExpanded: Boolean = false
) : Serializable

/**
 * Comparator used to display the data in a valid order
 */
class ResultWrapperComparator : Comparator<ResultDisplayWrapper> {
    override fun compare(r1: ResultDisplayWrapper, r2: ResultDisplayWrapper): Int {
        return 1 //TODO:FIX
    }
}