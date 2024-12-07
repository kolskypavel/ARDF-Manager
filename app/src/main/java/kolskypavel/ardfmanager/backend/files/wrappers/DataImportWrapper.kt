package kolskypavel.ardfmanager.backend.files.wrappers

import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorCategory

data class DataImportWrapper(
    var competitorCategories: List<CompetitorCategory>,
    var categories: List<CategoryData>
) {}