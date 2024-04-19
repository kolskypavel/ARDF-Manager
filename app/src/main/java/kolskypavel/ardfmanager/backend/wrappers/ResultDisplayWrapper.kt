package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import java.io.Serializable

/**
 * Used for displaying the result data
 */
data class ResultDisplayWrapper(
    val category: Category? = null,
    var isChild: Int = 0,
    var subList: MutableList<ReadoutData> = ArrayList(),
    var isExpanded: Boolean = false
) : Serializable