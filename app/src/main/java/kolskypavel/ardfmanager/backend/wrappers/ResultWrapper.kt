package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import java.io.Serializable

/** UI grouping model for category result lists and expandable child rows. */
data class ResultWrapper(
    val category: Category? = null,
    var isChild: Int = 0,
    var competitorData: MutableList<CompetitorData> = ArrayList(),
    var isExpanded: Boolean = false,
    var childPosition: Int = 0,
    var finished: Int // Number of competitors with finished readouts in this group.
) : Serializable
