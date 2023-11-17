package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor

data class CategoryCompetitorWrapper(
    val category: Category,
    var child: Boolean = false,
    var subList: MutableList<Competitor> = ArrayList(),
    var isExpanded: Boolean = false
)