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