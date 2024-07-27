package kolskypavel.ardfmanager.backend.files.wrappers

import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorCategory

data class DataImportWrapper(
    var competitorCategories: List<CompetitorCategory>,
    var categories: List<CategoryData>
) {}