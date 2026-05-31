package kolskypavel.ardfmanager.backend.files.wrappers

import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorCategory

/** Result of an import pass, including accepted rows and row-level validation failures. */
data class DataImportWrapper(
    var competitorCategories: List<CompetitorCategory>,
    var categories: List<CategoryData>,
    var invalidLines: ArrayList<Pair<Int, String>> // Row index plus validation failure reason.
) {
    /** Returns the number of accepted rows relevant to the requested import data type. */
    fun getCount(dataType: DataType): Int {
        return when (dataType) {
            DataType.CATEGORIES -> categories.size
            DataType.COMPETITORS -> competitorCategories.size
            else -> 0
        }
    }
}
