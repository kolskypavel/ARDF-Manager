package kolskypavel.ardfmanager.backend.files.wrappers

import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor

data class CompetitorImportDataWrapper(
    var competitors: List<Competitor>,
    var newCategories: List<Category>
) {
}